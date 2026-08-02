package com.example.data.repository

import com.example.data.local.SiteRule
import com.example.data.local.SiteRuleDao
import kotlinx.coroutines.flow.Flow

class SiteRuleRepository(private val dao: SiteRuleDao) {
    val allRules: Flow<List<SiteRule>> = dao.getAllRules()

    suspend fun insertRule(rule: SiteRule) {
        dao.insertRule(rule)
    }

    suspend fun updateRule(rule: SiteRule) {
        dao.updateRule(rule)
    }

    suspend fun deleteRule(rule: SiteRule) {
        dao.deleteRule(rule)
    }
}
