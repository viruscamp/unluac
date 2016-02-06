local upvalue = 0
local a = x == 0 or function()
  print(upvalue)
end
