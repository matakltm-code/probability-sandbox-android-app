package com.example

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.MyApplicationTheme

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.MoreVert
import android.webkit.JavascriptInterface

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color(0xFF1C1B1F)
                ) { innerPadding ->
                    val activityLogViewModel: ActivityLogViewModel = viewModel()
                    KetayPredictorApp(
                        modifier = Modifier.padding(innerPadding),
                        activityLogViewModel = activityLogViewModel
                    )
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun KetayPredictorApp(modifier: Modifier = Modifier, activityLogViewModel: ActivityLogViewModel) {
    var isWebViewVisible by remember { mutableStateOf(false) }
    var isActivityScreenVisible by remember { mutableStateOf(false) }
    var isDeveloperScreenVisible by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var swipeRefreshLayoutRef by remember { mutableStateOf<SwipeRefreshLayout?>(null) }
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
    
    var activePanelTab by remember { mutableStateOf("Predictor") }
    var isPanelDropdownExpanded by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val toolProfileDao = remember { AppDatabase.getDatabase(context).toolProfileDao() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(isSelectionModeActive) {
        if (isSelectionModeActive) {
            webViewRef?.evaluateJavascript(MapperJS.enableScript, null)
        } else {
            webViewRef?.evaluateJavascript(MapperJS.disableScript, null)
        }
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

    BackHandler(enabled = isWebViewVisible || isActivityScreenVisible || isDeveloperScreenVisible) {
        if (isDeveloperScreenVisible) {
            isDeveloperScreenVisible = false
        } else if (isActivityScreenVisible) {
            isActivityScreenVisible = false
        } else if (webViewRef?.canGoBack() == true) {
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
        if (isDeveloperScreenVisible) {
            DeveloperScreen(
                onBack = { isDeveloperScreenVisible = false }
            )
        } else if (isActivityScreenVisible) {
            ActivityLogScreen(
                viewModel = activityLogViewModel,
                onBack = { isActivityScreenVisible = false }
            )
        } else if (!isWebViewVisible) {
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
                            onClick = { isActivityScreenVisible = true },
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
                            onClick = { isDeveloperScreenVisible = true },
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
                    val swipeLayout = SwipeRefreshLayout(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
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
                                    showMappingDialog = true
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
                                swipeLayout.isRefreshing = false
                                val title = view.title ?: ""
                                url?.let { activityLogViewModel.logActivity("Visited site: $it (Title: $title)") }
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
                    swipeLayout.addView(webView)
                    swipeLayout.setOnRefreshListener {
                        webView.reload()
                    }
                    
                    webViewRef = webView
                    swipeRefreshLayoutRef = swipeLayout
                    
                    swipeLayout
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
                            // Settings Content (Empty State)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No settings available.",
                                    color = Color(0xFF938F99),
                                    fontSize = 14.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityLogScreen(viewModel: ActivityLogViewModel, onBack: () -> Unit) {
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredLogs = remember(logs, searchQuery) {
        if (searchQuery.isBlank()) {
            logs
        } else {
            logs.filter { it.description.contains(searchQuery, ignoreCase = true) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1B1F))
    ) {
        TopAppBar(
            title = { Text("Activity Logs", color = Color(0xFFE6E1E5)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            actions = {
                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Delete options", tint = Color.White)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color(0xFF2C2C2E))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Clear Last 24 Hours", color = Color.White) },
                            onClick = { viewModel.deleteLogs("24_hours"); expanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Clear Last Week", color = Color.White) },
                            onClick = { viewModel.deleteLogs("week"); expanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Clear Last Month", color = Color.White) },
                            onClick = { viewModel.deleteLogs("month"); expanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Clear Last Year", color = Color.White) },
                            onClick = { viewModel.deleteLogs("year"); expanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Clear All Time", color = Color.White) },
                            onClick = { viewModel.deleteLogs("all_time"); expanded = false }
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Filter by domain or activity...", color = Color(0xFF938F99)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF4CAF50),
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                cursorColor = Color(0xFF4CAF50),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredLogs) { log ->
                val dateStr = remember(log.timestamp) {
                    SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = log.description,
                            color = Color(0xFFE6E1E5),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dateStr,
                            color = Color(0xFF938F99),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeveloperScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1B1F))
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Developed by Micheal",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Purpose of the app is to teach users how probability calculations, the Law of Large Numbers, or neural networks analyze patterns in independent random events.",
            fontSize = 16.sp,
            color = Color(0xFF938F99),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = "Back to Home",
                fontSize = 16.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
