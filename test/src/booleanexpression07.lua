function obj.method(self, x)
	self = {name = "asdf"}
	self.field = setmetatable(x and table.copy(x) or {}, self)
	t[self.name] = self.field
	return self
end
