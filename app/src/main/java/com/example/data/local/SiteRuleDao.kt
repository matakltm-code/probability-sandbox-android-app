package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SiteRuleDao {
    @Query("SELECT * FROM site_rules")
    fun getAllRules(): Flow<List<SiteRule>>

    @Insert
    suspend fun insertRule(rule: SiteRule)

    @Update
    suspend fun updateRule(rule: SiteRule)

    @Delete
    suspend fun deleteRule(rule: SiteRule)
}
