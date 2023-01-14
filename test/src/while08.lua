-- inner function to avoid varargprep in 5.4
function f(x)
  -- loopback at 1
  while g1(x) do
    -- testset redirected to loopback (only in 5.4+)
    x = g2(x) and "a" or "b"
  end
end

function f2(x)
  local y
  --loopback at 2
  while g1(x) do
    x = g2(x) and "a" or "b"
  end
end
