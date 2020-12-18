while c do
  print("guard")
  if d then
    print("hanger (break redirect)")
  end
  break
end

if b then
  if a then
    print("hanger (else)")
  end
else
  print("else")
end
