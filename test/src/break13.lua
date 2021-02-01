while true do
  if a then
    if b then
      print("ab")
    end
    if c then
      if d then
        print("acd")
      end
      break
    end
    print("guard")
    if e then
      print("ae")
    end
  else
    print("~a")
  end
end
