for x = 1, 9 do
  print(x)
  if test1() then goto continue end
  if test2() then goto continue end
  break
  ::continue::
end
