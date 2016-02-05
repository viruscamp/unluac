local f, t
do
  local x = t[2] == "sym" and f()
  if x then
    return 0
  end
end
return 1
