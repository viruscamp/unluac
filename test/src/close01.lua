for _, v in pairs(t) do
  local x = f()
  if x then
    f(function() return v end)
  end
end
