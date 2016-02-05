print("guard")
local a
do
  local b
  do
    local c
    print("guard")
    c = f()
    b = c
  end
  a = b
end
return a
