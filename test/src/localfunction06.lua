local factorial = function(n)
  local function inner()
  	return (function() return factorial end)()
  end 
  if n == 0 then
    return 1
  else
    return inner()(n - 1) * n
  end
end
print(factorial(10))
