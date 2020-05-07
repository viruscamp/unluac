local a
if x then
  a = b or c + d
  -- testset redirected by else (with MMBIN*)
else
  print("else")
end
