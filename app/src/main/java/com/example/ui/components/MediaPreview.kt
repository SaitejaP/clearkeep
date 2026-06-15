package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MediaPreview(mockImageKey: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF2C3E50), Color(0xFF0F2027))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (mockImageKey.startsWith("content://") || mockImageKey.startsWith("file://") || mockImageKey.contains("/") || mockImageKey.contains(".")) {
            coil.compose.AsyncImage(
                model = mockImageKey,
                contentDescription = "Real scanned media",
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        } else when (mockImageKey) {
            "meme_cat" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFFFF7E5F), Color(0xFFFEB47B)),
                                radius = 400f
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Draw funny sunglasses
                        drawCircle(color = Color.Black, radius = 40f, center = Offset(size.width / 2 - 50f, size.height / 2 - 20f))
                        drawCircle(color = Color.Black, radius = 40f, center = Offset(size.width / 2 + 50f, size.height / 2 - 20f))
                        drawLine(color = Color.Black, start = Offset(size.width / 2 - 10f, size.height / 2 - 20f), end = Offset(size.width / 2 + 10f, size.height / 2 - 20f), strokeWidth = 12f)
                        
                        // Draw cat ears
                        drawRect(color = Color(0xFF3E2723), size = Size(60f, 60f), topLeft = Offset(size.width / 2 - 120f, size.height / 2 - 130f))
                        drawRect(color = Color(0xFF3E2723), size = Size(60f, 60f), topLeft = Offset(size.width / 2 + 60f, size.height / 2 - 130f))
                    }
                    Text(
                        text = "ME WHEN CODE FINALLY\nCOMPILES FIRST TRY",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(12.dp)
                            .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    Text(
                        text = "🐈 Meme Template",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            "tiktok_dance" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Draw colorful vertical neon rings
                        drawCircle(color = Color(0xFF00F2FE), radius = 220f, style = Stroke(width = 8f), center = Offset(size.width / 2, size.height / 2))
                        drawCircle(color = Color(0xFF4FACFE), radius = 150f, style = Stroke(width = 4f), center = Offset(size.width / 2, size.height / 2))
                        drawCircle(color = Color(0xFFFF0844), radius = 80f, style = Stroke(width = 6f), center = Offset(size.width / 2, size.height / 2))

                        // Audio waves
                        drawLine(color = Color.White, start = Offset(100f, size.height - 80f), end = Offset(100f, size.height - 180f), strokeWidth = 8f)
                        drawLine(color = Color.White, start = Offset(130f, size.height - 80f), end = Offset(130f, size.height - 220f), strokeWidth = 8f)
                        drawLine(color = Color.White, start = Offset(160f, size.height - 80f), end = Offset(160f, size.height - 140f), strokeWidth = 8f)
                    }
                    Text(
                        text = "🎵 TikTok Trend Music Loop",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    )
                    Text(
                        text = "🎥 TikTok Reshare VM",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .background(Color(0xFFE94057), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            "promo_bogo" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFFF416C), Color(0xFFFF4B2B))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Draw product flyer border details
                        drawRect(color = Color.Yellow, size = Size(size.width - 40f, size.height - 40f), topLeft = Offset(20f, 20f), style = Stroke(width = 6f))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "50% OFF",
                            color = Color.Yellow,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "SUMMER FASHION SALE",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "Promo expires tomorrow. Click link in bio!",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
            "receipt_gas" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF7F5F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                            .background(Color.White)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "SHELL STATION #8271",
                            color = Color.DarkGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "REGULAR FUEL    45L @ $1.52/L\nTOTAL AMOUNT    $68.40\nTRANSACTION APPROVED UN-87321",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Text(
                        text = "🧾 Receipt Image File",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .background(Color.Gray, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            "news_weather" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF1F4068), Color(0xFF162447))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Draw lightning bolting down
                        val path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(size.width / 2 + 50f, 40f)
                            lineTo(size.width / 2 - 30f, size.height / 2)
                            lineTo(size.width / 2 + 20f, size.height / 2)
                            lineTo(size.width / 2 - 50f, size.height - 30f)
                        }
                        drawPath(path = path, color = Color.Yellow, style = Stroke(width = 8f))
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color.Black.copy(alpha = 0.75f))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "⛈️ CATEGORY 3 STORM WARNING",
                            color = Color.Red,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Heavy wind gusts, rainfall and flash flooding risk. Share to WhatsApp family chat.",
                            color = Color.White,
                            fontSize = 10.sp
                        )
                    }
                }
            }
            "personal_vacation" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFFC5C7D), Color(0xFF6A82FB))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Draw serene mountains
                        val mountain1 = androidx.compose.ui.graphics.Path().apply {
                            moveTo(0f, size.height)
                            lineTo(size.width * 0.4f, size.height * 0.4f)
                            lineTo(size.width * 0.8f, size.height)
                        }
                        drawPath(path = mountain1, color = Color(0x7F2C3E50))

                        val mountain2 = androidx.compose.ui.graphics.Path().apply {
                            moveTo(size.width * 0.3f, size.height)
                            lineTo(size.width * 0.7f, size.height * 0.5f)
                            lineTo(size.width, size.height)
                        }
                        drawPath(path = mountain2, color = Color(0x992C3E50))
                    }
                    Text(
                        text = "🌲 Yosemite Valley Memories (June 2026)",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(6.dp)
                    )
                }
            }
            "screenshot_recipe" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFFFF9E6)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🥐 BAKING INSTRUCTIONS",
                            color = Color(0xFF8B4513),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "1. Active yeast with warm water\n2. Knead dough until springy (8 mins)\n3. Set oven temperature to 375F\n4. Bake for 30 minutes until golden brown",
                            color = Color.Black,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                    Text(
                        text = "📱 Instagram Screenshot",
                        color = Color.White,
                        fontSize = 10.sp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color(0xFFFF9800), RoundedCornerShape(12.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            "meme_twitter" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF15202B)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .background(Color(0xFF192734))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "🐦 @developer_guy",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "My app has 0 bugs.\nWait, that's because I haven't run compile_applet yet. 💀",
                            color = Color.White,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            "promo_shoes" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFED213A), Color(0xFF93291E))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Draw elegant outline silhouette of a sneaker
                        drawCircle(color = Color.White.copy(alpha = 0.15f), radius = 250f, center = Offset(size.width / 2 + 50f, size.height / 2))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "AIR MAX ULTRA",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "⚡ TODAY ONLY: $79.99 (Reg. $150.00)",
                            color = Color.Yellow,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            "personal_dog" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF11998e), Color(0xFF38ef7d))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Draw dog body silhouette (simple curves)
                        drawCircle(color = Color.White.copy(alpha = 0.2f), radius = 100f, center = Offset(size.width / 2, size.height / 2))
                    }
                    Text(
                        text = "🐕 Buddy Napping on Carpet (May 2026)",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(6.dp)
                    )
                }
            }
            else -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text(
                        text = "📁",
                        fontSize = 42.sp
                    )
                    Text(
                        text = mockImageKey.ifEmpty { "Downloaded File Item" },
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}
