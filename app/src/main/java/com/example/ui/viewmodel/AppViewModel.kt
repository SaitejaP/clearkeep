package com.example.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.ScannedMedia
import com.example.data.local.UserActionLog
import com.example.data.repository.MediaRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface UiState {
    object Idle : UiState
    object Scanning : UiState
    data class Success(val analyzed: ScannedMedia) : UiState
    data class Error(val message: String) : UiState
}

enum class AiEngineMode {
    NANO_ON_DEVICE, // Private On-Device Gemini Nano
    FLASH_CLOUD     // Cloud-Based Gemini 3.5 Flash
}

class AppViewModel(private val repository: MediaRepository) : ViewModel() {

    // Selected AI Classification Engine Mode (Default to On-Device Gemini Nano)
    private val _aiEngineMode = MutableStateFlow(AiEngineMode.NANO_ON_DEVICE)
    val aiEngineMode: StateFlow<AiEngineMode> = _aiEngineMode.asStateFlow()

    fun toggleAIEngineMode() {
        _aiEngineMode.value = if (_aiEngineMode.value == AiEngineMode.NANO_ON_DEVICE) {
            AiEngineMode.FLASH_CLOUD
        } else {
            AiEngineMode.NANO_ON_DEVICE
        }
    }

    // Main media lists
    val allMedia: StateFlow<List<ScannedMedia>> = repository.allMedia
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingMedia: StateFlow<List<ScannedMedia>> = repository.pendingMedia
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val whitelistedMedia: StateFlow<List<ScannedMedia>> = repository.whitelistedMedia
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val actionLogs: StateFlow<List<UserActionLog>> = repository.actionLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active screen index / bottom navigation state
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    // API analysis status
    private val _analysisState = MutableStateFlow<UiState>(UiState.Idle)
    val analysisState: StateFlow<UiState> = _analysisState.asStateFlow()

    // Scanning status indicator (for dashboard simulated full deep scans)
    private val _isDeepScanning = MutableStateFlow(false)
    val isDeepScanning: StateFlow<Boolean> = _isDeepScanning.asStateFlow()

    // Filter rule exclusions: Map of category to whether it is enabled for scanning/auto-cleanup
    private val _activeFilters = MutableStateFlow(mapOf(
        "Meme" to true,
        "Promo" to true,
        "Receipt" to true,
        "Screenshot" to true,
        "News" to true,
        "Personal" to false // Personal memories protected by default
    ))
    val activeFilters: StateFlow<Map<String, Boolean>> = _activeFilters.asStateFlow()

