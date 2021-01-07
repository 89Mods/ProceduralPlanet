package theGhastModding.planetGen.utils;

public class ProgressBars {
	
	private static int printed;
	
	public static void printBar() {
		System.out.print("|");
		for(int i = 0; i < 98; i++) System.out.print("-");
		System.out.println("|");
		printed = 0;
	}
	
	public static void printProgress(int i, int max) {
		int percentage = (int)((double)i / (double)max * 100.0);
		if(percentage >= printed) {
			int cnt = percentage - printed;
			for(int j = 0; j < cnt; j++) {
				System.out.print(">");
				printed++;
			}
		}
	}
	
	public static void finishProgress() {
		for(int i = printed; i < 100; i++) System.out.print(">");
		System.out.println();
		printed = 0;
	}
	
}