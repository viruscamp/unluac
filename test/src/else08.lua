local up = "upvalue"
local up2 = "upvalue"
local a
if x then
  a = b or function() print(up, up2) end
  -- testset redirected by else (with upvalue declaration)
else
  print("else")
end