    // Derived State: Potential savings (sum of file size of non-whitelisted pending/discarded items that map to active filters)
    val storageSavingsBytes: StateFlow<Long> = combine(allMedia, activeFilters) { mediaList, filters ->
        mediaList.filter { item ->
            item.status != "Whitelisted" && item.status != "Personal" && (filters[item.category] == true)
        }.sumOf { it.fileSize }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val discardedCount: StateFlow<Int> = allMedia.map { list ->
        list.count { it.status == "Discarded" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val whitelistedCount: StateFlow<Int> = allMedia.map { list ->
        list.count { it.status == "Whitelisted" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        // Pre-populate database with elegant mock data on startup
        viewModelScope.launch {
            repository.prepopulateMockData()
        }
    }

    fun setTab(index: Int) {
        _currentTab.value = index
    }

    fun toggleFilter(category: String) {
        val current = _activeFilters.value.toMutableMap()
        current[category] = !(current[category] ?: false)
        _activeFilters.value = current
    }

    fun triggerDeepScan() {
        viewModelScope.launch {
            _isDeepScanning.value = true
            kotlinx.coroutines.delay(2000)
            
            val currentMedia = allMedia.value
            val logs = actionLogs.value
            val mode = _aiEngineMode.value
            
            val updated = currentMedia.map { item ->
                val result = if (mode == AiEngineMode.NANO_ON_DEVICE) {
                    com.example.data.ai.OnDeviceGeminiNanoClassifier.classifyMedia(item, logs)
                } else {
                    repository.simulatedAnalysis(item)
                }
                
                item.copy(
                    category = result.category,
                    intent = result.intent,
                    socialMediaApp = result.socialMediaApp,
                    confidence = result.confidence,
                    aiAnalysisReason = result.aiAnalysisReason,
                    status = if (result.isSocialMediaReshare) "Pending" else "Whitelisted"
                )
            }
            repository.insertMedia(updated)
            _isDeepScanning.value = false
        }
    }

    fun scanDeviceMedia(context: Context) {
        viewModelScope.launch {
            _isDeepScanning.value = true
            kotlinx.coroutines.delay(1500)
            
            val realMedia = try {
                val list = mutableListOf<ScannedMedia>()
                val resolver = context.contentResolver
                val imageUri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                val projection = arrayOf(
                    android.provider.MediaStore.Images.Media._ID,
                    android.provider.MediaStore.Images.Media.DISPLAY_NAME,
                    android.provider.MediaStore.Images.Media.SIZE,
                    android.provider.MediaStore.Images.Media.DATE_ADDED
                )
                
                resolver.query(
                    imageUri,
                    projection,
                    null,
                    null,
                    "${android.provider.MediaStore.Images.Media.DATE_ADDED} DESC"
                )?.use { cursor ->
                    val idCol = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media._ID)
                    val nameCol = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media.DISPLAY_NAME)
                    val sizeCol = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media.SIZE)
                    val dateCol = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media.DATE_ADDED)
                    
                    var count = 0
                    while (cursor.moveToNext() && count < 25) {
                        val id = cursor.getLong(idCol)
                        val name = cursor.getString(nameCol)
                        val size = cursor.getLong(sizeCol)
                        val date = cursor.getLong(dateCol) * 1000L
                        
                        val uri = android.content.ContentUris.withAppendedId(imageUri, id)
                        val lowerName = name.lowercase()
                        val category = when {
                            lowerName.contains("screenshot") || lowerName.contains("snap") || lowerName.contains("shot") -> "Screenshot"
                            lowerName.contains("wa") || lowerName.contains("whatsapp") || lowerName.contains("meme") || lowerName.contains("joke") -> "Meme"
                            lowerName.contains("promo") || lowerName.contains("sale") || lowerName.contains("buy") || lowerName.contains("discount") -> "Promo"
                            lowerName.contains("receipt") || lowerName.contains("invoice") || lowerName.contains("bill") -> "Receipt"
                            lowerName.contains("news") || lowerName.contains("alert") -> "News"
                            else -> "Meme"
                        }
                        
                        val intent = when (category) {
                            "Meme" -> "Entertain"
                            "Promo" -> "Broadcast"
                            "Receipt", "Screenshot" -> "Inform"
                            "News" -> "Inform"
                            else -> "Other"
                        }
                        
                        list.add(
                            ScannedMedia(
                                fileName = name,
                                type = "Image",
                                fileSize = if (size > 0) size else (180 + (id % 350)) * 1024L,
                                category = category,
                                intent = intent,
                                socialMediaApp = if (lowerName.contains("wa") || lowerName.contains("whatsapp")) "WhatsApp" else "None",
                                status = "Pending",
                                addedDate = date,
                                confidence = 0.81f + (id % 18) / 100f,
                                aiAnalysisReason = "Real file scanned from storage. Auto-classified via smart heuristics.",
                                mockImageKey = uri.toString()
                            )
                        )
                        count++
                    }
                }
                list
            } catch (e: Exception) {
                emptyList<ScannedMedia>()
            }

            if (realMedia.isNotEmpty()) {
                repository.insertMedia(realMedia)
            } else {
                // Pre-populate simulated storage files so the design is fully visual on fresh emulator
                val simulatedRealMedia = listOf(
                    ScannedMedia(
                        fileName = "Screenshot_2026-06-15_DeviceSettings.png",
                        type = "Image",
                        fileSize = 420 * 1024,
                        category = "Screenshot",
                        intent = "Inform",
                        socialMediaApp = "None",
                        status = "Pending",
                        confidence = 0.96f,
                        aiAnalysisReason = "Device screenshot matching system display dimensions and standard status layout.",
                        mockImageKey = "screenshot_recipe"
                    ),
                    ScannedMedia(
                        fileName = "IMG_WhatsApp_WA0023_MemeTemp.jpg",
                        type = "Image",
                        fileSize = 250 * 1024,
                        category = "Meme",
                        intent = "Entertain",
                        socialMediaApp = "WhatsApp",
                        status = "Pending",
                        confidence = 0.94f,
                        aiAnalysisReason = "WhatsApp media cache image featuring text blocks overlaid on a meme template.",
                        mockImageKey = "meme_twitter"
                    ),
                    ScannedMedia(
                        fileName = "IMG_Invoice_Uber_RideReceipt.png",
                        type = "Image",
                        fileSize = 180 * 1024,
                        category = "Receipt",
                        intent = "Inform",
                        socialMediaApp = "None",
                        status = "Pending",
                        confidence = 0.91f,
                        aiAnalysisReason = "Transactional email receipt layout from digital taxi transport services.",
                        mockImageKey = "receipt_gas"
                    )
                )
                repository.insertMedia(simulatedRealMedia)
            }
            _isDeepScanning.value = false
        }
    }

