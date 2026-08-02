import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace('Color(0xFF1C1B1F)', 'MaterialTheme.colorScheme.background')

if 'import androidx.compose.material3.MaterialTheme' not in content:
    content = content.replace('import androidx.compose.material3.Scaffold', 'import androidx.compose.material3.Scaffold\nimport androidx.compose.material3.MaterialTheme')

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
