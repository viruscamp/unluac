for i = 1, 10 do
  if f(i) then
    break
  end
  if g(i) then
    h()
  else
    break
  end
end
