import re

with open('app/src/main/java/com/example/ui/screens/DeveloperScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('Color(0xFF1C1B1F)', 'MaterialTheme.colorScheme.background')
content = content.replace('Color(0xFF938F99)', 'MaterialTheme.colorScheme.onSurfaceVariant')

if 'import androidx.compose.material3.MaterialTheme' not in content:
    content = content.replace('import androidx.compose.material3.Text', 'import androidx.compose.material3.Text\nimport androidx.compose.material3.MaterialTheme')

with open('app/src/main/java/com/example/ui/screens/DeveloperScreen.kt', 'w') as f:
    f.write(content)
