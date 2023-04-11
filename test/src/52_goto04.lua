repeat
  ::continue::
  if y == 0 then
    if x == 0 then
      return "a"
    elseif x == 1 then
      f()
      goto continue
    end
    if x == 2 then
      return "b"
    elseif x == 3 then
      i()
      goto continue
    end
    print("end")
  end
until g()
