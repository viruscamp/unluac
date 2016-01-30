for k, v in pairs(t) do
  function v.__tostring()
    return k
  end
end

for k, v in pairs(t) do
  local x = v()
  function v.__tostring()
    return munge(k, x)
  end
end

for k, v in pairs(t) do
  local x = v()
  function v.__tostring()
    return k
  end
end

for k, v in pairs(t) do
  local x = v()
  print(x)
end

for k, v in pairs(t) do
  local x = v()
  local y = g(x)
  function v.__tostring()
    return munge(k, y)
  end
end


for i = 1, 1000 do
  t[i].__tostring = function()
    return i
  end
end

for i = 1, 1000 do
  local x = t[i].call()
  t[i].__tostring = function()
    return munge(i, x)
  end
end

for i = 1, 1000 do
  local x = t[i].call()
  t[i].__tostring = function()
    return i
  end
end

for i = 1, 1000 do
  local x = t[i].call()
  print(x)
end

for i = 1, 1000 do
  local x = t[i].call()
  local y = g(x)
  t[i].__tostring = function()
    return munge(i, y)
  end
end
