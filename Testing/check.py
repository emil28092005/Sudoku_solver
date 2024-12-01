import subprocess
from time import sleep

process = subprocess.Popen(['java', './Main.java'], stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)

info = []
for i in range(9):
    info.append([""] * 9)

def check(x, y):
    a = 0
    res = []
    if info[x][y] != "":
        a += 1
        res.append(info[x][y])
    for dx in range(-1, 2):
        for dy in range(-1, 2):
            if (0 <= x+dx <= 8 and 0 <= y+dy <= 8 and (dy != 0 or dx != 0) and info[x+dx][y+dy] == 'A'):
                a += 1
                res.append("P")
                return [a, res]
    if (0 <= x-1 <= 8 and info[x-1][y] == 'S' or 0 <= x+1 <= 8 and info[x+1][y] == 'S' or 0 <= y-1 <= 8 and info[x][y-1] == 'S' or 0 <= y+1 <= 8 and info[x][y+1] == 'S'):
        a += 1
        res.append("P")
        return [a, res]

    return [a, res]


def show(neo_x, neo_y):
    with open('close.txt', encoding='utf-8', mode='w') as f1:
        a = list(map)
        a[map.find('𖨆')] = ' '
        a[map.find(str(neo_x) + " |") + 4 + neo_y * 4] = '𖨆'
        f1.write(''.join(a))

map = ''
keymaker_x = 0
keymaker_y = 0
key_x = 99
key_y = 99
with open('field.txt', encoding='utf-8') as initial:
    i = 1
    for line in initial:
        map += line
        if (3 <= i and i <= 19 and i % 2 == 1):
            line = line.split('|')[1:-1]
            for j in range(len(line)):
                if (line[j] == ' ■ '):
                    info[i // 2 - 1][j] = 'A'
                elif (line[j] == ' □ '):
                    info[i // 2 - 1][j] = 'S'
                elif (line[j] == ' ⚷ '):
                    info[i // 2 - 1][j] = 'B'
                    key_x = i // 2 - 1
                    key_y = j
                elif (line[j] == ' K '):
                    info[i // 2 - 1][j] = 'K'
                    keymaker_x = i // 2 - 1
                    keymaker_y = j
        i += 1

# show(0, 0)
mode = 1
process.stdin.write((str(mode) + "\n" +
                        str(keymaker_x) + " " +
                        str(keymaker_y) + "\n"))
process.stdin.flush()
print(str(mode) + "\n" + str(keymaker_x) + " " + str(keymaker_y))

now_x = 0
now_y = 0

while True:
    move = process.stdout.readline().replace('\n', '')
    print(move)
    if 'e' in move:
        break
    
    move = move.split()
    temp_x = int(move[1])
    temp_y = int(move[2])
    # show(temp_x, temp_y)
    if not(temp_x + 1 == now_x and temp_y == now_y or temp_x - 1 == now_x and temp_y == now_y or temp_x == now_x and temp_y + 1 == now_y or temp_x == now_x and temp_y - 1 == now_y or temp_x == now_x and temp_y == now_y):
        print("Error: Teleport")
        break
    now_x = int(move[1])
    now_y = int(move[2])
    # sleep(0.5)

    res = ''
    num = 0

    if mode == 1:
        left = -1
        right = 2
    else:
        left = -2
        right = 3

    for dx in range(left, right):
        for dy in range(left, right): 
            new_x = now_x + dx
            new_y = now_y + dy
            if 0 <= new_x <= 8 and 0 <= new_y <= 8:
                a = check(new_x, new_y)
                num += a[0]
                for elem in a[1]:
                    res += str(new_x) + " " + str(new_y) + " " + elem + "\n"

    # Кодируем результат перед записью
    send = str(num) + '\n' + res
    process.stdin.write(send)
    print(send[:-1])
    process.stdin.flush()