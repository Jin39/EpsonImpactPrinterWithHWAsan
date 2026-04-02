branch: `feature/epson/test-crash-2.43`

location: Payment Location (ID: 78130)

printer: Impact_U220

STR:

1. login with pin code
2. go settings -> printer -> select printer
3. enter room screen
4. run script

automation script:

```python
#python3
#! encoding=utf-8

# autoSend.py CA, checkout USD ticket

import os
import time

def do(point):
    os.system(cmd%(point))

    time.sleep(0.5)

cmd = 'adb -s 10.50.0.120:5555 shell input tap %s'

dictButton = {
    'addTicket': '150 182',
    }

addTicket = '150 182'
item1 = '675 217'
item2 = '875 217'
item3 = '1075 217'
item4 = '1275 217'
item5 = '1075 317'
item6 = '675 417'
checkout = '287 1025'
custom = '1620 670'
custom_tender = '971 390'
go = '1717 945'
close = '285 1035'

print('begin')
i = 0


printer_icon = '1748 45'
remove_all = '1375 793'
remove = '965 605'


while(True):
    i += 1
    do(addTicket)
    do(item1)
    do(item2)
    do(item2)
    do(item2)
    do(item2)
    do(item2)
    do(checkout)
    do(custom)
    do(custom_tender)
    do(go)
    do(close)
    print(i, 'done')
##    if i%5 == 0:
##        do(printer_icon)
##        do(remove_all)
##        do(remove)
##        print('remove print task done')
    time.sleep(3)


```
