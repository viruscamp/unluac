print("guard")
local a
do
  local b
  print("guard")
  b = f()
  a = b
end
return a
