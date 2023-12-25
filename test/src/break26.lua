for k, v in pairs(t) do
  if v.x == 1 then
  	if v.y == 1 then
  	  print("A")
  	  break
  	elseif v.y == 2 then
  	  print("B")
  	  if v.z == 1 then
  	  	print("C")
  	  end
  	  break
  	end
  elseif v.x == 2 then
    if v.y == 1 then
      print("D")
      break
    elseif v.y == 2 then
      print("E")
      break
    end
  end
end
