package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val isFavorite: Boolean = false,
    val addedTimestamp: Long = System.currentTimeMillis(),
    val isSystemDetected: Boolean = false
)
