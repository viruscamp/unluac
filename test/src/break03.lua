if x then
  for i=1,10 do
    if a then
      print("okay")
    elseif b then
      break
    elseif c then
      dontbreak()
    elseif d then
      alsodontbreak()
      for k = 1, 10 do print("breakable loop") end
    else
      break
    end
    print("next")
  end
else
  print("else")
end
print("done")
