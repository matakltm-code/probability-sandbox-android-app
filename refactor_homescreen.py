import re

with open('app/src/main/java/com/example/ui/screens/KetayPredictorApp.kt', 'r') as f:
    content = f.read()

# Add ViewModel parameter and lifecycle imports
content = content.replace('fun HomeScreen(modifier: Modifier = Modifier, activityLogViewModel: ActivityLogViewModel, onNavigateToLogs: () -> Unit, onNavigateToDeveloper: () -> Unit) {',
                          'import androidx.lifecycle.viewmodel.compose.viewModel\nimport androidx.lifecycle.compose.collectAsStateWithLifecycle\n\n@Composable\nfun HomeScreen(modifier: Modifier = Modifier, activityLogViewModel: ActivityLogViewModel, onNavigateToLogs: () -> Unit, onNavigateToDeveloper: () -> Unit, viewModel: HomeScreenViewModel = viewModel()) {')

# Remove @Composable annotation before HomeScreen because we added it in the replace string
content = content.replace('@Composable\nimport androidx.lifecycle.viewmodel.compose.viewModel', 'import androidx.lifecycle.viewmodel.compose.viewModel')
content = content.replace('import androidx.lifecycle.compose.collectAsStateWithLifecycle\n\n@Composable\nfun HomeScreen', '@Composable\nfun HomeScreen')
content = content.replace('@Composable\n@Composable\nfun HomeScreen', '@Composable\nfun HomeScreen')

# Define state collections inside HomeScreen
state_collections = """
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
"""

# Replace all var ... by remember { mutableStateOf(...) } blocks
var_pattern = re.compile(r'\s*var\s+\w+\s+by\s+remember\s*\{\s*mutableStateOf[^}]+\}\s*\n')

# Find first occurrence to insert our state_collections
first_var_match = var_pattern.search(content)

if first_var_match:
    start_idx = first_var_match.start()
    end_idx = start_idx
    # Find the end of the consecutive var declarations
    while True:
        match = var_pattern.match(content, end_idx)
        if match:
            end_idx = match.end()
        else:
            break
    
    # Also skip any empty lines inside the var block
    content = content[:start_idx] + state_collections + re.sub(r'^\s*$\n', '', content[end_idx:], flags=re.MULTILINE)


replacements = {
    'isWebViewVisible = true': 'viewModel.setWebViewVisible(true)',
    'isWebViewVisible = false': 'viewModel.setWebViewVisible(false)',
    'isControlPanelExpanded = true': 'viewModel.setControlPanelExpanded(true)',
    'isControlPanelExpanded = false': 'viewModel.setControlPanelExpanded(false)',
    'isControlPanelExpanded = !isControlPanelExpanded': 'viewModel.setControlPanelExpanded(!isControlPanelExpanded)',
    'targetUrl = "https://google.com"': 'viewModel.setTargetUrl("https://google.com")',
    'targetUrl = "https://melbet-et.com"': 'viewModel.setTargetUrl("https://melbet-et.com")',
    'showExitConfirmation = true': 'viewModel.setShowExitConfirmation(true)',
    'showExitConfirmation = false': 'viewModel.setShowExitConfirmation(false)',
    'isPageLoading = true': 'viewModel.setPageLoading(true)',
    'isPageLoading = false': 'viewModel.setPageLoading(false)',
    'activeTool = tool': 'viewModel.setActiveTool(tool)',
    'kenoData = ': 'viewModel.setKenoData(',
    'aviatorData = ': 'viewModel.setAviatorData(',
    'isSelectionModeActive = it': 'viewModel.setSelectionModeActive(it)',
    'isSelectionModeActive = false': 'viewModel.setSelectionModeActive(false)',
    'showMappingDialog = true': 'viewModel.setShowMappingDialog(true)',
    'showMappingDialog = false': 'viewModel.setShowMappingDialog(false)',
    'isSiteRuleSelectionMode = it': 'viewModel.setSiteRuleSelectionMode(it)',
    'isSiteRuleSelectionMode = false': 'viewModel.setSiteRuleSelectionMode(false)',
    'showSiteRuleDialog = true': 'viewModel.setShowSiteRuleDialog(true)',
    'showSiteRuleDialog = false': 'viewModel.setShowSiteRuleDialog(false)',
    'activePanelTab = "Predictor"': 'viewModel.setActivePanelTab("Predictor")',
    'activePanelTab = "Settings"': 'viewModel.setActivePanelTab("Settings")',
    'isPanelDropdownExpanded = true': 'viewModel.setPanelDropdownExpanded(true)',
    'isPanelDropdownExpanded = false': 'viewModel.setPanelDropdownExpanded(false)',
    'isLivePollingActive = it': 'viewModel.setLivePollingActive(it)',
    'selectedCssSelector = cssSelector': 'viewModel.setSelectedElement(cssSelector, selectedText)',
    'selectedText = text': 'viewModel.setSelectedElement(selectedCssSelector, text)',
}

for old, new in replacements.items():
    content = content.replace(old, new)
    
# Handle kenoData and aviatorData assignments which are multiline strings or use string interpolation
content = re.sub(r'viewModel\.setKenoData\((.*?)\n', r'viewModel.setKenoData(\1)\n', content)
content = re.sub(r'viewModel\.setAviatorData\((.*?)\n', r'viewModel.setAviatorData(\1)\n', content)


with open('app/src/main/java/com/example/ui/screens/KetayPredictorApp.kt', 'w') as f:
    f.write(content)