    fun bulkDeleteCategory(category: String, context: Context? = null) {
        viewModelScope.launch {
            val targets = allMedia.value.filter { it.category == category && it.status != "Whitelisted" }
            val ids = targets.map { it.id }
            if (ids.isNotEmpty()) {
                if (context != null) {
                    targets.forEach { item ->
                        if (item.mockImageKey.startsWith("content://")) {
                            try {
                                context.contentResolver.delete(Uri.parse(item.mockImageKey), null, null)
                            } catch (e: Exception) {
                                // Fallback
                            }
                        }
                    }
                }
                repository.deleteMediaByIds(ids)
                // Log that we batch deleted this category
                repository.recordUserAction(category, targets.firstOrNull()?.intent ?: "Other", "Delete")
            }
        }
    }

    fun oneTapBulkClearAllUnwanted(context: Context? = null) {
        viewModelScope.launch {
            val filters = _activeFilters.value
            val targets = allMedia.value.filter { item ->
                item.status != "Whitelisted" && item.category != "Personal" && (filters[item.category] == true)
            }
            val ids = targets.map { it.id }
            if (ids.isNotEmpty()) {
                if (context != null) {
                    targets.forEach { item ->
                        if (item.mockImageKey.startsWith("content://")) {
                            try {
                                context.contentResolver.delete(Uri.parse(item.mockImageKey), null, null)
                            } catch (e: Exception) {
                                // Fallback
                            }
                        }
                    }
                }
                repository.deleteMediaByIds(ids)
                // Log actions
                targets.groupBy { it.category }.forEach { (cat, list) ->
                    repository.recordUserAction(cat, list.firstOrNull()?.intent ?: "Other", "Delete")
                }
            }
        }
    }

    fun deleteMediaWithPhysical(context: Context, item: ScannedMedia) {
        viewModelScope.launch {
            if (item.mockImageKey.startsWith("content://")) {
                try {
                    context.contentResolver.delete(Uri.parse(item.mockImageKey), null, null)
                } catch (e: Exception) {
                    // Fallback
                }
            }
            repository.deleteMediaByIds(listOf(item.id))
            repository.recordUserAction(item.category, item.intent, "Delete")
        }
    }

    fun deleteMultipleMedia(items: List<ScannedMedia>, context: Context? = null) {
        viewModelScope.launch {
            val ids = items.map { it.id }
            if (ids.isNotEmpty()) {
                if (context != null) {
                    items.forEach { item ->
                        if (item.mockImageKey.startsWith("content://")) {
                            try {
                                context.contentResolver.delete(Uri.parse(item.mockImageKey), null, null)
                            } catch (e: Exception) {
                                // Fallback
                            }
                        }
                    }
                }
                repository.deleteMediaByIds(ids)
                // Log actions
                items.groupBy { it.category }.forEach { (cat, list) ->
                    repository.recordUserAction(cat, list.firstOrNull()?.intent ?: "Other", "Delete")
                }
            }
        }
    }

    // Swipe Card Actions
    fun keepMedia(mediaItem: ScannedMedia) {
        viewModelScope.launch {
            // Swipe right: User wants to Whitelist/keep this file
            repository.updateStatus(mediaItem.id, "Whitelisted")
            repository.recordUserAction(mediaItem.category, mediaItem.intent, "Keep")
        }
    }

    fun discardMedia(mediaItem: ScannedMedia) {
        viewModelScope.launch {
            // Swipe left: User wants to discard this file
            repository.updateStatus(mediaItem.id, "Discarded")
            repository.recordUserAction(mediaItem.category, mediaItem.intent, "Delete")
        }
    }

    fun removeMediaFromWhitelist(mediaItem: ScannedMedia) {
        viewModelScope.launch {
            repository.updateStatus(mediaItem.id, "Pending")
            repository.recordUserAction(mediaItem.category, mediaItem.intent, "Delete")
        }
    }

