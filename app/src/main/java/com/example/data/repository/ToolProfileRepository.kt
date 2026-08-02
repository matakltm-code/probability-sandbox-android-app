package com.example.data.repository

import com.example.data.local.ToolProfile
import com.example.data.local.ToolProfileDao

class ToolProfileRepository(private val dao: ToolProfileDao) {
    suspend fun getProfile(toolName: String): ToolProfile? {
        return dao.getProfile(toolName)
    }

    suspend fun insertProfile(profile: ToolProfile) {
        dao.insertProfile(profile)
    }
}
