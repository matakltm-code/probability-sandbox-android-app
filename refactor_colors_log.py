import re

with open('app/src/main/java/com/example/ui/screens/ActivityLogScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('Color(0xFF1C1B1F)', 'MaterialTheme.colorScheme.background')
content = content.replace('Color(0xFF2C2C2E)', 'MaterialTheme.colorScheme.surface')
content = content.replace('Color(0xFF4CAF50)', 'MaterialTheme.colorScheme.primary')
content = content.replace('Color(0xFFE6E1E5)', 'MaterialTheme.colorScheme.onBackground')
content = content.replace('Color(0xFF938F99)', 'androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant')

if 'import androidx.compose.material3.MaterialTheme' not in content:
    content = content.replace('import androidx.compose.material3.TopAppBarDefaults', 'import androidx.compose.material3.TopAppBarDefaults\nimport androidx.compose.material3.MaterialTheme')

with open('app/src/main/java/com/example/ui/screens/ActivityLogScreen.kt', 'w') as f:
    f.write(content)
