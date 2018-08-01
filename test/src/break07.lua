while x do
  if b then
    guard()
    if c then
      print("c")
      break
    end
    guard()
  else
    print("not b")
    break
  end
  guard()
end