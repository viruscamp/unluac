for _, request in pairs(t) do
  local finished = false
  request:executeWithCallback(function() finished = true end)
  while not finished do Sleep() end
  -- CLOSE
end
