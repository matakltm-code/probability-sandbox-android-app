with open('app/src/main/java/com/example/ui/screens/KetayPredictorApp.kt', 'r') as f:
    content = f.read()

# Remove the Dao definitions
content = content.replace('val toolProfileDao = remember { AppDatabase.getDatabase(context).toolProfileDao() }', '')
content = content.replace('val siteRuleDao = remember { AppDatabase.getDatabase(context).siteRuleDao() }', '')
content = content.replace('val siteRules by siteRuleDao.getAllRules().collectAsState(initial = emptyList())', 'val siteRules by viewModel.siteRules.collectAsStateWithLifecycle()')
content = content.replace('val context = androidx.compose.ui.platform.LocalContext.current', '')

# Replace toolProfileDao calls
content = content.replace('toolProfileDao.getProfile', 'viewModel.getToolProfile')
content = content.replace('toolProfileDao.insertProfile', 'viewModel.insertToolProfile')

# Replace siteRuleDao calls
content = content.replace('siteRuleDao.insertRule', 'viewModel.insertSiteRule')
content = content.replace('siteRuleDao.updateRule', 'viewModel.updateSiteRule')
content = content.replace('siteRuleDao.deleteRule', 'viewModel.deleteSiteRule')

with open('app/src/main/java/com/example/ui/screens/KetayPredictorApp.kt', 'w') as f:
    f.write(content)
