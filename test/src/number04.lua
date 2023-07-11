-- these are infinite literals (for float/double numbers)
print(1e9999)
print(-1e9999)
print(1e99999)

-- 32-bit float subnormals
print(1e-45)
print(-1e-45)
print(1e-38)

-- 64-bit float subnormals
print(5e-324)
print(-5e-324)
print(2e-308)

-- many digits of precision
print(3.14159265358979323846264338327950288419716939937510582097494459230781640628620899862803482534)
