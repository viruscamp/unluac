return function( self )
  local x = 0
  local z = false
  if a then
    if x == 1 then
      self.y = "1"
    elseif math.random(100) <= self.x then
      self.y = "2"
    end
  end
  z = false
  return self.y
end
