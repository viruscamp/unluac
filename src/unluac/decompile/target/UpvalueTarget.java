package unluac.decompile.target;

import unluac.decompile.Decompiler;
import unluac.decompile.Output;
import unluac.decompile.Walker;

public class UpvalueTarget extends Target{

  private final String name;
  
  public UpvalueTarget(String name) {
    this.name = name;
  }

  @Override
  public void walk(Walker w) {}
  
  @Override
  public void print(Decompiler d, Output out) {
    out.print(name);    
  }
  
  @Override
  public void printMethod(Decompiler d, Output out) {
    throw new IllegalStateException();
  }
  
}
