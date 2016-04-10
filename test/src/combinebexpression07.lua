local t, s
function f(a, b)
  return (a == b and true) or t[a] and t[b] and t[a] == t[b]
end