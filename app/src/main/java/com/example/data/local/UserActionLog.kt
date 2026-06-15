package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_action_logs")
data class UserActionLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String,
    val intent: String,
    val action: String, // "Keep" (Whitelisted/Preserved) or "Delete" (Discarded)
    val timestamp: Long = System.currentTimeMillis()
)
