package theGhastModding.planetGen.noise;

import java.util.*;

public class PerlinNoise4D extends NoiseFunction4D {
	
	private double[][][][][] noiseMap;
	
	public PerlinNoise4D(Random rng) {
		this(rng, 8, 8, 8, 8);
	}
	
	public PerlinNoise4D(Random rng, int width, int height, int depth, int trength) {
		super(width, height, depth, trength);
		this.noiseMap = new double[width + 1][height + 1][depth + 1][trength + 1][4];
		for(int i = 0; i < width + 1; i++) {
			for(int j = 0; j < height + 1; j++) {
				for(int k = 0; k < depth + 1; k++) {
					for(int l = 0; l < trength + 1; l++) {
						noiseMap[i][j][k][l][0] = rng.nextDouble() * 2.0 - 1.0;
						noiseMap[i][j][k][l][1] = rng.nextDouble() * 2.0 - 1.0;
						noiseMap[i][j][k][l][2] = rng.nextDouble() * 2.0 - 1.0;
						noiseMap[i][j][k][l][3] = rng.nextDouble() * 2.0 - 1.0;
					}
				}
			}
		}
	}
	
	private static double lerp(double a0, double a1, double w) {
		return a0 + w * (a1 - a0);
	}
	
	private static double weight(double x) {
		return 3 * (x * x) - 2 * (x * x * x);
	}
	
