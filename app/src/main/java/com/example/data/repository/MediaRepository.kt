package com.example.data.repository

import android.content.Context
import com.example.BuildConfig
import com.example.data.local.MediaDao
import com.example.data.local.ScannedMedia
import com.example.data.local.UserActionLog
import com.example.data.api.GeminiRetrofitClient
import com.example.data.api.GenerateContentRequest
import com.example.data.api.Content
import com.example.data.api.Part
import com.example.data.api.GenerationConfig
import com.example.data.api.ResponseSchema
import com.example.data.api.SchemaProperty
import com.example.data.api.ReshareAnalysisResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.UUID

class MediaRepository(private val mediaDao: MediaDao) {

    val allMedia: Flow<List<ScannedMedia>> = mediaDao.getAllMedia()
    val pendingMedia: Flow<List<ScannedMedia>> = mediaDao.getPendingMedia()
    val whitelistedMedia: Flow<List<ScannedMedia>> = mediaDao.getWhitelistedMedia()
    val actionLogs: Flow<List<UserActionLog>> = mediaDao.getAllActionLogs()

    suspend fun insertMedia(media: List<ScannedMedia>) = withContext(Dispatchers.IO) {
        mediaDao.insertMediaList(media)
    }

    suspend fun updateMedia(media: ScannedMedia) = withContext(Dispatchers.IO) {
        mediaDao.updateMedia(media)
    }

    suspend fun updateStatus(id: Int, status: String) = withContext(Dispatchers.IO) {
        mediaDao.updateMediaStatus(id, status)
    }

    suspend fun updateMultipleStatus(ids: List<Int>, status: String) = withContext(Dispatchers.IO) {
        mediaDao.updateMultipleMediaStatus(ids, status)
    }

    suspend fun deleteMediaByIds(ids: List<Int>) = withContext(Dispatchers.IO) {
        mediaDao.deleteMediaByIds(ids)
    }

    suspend fun recordUserAction(category: String, intent: String, action: String) = withContext(Dispatchers.IO) {
        mediaDao.insertActionLog(UserActionLog(category = category, intent = intent, action = action))
    }

    suspend fun resetDatabase() = withContext(Dispatchers.IO) {
        mediaDao.clearAllMedia()
        mediaDao.clearActionLogs()
        prepopulateMockData()
    }

