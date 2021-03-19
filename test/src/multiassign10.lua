local up = "upvalue"
local x, y = 0, f() or function() print(up) end
y()