	public double sample(double x, double y, double z, double w) {
		
		int nodex = (int)x;
		int nodey = (int)y;
		int nodez = (int)z;
		int nodew = (int)w;
		double sx = x - (double)nodex;
		double sy = y - (double)nodey;
		double sz = z - (double)nodez;
		double sw = w - (double)nodew;
		
		double wx = weight(sx);
		double wy = weight(sy);
		double wz = weight(sz);
		double ww = weight(sw);
		
		nodex %= width + 1;
		nodey %= height + 1;
		nodez %= depth + 1;
		nodew %= trength + 1;
		int nodex1 = nodex + 1;
		int nodey1 = nodey + 1;
		int nodez1 = nodez + 1;
		int nodew1 = nodew + 1;
		nodex1 %= width + 1;
		nodey1 %= height + 1;
		nodez1 %= depth + 1;
		nodew1 %= trength + 1;
		
		double dot0 = sx * noiseMap[nodex][nodey][nodez][nodew][0] + sy * noiseMap[nodex][nodey][nodez][nodew][1] + sz * noiseMap[nodex][nodey][nodez][nodew][2] + sw * noiseMap[nodex][nodey][nodez][nodew][3];
		double dot1 = (sx - 1) * noiseMap[nodex1][nodey][nodez][nodew][0] + sy * noiseMap[nodex1][nodey][nodez][nodew][1] + sz * noiseMap[nodex1][nodey][nodez][nodew][2] + sw * noiseMap[nodex1][nodey][nodez][nodew][3];
		double dot2 = sx * noiseMap[nodex][nodey1][nodez][nodew][0] + (sy - 1) * noiseMap[nodex][nodey1][nodez][nodew][1] + sz * noiseMap[nodex][nodey1][nodez][nodew][2] + sw * noiseMap[nodex][nodey1][nodez][nodew][3];
		double dot3 = (sx - 1) * noiseMap[nodex1][nodey1][nodez][nodew][0] + (sy - 1) * noiseMap[nodex1][nodey1][nodez][nodew][1] + sz * noiseMap[nodex1][nodey1][nodez][nodew][2] + sw * noiseMap[nodex1][nodey1][nodez][nodew][3];
		double dot4 = sx * noiseMap[nodex][nodey][nodez1][nodew][0] + sy * noiseMap[nodex][nodey][nodez1][nodew][1] + (sz - 1) * noiseMap[nodex][nodey][nodez1][nodew][2] + sw * noiseMap[nodex][nodey][nodez1][nodew][3];
		double dot5 = (sx - 1) * noiseMap[nodex1][nodey][nodez1][nodew][0] + sy * noiseMap[nodex1][nodey][nodez1][nodew][1] + (sz - 1) * noiseMap[nodex1][nodey][nodez1][nodew][2] + sw * noiseMap[nodex1][nodey][nodez1][nodew][3];
		double dot6 = sx * noiseMap[nodex][nodey1][nodez1][nodew][0] + (sy - 1) * noiseMap[nodex][nodey1][nodez1][nodew][1] + (sz - 1) * noiseMap[nodex][nodey1][nodez1][nodew][2] + sw * noiseMap[nodex][nodey1][nodez1][nodew][3];
		double dot7 = (sx - 1) * noiseMap[nodex1][nodey1][nodez1][nodew][0] + (sy - 1) * noiseMap[nodex1][nodey1][nodez1][nodew][1] + (sz - 1) * noiseMap[nodex1][nodey1][nodez1][nodew][2] + sw * noiseMap[nodex1][nodey1][nodez1][nodew][3];
		
		double dot8 = sx * noiseMap[nodex][nodey][nodez][nodew1][0] + sy * noiseMap[nodex][nodey][nodez][nodew1][1] + sz * noiseMap[nodex][nodey][nodez][nodew1][2] + (sw - 1) * noiseMap[nodex][nodey][nodez][nodew1][3];
		double dot9 = (sx - 1) * noiseMap[nodex1][nodey][nodez][nodew1][0] + sy * noiseMap[nodex1][nodey][nodez][nodew1][1] + sz * noiseMap[nodex1][nodey][nodez][nodew1][2] + (sw - 1) * noiseMap[nodex1][nodey][nodez][nodew1][3];
		double dot10 = sx * noiseMap[nodex][nodey1][nodez][nodew1][0] + (sy - 1) * noiseMap[nodex][nodey1][nodez][nodew1][1] + sz * noiseMap[nodex][nodey1][nodez][nodew1][2] + (sw - 1) * noiseMap[nodex][nodey1][nodez][nodew1][3];
		double dot11 = (sx - 1) * noiseMap[nodex1][nodey1][nodez][nodew1][0] + (sy - 1) * noiseMap[nodex1][nodey1][nodez][nodew1][1] + sz * noiseMap[nodex1][nodey1][nodez][nodew1][2] + (sw - 1) * noiseMap[nodex1][nodey1][nodez][nodew1][3];
		double dot12 = sx * noiseMap[nodex][nodey][nodez1][nodew1][0] + sy * noiseMap[nodex][nodey][nodez1][nodew1][1] + (sz - 1) * noiseMap[nodex][nodey][nodez1][nodew1][2] + (sw - 1) * noiseMap[nodex][nodey][nodez1][nodew1][3];
		double dot13 = (sx - 1) * noiseMap[nodex1][nodey][nodez1][nodew1][0] + sy * noiseMap[nodex1][nodey][nodez1][nodew1][1] + (sz - 1) * noiseMap[nodex1][nodey][nodez1][nodew1][2] + (sw - 1) * noiseMap[nodex1][nodey][nodez1][nodew1][3];
		double dot14 = sx * noiseMap[nodex][nodey1][nodez1][nodew1][0] + (sy - 1) * noiseMap[nodex][nodey1][nodez1][nodew1][1] + (sz - 1) * noiseMap[nodex][nodey1][nodez1][nodew1][2] + (sw - 1) * noiseMap[nodex][nodey1][nodez1][nodew1][3];
		double dot15 = (sx - 1) * noiseMap[nodex1][nodey1][nodez1][nodew1][0] + (sy - 1) * noiseMap[nodex1][nodey1][nodez1][nodew1][1] + (sz - 1) * noiseMap[nodex1][nodey1][nodez1][nodew1][2] + (sw - 1) * noiseMap[nodex1][nodey1][nodez1][nodew1][3];
		
		double ix0 = lerp(dot0, dot1, wx);
		double ix1 = lerp(dot2, dot3, wx);
		
		double iy0 = lerp(ix0, ix1, wy);
		
		double ix2 = lerp(dot4, dot5, wx);
		double ix3 = lerp(dot6, dot7, wx);
		
		double iy1 = lerp(ix2, ix3, wy);
		
		double ix4 = lerp(dot8, dot9, wx);
		double ix5 = lerp(dot10, dot11, wx);
		
		double iy2 = lerp(ix4, ix5, wy);
		
		double ix6 = lerp(dot12, dot13, wx);
		double ix7 = lerp(dot14, dot15, wx);
		
		double iy3 = lerp(ix6, ix7, wy);
		
		double iz0 = lerp(iy0, iy1, wz);
		double iz1 = lerp(iy2, iy3, wz);
		
		return lerp(iz0, iz1, ww);
	}
	
}