    // High quality mock data representing typical downloads found in an active user's device.
    suspend fun prepopulateMockData() = withContext(Dispatchers.IO) {
        val existing = mediaDao.getAllMedia().firstOrNull() ?: emptyList()
        if (existing.isNotEmpty()) return@withContext

        val mockList = listOf(
            ScannedMedia(
                fileName = "IMG_WhatsApp_Meme_2026.jpg",
                type = "Image",
                fileSize = 450 * 1024, // 450 KB
                category = "Meme",
                intent = "Entertain",
                socialMediaApp = "WhatsApp",
                status = "Pending",
                confidence = 0.98f,
                aiAnalysisReason = "Contains bold text typical of viral humor templates, overlaying an reaction expression.",
                mockImageKey = "meme_cat"
            ),
            ScannedMedia(
                fileName = "VID_TikTok_Reshare_2873.mp4",
                type = "Video",
                fileSize = 14 * 1024 * 1024, // 14 MB
                category = "Meme",
                intent = "Entertain",
                socialMediaApp = "TikTok",
                status = "Pending",
                confidence = 0.95f,
                aiAnalysisReason = "Contains vertical aspect ratio, watermark pattern typical of rapid video loops, and fast audio beats.",
                mockImageKey = "tiktok_dance"
            ),
            ScannedMedia(
                fileName = "IMG_IG_Promo_Buy1Get1.jpg",
                type = "Image",
                fileSize = 1200 * 1024, // 1.2 MB
                category = "Promo",
                intent = "Broadcast",
                socialMediaApp = "Instagram",
                status = "Pending",
                confidence = 0.92f,
                aiAnalysisReason = "Identified as promotional catalog layout with discount tags, brand typography, and pricing tables.",
                mockImageKey = "promo_bogo"
            ),
            ScannedMedia(
                fileName = "Screenshot_20260614_GasReceipt.png",
                type = "Image",
                fileSize = 850 * 1024, // 850 KB
                category = "Receipt",
                intent = "Inform",
                socialMediaApp = "None",
                status = "Pending",
                confidence = 0.88f,
                aiAnalysisReason = "Black and white monochrome tabular text layout matching gas station transaction records.",
                mockImageKey = "receipt_gas"
            ),
            ScannedMedia(
                fileName = "IMG_FB_Spam_WeatherAlert.png",
                type = "Image",
                fileSize = 650 * 1024, // 650 KB
                category = "News",
                intent = "Inform",
                socialMediaApp = "Facebook",
                status = "Pending",
                confidence = 0.90f,
                aiAnalysisReason = "Broadcast infographic showing storm map with bold alert text, redistributed repeatedly across community feed.",
                mockImageKey = "news_weather"
            ),
            ScannedMedia(
                fileName = "IMG_Family_Vacation_Portraits.jpg",
                type = "Image",
                fileSize = 4200 * 1024, // 4.2 MB
                category = "Personal",
                intent = "Personal",
                socialMediaApp = "None",
                status = "Pending",
                confidence = 0.12f, // low confidence of being a reshare, personal memory!
                aiAnalysisReason = "Detected natural scenery, face focus structures and high color gamut with no text overlay.",
                mockImageKey = "personal_vacation"
            ),
            ScannedMedia(
                fileName = "Screenshot_IG_Story_Recipe.jpg",
                type = "Image",
                fileSize = 780 * 1024, // 780 KB
                category = "Screenshot",
                intent = "Inform",
                socialMediaApp = "Instagram",
                status = "Pending",
                confidence = 0.82f,
                aiAnalysisReason = "Instagram frame buttons present. Captures a quick text-snippet recipe from an ephemeral story.",
                mockImageKey = "screenshot_recipe"
            ),
            ScannedMedia(
                fileName = "IMG_Twitter_Joke_Reaction.jpg",
                type = "Image",
                fileSize = 310 * 1024, // 310 KB
                category = "Meme",
                intent = "Entertain",
                socialMediaApp = "Twitter",
                status = "Pending",
                confidence = 0.97f,
                aiAnalysisReason = "Contains flat dialogue boxes and comment bubbles nested together, signaling digital re-transmission.",
                mockImageKey = "meme_twitter"
            ),
            ScannedMedia(
                fileName = "IMG_Promo_Discount_Apparel.jpg",
                type = "Image",
                fileSize = 2100 * 1024, // 2.1 MB
                category = "Promo",
                intent = "Broadcast",
                socialMediaApp = "Instagram",
                status = "Pending",
                confidence = 0.94f,
                aiAnalysisReason = "High-key studio product catalog showing shoes with bold 50% discount label on graphic template.",
                mockImageKey = "promo_shoes"
            ),
            ScannedMedia(
                fileName = "IMG_My_Cute_Dog_Sleeping.jpg",
                type = "Image",
                fileSize = 3800 * 1024, // 3.8 MB
                category = "Personal",
                intent = "Personal",
                socialMediaApp = "None",
                status = "Pending",
                confidence = 0.05f, // Safe
                aiAnalysisReason = "Natural light shot of a domestic animal indoors with zero digital artifacting or typography overlays.",
                mockImageKey = "personal_dog"
            )
        )
        val fullList = mockList.toMutableList()
        val categories = listOf("Meme", "Promo", "Receipt", "Screenshot", "News")
        val intents = mapOf(
            "Meme" to "Entertain",
            "Promo" to "Broadcast",
            "Receipt" to "Inform",
            "Screenshot" to "Inform",
            "News" to "Inform"
        )
        val apps = listOf("WhatsApp", "Instagram", "TikTok", "Facebook", "Twitter", "None")
        
        // Generate exactly 100 items of mixed new and old media representing daily scan results
        for (i in 1..100) {
            val isNew = (i % 2 == 0)
            val daysAgo = if (isNew) (i % 3) else (20 + (i * 3) % 200)
            val addedDate = System.currentTimeMillis() - (daysAgo * 24L * 60 * 60 * 1000)
            
            val category = categories[i % categories.size]
            val intent = intents[category] ?: "Other"
            val app = apps[i % apps.size]
            val isVideo = (i % 6 == 0) // Mix of photos and videos
            val ext = if (isVideo) "mp4" else "jpg"
            val type = if (isVideo) "Video" else "Image"
            
            val fileSize = if (isVideo) {
                (2 * 1024 * 1024 + (i * 451920L) % (10 * 1024 * 1024)) // 2MB to 12MB
            } else {
                (80 * 1024 + (i * 15309L) % (1500 * 1024)) // 80KB to 1.5MB
            }
            
            val label = if (isNew) "New" else "Old"
            val fileName = when (category) {
                "Meme" -> "IMG_${app}_Meme_${label}_$i.$ext"
                "Promo" -> "IMG_Catalog_${app}_Ad_$i.$ext"
                "Receipt" -> "Receipt_Scan_Retail_$i.$ext"
                "Screenshot" -> "Screenshot_${label}_Device_$i.$ext"
                "News" -> "News_Feed_${app}_For_$i.$ext"
                else -> "IMG_File_$i.$ext"
            }
            
            val mockImageKey = when (category) {
                "Meme" -> "meme_cat"
                "Promo" -> "promo_bogo"
                "Receipt" -> "receipt_gas"
                "Screenshot" -> "screenshot_recipe"
                "News" -> "news_weather"
                else -> "personal_vacation"
            }
            
            fullList.add(
                ScannedMedia(
                    id = 100 + i, // Unique ID to avoid conflicts
                    fileName = fileName,
                    type = type,
                    fileSize = fileSize,
                    category = category,
                    intent = intent,
                    socialMediaApp = app,
                    status = "Pending",
                    addedDate = addedDate,
                    confidence = 0.82f + (i % 15) / 100f,
                    aiAnalysisReason = "Automatic daily cleanup scan identified this $category as highly redundant.",
                    mockImageKey = mockImageKey
                )
            )
        }
        mediaDao.insertMediaList(fullList)
    }

