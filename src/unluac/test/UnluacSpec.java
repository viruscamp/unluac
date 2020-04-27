package unluac.test;

import java.io.IOException;

import unluac.Main;

public class UnluacSpec {
  
  public UnluacSpec() {
    disassemble = false;
  }
  
  public void run(String in, String out) throws IOException {
    if(!disassemble) {
      Main.decompile(in, out);
    } else {
      Main.disassemble(in, out);
    }
  }
  
  public boolean disassemble;
  
}
