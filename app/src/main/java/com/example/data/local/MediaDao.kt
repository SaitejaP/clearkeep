package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Query("SELECT * FROM scanned_media ORDER BY addedDate DESC")
    fun getAllMedia(): Flow<List<ScannedMedia>>

    @Query("SELECT * FROM scanned_media WHERE status = 'Pending' ORDER BY addedDate DESC")
    fun getPendingMedia(): Flow<List<ScannedMedia>>

    @Query("SELECT * FROM scanned_media WHERE status = 'Whitelisted' ORDER BY addedDate DESC")
    fun getWhitelistedMedia(): Flow<List<ScannedMedia>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaList(list: List<ScannedMedia>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: ScannedMedia)

    @Update
    suspend fun updateMedia(media: ScannedMedia)

    @Query("UPDATE scanned_media SET status = :status WHERE id = :id")
    suspend fun updateMediaStatus(id: Int, status: String)

    @Query("UPDATE scanned_media SET status = :status WHERE id IN (:ids)")
    suspend fun updateMultipleMediaStatus(ids: List<Int>, status: String)

    @Query("DELETE FROM scanned_media WHERE id IN (:ids)")
    suspend fun deleteMediaByIds(ids: List<Int>)

    @Query("DELETE FROM scanned_media")
    suspend fun clearAllMedia()

    // Action Logs
    @Query("SELECT * FROM user_action_logs ORDER BY timestamp DESC")
    fun getAllActionLogs(): Flow<List<UserActionLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActionLog(log: UserActionLog)

    @Query("DELETE FROM user_action_logs")
    suspend fun clearActionLogs()
}
