local a, b
a = b ~= nil and f(b) or "else"

a = b == nil and f(b) or "else"

a = (b ~= nil or f(b)) and "else"

a = (b == nil or f(b)) and "else"
