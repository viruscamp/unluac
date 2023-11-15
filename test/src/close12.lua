for k, v in iter() do
  local x = f(k, v)
  table.insert(t, function() print(x) end)
  break
end
