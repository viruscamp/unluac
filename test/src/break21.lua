local a, x
repeat
  if a then
    x = 1
    do break end -- pseudo-goto
    x = 2
    do break end -- pseudo-goto
    x = 3
  else
    x = 4
  end
until true
