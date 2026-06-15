package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AppViewModel
import com.example.ui.viewmodel.Recommendation
import com.example.ui.viewmodel.AiEngineMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val insights by viewModel.personalizedInsights.collectAsState()
    val actionLogs by viewModel.actionLogs.collectAsState()

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
                                    imageVector = Icons.Default.Settings,
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
                                text = "SETTINGS & INSIGHTS",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 🛡️ AI Model Settings Panel with Pros/Cons
            item {
                val aiEngineMode by viewModel.aiEngineMode.collectAsState()
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "⚙️ AI Classification Engine",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Choose which intelligence model filters your gallery.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // On-Device (Nano) Option
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { if (aiEngineMode != AiEngineMode.NANO_ON_DEVICE) viewModel.toggleAIEngineMode() }
                                    .testTag("ai_engine_nano"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (aiEngineMode == AiEngineMode.NANO_ON_DEVICE) {
                                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                                    } else {
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                    }
                                ),
                                border = BorderStroke(
                                    width = if (aiEngineMode == AiEngineMode.NANO_ON_DEVICE) 2.dp else 1.dp,
                                    color = if (aiEngineMode == AiEngineMode.NANO_ON_DEVICE) {
                                        MaterialTheme.colorScheme.tertiary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                    }
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🛡️", fontSize = 24.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "On-Device",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (aiEngineMode == AiEngineMode.NANO_ON_DEVICE) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Gemini Nano",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Cloud (Flash) Option
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { if (aiEngineMode != AiEngineMode.FLASH_CLOUD) viewModel.toggleAIEngineMode() }
                                    .testTag("ai_engine_flash"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (aiEngineMode == AiEngineMode.FLASH_CLOUD) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                    } else {
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                    }
                                ),
                                border = BorderStroke(
                                    width = if (aiEngineMode == AiEngineMode.FLASH_CLOUD) 2.dp else 1.dp,
                                    color = if (aiEngineMode == AiEngineMode.FLASH_CLOUD) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                    }
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🌐", fontSize = 24.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Cloud Core",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (aiEngineMode == AiEngineMode.FLASH_CLOUD) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Gemini Flash",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(16.dp))

                        // Pros and Cons text depending on mode
                        if (aiEngineMode == AiEngineMode.NANO_ON_DEVICE) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "🛡️ On-Device Gemini Nano Properties:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                                Row(verticalAlignment = Alignment.Top) {
                                    Text("✅", fontSize = 11.sp, modifier = Modifier.padding(top = 1.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text("Absolute User Privacy", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Text("Images are analyzed directly on your device chip. 0KB is ever uploaded.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Row(verticalAlignment = Alignment.Top) {
                                    Text("✅", fontSize = 11.sp, modifier = Modifier.padding(top = 1.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text("Network Independent", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Text("Continuously filters reshares on flights, offline areas, or weak connections.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Row(verticalAlignment = Alignment.Top) {
                                    Text("❌", fontSize = 11.sp, modifier = Modifier.padding(top = 1.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text("Lighter Context Comprehension", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Text("Compressed local weights may occasionally overlook complex pictorial jokes or hidden visual clues.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "🌐 Cloud Gemini 3.5 Flash Properties:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Row(verticalAlignment = Alignment.Top) {
                                    Text("✅", fontSize = 11.sp, modifier = Modifier.padding(top = 1.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text("Advanced Semantic Intelligence", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Text("Full-scale deep neural reasoning guarantees higher recall and scene understanding.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Row(verticalAlignment = Alignment.Top) {
                                    Text("❌", fontSize = 11.sp, modifier = Modifier.padding(top = 1.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text("Cloud Offloading & Signatures Transfer", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Text("Requires secure network packets to transmit file metadata and structural indicators to external Cloud Core servers.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Row(verticalAlignment = Alignment.Top) {
                                    Text("❌", fontSize = 11.sp, modifier = Modifier.padding(top = 1.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text("Cellular & Internet Required", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Text("Analyzers will skip items if there is no stable internet or network bandwidth available.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Profile card showing learned state
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(20.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🧠",
                                fontSize = 32.sp,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column {
                                Text(
                                    text = "Habits Neural Learning",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Active Gallery Shield Profile",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Every card you swipe left to delete or swipe right to whitelist trains local cleanup rules. Future scans adapt automatically to prioritize types of media you discard quickly.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total user decisions tracked:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Medium
							)
                            Text(
                                text = "${actionLogs.size} decisions",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            // Learning Recommendations Header
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Clean Suggestions",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Smart Custom Recommendations",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // List custom calculated recommendations
            if (insights.isEmpty()) {
                item {
                    Text(
                        text = "Loading recommendations...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(insights) { insight ->
                    RecommendationCard(insight = insight)
                }
            }

            // Section: History Logs audit
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Habit Training Log Audit",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (actionLogs.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No decisions tracked yet",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Decisions will appear here as you swipe left/right in the deck.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 4.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(actionLogs.take(5)) { log ->
                    LogAuditItem(log = log)
                }
                if (actionLogs.size > 5) {
                    item {
                        Text(
                            text = "+ ${actionLogs.size - 5} more neural logs in database",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 24.dp)
                        )
                    }
                } else {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendationCard(insight: Recommendation) {
    val cardBg = if (insight.alertType == "danger") {
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
    }

    val themeColor = if (insight.alertType == "danger") {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, themeColor.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (insight.alertType == "danger") Icons.Default.Settings else Icons.Default.Info,
                        contentDescription = "Alert",
                        tint = themeColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = insight.title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = insight.statsText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = themeColor,
                    modifier = Modifier
                        .border(1.dp, themeColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = insight.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
fun LogAuditItem(log: com.example.data.local.UserActionLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (log.action == "Keep") Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (log.action == "Keep") "💚" else "🗑️",
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "${log.category} swipe decision",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Intent tag: ${log.intent}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = if (log.action == "Keep") "WHITELISTED" else "DISCARDED",
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (log.action == "Keep") Color(0xFF2ECC71) else Color(0xFFEF4444)
            )
        }
    }
}
