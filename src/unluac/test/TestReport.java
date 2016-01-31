package unluac.test;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class TestReport {
  
  private int passed = 0;
  private int failed = 0;
  private int skipped = 0;

  private List<String> failedTests = new ArrayList<String>();
  private List<String> skippedTests = new ArrayList<String>();
  
  public void report(PrintStream out) {
    if(failed == 0 && skipped == 0) {
      out.println("All tests passed!");
    } else {
      for(String failed : failedTests) {
        out.println("Failed: " + failed);
      }
      for(String skipped : skippedTests) {
        out.println("Skipped: " + skipped);
      }
      out.println("Failed " + failed + " of " + (failed + passed) + " tests, skipped " + skipped + " tests.");
    }
  }

  public void result(String test, TestResult result) {
    switch(result) {
    case OK:
      passed++;
      break;
    case FAILED:
      failedTests.add(test);
      failed++;
      break;
    case SKIPPED:
      skippedTests.add(test);
      skipped++;
      break;
    default:
      throw new IllegalStateException();
    }
  }
  
}
