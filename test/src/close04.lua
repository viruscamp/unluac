repeat
  local x = f()
  table.insert(t, function() return x end)
until not x
