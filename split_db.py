import re

with open('app/src/main/java/com/example/ActivityLogDB.kt', 'r') as f:
    content = f.read()

# Since we will move everything to their respective folders, I'll just write new files.
