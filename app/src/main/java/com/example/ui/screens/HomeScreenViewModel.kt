package com.example.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.SiteRule
import com.example.data.local.ToolProfile
import com.example.data.repository.SiteRuleRepository
import com.example.data.repository.ToolProfileRepository
import com.example.data.local.Bookmark
import com.example.data.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeScreenViewModel(application: Application) : AndroidViewModel(application) {
    private val siteRuleRepository: SiteRuleRepository
    private val toolProfileRepository: ToolProfileRepository
    private val bookmarkRepository: BookmarkRepository
    val siteRules: StateFlow<List<SiteRule>>
    val bookmarks: StateFlow<List<Bookmark>>
    init {
        val database = AppDatabase.getDatabase(application)
        siteRuleRepository = SiteRuleRepository(database.siteRuleDao())
        toolProfileRepository = ToolProfileRepository(database.toolProfileDao())
        bookmarkRepository = BookmarkRepository(database.bookmarkDao())

        siteRules = siteRuleRepository.allRules.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        bookmarks = bookmarkRepository.allBookmarks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    private val _isWebViewVisible = MutableStateFlow(false)
    val isWebViewVisible: StateFlow<Boolean> = _isWebViewVisible.asStateFlow()

    private val _isControlPanelExpanded = MutableStateFlow(false)
    val isControlPanelExpanded: StateFlow<Boolean> = _isControlPanelExpanded.asStateFlow()

    private val _targetUrl = MutableStateFlow("https://google.com")
    val targetUrl: StateFlow<String> = _targetUrl.asStateFlow()

    private val _showExitConfirmation = MutableStateFlow(false)
    val showExitConfirmation: StateFlow<Boolean> = _showExitConfirmation.asStateFlow()

    private val _isPageLoading = MutableStateFlow(false)
    val isPageLoading: StateFlow<Boolean> = _isPageLoading.asStateFlow()

    private val _activeTool = MutableStateFlow("Inactive")
    val activeTool: StateFlow<String> = _activeTool.asStateFlow()

    private val _kenoData = MutableStateFlow("Ready to extract...")
    val kenoData: StateFlow<String> = _kenoData.asStateFlow()

    private val _aviatorData = MutableStateFlow("Ready to extract...")
    val aviatorData: StateFlow<String> = _aviatorData.asStateFlow()

    private val _isSelectionModeActive = MutableStateFlow(false)
    val isSelectionModeActive: StateFlow<Boolean> = _isSelectionModeActive.asStateFlow()

    private val _showMappingDialog = MutableStateFlow(false)
    val showMappingDialog: StateFlow<Boolean> = _showMappingDialog.asStateFlow()

    private val _selectedCssSelector = MutableStateFlow("")
    val selectedCssSelector: StateFlow<String> = _selectedCssSelector.asStateFlow()

    private val _selectedText = MutableStateFlow("")
    val selectedText: StateFlow<String> = _selectedText.asStateFlow()

    private val _isSiteRuleSelectionMode = MutableStateFlow(false)
    val isSiteRuleSelectionMode: StateFlow<Boolean> = _isSiteRuleSelectionMode.asStateFlow()

    private val _showSiteRuleDialog = MutableStateFlow(false)
    val showSiteRuleDialog: StateFlow<Boolean> = _showSiteRuleDialog.asStateFlow()

    private val _activePanelTab = MutableStateFlow("Predictor")
    val activePanelTab: StateFlow<String> = _activePanelTab.asStateFlow()

    private val _isPanelDropdownExpanded = MutableStateFlow(false)
    val isPanelDropdownExpanded: StateFlow<Boolean> = _isPanelDropdownExpanded.asStateFlow()

    private val _isLivePollingActive = MutableStateFlow(false)
    val isLivePollingActive: StateFlow<Boolean> = _isLivePollingActive.asStateFlow()

    fun setWebViewVisible(visible: Boolean) { _isWebViewVisible.value = visible }
    fun setControlPanelExpanded(expanded: Boolean) { _isControlPanelExpanded.value = expanded }
    fun setTargetUrl(url: String) { _targetUrl.value = url }
    fun setShowExitConfirmation(show: Boolean) { _showExitConfirmation.value = show }
    fun setPageLoading(loading: Boolean) { _isPageLoading.value = loading }
    fun setActiveTool(tool: String) { _activeTool.value = tool }
    fun setKenoData(data: String) { _kenoData.value = data }
    fun setAviatorData(data: String) { _aviatorData.value = data }
    fun setSelectionModeActive(active: Boolean) { _isSelectionModeActive.value = active }
    fun setShowMappingDialog(show: Boolean) { _showMappingDialog.value = show }
    fun setSelectedElement(cssSelector: String, text: String) {
        _selectedCssSelector.value = cssSelector
        _selectedText.value = text
    }
    fun setSiteRuleSelectionMode(active: Boolean) { _isSiteRuleSelectionMode.value = active }
    fun setShowSiteRuleDialog(show: Boolean) { _showSiteRuleDialog.value = show }
    fun setActivePanelTab(tab: String) { _activePanelTab.value = tab }
    fun setPanelDropdownExpanded(expanded: Boolean) { _isPanelDropdownExpanded.value = expanded }
    fun setLivePollingActive(active: Boolean) { _isLivePollingActive.value = active }

    // Repository operations
    fun insertSiteRule(rule: SiteRule) {
        viewModelScope.launch {
            siteRuleRepository.insertRule(rule)
        }
    }

    fun updateSiteRule(rule: SiteRule) {
        viewModelScope.launch {
            siteRuleRepository.updateRule(rule)
        }
    }

    fun deleteSiteRule(rule: SiteRule) {
        viewModelScope.launch {
            siteRuleRepository.deleteRule(rule)
        }
    }

    fun insertToolProfile(profile: ToolProfile) {
        viewModelScope.launch {
            toolProfileRepository.insertProfile(profile)
        }
    }

    suspend fun getToolProfile(toolName: String): ToolProfile? {
        return toolProfileRepository.getProfile(toolName)
    }

    fun isBookmarked(url: String): Flow<Boolean> {
        return bookmarkRepository.isBookmarked(url)
    }

    fun toggleBookmark(url: String, title: String) {
        viewModelScope.launch {
            val isCurrentlyBookmarked = bookmarks.value.any { it.url == url }
            if (isCurrentlyBookmarked) {
                bookmarkRepository.deleteBookmark(Bookmark(url, title))
            } else {
                bookmarkRepository.insertBookmark(Bookmark(url, title))
            }
        }
    }

}
