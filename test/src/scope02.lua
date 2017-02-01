-- Compare with scope03.lua
-- The only difference in the compiled output is the scope of local y.

if x then
   local y = f()
   if y then
      print("y")
   else
      -- nothing
   end
end
print("done")
