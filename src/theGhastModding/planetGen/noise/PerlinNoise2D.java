package theGhastModding.planetGen.noise;

import java.util.Random;

public class PerlinNoise2D extends NoiseFunction {
	
	private double[][][] noiseMap;
	
	public PerlinNoise2D(int width, int height) {
		super(width, height, 1);
	}
	
	@Override
	public void initialize(Random rng) {
		if(this.noiseMap == null) this.noiseMap = new double[width + 1][height + 1][2];
		for(int i = 0; i < width + 1; i++) {
			for(int j = 0; j < height + 1; j++) {
				double d1 = rng.nextDouble() * 2.0 - 1.0;
				double d2 = rng.nextDouble() * 2.0 - 1.0;
				double length = Math.sqrt(d1 * d1 + d2 * d2);
				noiseMap[i][j][0] = d1 / length;
				noiseMap[i][j][1] = d2 / length;
			}
		}
	}
	
	@Override
	public void cleanUp() {
		this.noiseMap = null;
		System.gc();
	}
	
	private static double lerp(double a0, double a1, double w) {
		return a0 + w * (a1 - a0);
	}
	
	private static double weight(double x) {
		return 3 * (x * x) - 2 * (x * x * x);
	}
	
	@Override
	public double sample(double x, double y, double z) {
		int nodex = (int)x;
		int nodey = (int)y;
		double sx = x - (double)nodex;
		double sy = y - (double)nodey;
		
		double wx = weight(sx);
		double wy = weight(sy);
		
		nodex %= width + 1;
		nodey %= height + 1;
		int nodex1 = nodex + 1;
		int nodey1 = nodey + 1;
		nodex1 %= width + 1;
		nodey1 %= height + 1;
		
		double dot0 = sx * noiseMap[nodex][nodey][0] + sy * noiseMap[nodex][nodey][1];
		double dot1 = (sx - 1) * noiseMap[nodex1][nodey][0] + sy * noiseMap[nodex1][nodey][1];
		double dot2 = sx * noiseMap[nodex][nodey1][0] + (sy - 1) * noiseMap[nodex][nodey1][1];
		double dot3 = (sx - 1) * noiseMap[nodex1][nodey1][0] + (sy - 1) * noiseMap[nodex1][nodey1][1];
		
		double ix0 = lerp(dot0, dot1, wx);
		double ix1 = lerp(dot2, dot3, wx);
		
		return lerp(ix0, ix1, wy);
	}
	
}