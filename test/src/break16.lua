local b, c
while true do
  print("guard")
  if a then
    -- no guard
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
    if d then
      print("d")
    end
    -- loopback
  else
    break
  end
end
