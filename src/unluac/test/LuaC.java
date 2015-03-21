package unluac.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LuaC {

  public static void compile(LuaSpec spec, String in, String out) throws IOException {
    String luac = spec.getLuaCName();
    luac = System.getProperty(luac, luac);
    if(System.getProperty("os.name").contains("Windows")) {
      luac = luac + ".exe";
    }
    String[] args = spec.getArgs();
    String[] full = new String[4 + args.length];
    int i = 0;
    full[i++] = luac;
    for(String arg : args) {
      full[i++] = arg;
    }
    full[i++] = "-o";
    full[i++] = out;
    full[i++] = in;
    ProcessBuilder pb = new ProcessBuilder(full);
    pb.directory(null);
    Process p = pb.start();
    while(true) {
      try {
        if(p.waitFor() == 0) {
          return;
        } else {
          BufferedReader r = new BufferedReader(new InputStreamReader(p.getErrorStream()));
          String line = null;
          do {
            line = r.readLine();
            if(line != null) {
              System.err.println(line);
            }
          } while(line != null);

          throw new IOException("luac failed on file: " + in);
        }
      } catch(InterruptedException e ) {

      }
    }
  }

}
