package unluac.decompile;

import java.io.FileOutputStream;
import java.io.IOException;

public class FileOutputProvider implements OutputProvider {
  
  private final FileOutputStream out;
  private final String eol;
  
  public FileOutputProvider(FileOutputStream out) {
    this.out = out;
    eol = System.lineSeparator();
  }
  
  @Override
  public void print(String s) {
    for(int i = 0; i < s.length(); i++) {
      int c = s.charAt(i);
      if(c < 0 || c > 255) throw new IllegalStateException();
      print((byte) c);
    }
  }

  @Override
  public void print(byte b) {
    try {
      out.write(b);
    } catch(IOException e) {
      throw new RuntimeException(e.getMessage());
    }
  }
  
  @Override
  public void println() {
    print(eol);
  }
  
}
