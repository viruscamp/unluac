local x, y, z = f()

local result = (x == y)
if not result then
  local temp = g()
  if temp then
    result = (temp.field == z)
  end
end
print(result)
