print("guard")
local a = 0
repeat -- once
  local b = 1
  print("1")
  if a then
    print("2")
    break
  else
    print("3")
  end
  print("4")
until true
print("5")
