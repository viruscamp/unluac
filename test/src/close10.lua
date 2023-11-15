for i = 1, 100 do
  local x = f(i)
  table.insert(t, function() print(x) end)
end
