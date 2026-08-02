import re
with open('app/src/main/java/com/example/ui/screens/KetayPredictorApp.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
skip = False
for i, line in enumerate(lines):
    if '"  var items = allText.split(/\\s+/).filter(Boolean); " +' in line:
        line = line.replace('/\\s+/', '/\\\\s+/')
    if 'predictedTickets.append("Target Data: $displayHistory' in line:
        line = '                            predictedTickets.append("Target Data: $displayHistory\\n\\nPredicted Tickets:\\n")\n'
        skip = True
    elif skip and 'Predicted Tickets:' in line:
        continue
    elif skip and '")' in line:
        skip = False
        continue
        
    if 'predictedTickets.append("T$i: $ticketNums' in line:
        line = '                                predictedTickets.append("T$i: $ticketNums\\n")\n'
        skip = True
    elif skip and '")' in line and 'T$i' not in line and 'Target Data' not in line and 'No mapped element!' not in line:
        skip = False
        continue
        
    if 'viewModel.setAviatorData("Target Data: $displayHistory' in line:
        line = '                            viewModel.setAviatorData("Target Data: $displayHistory\\n\\nNext Expected Multiplier:\\n$multiplier")\n'
        skip = True
    elif skip and 'Next Expected Multiplier:' in line:
        continue
    elif skip and '$multiplier")' in line:
        skip = False
        continue

    if 'predictedTickets.append("No mapped element! Simulating data...' in line:
        line = '                                                predictedTickets.append("No mapped element! Simulating data...\\n\\nPredicted Tickets:\\n")\n'
        skip = True
    elif skip and 'Predicted Tickets:' in line:
        continue
    elif skip and '")' in line and 'T$i' not in line and 'Target Data' not in line:
        skip = False
        continue

    if 'viewModel.setAviatorData("No mapped element! Simulating data...' in line:
        line = '                                                viewModel.setAviatorData("No mapped element! Simulating data...\\n\\nNext Expected Multiplier:\\n$multiplier")\n'
        skip = True
    elif skip and 'Next Expected Multiplier:' in line:
        continue
    elif skip and '$multiplier")' in line:
        skip = False
        continue

    new_lines.append(line)

with open('app/src/main/java/com/example/ui/screens/KetayPredictorApp.kt', 'w') as f:
    f.writelines(new_lines)

