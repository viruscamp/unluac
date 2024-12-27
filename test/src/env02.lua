local up = 0
function f()
  print(up)
  _ENV.up = 1
  print(_ENV.up)
end

local notup = 0
function g()
  _ENV.notup = 1
  print(_ENV.notup)
end
