package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.ui.theme.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ScannedMedia
import com.example.ui.components.MediaPreview
import com.example.ui.viewmodel.AppViewModel
import com.example.ui.viewmodel.UiState
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val pendingItems by viewModel.pendingMedia.collectAsState()
    val activeFilters by viewModel.activeFilters.collectAsState()
    val rawState by viewModel.analysisState.collectAsState()
    val aiEngineMode by viewModel.aiEngineMode.collectAsState()

    // Filter items to match active scanner preferences
    val swipeDeck = remember(pendingItems, activeFilters) {
        pendingItems.filter { activeFilters[it.category] == true }
    }

    val coroutineScope = rememberCoroutineScope()

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
                                    imageVector = Icons.Default.Favorite,
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
                                text = "SWIPE DECK",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (swipeDeck.isEmpty()) {
                SwipeEmptyState(onSimulateScan = { viewModel.triggerDeepScan() })
            } else {
                // Info banner
                Text(
                    text = "Swipe RIGHT to PRESERVE (Whitelist) • Swipe LEFT to DISCARD (Mark Deletion)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    // Render back card for beautiful stacked depth if there is more than 1 card
                    if (swipeDeck.size > 1) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(0.92f)
                                .height(460.dp)
                                .offset(y = 12.dp)
                                .alpha(0.6f)
                                .shadow(2.dp, RoundedCornerShape(24.dp)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(24.dp)
                        ) {}
                    }

                    // Top/Active card
                    val topItem = swipeDeck.first()
                    SwipeCard(
                        mediaItem = topItem,
                        aiEngineMode = aiEngineMode,
                        onSwipeLeft = {
                            viewModel.discardMedia(topItem)
                        },
                        onSwipeRight = {
                            viewModel.keepMedia(topItem)
                        },
                        onRunAIScan = {
                            viewModel.runRealTimeAICategoryScan(topItem)
                        },
                        rawState = rawState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(470.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons under deck
                val topItem = swipeDeck.first()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Discard Red circular border button (Close / Swipes Left)
                    IconButton(
                        onClick = { viewModel.discardMedia(topItem) },
                        modifier = Modifier
                            .size(48.dp)
                            .border(BorderStroke(1.dp, SwipeDiscardRed), CircleShape)
                            .testTag("discard_fab_left")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Discard File",
                            tint = SwipeDiscardRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(28.dp))

                    // AI Verify Filled Heart circle button
                    IconButton(
                        onClick = { viewModel.runRealTimeAICategoryScan(topItem) },
                        modifier = Modifier
                            .size(56.dp)
                            .background(SwipeHeartPurple, CircleShape)
                            .testTag("ai_verify_middle")
                    ) {
                        if (rawState is UiState.Scanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = SwipeHeartOnPurple
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Verify via AI",
                                tint = SwipeHeartOnPurple,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(28.dp))

                    // Keep Green circular border button (Check / Swipes Right)
                    IconButton(
                        onClick = { viewModel.keepMedia(topItem) },
                        modifier = Modifier
                            .size(48.dp)
                            .border(BorderStroke(1.dp, SwipeKeepGreen), CircleShape)
                            .testTag("keep_fab_right")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Keep Safe",
                            tint = SwipeKeepGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SwipeCard(
    mediaItem: ScannedMedia,
    aiEngineMode: com.example.ui.viewmodel.AiEngineMode,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onRunAIScan: () -> Unit,
    rawState: UiState,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    // Drag Offset State for swipe simulation and interactive drag gestures
    val dragOffsetX = remember { Animatable(0f) }
    val dragOffsetY = remember { Animatable(0f) }

    val formatBytes: (Long) -> String = { bytes ->
        when {
            bytes >= 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f MB", bytes.toFloat() / (1024 * 1024))
            else -> String.format(Locale.getDefault(), "%.1f KB", bytes.toFloat() / 1024)
        }
    }

    // Dynamic rotation angle based on swipe displacement
    val rotationFraction = (dragOffsetX.value / 300f).coerceIn(-1f, 1f)
    val cardRotation = rotationFraction * 12f // Max 12 degrees rotation

    Card(
        modifier = modifier
            .offset { IntOffset(dragOffsetX.value.toInt(), dragOffsetY.value.toInt()) }
            .rotate(cardRotation)
            .shadow(4.dp, RoundedCornerShape(24.dp))
            .pointerInput(mediaItem.id) {
                detectDragGestures(
                    onDragEnd = {
                        val thresholdX = 250f
                        if (dragOffsetX.value > thresholdX) {
                            coroutineScope.launch {
                                dragOffsetX.animateTo(600f, tween(150))
                                onSwipeRight()
                                dragOffsetX.snapTo(0f)
                                dragOffsetY.snapTo(0f)
                            }
                        } else if (dragOffsetX.value < -thresholdX) {
                            coroutineScope.launch {
                                dragOffsetX.animateTo(-600f, tween(150))
                                onSwipeLeft()
                                dragOffsetX.snapTo(0f)
                                dragOffsetY.snapTo(0f)
                            }
                        } else {
                            coroutineScope.launch {
                                dragOffsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                                dragOffsetY.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch {
                            dragOffsetX.snapTo(dragOffsetX.value + dragAmount.x)
                            dragOffsetY.snapTo(dragOffsetY.value + dragAmount.y)
                        }
                    }
                )
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Visual Preview Section
            Box(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxWidth()
            ) {
                MediaPreview(
                    mockImageKey = mediaItem.mockImageKey,
                    modifier = Modifier.fillMaxSize()
                )

                // Swipe overlay action indicator icons (Fade overlays like real Tinder)
                val dragX = dragOffsetX.value
                if (dragX.absoluteValue > 10f) {
                    val fadeValue = (dragX.absoluteValue / 200f).coerceIn(0f, 0.85f)
                    val cardOverlayColor = if (dragX > 0) Color(0xFF22C55E) else Color(0xFFEF4444)
                    val overlayIcon = if (dragX > 0) Icons.Default.Favorite else Icons.Default.Close

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(cardOverlayColor.copy(alpha = fadeValue))
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = overlayIcon,
                                contentDescription = if (dragX > 0) "Save" else "Discard",
                                tint = Color.White,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (dragX > 0) "PRESERVE MEMENTO" else "DISCARD SPAM",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Header app overlay tag if available
                if (mediaItem.socialMediaApp != "None") {
                    Box(
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.TopEnd)
                            .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = mediaItem.socialMediaApp,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Specs / Metadata Details Section
            Column(
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = mediaItem.fileName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatBytes(mediaItem.fileSize),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(mediaItem.category) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    SuggestionChip(
                        onClick = {},
                        label = { Text("Intent: ${mediaItem.intent}") },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                if (aiEngineMode == com.example.ui.viewmodel.AiEngineMode.NANO_ON_DEVICE) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🛡️", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "On-Device Gemini Nano active (100% private)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // AI Explanation Reason block
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp)
                ) {
                    Column(modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "AI Reason",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Intelligent AI Scan Verdict (${(mediaItem.confidence * 100).toInt()}% Conf)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = mediaItem.aiAnalysisReason.ifEmpty { "Pending deep analysis. Tap the Verify via AI action below to perform real-time verification using Gemini." },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SwipeEmptyState(
    onSimulateScan: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color(0xFFDCFCE7), androidx.compose.foundation.shape.CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF22C55E),
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Your Gallery is Clean! 🎉",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "All downloaded social media reshares have been organized or cleared. Excellent storage status!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onSimulateScan,
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Simulate Scan")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Discover New Downloads")
        }
    }
}

