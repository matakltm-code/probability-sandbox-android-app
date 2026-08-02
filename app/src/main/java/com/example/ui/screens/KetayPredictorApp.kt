package com.example.ui.screens

import android.annotation.SuppressLint
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.CookieManager
import android.webkit.WebSettings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ActivityLogViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import com.example.data.local.AppDatabase
import com.example.data.local.SiteRule
import com.example.data.local.ToolProfile
import com.example.MapperJS
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.LocalContext
import java.util.Locale
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.lifecycle.viewmodel.compose.viewModel
@Composable
fun HomeScreen(modifier: Modifier = Modifier, activityLogViewModel: ActivityLogViewModel, onNavigateToLogs: () -> Unit, onNavigateToDeveloper: () -> Unit, viewModel: HomeScreenViewModel = viewModel()) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val isWebViewVisible by viewModel.isWebViewVisible.collectAsStateWithLifecycle()
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    val isControlPanelExpanded by viewModel.isControlPanelExpanded.collectAsStateWithLifecycle()
    val targetUrl by viewModel.targetUrl.collectAsStateWithLifecycle()
    val showExitConfirmation by viewModel.showExitConfirmation.collectAsStateWithLifecycle()
    val isPageLoading by viewModel.isPageLoading.collectAsStateWithLifecycle()
    val activeTool by viewModel.activeTool.collectAsStateWithLifecycle()
    val kenoData by viewModel.kenoData.collectAsStateWithLifecycle()
    val aviatorData by viewModel.aviatorData.collectAsStateWithLifecycle()
    val isSelectionModeActive by viewModel.isSelectionModeActive.collectAsStateWithLifecycle()
    val showMappingDialog by viewModel.showMappingDialog.collectAsStateWithLifecycle()
    val selectedCssSelector by viewModel.selectedCssSelector.collectAsStateWithLifecycle()
    val selectedText by viewModel.selectedText.collectAsStateWithLifecycle()
    val isSiteRuleSelectionMode by viewModel.isSiteRuleSelectionMode.collectAsStateWithLifecycle()
    val showSiteRuleDialog by viewModel.showSiteRuleDialog.collectAsStateWithLifecycle()
    val activePanelTab by viewModel.activePanelTab.collectAsStateWithLifecycle()
    val isPanelDropdownExpanded by viewModel.isPanelDropdownExpanded.collectAsStateWithLifecycle()
    val isLivePollingActive by viewModel.isLivePollingActive.collectAsStateWithLifecycle()
    
    
    
    val siteRules by viewModel.siteRules.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(isLivePollingActive, activeTool) {
        if (isLivePollingActive && activeTool != "Inactive") {
            while (true) {
                kotlinx.coroutines.delay(2000)
                val profile = viewModel.getToolProfile(activeTool)
                if (profile != null && profile.cssSelector.isNotEmpty()) {
                    val jsExtractor = "(function() { " +
                        "var elements = document.querySelectorAll('${profile.cssSelector}'); " +
                        "var texts = []; " +
                        "for (var i = 0; i < elements.length; i++) { texts.push(elements[i].innerText); } " +
                        "return texts.join(', '); " +
                    "})();"
                    webViewRef?.evaluateJavascript(jsExtractor) { result ->
                        val cleanResult = result?.removeSurrounding("\"") ?: "none"
                        if (activeTool == "Keno") {
                            val newNumbers = (1..80).shuffled().take(5).joinToString("-")
                            viewModel.setKenoData("Mapped Data: $cleanResult\nPattern: $newNumbers")
                        } else if (activeTool == "Aviator") {
                            val multiplier = String.format(java.util.Locale.US, "%.2fx", kotlin.random.Random.nextDouble(1.0, 10.0))
                            viewModel.setAviatorData("Mapped Data: $cleanResult\nTrend: $multiplier")
                        }
                    }
                }
            }
        }
    }
    LaunchedEffect(isSelectionModeActive, isSiteRuleSelectionMode) {
        if (isSelectionModeActive || isSiteRuleSelectionMode) {
            webViewRef?.evaluateJavascript(MapperJS.enableScript, null)
        } else {
            webViewRef?.evaluateJavascript(MapperJS.disableScript, null)
        }
    }
    LaunchedEffect(siteRules) {
        val js = java.lang.StringBuilder("(function() { ")
        js.append("var style = document.getElementById('site-rules-style'); ")
        js.append("if (!style) { style = document.createElement('style'); style.id = 'site-rules-style'; document.head.appendChild(style); } ")
        val css = java.lang.StringBuilder()
        siteRules.filter { it.isEnabled }.forEach { rule ->
            css.append("${rule.cssSelector} { display: none !important; } ")
        }
        js.append("style.innerHTML = `${css.toString()}`; ")
        js.append("})();")
        webViewRef?.evaluateJavascript(js.toString(), null)
    }
    if (showSiteRuleDialog) {
        AlertDialog(
            onDismissRequest = { 
                viewModel.setShowSiteRuleDialog(false)
                viewModel.setSiteRuleSelectionMode(false)
            },
            title = { Text("Hide this element?", color = Color.White) },
            text = { 
                Column {
                    Text("Path: $selectedCssSelector", color = Color.Gray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Sample text: $selectedText", color = MaterialTheme.colorScheme.onBackground)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.insertSiteRule(SiteRule(cssSelector = selectedCssSelector, label = selectedText.take(20)))
                        }
                        viewModel.setShowSiteRuleDialog(false)
                        viewModel.setSiteRuleSelectionMode(false)
                    }
                ) {
                    Text("Save Rule", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    viewModel.setShowSiteRuleDialog(false) 
                    viewModel.setSiteRuleSelectionMode(false)
                }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
    if (showMappingDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setShowMappingDialog(false) },
            title = { Text("Map this element?", color = Color.White) },
            text = { 
                Column {
                    Text("Target Tool: $activeTool", color = com.example.ui.theme.TextHighlight)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Path: $selectedCssSelector", color = Color.Gray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Sample text: $selectedText", color = MaterialTheme.colorScheme.onBackground)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.insertToolProfile(
                                ToolProfile(
                                    toolName = activeTool,
                                    cssSelector = selectedCssSelector,
                                    label = "version_0000001"
                                )
                            )
                            activityLogViewModel.logActivity("Mapped $activeTool CSS: $selectedCssSelector")
                        }
                        viewModel.setShowMappingDialog(false)
                        viewModel.setSelectionModeActive(false)
                    }
                ) {
                    Text("Save to ObjectBox", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setShowMappingDialog(false) }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }
    LaunchedEffect(Unit) {
        activityLogViewModel.logActivity("Opened app")
    }
    BackHandler(enabled = isWebViewVisible) {
        if (webViewRef?.canGoBack() == true) {
            webViewRef?.goBack()
        } else {
            viewModel.setShowExitConfirmation(true)
        }
    }
    if (showExitConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.setShowExitConfirmation(false) },
            title = { Text("Confirm Exit") },
            text = { Text("Did you want to leave this site?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setShowExitConfirmation(false)
                        webViewRef?.loadUrl("about:blank")
                        viewModel.setWebViewVisible(false)
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.setShowExitConfirmation(false) }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (!isWebViewVisible) {
            // Background Mesh
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                // Top right green blur
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(primaryColor.copy(alpha = 0.2f), Color.Transparent),
                        center = Offset(canvasWidth, 0f),
                        radius = 600f
                    ),
                    center = Offset(canvasWidth, 0f),
                    radius = 600f
                )
                // Center left blue blur
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(secondaryColor.copy(alpha = 0.2f), Color.Transparent),
                        center = Offset(0f, canvasHeight / 2f),
                        radius = 500f
                    ),
                    center = Offset(0f, canvasHeight / 2f),
                    radius = 500f
                )
            }
            // Home Menu Layer
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                // Header Section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Code Icon",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "NATIVE WRAPPER",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "Probability Sandbox",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Secured sandbox browser for predictive model viewing and engine analytics.",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 48.dp)
                )
                // Action Cards
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(28.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(28.dp))
                            .padding(24.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(
                                        text = "ACTIVE SESSION",
                                        color = com.example.ui.theme.TextHighlight,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Analytics Engine",
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                        .border(1.dp, primaryColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "READY",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { 
                                    viewModel.setTargetUrl("https://google.com")
                                    viewModel.setWebViewVisible(true) 
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(28.dp)
                            ) {
                                Text(
                                    text = "Open Google",
                                    fontSize = 18.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Forward",
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { 
                                    viewModel.setTargetUrl("https://melbet-et.com")
                                    viewModel.setWebViewVisible(true) 
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(28.dp)
                            ) {
                                Text(
                                    text = "https://melbet-et.com",
                                    fontSize = 18.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Forward",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TextButton(
                            onClick = { onNavigateToLogs() },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        ) {
                            Text(
                                text = "View Logs",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        TextButton(
                            onClick = { onNavigateToDeveloper() },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        ) {
                            Text(
                                text = "Developer",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    // Console Log Preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row {
                                Text(
                                    text = "JS CONSOLE",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.4f)
                                )
                                Text(
                                    text = "_",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.4f)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "> Probability Sandbox Engine Initialized",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = com.example.ui.theme.KenoGreen.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "> Waiting for activity trigger...",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = com.example.ui.theme.KenoGreen.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "> DOMStorage: Enabled",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = com.example.ui.theme.KenoGreen.copy(alpha = 0.35f)
                            )
                        }
                    }
                }
                // Status Info Footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "PACKAGE",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "com.probability.sandbox",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "PERMISSIONS",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "INTERNET • NETWORK",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        } else {
            // Browser Web Layer
            AndroidView(
                factory = { context ->
                    val webView = WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.databaseEnabled = true
                        settings.useWideViewPort = true
                        settings.loadWithOverviewMode = true
                        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        settings.userAgentString = settings.userAgentString.replace("; wv", "")
                        
                        // Enable cookies
                        val cookieManager = CookieManager.getInstance()
                        cookieManager.setAcceptCookie(true)
                        cookieManager.setAcceptThirdPartyCookies(this, true)

                        addJavascriptInterface(object {
                            @JavascriptInterface
                            fun onScrollDetected() {
                                activityLogViewModel.logActivity("Page scrolled")
                            }
                            @JavascriptInterface
                            fun onElementSelected(cssSelector: String, text: String) {
                                coroutineScope.launch {
                                    viewModel.setSelectedElement(cssSelector, selectedText)
                                    viewModel.setSelectedElement(selectedCssSelector, text)
                                    if (isSiteRuleSelectionMode) {
                                        viewModel.setShowSiteRuleDialog(true)
                                    } else {
                                        viewModel.setShowMappingDialog(true)
                                    }
                                }
                            }
                        }, "AndroidBridge")
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                viewModel.setPageLoading(true)
                            }
                            override fun onPageFinished(view: WebView, url: String?) {
                                super.onPageFinished(view, url)
                                viewModel.setPageLoading(false)
                                val title = view.title ?: ""
                                url?.let { activityLogViewModel.logActivity("Visited site: $it (Title: $title)") }
                                val jsRules = java.lang.StringBuilder("(function() { ")
                                jsRules.append("var style = document.createElement('style'); style.id = 'site-rules-style'; ")
                                val css = java.lang.StringBuilder()
                                siteRules.filter { it.isEnabled }.forEach { rule ->
                                    css.append("${rule.cssSelector} { display: none !important; } ")
                                }
                                jsRules.append("style.innerHTML = `${css.toString()}`; document.head.appendChild(style); ")
                                jsRules.append("})();")
                                view.evaluateJavascript(jsRules.toString(), null)
                                view.evaluateJavascript(
                                    "(function() { " +
                                    "   console.log('Probability Sandbox Engine Active on: ' + window.location.href); " +
                                    "   var lastScrollTop = 0;" +
                                    "   window.addEventListener('scroll', function() { " +
                                    "       var st = window.pageYOffset || document.documentElement.scrollTop; " +
                                    "       if (Math.abs(st - lastScrollTop) > 300) { " +
                                    "           lastScrollTop = st; " +
                                    "           AndroidBridge.onScrollDetected(); " +
                                    "       } " +
                                    "   }, false); " +
                                    "})();",
                                    null
                                )
                            }
                        }
                        loadUrl(targetUrl)
                    }
                    webViewRef = webView
                    webView
                },
                modifier = Modifier.fillMaxSize()
            )
            // Sticky Bottom Control
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color.Black.copy(alpha = 0.8f),
                            RoundedCornerShape(12.dp)
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            IconButton(
                                onClick = {
                                    viewModel.setShowExitConfirmation(true)
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box {
                                TextButton(
                                    onClick = { viewModel.setPanelDropdownExpanded(true) },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    val titleText = when (activeTool) {
                                        "Keno" -> if (activePanelTab == "Predictor") "TOOL ACTIVE: KENO MATRIX ENGINE ▼" else "SETTINGS ▼"
                                        "Aviator" -> if (activePanelTab == "Predictor") "TOOL ACTIVE: AVIATOR TREND ENGINE ▼" else "SETTINGS ▼"
                                        else -> if (activePanelTab == "Predictor") "PREDICTOR CONTROLS ▼" else "SETTINGS ▼"
                                    }
                                    Text(
                                        text = titleText,
                                        color = com.example.ui.theme.TextHighlight,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                DropdownMenu(
                                    expanded = isPanelDropdownExpanded,
                                    onDismissRequest = { viewModel.setPanelDropdownExpanded(false) },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Predictor", color = Color.White) },
                                        onClick = { 
                                            viewModel.setActivePanelTab("Predictor")
                                            viewModel.setPanelDropdownExpanded(false)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Settings", color = Color.White) },
                                        onClick = { 
                                            viewModel.setActivePanelTab("Settings")
                                            viewModel.setPanelDropdownExpanded(false)
                                        }
                                    )
                                }
                            }
                        }
                        // Green refresh button
                        if (activeTool != "Inactive") {
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        val profile = viewModel.getToolProfile(activeTool)
                                        if (profile != null && profile.cssSelector.isNotEmpty()) {
                                            val jsExtractor = "(function() { " +
                                                "var elements = document.querySelectorAll('${profile.cssSelector}'); " +
                                                "var texts = []; " +
                                                "for (var i = 0; i < elements.length; i++) { texts.push(elements[i].innerText); } " +
                                                "return texts.join(', '); " +
                                            "})();"
                                            webViewRef?.evaluateJavascript(jsExtractor) { result ->
                                                val cleanResult = result?.removeSurrounding("\"") ?: "none"
                                                if (activeTool == "Keno") {
                                                    val newNumbers = (1..80).shuffled().take(5).joinToString("-")
                                                    viewModel.setKenoData("Mapped Data: $cleanResult\nPattern: $newNumbers")
                                                    activityLogViewModel.logActivity("Extracted Keno pattern: $newNumbers based on mapping")
                                                } else if (activeTool == "Aviator") {
                                                    val multiplier = String.format(Locale.US, "%.2fx", kotlin.random.Random.nextDouble(1.0, 10.0))
                                                    viewModel.setAviatorData("Mapped Data: $cleanResult\nTrend: $multiplier")
                                                    activityLogViewModel.logActivity("Extracted Aviator trend: $multiplier based on mapping")
                                                }
                                            }
                                        } else {
                                            if (activeTool == "Keno") {
                                                webViewRef?.evaluateJavascript(
                                                    "(function() { return 'extracted_keno_data'; })();"
                                                ) { result ->
                                                    val newNumbers = (1..80).shuffled().take(5).joinToString("-")
                                                    viewModel.setKenoData("Hot Pairs: 12-45, 7-22\nFreq: 12 (5x), 45 (4x)\nRecent Pattern: $newNumbers\nExtracted: $result")
                                                    activityLogViewModel.logActivity("Extracted Keno pattern: $newNumbers")
                                                }
                                            } else if (activeTool == "Aviator") {
                                                webViewRef?.evaluateJavascript(
                                                    "(function() { return 'extracted_aviator_data'; })();"
                                                ) { result ->
                                                    val multiplier = String.format(Locale.US, "%.2fx", kotlin.random.Random.nextDouble(1.0, 10.0))
                                                    viewModel.setAviatorData("Trend: Upward\nLast Multipliers: $multiplier, 1.25x, 2.10x\nExtracted: $result")
                                                    activityLogViewModel.logActivity("Extracted Aviator trend: $multiplier")
                                                }
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(18.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Sync",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Button(
                            onClick = { viewModel.setControlPanelExpanded(!isControlPanelExpanded) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text(if (isControlPanelExpanded) "▼" else "▲", color = Color.White)
                        }
                    }
                    if (isControlPanelExpanded) {
                        Spacer(modifier = Modifier.height(8.dp))
                        if (activePanelTab == "Predictor") {
                            // Tool Switcher
                            Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Inactive", "Keno", "Aviator").forEach { tool ->
                                Button(
                                    onClick = { viewModel.setActiveTool(tool) },
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (activeTool == tool) primaryColor.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)
                                    ),
                                    contentPadding = PaddingValues(0.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = tool,
                                        fontSize = 12.sp,
                                        color = if (activeTool == tool) MaterialTheme.colorScheme.primary else Color.White
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Map Page Elements", color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)
                            Switch(
                                checked = isSelectionModeActive,
                                onCheckedChange = { viewModel.setSelectionModeActive(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Auto Extraction (Live Sync)", color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)
                            Switch(
                                checked = isLivePollingActive,
                                onCheckedChange = { viewModel.setLivePollingActive(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        if (activeTool == "Inactive") {
                            // Hidden content, maybe just a placeholder
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Select a tool to begin pattern extraction.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        } else {
                            // Tool Content
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                    .padding(16.dp)
                            ) {
                                if (activeTool == "Keno") {
                                    Text(
                                        text = kenoData,
                                        color = com.example.ui.theme.KenoGreen,
                                        fontSize = 14.sp,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 20.sp
                                    )
                                } else if (activeTool == "Aviator") {
                                    Text(
                                        text = aviatorData,
                                        color = com.example.ui.theme.AviatorYellow,
                                        fontSize = 14.sp,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                        } else {
                            // Settings Content
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Inspect & Hide Elements", color = Color.White, fontSize = 14.sp)
                                    Switch(
                                        checked = isSiteRuleSelectionMode,
                                        onCheckedChange = { viewModel.setSiteRuleSelectionMode(it) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                                            checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                        )
                                    )
                                }
                                if (siteRules.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Saved Rules:", color = Color.Gray, fontSize = 12.sp)
                                    LazyColumn(modifier = Modifier.heightIn(max = 150.dp)) {
                                        items(siteRules) { rule ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(if (rule.label.isNotBlank()) rule.label else "Element", color = Color.White, fontSize = 12.sp, maxLines = 1)
                                                    Text(rule.cssSelector, color = Color.Gray, fontSize = 10.sp, maxLines = 1)
                                                }
                                                Switch(
                                                    checked = rule.isEnabled,
                                                    onCheckedChange = { 
                                                        coroutineScope.launch { viewModel.updateSiteRule(rule.copy(isEnabled = it)) } 
                                                    },
                                                    modifier = Modifier.scale(0.8f)
                                                )
                                                IconButton(
                                                    onClick = { coroutineScope.launch { viewModel.deleteSiteRule(rule) } },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (isPageLoading) {
                val loadingTexts = listOf(
                    "Preparing tools for secured simulation...",
                    "Establishing secure connection...",
                    "Bypassing security protocols...",
                    "Initializing prediction engine..."
                )
                var currentTextIndex by remember { mutableStateOf(0) }
                LaunchedEffect(Unit) {
                    while (true) {
                        kotlinx.coroutines.delay(1500)
                        currentTextIndex = (currentTextIndex + 1) % loadingTexts.size
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = loadingTexts[currentTextIndex],
                            color = Color.White,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
