while "outer" do
  for k, v in pairs(t) do
    if x then
      if y then
        print("guard")
        goto out
      else
        print("else")
      end
    else
      goto out
    end
  end
end
::out::
