local t = {f()}
local s = {1, 2, 3, g()}

for i = 1, 10 do
  print(t[i], s[i])
end
