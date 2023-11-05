for _, x in ipairs(t) do
  local test1 = f(x)
  if test1 then
    local test2 = f()
    if test2 then
      if test3 then
        print("okay")
        break
      end
      if not test4 or test5 == 0 then
      end
    end
  end
  break
end
