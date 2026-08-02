package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tool_profiles")
data class ToolProfile(
    @PrimaryKey
    val toolName: String,
    val cssSelector: String,
    val label: String
)
