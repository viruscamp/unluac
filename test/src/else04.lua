local a
if x then
  a = b or c
  -- testset redirected by else
else
  print("else")
end
