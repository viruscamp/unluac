for k, v in ipairs(t) do
  if f(k, v) then
    guard()
    if g(k, v) then
      guard()
      if h(k, v) then
        f2()
      else
        -- empty
      end
    else
      -- empty
    end
  else
    -- empty
  end
end
