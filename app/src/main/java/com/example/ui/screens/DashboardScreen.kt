package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ScannedMedia
import com.example.ui.viewmodel.AppViewModel
import java.util.Locale
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.border

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: AppViewModel,
    onNavigateToSwipe: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allMedia by viewModel.allMedia.collectAsState()
    val isScanning by viewModel.isDeepScanning.collectAsState()
    val activeFilters by viewModel.activeFilters.collectAsState()
    val savingsBytes by viewModel.storageSavingsBytes.collectAsState()

    val pendingCount = allMedia.count { it.status == "Pending" && activeFilters[it.category] == true }
    val totalCount = allMedia.size

    val context = LocalContext.current
    var showPreviewGrid by remember { mutableStateOf(false) }
    var selectedItemIds by remember { mutableStateOf(setOf<Int>()) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var hasPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        hasPermission = granted
        if (granted) {
            viewModel.scanDeviceMedia(context)
        }
    }

    var activeInspectCategory by remember { mutableStateOf<String?>(null) }

    val formatBytes: (Long) -> String = { bytes ->
        when {
            bytes >= 1024 * 1024 * 1024 -> String.format(Locale.getDefault(), "%.2f GB", bytes.toFloat() / (1024 * 1024 * 1024))
            bytes >= 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f MB", bytes.toFloat() / (1024 * 1024))
            else -> String.format(Locale.getDefault(), "%.1f KB", bytes.toFloat() / 1024)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.fillMaxWidth().padding(start = 4.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "CleanSlate AI",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "STORAGE MANAGER",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (hasPermission) {
                                viewModel.scanDeviceMedia(context)
                            } else {
                                permissionLauncher.launch(permissionsToRequest)
                            }
                        },
                        enabled = !isScanning,
                        modifier = Modifier.testTag("scan_button")
                    ) {
                        val rotationAnim = rememberInfiniteTransition(label = "rotate")
                        val rotationAngle by rotationAnim.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1500, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "rotateAngle"
                        )
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Scan Gallery",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = if (isScanning) Modifier.rotate(rotationAngle) else Modifier
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Storage Savings Card (Circular ring styled)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = "Potential Savings",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = formatBytes(savingsBytes).substringBefore(" "),
                                        fontSize = 40.sp,
                                        fontWeight = FontWeight.Light,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = formatBytes(savingsBytes).substringAfter(" ", "GB"),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }
                            }
                            
                            Surface(
                                color = Color(0xFF001D35),
                                contentColor = Color.White,
                                shape = RoundedCornerShape(100.dp)
                            ) {
                                Text(
                                    text = "HIGH IMPACT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        // Linear progress indicator
                        val progressFraction = remember(pendingCount, totalCount) {
                            if (totalCount > 0) pendingCount.toFloat() / totalCount.toFloat() else 0.65f
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(100.dp))
                                .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progressFraction.coerceIn(0.05f, 1.0f))
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viewModel.oneTapBulkClearAllUnwanted(context) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("one_tap_clear_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(100.dp),
                                enabled = pendingCount > 0 && !isScanning
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Bulk Delete",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Clean identified media ($pendingCount items)", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            }

                            OutlinedButton(
                                onClick = { showPreviewGrid = true },
                                shape = RoundedCornerShape(100.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("preview_identified_media_button"),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Preview Identified Media",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Preview Identified Media", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }



            // Quick Stats
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatBox(
                        title = "Scanned Items",
                        value = "$totalCount",
                        icon = Icons.Default.List,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        title = "Protected Safe",
                        value = "${allMedia.count { it.status == "Whitelisted" }}",
                        icon = Icons.Default.CheckCircle,
                        tint = Color(0xFF2ECC71),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Storage access permission suggestion bar
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable {
                        if (!hasPermission) {
                            permissionLauncher.launch(permissionsToRequest)
                        }
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = if (hasPermission) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        1.dp,
                        if (hasPermission) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (hasPermission) "📂" else "🔒",
                            fontSize = 24.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (hasPermission) "Gallery File Access ACTIVE" else "Gallery Permission Required",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (hasPermission) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (hasPermission) "Allowing CleanSlate to go through images on your device and suggest deletion cleanups." else "Requesting access to read your phone's images to proceed with smart deletion suggestions.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (!hasPermission) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { permissionLauncher.launch(permissionsToRequest) },
                                shape = RoundedCornerShape(100.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text("Allow", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Filters Section (Toggle buttons)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Filter Scan Categories",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Toggle categories to include/exclude them from storage savings & bulk cleanup pools",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Wrapping Grid Flow Layout
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            activeFilters.forEach { (cat, enabled) ->
                                val count = allMedia.count { it.category == cat }
                                FilterChip(
                                    selected = enabled,
                                    onClick = { viewModel.toggleFilter(cat) },
                                    label = { Text("$cat ($count)") },
                                    leadingIcon = {
                                        if (enabled) {
                                            Icon(
                                                imageVector = Icons.Default.Done,
                                                contentDescription = "Enabled",
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Folders list
            item {
                Text(
                    text = "Intent Groups Outbox",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            val categoryGroups = allMedia
                .filter { it.status == "Pending" && (activeFilters[it.category] == true) }
                .groupBy { it.category }

            if (categoryGroups.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No pending reshares found!",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Your active selection is clean. Tap 'Simulate Scan' at the top to discover more social downloads.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.triggerDeepScan() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Scan")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Simulate Gallery Scan")
                        }
                    }
                }
            } else {
                items(categoryGroups.keys.toList()) { cat ->
                    val list = categoryGroups[cat] ?: emptyList()
                    val sizeSum = list.sumOf { it.fileSize }
                    FolderCleanupCard(
                        categoryName = cat,
                        itemCount = list.size,
                        sizeStr = formatBytes(sizeSum),
                        onBulkDelete = { viewModel.bulkDeleteCategory(cat, context) },
                        onClick = { activeInspectCategory = cat }
                    )
                }
            }

            // Demo Controls
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(onClick = { viewModel.resetDemo() }) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = "Reset Database")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset Demo Database", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        activeInspectCategory?.let { category ->
            val itemsToInspect = allMedia.filter { it.category == category && it.status == "Pending" }
            InspectMediaDialog(
                categoryName = category,
                items = itemsToInspect,
                onDismiss = { activeInspectCategory = null },
                onKeepItem = { item -> viewModel.keepMedia(item) },
                onDeleteItem = { item -> viewModel.deleteMediaWithPhysical(context, item) },
                onClearAllCategory = { viewModel.bulkDeleteCategory(category, context) }
            )
        }

        if (showPreviewGrid) {
            val suggestedMedia = remember(allMedia, activeFilters) {
                allMedia.filter { it.status == "Pending" && activeFilters[it.category] == true }
            }

            // Initialize/reset selection to all suggestions when opened
            LaunchedEffect(showPreviewGrid) {
                if (showPreviewGrid) {
                    selectedItemIds = suggestedMedia.map { it.id }.toSet()
                }
            }

            val selectedMediaItems = remember(selectedItemIds, suggestedMedia) {
                suggestedMedia.filter { it.id in selectedItemIds }
            }
            val selectedPhotos = selectedMediaItems.count { it.type == "Image" }
            val selectedVideos = selectedMediaItems.count { it.type == "Video" }

            Dialog(
                onDismissRequest = { showPreviewGrid = false },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = false
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                navigationIcon = {
                                    IconButton(onClick = { showPreviewGrid = false }) {
                                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Close")
                                    }
                                },
                                title = {
                                    Column {
                                        Text(
                                            text = "Cleanup Suggestions Grid",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${selectedItemIds.size} of ${suggestedMedia.size} Selected",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                actions = {
                                    IconButton(
                                        onClick = {
                                            if (selectedItemIds.isNotEmpty()) {
                                                showDeleteConfirmation = true
                                            }
                                        },
                                        enabled = selectedItemIds.isNotEmpty(),
                                        modifier = Modifier.testTag("grid_delete_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Selected",
                                            tint = if (selectedItemIds.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                    ) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            if (suggestedMedia.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF2ECC71),
                                            modifier = Modifier.size(64.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Your Gallery is Clean!",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "No pending files require review right now.",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(24.dp))
                                        Button(onClick = { showPreviewGrid = false }) {
                                            Text("Go Back")
                                        }
                                    }
                                }
                            } else {
                                val allSelected = selectedItemIds.size == suggestedMedia.size
                                
                                // Selection Actions Header
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable {
                                            if (allSelected) {
                                                selectedItemIds = emptySet()
                                            } else {
                                                selectedItemIds = suggestedMedia.map { it.id }.toSet()
                                            }
                                        }
                                    ) {
                                        Checkbox(
                                            checked = allSelected,
                                            onCheckedChange = { checked ->
                                                selectedItemIds = if (checked) {
                                                    suggestedMedia.map { it.id }.toSet()
                                                } else {
                                                    emptySet()
                                                }
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Select All",
                                            fontWeight = FontWeight.SemiBold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    
                                    Text(
                                        text = "Total Size: ${formatBytes(selectedMediaItems.sumOf { it.fileSize })}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(3),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .weight(1f),
                                    contentPadding = PaddingValues(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(suggestedMedia, key = { it.id }) { mediaItem ->
                                        val isSelected = selectedItemIds.contains(mediaItem.id)
                                        Card(
                                            modifier = Modifier
                                                .aspectRatio(1f)
                                                .clickable {
                                                    selectedItemIds = if (isSelected) {
                                                        selectedItemIds - mediaItem.id
                                                    } else {
                                                        selectedItemIds + mediaItem.id
                                                    }
                                                }
                                                .testTag("grid_item_${mediaItem.id}"),
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(
                                                width = if (isSelected) 3.dp else 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                            ),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                                            )
                                        ) {
                                            Box(modifier = Modifier.fillMaxSize()) {
                                                com.example.ui.components.MediaPreview(
                                                    mockImageKey = mediaItem.mockImageKey,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                                
                                                // Badges & info overlay
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.BottomStart)
                                                        .padding(4.dp)
                                                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                    ) {
                                                        Text(
                                                            text = if (mediaItem.type == "Video") "🎥" else "🖼️",
                                                            fontSize = 10.sp,
                                                            color = Color.White
                                                        )
                                                        Text(
                                                            text = formatBytes(mediaItem.fileSize),
                                                            fontSize = 9.sp,
                                                            color = Color.White
                                                        )
                                                    }
                                                }
                                                
                                                // Selection indicator check circle
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .padding(6.dp)
                                                        .size(22.dp)
                                                        .background(
                                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.4f),
                                                            shape = CircleShape
                                                        )
                                                        .border(
                                                            width = 1.dp,
                                                            color = Color.White,
                                                            shape = CircleShape
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (isSelected) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = "Selected",
                                                            tint = Color.White,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showDeleteConfirmation) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmation = false },
                    title = { Text("Confirm Cleanup") },
                    text = {
                        Text("Are you sure you want to delete these? $selectedPhotos photos and $selectedVideos videos will be deleted, freeing up ${formatBytes(selectedMediaItems.sumOf { it.fileSize })} of storage space.")
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.deleteMultipleMedia(selectedMediaItems, context)
                                showDeleteConfirmation = false
                                showPreviewGrid = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Delete Permanently")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirmation = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun StatBox(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun FolderCleanupCard(
    categoryName: String,
    itemCount: Int,
    sizeStr: String,
    onBulkDelete: () -> Unit,
    onClick: () -> Unit
) {
    val folderIcon = when (categoryName) {
        "Meme" -> "🎭"
        "Promo" -> "📢"
        "Receipt" -> "🧾"
        "Screenshot" -> "📱"
        "News" -> "📰"
        else -> "📁"
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = folderIcon,
                    fontSize = 28.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Column {
                    Text(
                        text = "$categoryName reshares",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$itemCount files • Cleans $sizeStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = onBulkDelete,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                    contentColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(100.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Bulk Clean",
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Clean", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

// FlowRow helper Composable (Simple row that wraps items)
@Composable
fun FlowRow(
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(content = content) { measurables, constraints ->
        val childConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val placeables = measurables.map { it.measure(childConstraints) }

        var currentX = 0
        var currentY = 0
        var maxRowHeight = 0
        val lP = mutableListOf<Pair<androidx.compose.ui.layout.Placeable, Pair<Int, Int>>>()

        placeables.forEach { p ->
            if (currentX + p.width > constraints.maxWidth) {
                currentX = 0
                currentY += maxRowHeight + verticalArrangement.let { 8.dp.roundToPx() } // default padding spacing
                maxRowHeight = 0
            }
            lP.add(p to Pair(currentX, currentY))
            currentX += p.width + horizontalArrangement.let { 8.dp.roundToPx() } // default chip spacing
            if (p.height > maxRowHeight) {
                maxRowHeight = p.height
            }
        }

        layout(
            width = constraints.maxWidth,
            height = currentY + maxRowHeight
        ) {
            lP.forEach { (p, offset) ->
                p.place(offset.first, offset.second)
            }
        }
    }
}

// Simple Composable Helper for Old Icon API
@Composable
fun Icon(imageName: androidx.compose.ui.graphics.vector.ImageVector, contentDescription: String, tint: Color, modifier: Modifier) {
    Icon(imageVector = imageName, contentDescription = contentDescription, modifier = modifier, tint = tint)
}

@Composable
fun InspectMediaDialog(
    categoryName: String,
    items: List<ScannedMedia>,
    onDismiss: () -> Unit,
    onKeepItem: (ScannedMedia) -> Unit,
    onDeleteItem: (ScannedMedia) -> Unit,
    onClearAllCategory: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close Review", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            if (items.isNotEmpty()) {
                Button(
                    onClick = {
                        onClearAllCategory()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = Color.White
                    )
                ) {
                    Text("Clean All (${items.size})")
                }
            }
        },
        title = {
            Column {
                Text(
                    text = "Review $categoryName Suggestions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${items.size} reshares flag under CleanSlate scan.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "This category pool is empty!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items) { mediaItem ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    com.example.ui.components.MediaPreview(
                                        mockImageKey = mediaItem.mockImageKey,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = mediaItem.fileName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val sizeKB = "${mediaItem.fileSize / 1024L} KB"
                                    Text(
                                        text = "$sizeKB • Match: ${(mediaItem.confidence * 100).toInt()}%",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = mediaItem.intent,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                if (mediaItem.aiAnalysisReason.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = mediaItem.aiAnalysisReason,
                                        fontSize = 9.sp,
                                        lineHeight = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { onKeepItem(mediaItem) },
                                        shape = RoundedCornerShape(100.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = Color(0xFF2ECC71)
                                        ),
                                        border = BorderStroke(1.dp, Color(0xFF2ECC71)),
                                        modifier = Modifier
                                            .weight(1.0f)
                                            .height(34.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Keep Safe",
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Whitelist", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { onDeleteItem(mediaItem) },
                                        shape = RoundedCornerShape(100.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer,
                                            contentColor = MaterialTheme.colorScheme.error
                                        ),
                                        modifier = Modifier
                                            .weight(1.0f)
                                            .height(34.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Delete",
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Delete", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
