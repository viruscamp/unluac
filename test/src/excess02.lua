local x, y

x = 1, ...
print(x)

x = 1, ..., 3
print(x)

x = 1, 2, 3, 4, 5, 6, ... -- try to ensure last stack slot is used only by excess '...'
print(x)

x, y = 1, 2, ...
print(x, y)

local a = 1, ...
print(a)

local b = 1, ..., 3
print(b)

local c = 1, 2, 3, ...
print(c)

local d = ..., 2
print(d)

local d, e = 1, 2, ...
print(d, e)
