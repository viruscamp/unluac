function CachedClass.__init(self, class)
	self = {}
	self.proxy = setmetatable(class and table.clear(class) or {}, self)
	ClassMap[self.class] = self.proxy
	return self
end
