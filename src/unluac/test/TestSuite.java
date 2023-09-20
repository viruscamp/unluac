package unluac.test;

import java.io.File;
import java.io.IOException;

import unluac.Configuration;
import unluac.Main;
import unluac.assemble.AssemblerException;

public class TestSuite {
  
  private static String working_dir = "./test/working/";
  private static String compiled = "luac.out";
  private static String decompiled = "unluac.out";
  private static String recompiled = "test.out";
  
  private String name;
  private String path;
  private TestFile[] files;
  private String ext = ".lua";
  
  public TestSuite(String name, String path, TestFile[] files) {
    this.name = name;
    this.path = path;
    this.files = files;
  }
  
  public String testName(LuaSpec spec, String file) {
    if(name == null) {
      return spec.id() + ": " + file;
    } else {
      return spec.id() + ": " + name + "/" + file.replace('\\', '/');
    }
  }
  
  private TestResult test(LuaSpec spec, UnluacSpec uspec, String file, Configuration config) {
    try {
      LuaC.compile(spec, file, working_dir + compiled);
    } catch (IOException e) {
      return TestResult.SKIPPED;
    }
    try {
      uspec.run(working_dir + compiled, working_dir + decompiled, config);
      if(!uspec.disassemble) {
        LuaC.compile(spec, working_dir + decompiled, working_dir + recompiled);
      } else {
        Main.assemble(working_dir + decompiled, working_dir + recompiled);
      }
      Compare compare;
      if(!uspec.disassemble) {
        compare = new Compare(Compare.Mode.NORMAL);
      } else {
        compare = new Compare(Compare.Mode.FULL);
      }
      return compare.bytecode_equal(working_dir + compiled, working_dir + recompiled) ? TestResult.OK : TestResult.FAILED;
    } catch (IOException e) {
      return TestResult.FAILED;
    } catch (RuntimeException e) {
      e.printStackTrace();
      return TestResult.FAILED;
    } catch (AssemblerException e) {
      e.printStackTrace();
      return TestResult.FAILED;
    }
  }
  
  private TestResult testc(LuaSpec spec, UnluacSpec uspec, String file, Configuration config) {
    try {
      uspec.run(file, working_dir + decompiled, config);
      LuaC.compile(spec, working_dir + decompiled, working_dir + recompiled);
      Compare compare = new Compare(Compare.Mode.NORMAL);
      return compare.bytecode_equal(file, working_dir + recompiled) ? TestResult.OK : TestResult.FAILED;
    } catch(IOException e) {
      return TestResult.FAILED;
    } catch(RuntimeException e) {
      e.printStackTrace();
      return TestResult.FAILED;
    }
  }
  
  public boolean run(LuaSpec spec, UnluacSpec uspec, TestReport report, Configuration base) throws IOException {
    int failed = 0;
    File working = new File(working_dir);
    if(!working.exists()) {
      working.mkdir();
    }
    for(TestFile testfile : files) {
      if(spec.compatible(testfile)) {
        String name = testfile.name;
        Configuration config = configure(testfile, base);
        TestResult result = test(spec, uspec, path + name + ext, config);
        report.result(testName(spec, name), result);
        switch(result) {
          case OK:
            System.out.print(".");
            break;
          case SKIPPED:
            System.out.print(",");
            break;
          default:
            System.out.print("!");
            failed++;
        }
      }
    }
    return failed == 0;
  }
  
  public boolean run(LuaSpec spec, UnluacSpec uspec, String file, boolean compiled, Configuration config) throws IOException {
    int passed = 0;
    int skipped = 0;
    int failed = 0;
    File working = new File(working_dir);
    if(!working.exists()) {
      working.mkdir();
    }
    {
      String name = file;
      String full;
      if(!file.contains("/")) {
        full = path + name + ext;
      } else {
        full = name;
      }
      TestResult result;
      if(!compiled) {
        result = test(spec, uspec, full, config);
      } else {
        result = testc(spec, uspec, full, config);
      }
      switch(result) {
        case OK:
          System.out.println("Passed: " + name);
          passed++;
          break;
        case SKIPPED:
          System.out.println("Skipped: " + name);
          skipped++;
          break;
        default:
          System.out.println("Failed: " + name);
          failed++;
      }
    }
    if(failed == 0 && skipped == 0) {
      System.out.println(spec.getLuaCName() + ": All tests passed!");
    } else {
      System.out.println(spec.getLuaCName() + ": Failed " + failed + " of " + (failed + passed) + " tests, skipped "+skipped+" tests.");
    }
    return failed == 0;
  }
  
  private Configuration configure(TestFile testfile, Configuration config) {
    Configuration modified = null;
    if(testfile.getFlag(TestFile.RELAXED_SCOPE) && config.strict_scope) {
      modified = extend(config, modified);
      modified.strict_scope = false;
    }
    return modified != null ? modified : config;
  }
  
  private Configuration extend(Configuration base, Configuration modified) {
    return modified != null ? modified : new Configuration(base);
  }
  
}
