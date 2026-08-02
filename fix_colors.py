with open('app/src/main/java/com/example/ui/screens/KetayPredictorApp.kt', 'r') as f:
    content = f.read()

# Define colors at the top of HomeScreen
content = content.replace('fun HomeScreen(modifier: Modifier = Modifier, activityLogViewModel: ActivityLogViewModel, onNavigateToLogs: () -> Unit, onNavigateToDeveloper: () -> Unit, viewModel: HomeScreenViewModel = viewModel()) {\n',
                          'fun HomeScreen(modifier: Modifier = Modifier, activityLogViewModel: ActivityLogViewModel, onNavigateToLogs: () -> Unit, onNavigateToDeveloper: () -> Unit, viewModel: HomeScreenViewModel = viewModel()) {\n    val primaryColor = MaterialTheme.colorScheme.primary\n    val secondaryColor = MaterialTheme.colorScheme.secondary\n')

# Remove the inner definitions
content = content.replace('''            val primaryColor = MaterialTheme.colorScheme.primary
            val secondaryColor = MaterialTheme.colorScheme.secondary
            Canvas(modifier = Modifier.fillMaxSize()) {''', '            Canvas(modifier = Modifier.fillMaxSize()) {')

with open('app/src/main/java/com/example/ui/screens/KetayPredictorApp.kt', 'w') as f:
    f.write(content)
