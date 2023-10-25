local x, y

x = 1, f()
print(x)

x = 1, f(), 3
print(x)

x = 1, 2, 3, 4, 5, 6, f()
print(x)

x, y = 1, 2, f()
print(x, y)

local a = 1, f()
print(a)

local b = 1, f(), 3
print(b)

local c = 1, 2, 3, f()
print(c)

local d = f(), 2
print(d)

local d, e = 1, 2, f()
print(d, e)
