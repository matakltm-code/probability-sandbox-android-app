package com.example

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Entity(tableName = "tool_profiles")
data class ToolProfile(
    @PrimaryKey
    val toolName: String,
    val cssSelector: String,
    val label: String
)

@Dao
interface ToolProfileDao {
    @Query("SELECT * FROM tool_profiles WHERE toolName = :toolName")
    suspend fun getProfile(toolName: String): ToolProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ToolProfile)
}