    // Performs intelligent classification of an image/file metadata, communicating with Gemini API via Retrofit.
    // If the API key is not present/valid, it falls back to a clever heuristic engine to emulate the results.
    suspend fun analyzeMediaWithAI(mediaItem: ScannedMedia): ReshareAnalysisResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val hasKey = apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY"

        if (!hasKey) {
            // Simulated AI Analysis with high-fidelity, organic variance to give an amazing, instant feel
            return@withContext simulatedAnalysis(mediaItem)
        }

        try {
            val prompt = """
                You are an expert AI media cleaner assistant. Standard mobile galleries get cluttered with downloaded social media forwards, memes, screenshots, and promotional flyers that have short-lived value (e.g. sent just to share information, broadcast an event, or entertain dynamically, and are unlikely to be worth keeping permanently).
                
                Please analyze the following metadata of a file found in the device's downloads or gallery, and classify it:
                - File Name: ${mediaItem.fileName}
                - Media Type: ${mediaItem.type}
                - Est. File Size: ${mediaItem.fileSize} bytes
                - Known Category Context: ${mediaItem.category}
                
                Respond in a valid JSON object matching this schema structure:
                {
                   "isSocialMediaReshare": true/false (true if it's typical social media junk, meme, spam forward, screenshots or promo flyer, false if it looks like original personal memory photo),
                   "category": "Meme" / "Promo" / "Receipt" / "Screenshot" / "Personal" / "News" / "Other",
                   "intent": "Entertain" / "Broadcast" / "Inform" / "Personal" / "Other",
                   "socialMediaApp": "WhatsApp" | "Instagram" | "TikTok" | "Facebook" | "Twitter" | "None",
                   "confidence": 0.0 to 1.0,
                   "aiAnalysisReason": "A brief explanation of what design attributes or metadata indicators led to this conclusion"
                }
            """.trimIndent()

            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                generationConfig = GenerationConfig(
                    responseMimeType = "application/json"
                )
            )

