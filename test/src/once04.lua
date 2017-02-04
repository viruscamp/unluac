
for i = 1, 10 do
  local t = false
  repeat
    t = f3(i)
    if f4() then
      break
    else
      f1()
    end
    f2()
  until true
  if t then
    break
  end
end
