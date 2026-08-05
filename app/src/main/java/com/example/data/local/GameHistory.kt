package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_history")
data class GameHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val gameType: String,
    val siteUrl: String,
    val timestamp: Long,
    val data: String
)
