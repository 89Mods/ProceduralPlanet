package theGhastModding.planetGen.noise;

import java.util.Random;

public class WorleyNoise extends NoiseFunction {
	
	private double[][] points;
	
	public WorleyNoise(Random rng, int width, int height, int depth) {
		super(width, height, depth);
		this.points = new double[width * height * depth][3];
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				for(int k = 0; k < depth; k++) {
					this.points[k * (width * height) + j * width + i][0] = rng.nextDouble();
					this.points[k * (width * height) + j * width + i][1] = rng.nextDouble();
					this.points[k * (width * height) + j * width + i][2] = rng.nextDouble();
				}
			}
		}
	}
	
	public double sample(double x, double y, double z) {
		int cellx = ((int)x) % width;
		int celly = ((int)y) % height;
		int cellz = ((int)z) % depth;
		if(cellx < 0) cellx = width + cellx;
		if(celly < 0) celly = height + celly;
		if(cellz < 0) cellz = depth + cellz;
		
		double smallestDist = 1000.0;
		for(int i = 0; i < 27; i++) {
			int nCellx = ((int)x + (i % 3) - 1);
			int nCelly = ((int)y + ((i / 3) % 3) - 1);
			int nCellz = ((int)z + (i / 9) - 1);
			int nCellx2 = (cellx + (i % 3) - 1) % width;
			int nCelly2 = (celly + ((i / 3) % 3) - 1) % height;
			int nCellz2 = (cellz + (i / 9) - 1) % depth;
			if(nCellx2 < 0) nCellx2 = width + nCellx2;
			if(nCelly2 < 0) nCelly2 = height + nCelly2;
			if(nCellz2 < 0) nCellz2 = depth + nCellz2;
			double[] cellPoint = points[nCellz2 * (width * height) + nCelly2 * width + nCellx2];
			double diffx = x - (cellPoint[0] + nCellx);
			double diffy = y - (cellPoint[1] + nCelly);
			double diffz = z - (cellPoint[2] + nCellz);
			double dist = Math.sqrt(diffx * diffx + diffy * diffy + diffz * diffz);
			if(dist < smallestDist) {
				smallestDist = dist;
			}
		}
		
		return smallestDist;
	}
	
}