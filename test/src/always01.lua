while true do
  local a = {feed()}
  if -a == 0 then break end
  process(a)
end
