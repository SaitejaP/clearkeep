package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scanned_media")
data class ScannedMedia(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fileName: String,
    val type: String, // "Image" or "Video"
    val fileSize: Long, // Size in bytes
    val category: String, // "Meme", "Promo", "Receipt", "Screenshot", "Personal", "News", "Other"
    val intent: String, // "Entertain", "Broadcast", "Inform", "Personal", "Other"
    val socialMediaApp: String, // "WhatsApp", "Instagram", "TikTok", "Facebook", "Twitter", "None"
    val status: String, // "Pending", "Processed", "Discarded", "Whitelisted"
    val addedDate: Long = System.currentTimeMillis(),
    val confidence: Float = 0.85f,
    val aiAnalysisReason: String = "",
    val mockImageKey: String = "" // Code used to draw different visually detailed mock illustrations
)
