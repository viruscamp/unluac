local x, y

x, y = 1, 2, 3
print(x, y)

x, y = 1, 2, 3, 4
print(x, y)

x = 1, 2, 3, 4
print(x)

x = 1, 2
print(x)

local a = 1, 2
print(a)

local b, c = 1, 2, 3, 4
print(b, c)

local u = "upvalue"

x = function() print(u) end, 2
x()

local d = function() print(u) end, 2
d()

x = 1, function() print(u) end
print(x)

local e = 1, function() print(u) end
print(e)
