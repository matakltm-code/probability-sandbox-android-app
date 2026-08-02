with open('app/src/main/java/com/example/ui/screens/KetayPredictorApp.kt', 'r') as f:
    content = f.read()

content = content.replace('''            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height''', '''            val primaryColor = MaterialTheme.colorScheme.primary
            val secondaryColor = MaterialTheme.colorScheme.secondary
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height''')

content = content.replace('MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)', 'primaryColor.copy(alpha = 0.2f)')
content = content.replace('MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)', 'secondaryColor.copy(alpha = 0.2f)')

with open('app/src/main/java/com/example/ui/screens/KetayPredictorApp.kt', 'w') as f:
    f.write(content)
