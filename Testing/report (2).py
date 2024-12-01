import subprocess
from random import randint
import timeit
import statistics

# astar = ['python', './astar.py']
# backtracking = ['python', './bt.exe']
astar = ['./code.exe']
backtracking = ['./bt.exe']

process1 = subprocess.Popen(astar, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
process2 = subprocess.Popen(astar, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
process3 = subprocess.Popen(backtracking, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)

wins_Astar = 0
losses_Astar = 0
wins_bt = 0
losses_bt = 0

def check(x, y):
    a = 0
    res = []
    if info[x][y] != "":
        a += 1
        res.append(info[x][y])
    for dx in range(-1, 2):
        for dy in range(-1, 2):
            if (0 <= x+dx <= 8 and 0 <= y+dy <= 8 and info[x+dx][y+dy] == 'A'):
                a += 1
                res.append("P")
                return [a, res]
    if (0 <= x-1 <= 8 and info[x-1][y] == 'S' or 0 <= x+1 <= 8 and info[x+1][y] == 'S' or 0 <= y-1 <= 8 and info[x][y-1] == 'S' or 0 <= y+1 <= 8 and info[x][y+1] == 'S'):
        a += 1
        res.append("P")
        return [a, res]

    return [a, res]

def Astar_mode_1():
    global info, process1, wins_Astar, losses_Astar, keymaker_x, keymaker_y, key_x, key_y
    mode = 1
    process1.stdin.write(str(mode) + "\n" + str(keymaker_x) + " " + str(keymaker_y) + "\n")
    process1.stdin.flush()
    now_x = 0
    now_y = 0

    while True:
        move = process1.stdout.readline().strip()
        if 'e' in move:
            if (move[-2:] == '-1'):
                losses_Astar += 1
            else:
                wins_Astar += 1
            break
        move = move.split()
        now_x = int(move[1])
        now_y = int(move[2])

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

        process1.stdin.write(str(num) + '\n' + res)
        process1.stdin.flush()

def Astar_mode_2():
    global info, process2, keymaker_x, keymaker_y, key_x, key_y
    mode = 2
    process2.stdin.write(str(mode) + "\n" + str(keymaker_x) + " " + str(keymaker_y) + "\n")
    process2.stdin.flush()
    now_x = 0
    now_y = 0

    while True:
        move = process2.stdout.readline().strip()
        if 'e' in move:
            break
        move = move.split()
        now_x = int(move[1])
        now_y = int(move[2])

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

        process2.stdin.write(str(num) + '\n' + res)
        process2.stdin.flush()

def back():
    global info, process3, wins_bt, losses_bt, keymaker_x, keymaker_y, key_x, key_y
    mode = randint(1, 2)
    process3.stdin.write(str(mode) + "\n" + str(keymaker_x) + " " + str(keymaker_y) + "\n")
    process3.stdin.flush()
    now_x = 0
    now_y = 0

    while True:
        move = process3.stdout.readline().strip()
        if 'e' in move:
            if (move[-2:] == '-1'):
                losses_bt += 1
            else:
                wins_bt += 1
            break
        move = move.split()
        now_x = int(move[1])
        now_y = int(move[2])

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

        process3.stdin.write(str(num) + '\n' + res)
        process3.stdin.flush()

def mapgen():
    global info, keymaker_x, keymaker_y, key_x, key_y, process1, process2, process3, astar, backtracking

    info = []
    for i in range(9):
        info.append([""] * 9)
    info[0][0] = 'N'
    keymaker_x = randint(0, 8)
    keymaker_y = randint(0, 8)
    while (info[keymaker_x][keymaker_y] != ''):
        keymaker_x = randint(0, 8)
        keymaker_y = randint(0, 8)
    info[keymaker_x][keymaker_y] = 'K'

    key_x = randint(0, 8)
    key_y = randint(0, 8)
    while (info[key_x][key_y] != ""):
        key_x = randint(0, 8)
        key_y = randint(0, 8)
    info[key_x][key_y] = 'B'

    for smith in range(randint(0, 3)):
        x = randint(0, 8)
        y = randint(0, 8)
        while (x == 0 and y == 0 or x == 0 and y == 1 or x == 1 and y == 0 or x == 1 and y == 1 or info[x][y] != '' or (x-1 >= 0 and y-1 >=0 and (info[x-1][y-1] == 'K' or info[x-1][y-1] == "B")) or (x-1 >= 0 and (info[x-1][y] == 'K' or info[x-1][y] == 'B')) or (x-1 >= 0 and y+1 <= 8 and (info[x-1][y+1] == 'K' or info[x-1][y+1] == 'B')) or (y-1 >= 0 and (info[x][y-1] == 'K' or info[x][y-1] == 'B')) or (y+1 <= 8 and (info[x][y+1] == 'K' or info[x][y+1] == 'B')) or (x+1 <= 8 and y-1 >= 0 and (info[x+1][y-1] == 'K' or info[x+1][y-1] == 'B')) or (x+1 <= 8 and (info[x+1][y] == 'K' or info[x+1][y] == 'B')) or (x+1 <= 8 and y+1 <= 8 and (info[x+1][y+1] == 'K' or info[x+1][y+1] == 'B'))):
            x = randint(0, 8)
            y = randint(0, 8)
        info[x][y] = 'A'

    for sentiel in range(randint(0, 1)):
        x = randint(0, 8)
        y = randint(0, 8)
        while (x == 0 and y == 0 or x == 0 and y == 1 or x == 1 and y == 0 or info[x][y] != '' or (x-1 >= 0 and (info[x-1][y] == 'K' or info[x-1][y] == 'B')) or (x+1 <= 8 and (info[x+1][y] == 'K' or info[x+1][y] == 'B')) or (y-1 >= 0 and (info[x][y-1] == 'K' or info[x][y-1] == 'B')) or (y+1 <= 8 and (info[x][y+1] == 'K' or info[x][y+1] == 'B'))):
            x = randint(0, 8)
            y = randint(0, 8)
        info[x][y] = 'S'

    info[0][0] = ''
    process1 = subprocess.Popen(astar, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    process2 = subprocess.Popen(astar, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    process3 = subprocess.Popen(backtracking, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)

execution_time_1 = []
execution_time_2 = []
execution_time_3 = []
for i in range(1, 1001):
    mapgen()
    execution_time_1.append(timeit.timeit(Astar_mode_1, number=1) * 1000000)
    execution_time_2.append(timeit.timeit(Astar_mode_2, number=1) * 1000000)
    execution_time_3.append(timeit.timeit(back, number=1) * 1000000)
    if (i % 100 == 0):
        print('Запущено карт', i)

print("Execution time (A* mode 1)")
print("Mean:", statistics.mean(execution_time_1))
print("Mode:", statistics.mode(execution_time_1))
print("Median:", statistics.median(execution_time_1))
print("Standart deviation:", statistics.stdev(execution_time_1))
print()
print("Execution time (A* mode 2)")
print("Mean:", statistics.mean(execution_time_2))
print("Mode:", statistics.mode(execution_time_2))
print("Median:", statistics.median(execution_time_2))
print("Standart deviation:", statistics.stdev(execution_time_2))
print()
print("Execution time (Backtrack)")
print("Mean:", statistics.mean(execution_time_3))
print("Mode:", statistics.mode(execution_time_3))
print("Median:", statistics.median(execution_time_3))
print("Standart deviation:", statistics.stdev(execution_time_3))
print()
print("Wins A*:", wins_Astar)
print("Losses A*:", losses_Astar)
print("Wins bt:", wins_bt)
print("Losses bt:", losses_bt)
