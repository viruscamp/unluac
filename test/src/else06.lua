local x
if test then
  print("guard")
  x = a or b
  -- redirect (5.4)
else
  print("guard")
  x = c or d
end
