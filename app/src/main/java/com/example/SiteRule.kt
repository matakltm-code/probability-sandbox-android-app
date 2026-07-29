package com.example

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "site_rules")
data class SiteRule(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val cssSelector: String,
    val isEnabled: Boolean = true,
    val label: String = ""
)

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
