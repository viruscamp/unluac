local x = 0

-- x is shadowed, _ENV must be explicit

_ENV.x = 1

printA(x, _ENV.x)
