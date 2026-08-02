with open('app/src/main/java/com/example/ui/screens/KetayPredictorApp.kt', 'r') as f:
    content = f.read()

content = content.replace('Target Data: $displayHistory\n\nPredicted Tickets:\n', 'Target Data: $displayHistory\\n\\nPredicted Tickets:\\n')
content = content.replace('Target Data: $displayHistory\n\nNext Expected Multiplier:\n', 'Target Data: $displayHistory\\n\\nNext Expected Multiplier:\\n')
content = content.replace('T$i: $ticketNums\n', 'T$i: $ticketNums\\n')
content = content.replace('No mapped element! Simulating data...\n\nPredicted Tickets:\n', 'No mapped element! Simulating data...\\n\\nPredicted Tickets:\\n')
content = content.replace('No mapped element! Simulating data...\n\nNext Expected Multiplier:\n', 'No mapped element! Simulating data...\\n\\nNext Expected Multiplier:\\n')

with open('app/src/main/java/com/example/ui/screens/KetayPredictorApp.kt', 'w') as f:
    f.write(content)
