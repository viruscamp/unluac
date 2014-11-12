package unluac.decompile;

public class AssertionManager {

	public static boolean assertCritical(boolean condition, String message) {
		if(condition) {
			// okay
		} else {
			critical(message);
		}
		return condition;
	}
	
	public static void critical(String message) {
		throw new IllegalStateException(message);
	}
	
	//static only
	private AssertionManager() {}
	
}
