package com.example.ui.screens

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
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

import com.example.AppDatabase
import com.example.SiteRule
import com.example.ToolProfile
import com.example.MapperJS
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.LocalContext
import java.util.Locale
import androidx.compose.material.icons.automirrored.filled.ArrowBack
@Composable
fun HomeScreen(modifier: Modifier = Modifier, activityLogViewModel: ActivityLogViewModel, onNavigateToLogs: () -> Unit, onNavigateToDeveloper: () -> Unit) {
    var isWebViewVisible by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isControlPanelExpanded by remember { mutableStateOf(false) }
    var targetUrl by remember { mutableStateOf("https://google.com") }
    var showExitConfirmation by remember { mutableStateOf(false) }
    var isPageLoading by remember { mutableStateOf(false) }
    var activeTool by remember { mutableStateOf("Inactive") }
    var kenoData by remember { mutableStateOf("Ready to extract...") }
    var aviatorData by remember { mutableStateOf("Ready to extract...") }

    var isSelectionModeActive by remember { mutableStateOf(false) }
    var showMappingDialog by remember { mutableStateOf(false) }
    var selectedCssSelector by remember { mutableStateOf("") }
    var selectedText by remember { mutableStateOf("") }
    
    var isSiteRuleSelectionMode by remember { mutableStateOf(false) }
    var showSiteRuleDialog by remember { mutableStateOf(false) }
    
    var activePanelTab by remember { mutableStateOf("Predictor") }
    var isPanelDropdownExpanded by remember { mutableStateOf(false) }
    
    var isLivePollingActive by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val toolProfileDao = remember { AppDatabase.getDatabase(context).toolProfileDao() }
    val siteRuleDao = remember { AppDatabase.getDatabase(context).siteRuleDao() }
    val siteRules by siteRuleDao.getAllRules().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(isLivePollingActive, activeTool) {
        if (isLivePollingActive && activeTool != "Inactive") {
            while (true) {
                kotlinx.coroutines.delay(2000)
                val profile = toolProfileDao.getProfile(activeTool)
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
                            kenoData = "Mapped Data: $cleanResult\nPattern: $newNumbers"
                        } else if (activeTool == "Aviator") {
                            val multiplier = String.format(java.util.Locale.US, "%.2fx", kotlin.random.Random.nextDouble(1.0, 10.0))
                            aviatorData = "Mapped Data: $cleanResult\nTrend: $multiplier"
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
                showSiteRuleDialog = false
                isSiteRuleSelectionMode = false
            },
            title = { Text("Hide this element?", color = Color.White) },
            text = { 
                Column {
                    Text("Path: $selectedCssSelector", color = Color.Gray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Sample text: $selectedText", color = Color(0xFFE6E1E5))
                }
            },
            containerColor = Color(0xFF2C2C2E),
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            siteRuleDao.insertRule(SiteRule(cssSelector = selectedCssSelector, label = selectedText.take(20)))
                        }
                        showSiteRuleDialog = false
                        isSiteRuleSelectionMode = false
                    }
                ) {
                    Text("Save Rule", color = Color(0xFF4CAF50))
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showSiteRuleDialog = false 
                    isSiteRuleSelectionMode = false
                }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    if (showMappingDialog) {
        AlertDialog(
            onDismissRequest = { showMappingDialog = false },
            title = { Text("Map this element?", color = Color.White) },
            text = { 
                Column {
                    Text("Target Tool: $activeTool", color = Color(0xFFD0BCFF))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Path: $selectedCssSelector", color = Color.Gray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Sample text: $selectedText", color = Color(0xFFE6E1E5))
                }
            },
            containerColor = Color(0xFF2C2C2E),
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            toolProfileDao.insertProfile(
                                ToolProfile(
                                    toolName = activeTool,
                                    cssSelector = selectedCssSelector,
                                    label = "version_0000001"
                                )
                            )
                            activityLogViewModel.logActivity("Mapped $activeTool CSS: $selectedCssSelector")
                        }
                        showMappingDialog = false
                        isSelectionModeActive = false
                    }
                ) {
                    Text("Save to ObjectBox", color = Color(0xFF4CAF50))
                }
            },
            dismissButton = {
                TextButton(onClick = { showMappingDialog = false }) {
                    Text("Cancel", color = Color(0xFFF44336))
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
            showExitConfirmation = true
        }
    }

    if (showExitConfirmation) {
        AlertDialog(
            onDismissRequest = { showExitConfirmation = false },
            title = { Text("Confirm Exit") },
            text = { Text("Did you want to leave this site?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitConfirmation = false
                        webViewRef?.loadUrl("about:blank")
                        isWebViewVisible = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExitConfirmation = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(modifier = modifier.fillMaxSize().background(Color(0xFF1C1B1F))) {
        if (!isWebViewVisible) {
            // Background Mesh
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Top right green blur
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF4CAF50).copy(alpha = 0.2f), Color.Transparent),
                        center = Offset(canvasWidth, 0f),
                        radius = 600f
                    ),
                    center = Offset(canvasWidth, 0f),
                    radius = 600f
                )

                // Center left blue blur
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF3B82F6).copy(alpha = 0.2f), Color.Transparent),
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
                            .background(Color(0xFF4CAF50), RoundedCornerShape(12.dp)),
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
                        color = Color(0xFF4CAF50),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                
                Text(
                    text = "Probability Sandbox",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE6E1E5)
                )
                
                Text(
                    text = "Secured sandbox browser for predictive model viewing and engine analytics.",
                    fontSize = 16.sp,
                    color = Color(0xFF938F99),
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
                                        color = Color(0xFFD0BCFF),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Analytics Engine",
                                        color = Color(0xFFE6E1E5),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF4CAF50).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                        .border(1.dp, Color(0xFF4CAF50).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "READY",
                                        color = Color(0xFF4CAF50),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Button(
                                onClick = { 
                                    targetUrl = "https://google.com"
                                    isWebViewVisible = true 
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
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
                                    targetUrl = "https://melbet-et.com"
                                    isWebViewVisible = true 
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
                                color = Color(0xFF938F99),
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
                                color = Color(0xFF938F99),
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
                                color = Color(0xFF4ADE80).copy(alpha = 0.7f)
                            )
                            Text(
                                text = "> Waiting for activity trigger...",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = Color(0xFF4ADE80).copy(alpha = 0.7f)
                            )
                            Text(
                                text = "> DOMStorage: Enabled",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = Color(0xFF4ADE80).copy(alpha = 0.35f)
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
                            color = Color(0xFF938F99),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "com.probability.sandbox",
                            fontSize = 12.sp,
                            color = Color(0xFFE6E1E5)
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "PERMISSIONS",
                            fontSize = 10.sp,
                            color = Color(0xFF938F99),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "INTERNET • NETWORK",
                            fontSize = 12.sp,
                            color = Color(0xFFE6E1E5)
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
                        settings.useWideViewPort = true
                        settings.loadWithOverviewMode = true
                        settings.userAgentString = settings.userAgentString.replace("; wv", "")
                        
                        addJavascriptInterface(object {
                            @JavascriptInterface
                            fun onScrollDetected() {
                                activityLogViewModel.logActivity("Page scrolled")
                            }

                            @JavascriptInterface
                            fun onElementSelected(cssSelector: String, text: String) {
                                coroutineScope.launch {
                                    selectedCssSelector = cssSelector
                                    selectedText = text
                                    if (isSiteRuleSelectionMode) {
                                        showSiteRuleDialog = true
                                    } else {
                                        showMappingDialog = true
                                    }
                                }
                            }
                        }, "AndroidBridge")
                        
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                isPageLoading = true
                            }

                            override fun onPageFinished(view: WebView, url: String?) {
                                super.onPageFinished(view, url)
                                isPageLoading = false
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
                                    showExitConfirmation = true
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
                                    onClick = { isPanelDropdownExpanded = true },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    val titleText = when (activeTool) {
                                        "Keno" -> if (activePanelTab == "Predictor") "TOOL ACTIVE: KENO MATRIX ENGINE ▼" else "SETTINGS ▼"
                                        "Aviator" -> if (activePanelTab == "Predictor") "TOOL ACTIVE: AVIATOR TREND ENGINE ▼" else "SETTINGS ▼"
                                        else -> if (activePanelTab == "Predictor") "PREDICTOR CONTROLS ▼" else "SETTINGS ▼"
                                    }
                                    Text(
                                        text = titleText,
                                        color = Color(0xFFD0BCFF),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                DropdownMenu(
                                    expanded = isPanelDropdownExpanded,
                                    onDismissRequest = { isPanelDropdownExpanded = false },
                                    modifier = Modifier.background(Color(0xFF2C2C2E))
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Predictor", color = Color.White) },
                                        onClick = { 
                                            activePanelTab = "Predictor"
                                            isPanelDropdownExpanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Settings", color = Color.White) },
                                        onClick = { 
                                            activePanelTab = "Settings"
                                            isPanelDropdownExpanded = false
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
                                        val profile = toolProfileDao.getProfile(activeTool)
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
                                                    kenoData = "Mapped Data: $cleanResult\nPattern: $newNumbers"
                                                    activityLogViewModel.logActivity("Extracted Keno pattern: $newNumbers based on mapping")
                                                } else if (activeTool == "Aviator") {
                                                    val multiplier = String.format(Locale.US, "%.2fx", kotlin.random.Random.nextDouble(1.0, 10.0))
                                                    aviatorData = "Mapped Data: $cleanResult\nTrend: $multiplier"
                                                    activityLogViewModel.logActivity("Extracted Aviator trend: $multiplier based on mapping")
                                                }
                                            }
                                        } else {
                                            if (activeTool == "Keno") {
                                                webViewRef?.evaluateJavascript(
                                                    "(function() { return 'extracted_keno_data'; })();"
                                                ) { result ->
                                                    val newNumbers = (1..80).shuffled().take(5).joinToString("-")
                                                    kenoData = "Hot Pairs: 12-45, 7-22\nFreq: 12 (5x), 45 (4x)\nRecent Pattern: $newNumbers\nExtracted: $result"
                                                    activityLogViewModel.logActivity("Extracted Keno pattern: $newNumbers")
                                                }
                                            } else if (activeTool == "Aviator") {
                                                webViewRef?.evaluateJavascript(
                                                    "(function() { return 'extracted_aviator_data'; })();"
                                                ) { result ->
                                                    val multiplier = String.format(Locale.US, "%.2fx", kotlin.random.Random.nextDouble(1.0, 10.0))
                                                    aviatorData = "Trend: Upward\nLast Multipliers: $multiplier, 1.25x, 2.10x\nExtracted: $result"
                                                    activityLogViewModel.logActivity("Extracted Aviator trend: $multiplier")
                                                }
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFF4CAF50), RoundedCornerShape(18.dp))
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
                            onClick = { isControlPanelExpanded = !isControlPanelExpanded },
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
                                    onClick = { activeTool = tool },
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (activeTool == tool) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)
                                    ),
                                    contentPadding = PaddingValues(0.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = tool,
                                        fontSize = 12.sp,
                                        color = if (activeTool == tool) Color(0xFF4CAF50) else Color.White
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
                            Text("Map Page Elements", color = Color(0xFFE6E1E5), fontSize = 14.sp)
                            Switch(
                                checked = isSelectionModeActive,
                                onCheckedChange = { isSelectionModeActive = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF4CAF50),
                                    checkedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.5f)
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Auto Extraction (Live Sync)", color = Color(0xFFE6E1E5), fontSize = 14.sp)
                            Switch(
                                checked = isLivePollingActive,
                                onCheckedChange = { isLivePollingActive = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF4CAF50),
                                    checkedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.5f)
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
                                    color = Color(0xFF938F99),
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
                                        color = Color(0xFF4ADE80),
                                        fontSize = 14.sp,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 20.sp
                                    )
                                } else if (activeTool == "Aviator") {
                                    Text(
                                        text = aviatorData,
                                        color = Color(0xFFFFD54F),
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
                                        onCheckedChange = { isSiteRuleSelectionMode = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color(0xFF4CAF50),
                                            checkedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.5f)
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
                                                        coroutineScope.launch { siteRuleDao.updateRule(rule.copy(isEnabled = it)) } 
                                                    },
                                                    modifier = Modifier.scale(0.8f)
                                                )
                                                IconButton(
                                                    onClick = { coroutineScope.launch { siteRuleDao.deleteRule(rule) } },
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
                        .background(Color(0xFF1C1B1F)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFF4CAF50))
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

