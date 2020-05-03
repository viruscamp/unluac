for k, v in pairs(t) do
  if x then
    if y then
      print("guard")
      break
    else
      print("else")
    end
  else
    break
  end
end
