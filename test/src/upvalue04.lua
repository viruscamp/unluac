if x then
  local upvalue = 0
  f = function()
    print(upvalue)
    upvalue = upvalue + 1
  end
end
