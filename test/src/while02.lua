if elseredirect then
  if inner then
    local x = 1
    while x < 100 do
      print(x)
      x = x + 1
    end
  end
else
  print("else")
end
