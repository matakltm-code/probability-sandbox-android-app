package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "site_rules")
data class SiteRule(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val cssSelector: String,
    val isEnabled: Boolean = true,
    val label: String = ""
)
