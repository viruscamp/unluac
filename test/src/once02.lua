local L1 = 0
if a then
  print("A")
end
print("guard")
repeat
  do break end
  print("x")
  L1 = 6
until true
print(L1)
