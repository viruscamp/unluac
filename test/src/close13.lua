repeat
  local x = 0
  local f = function() return x end
  print(f())
  if test() then break end
  x = x + 1
until x > 4
