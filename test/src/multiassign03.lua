function f(a, b, c)
  a, b, c = b + c
  return a, b, c
end

function f(a, b, c)
  a, b, c = b + c, nil
  return a, b, c
end

function f(a, b, c)
  a, b, c = b + c, nil, nil
  return a, b, c
end

function f(a, b, c, d)
  a, b, c, d = b + c, nil, nil
  return a, b, c, d
end
