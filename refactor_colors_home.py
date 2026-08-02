import re

with open('app/src/main/java/com/example/ui/screens/KetayPredictorApp.kt', 'r') as f:
    content = f.read()

content = content.replace('Color(0xFF1C1B1F)', 'MaterialTheme.colorScheme.background')
content = content.replace('Color(0xFF2C2C2E)', 'MaterialTheme.colorScheme.surface')
content = content.replace('Color(0xFF4CAF50)', 'MaterialTheme.colorScheme.primary')
content = content.replace('Color(0xFF3B82F6)', 'MaterialTheme.colorScheme.secondary')
content = content.replace('Color(0xFFE6E1E5)', 'MaterialTheme.colorScheme.onBackground')
content = content.replace('Color(0xFF938F99)', 'MaterialTheme.colorScheme.onSurfaceVariant')
content = content.replace('Color(0xFFF44336)', 'MaterialTheme.colorScheme.error')
content = content.replace('Color(0xFFD0BCFF)', 'com.example.ui.theme.TextHighlight')
content = content.replace('Color(0xFF4ADE80)', 'com.example.ui.theme.KenoGreen')
content = content.replace('Color(0xFFFFD54F)', 'com.example.ui.theme.AviatorYellow')

with open('app/src/main/java/com/example/ui/screens/KetayPredictorApp.kt', 'w') as f:
    f.write(content)
