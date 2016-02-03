local t = {}

print("test 1")

while true do
  t[1] = 0
  if f() then
    break
  end
  print("loop")
end

print("test 2")

-- strange test
-- for Lua 5.1, while f() is not usually to while true if not f() break
-- This is because it doesn't have the 5.2-style if-break optimization
-- To get 5.2-style if-break jump, we need to second target of the
-- if to be redirected -- by putting a break after the if statement.
while true do
  t[1] = 0
  if f() then
  end
  do break end
  print("loop")
end


print("test 3")

while f() and {1, 2, 3} and g() do
  print("loop")
end

print("test 4")

while f() and {a = 1, b = 2, c = 3} and g() do
  print("loop")
end
