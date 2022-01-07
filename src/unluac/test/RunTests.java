package unluac.test;

import java.io.IOException;

import unluac.Configuration;

public class RunTests {

  public static void main(String[] args) throws IOException {
    boolean result = true;
    TestReport report = new TestReport();
    Configuration config = new Configuration();
    config.strict_scope = true;
    for(LuaSpec spec : new LuaSpec[] {
      new LuaSpec(0x50),
      new LuaSpec(0x51),
      new LuaSpec(0x51, 4),
      new LuaSpec(0x52),
      new LuaSpec(0x53),
      new LuaSpec(0x54),
    }) {
      UnluacSpec uspec = new UnluacSpec();
      System.out.print(spec.id());
      result = result & TestFiles.suite.run(spec, uspec, report, config);
      System.out.println();
    }
    report.report(System.out);
    if(result) {
      System.exit(0);
    } else {
      System.exit(1);
    }
  }
  
}