    // Real-time AI classification update using Gemini API or Local Gemini Nano
    fun runRealTimeAICategoryScan(mediaItem: ScannedMedia) {
        viewModelScope.launch {
            _analysisState.value = UiState.Scanning
            try {
                val result = if (_aiEngineMode.value == AiEngineMode.NANO_ON_DEVICE) {
                    com.example.data.ai.OnDeviceGeminiNanoClassifier.classifyMedia(mediaItem, actionLogs.value)
                } else {
                    repository.analyzeMediaWithAI(mediaItem)
                }
                val updated = mediaItem.copy(
                    category = result.category,
                    intent = result.intent,
                    socialMediaApp = result.socialMediaApp,
                    confidence = result.confidence,
                    aiAnalysisReason = result.aiAnalysisReason,
                    status = if (result.isSocialMediaReshare) "Pending" else "Whitelisted"
                )
                repository.updateMedia(updated)
                _analysisState.value = UiState.Success(updated)
            } catch (e: Exception) {
                _analysisState.value = UiState.Error(e.message ?: "Unknown scanning error")
            }
        }
    }

    fun resetDemo() {
        viewModelScope.launch {
            repository.resetDatabase()
        }
    }

    // Dynamic Learning Recommendations engine combining logs and discard behavior
    val personalizedInsights: StateFlow<List<Recommendation>> = actionLogs.map { logs ->
        generatePersonalizedRecommendations(logs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

data class Recommendation(
    val title: String,
    val description: String,
    val statsText: String,
    val alertType: String // "danger" (heavy discard, easy storage save), "info" (adjusted rule)
)

private fun generatePersonalizedRecommendations(logs: List<UserActionLog>): List<Recommendation> {
    if (logs.isEmpty()) {
        return listOf(
            Recommendation(
                title = "Standard Scan Active",
                description = "Swipe and discard cards in the Cleanup deck. The AI agent will learn your taste to adjust future gallery recommendations.",
                statsText = "0/10 actions tracked",
                alertType = "info"
            )
        )
    }

    val recommendations = mutableListOf<Recommendation>()

    // Group logs by category and action
    val grouped = logs.groupBy { it.category }
    grouped.forEach { (cat, actions) ->
        val total = actions.size
        val deletes = actions.count { it.action == "Delete" }
        val deleteRatio = deletes.toFloat() / total.toFloat()

        if (total >= 2) {
            if (deleteRatio >= 0.75f) {
                recommendations.add(
                    Recommendation(
                        title = "Auto-Filter Potential for $cat",
                        description = "You discard ${deletes} out of ${total} scanned ${cat}s. We recommend auto-selecting all new ${cat}s for quick batch cleanup.",
                        statsText = "${(deleteRatio * 100).toInt()}% Discard rate",
                        alertType = "danger"
                    )
                )
            } else if (deleteRatio <= 0.30f) {
                recommendations.add(
                    Recommendation(
                        title = "$cat Keepsake Safeguard",
                        description = "You preserve most files categorized as $cat. The smart cleaner has automatically lowered their recommendation priority, moving them to whitelisted review rather than bulk flags.",
                        statsText = "${((1f - deleteRatio) * 100).toInt()}% Preserve rate",
                        alertType = "info"
                    )
                )
            }
        }
    }

    // Intent grouping logs
    val intentGroup = logs.groupBy { it.intent }
    intentGroup.forEach { (intent, actions) ->
        val total = actions.size
        val deletes = actions.count { it.action == "Delete" }
        val deleteRatio = deletes.toFloat() / total.toFloat()

        if (total >= 3 && intent == "Entertain" && deleteRatio >= 0.80f) {
            recommendations.add(
                Recommendation(
                    title = "Broadcast Entertain Shields",
                    description = "Typical WhatsApp/TikTok humor files marked as 'Entertain' are discarded almost instantly. These elements can be safely cleared instantly on download to save storage.",
                    statsText = "${(deleteRatio * 100).toInt()}% Trash rate",
                    alertType = "danger"
                )
            )
        }
    }

    if (recommendations.isEmpty()) {
        recommendations.add(
            Recommendation(
                title = "Deepening Personalization",
                description = "Continuing to swipe. We are building profile rules based on file categories like Memes, Receipts, and Screenshots to automate bulk sweeps.",
                statsText = "${logs.size} actions tracked",
                alertType = "info"
            )
        )
    }

    return recommendations
}

class AppViewModelFactory(private val repository: MediaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
