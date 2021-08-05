package tholin.planetGen.utils;

public class PostProcessingEffects {
	
	private static double sampleAt(double[][] map, double x, double y) {
		int xi = (int)(x * map.length);
		int yi = (int)(y * 2.0 * map[0].length);
		if(yi < 0) {
			yi = -yi;
			xi += map.length / 2;
		}
		if(yi >= map[0].length) {
			yi = map[0].length - (yi - map[0].length) - 1;
			xi += map.length / 2;
		}
		while(xi < 0) xi += map.length;
		while(xi >= map.length) xi -= map.length;
		return map[xi][yi];
	}
	
	private static double gaussianFunction(double stdev, double dist) {
		double a = 1.0 / Math.sqrt(2 * Math.PI * stdev * stdev);
		double b = -(dist * dist) / (2 * stdev * stdev);
		return a * Math.exp(b);
	}
	
	public static void gaussianBlur(double[][] src, double[][] dest, double pixelSizeX, double pixelSizeY, int blurSize) {
		double[] rowBuff = new double[src.length];
		int kernelSize = blurSize + 2;
		for(int i = 0; i < src[0].length; i++) {
			for(int j = 0; j < src.length; j++) {
				rowBuff[j] = 0;
				for(int k = 0; k < kernelSize * 2 + 1; k++) {
					rowBuff[j] += sampleAt(src, (double)j / (double)src.length + (k - kernelSize) * pixelSizeX, (double)i / (double)src[0].length * 0.5) * gaussianFunction(2.02499 * (double)blurSize / 5.0, k - blurSize);
				}
			}
			for(int j = 0; j < src.length; j++) dest[j][i] = rowBuff[j];
		}
		
		double[] colBuff = src.length >= src[0].length ? rowBuff : new double[src[0].length];
		for(int i = 0; i < src.length; i++) {
			for(int j = 0; j < src[0].length; j++) {
				colBuff[j] = 0;
				for(int k = 0; k < kernelSize * 2 + 1; k++) {
					colBuff[j] += sampleAt(dest, (double)i / (double)src.length, ((double)j / (double)src[0].length + (k - kernelSize) * pixelSizeY) * 0.5) * gaussianFunction(2.02499 * (double)blurSize / 5.0, k - blurSize);
				}
			}
			for(int j = 0; j < src[0].length; j++) dest[i][j] = colBuff[j];
		}
	}
	
}