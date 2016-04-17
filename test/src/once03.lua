local x = 0
repeat
  do break end
  -- unreachable code
  x = 1
until true
print("guard")
repeat
  do break end
  -- unreachable code
  if cond() then
    x = 2
  end
until true
print("guard")
