package theGhastModding.planetGen.noise;

import java.util.Random;

public class PerlinNoise3D extends NoiseFunction {
	
	private double[][][][] noiseMap;
	
	public PerlinNoise3D(int width, int height, int depth) {
		super(width, height, depth);
	}
	
	@Override
	public void initialize(Random rng) {
		if(this.noiseMap == null)  this.noiseMap = new double[width + 1][height + 1][depth + 1][3];
		for(int i = 0; i < width + 1; i++) {
			for(int j = 0; j < height + 1; j++) {
				for(int k = 0; k < depth + 1; k++) {
					double d1 = rng.nextDouble() * 2.0 - 1.0;
					double d2 = rng.nextDouble() * 2.0 - 1.0;
					double d3 = rng.nextDouble() * 2.0 - 1.0;
					double dl = Math.sqrt(d1 * d1 + d2 * d2 + d3 * d3);
					d1 /= dl;
					d2 /= dl;
					d3 /= dl;
					noiseMap[i][j][k][0] = d1;
					noiseMap[i][j][k][1] = d2;
					noiseMap[i][j][k][2] = d3;
				}
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
	
	public double sample(double x, double y, double z) {
		int nodex = (int)x;
		int nodey = (int)y;
		int nodez = (int)z;
		double sx = x - (double)nodex;
		double sy = y - (double)nodey;
		double sz = z - (double)nodez;
		
		double wx = weight(sx);
		double wy = weight(sy);
		double wz = weight(sz);
		
		nodex %= width + 1;
		nodey %= height + 1;
		nodez %= depth + 1;
		int nodex1 = nodex + 1;
		int nodey1 = nodey + 1;
		int nodez1 = nodez + 1;
		nodex1 %= width + 1;
		nodey1 %= height + 1;
		nodez1 %= depth + 1;
		
		//SSE this
		
		double dot0 = sx * noiseMap[nodex][nodey][nodez][0] + sy * noiseMap[nodex][nodey][nodez][1] + sz * noiseMap[nodex][nodey][nodez][2];
		double dot1 = (sx - 1) * noiseMap[nodex1][nodey][nodez][0] + sy * noiseMap[nodex1][nodey][nodez][1] + sz * noiseMap[nodex1][nodey][nodez][2];
		double dot2 = sx * noiseMap[nodex][nodey1][nodez][0] + (sy - 1) * noiseMap[nodex][nodey1][nodez][1] + sz * noiseMap[nodex][nodey1][nodez][2];
		double dot3 = (sx - 1) * noiseMap[nodex1][nodey1][nodez][0] + (sy - 1) * noiseMap[nodex1][nodey1][nodez][1] + sz * noiseMap[nodex1][nodey1][nodez][2];
		double dot4 = sx * noiseMap[nodex][nodey][nodez1][0] + sy * noiseMap[nodex][nodey][nodez1][1] + (sz - 1) * noiseMap[nodex][nodey][nodez1][2];
		double dot5 = (sx - 1) * noiseMap[nodex1][nodey][nodez1][0] + sy * noiseMap[nodex1][nodey][nodez1][1] + (sz - 1) * noiseMap[nodex1][nodey][nodez1][2];
		double dot6 = sx * noiseMap[nodex][nodey1][nodez1][0] + (sy - 1) * noiseMap[nodex][nodey1][nodez1][1] + (sz - 1) * noiseMap[nodex][nodey1][nodez1][2];
		double dot7 = (sx - 1) * noiseMap[nodex1][nodey1][nodez1][0] + (sy - 1) * noiseMap[nodex1][nodey1][nodez1][1] + (sz - 1) * noiseMap[nodex1][nodey1][nodez1][2];
		
		double ix0 = lerp(dot0, dot1, wx);
		double ix1 = lerp(dot2, dot3, wx);
		
		double iy0 = lerp(ix0, ix1, wy);
		
		double ix2 = lerp(dot4, dot5, wx);
		double ix3 = lerp(dot6, dot7, wx);
		
		double iy1 = lerp(ix2, ix3, wy);
		
		return lerp(iy0, iy1, wz);
	}
	
}