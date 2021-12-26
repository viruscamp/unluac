if a then
  local x
  function f() return x end
else
  -- empty
end

if b then
  if c then
    local y
    function g() return y end
  else
    -- redirected
  end
else
  print("else")
end

if d then
  if e then
    local z
    function h() return z end
  else
    -- redirected
  end
else
  -- empty
end


while f do
  -- split
  if g then
    local w
    function i() return w end
  else
    break
  end
end
