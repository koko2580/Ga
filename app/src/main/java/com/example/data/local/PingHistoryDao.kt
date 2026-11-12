package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PingHistoryDao {
    @Query("SELECT * FROM ping_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentPings(limit: Int = 20): Flow<List<PingHistoryEntity>>

    @Insert
    suspend fun insertPing(entry: PingHistoryEntity)

    @Query("DELETE FROM ping_history")
    suspend fun clearHistory()
}
