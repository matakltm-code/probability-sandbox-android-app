package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ToolProfileDao {
    @Query("SELECT * FROM tool_profiles WHERE toolName = :toolName")
    suspend fun getProfile(toolName: String): ToolProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ToolProfile)
}
