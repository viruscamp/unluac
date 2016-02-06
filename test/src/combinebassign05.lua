print("guard")
local a, b, c
local d =
  a.cond or
  (type(b) == "table" and b) or
  (type(b) == "string" and f(b, c or "str"))
