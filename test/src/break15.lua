local b, c
while true do
  print("guard")
  if b then
    print("b")
    if c then
      print("c")
      break
    end
    -- loopback
  else
    break
  end
end
