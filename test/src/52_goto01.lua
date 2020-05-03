for x = 1, 10 do
  for y = 1, 10 do
    print(x, y)
    if f(x, y) then
      print("guard")
      goto eof
    end
  end
end
::eof::
