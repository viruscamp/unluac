local upvalue1 = 0
function outer()
  local upvalue2 = 1
  local upvalue3 = 2
  local x = f()
  local a = x == 0 or function()
    print(upvalue1, upvalue2, upvalue3)
  end
  return a
end