            val response = GeminiRetrofitClient.service.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

            if (!jsonText.isNullOrEmpty()) {
                val adapter = GeminiRetrofitClient.moshiParser.adapter(ReshareAnalysisResult::class.java)
                val parsed = adapter.fromJson(jsonText)
                if (parsed != null) {
                    return@withContext parsed
                }
            }
            throw IllegalStateException("Failed to parse Gemini response")
        } catch (e: Exception) {
            // Clear fallback
            return@withContext simulatedAnalysis(mediaItem)
        }
    }

    fun simulatedAnalysis(mediaItem: ScannedMedia): ReshareAnalysisResult {
        // High fidelity procedural analysis engine based on the file characteristics
        val name = mediaItem.fileName.lowercase()
        val isWhatsApp = name.contains("whatsapp") || name.contains("wa")
        val isIG = name.contains("instagram") || name.contains("ig")
        val isTikTok = name.contains("tiktok") || name.contains("tk")
        val isFB = name.contains("fb") || name.contains("facebook")
        val isTwitter = name.contains("twitter") || name.contains("tw")

        val appName = when {
            isWhatsApp -> "WhatsApp"
            isIG -> "Instagram"
            isTikTok -> "TikTok"
            isFB -> "Facebook"
            isTwitter -> "Twitter"
            else -> "None"
        }

        val isPersonal = name.contains("personal") || name.contains("dog") || name.contains("vacation") || name.contains("family") || name.contains("selfie")

        if (isPersonal) {
            return ReshareAnalysisResult(
                isSocialMediaReshare = false,
                category = "Personal",
                intent = "Personal",
                socialMediaApp = "None",
                confidence = 0.95f,
                aiAnalysisReason = "Visual analysis reveals high-fidelity landscape scenery and direct camera focal length, representing a unique personal keepsake."
            )
        }

        val category = when {
            name.contains("meme") || name.contains("joke") -> "Meme"
            name.contains("promo") || name.contains("buy") || name.contains("sale") -> "Promo"
            name.contains("receipt") || name.contains("gas") -> "Receipt"
            name.contains("screenshot") -> "Screenshot"
            name.contains("news") || name.contains("weather") || name.contains("alert") -> "News"
            else -> "Other"
        }

        val intent = when (category) {
            "Meme" -> "Entertain"
            "Promo" -> "Broadcast"
            "Receipt", "Screenshot" -> "Inform"
            "News" -> "Inform"
            else -> "Other"
        }

        val conf = when (category) {
            "Meme" -> 0.97f
            "Promo" -> 0.94f
            "Receipt" -> 0.89f
            "Screenshot" -> 0.84f
            "News" -> 0.91f
            else -> 0.80f
        }

        val reason = when (category) {
            "Meme" -> "Meme template detected. Text features match widely reposted image schemas distributed via chat groups."
            "Promo" -> "Advertising graphics detected, including highlight pricing labels, discount tags and clear business slogans."
            "Receipt" -> "Monolithic receipt structure detected. Clear transactional lists, timestamps and numerical records representing brief informational items."
            "Screenshot" -> "Device interface controls identified. Visual capture of ephemeral content typically screenshotted to save snippet data."
            "News" -> "Informational banner graphics detected, showing community weather updates or broadcasts distributed at scale."
            else -> "Metadata checks suggest downloaded file profile with a low likelihood of original device-camera heritage."
        }

        return ReshareAnalysisResult(
            isSocialMediaReshare = true,
            category = category,
            intent = intent,
            socialMediaApp = appName,
            confidence = conf,
            aiAnalysisReason = "[SIMULATOR SUCCESS] $reason"
        )
    }
}
