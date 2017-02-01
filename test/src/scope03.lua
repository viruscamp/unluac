-- Compare with scope02.lua
-- The only difference in the compiled output is the scope of local y.

if x then
   local y = f()
   if y then
      print("y")
   end
else
  -- nothing
end
print("done")
