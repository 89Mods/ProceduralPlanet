package theGhastModding.planetGen.noise;

import java.util.Random;

public class WorleyNoise extends NoiseFunction {
	
	private double[][] points;
	
	public WorleyNoise() {
		this(16, 16, 16);
	}
	
	public WorleyNoise(int width, int height, int depth) {
		super(width, height, depth);
	}
	
	@Override
	public void initialize(Random rng) {
		if(this.points == null) this.points = new double[width * height * depth][3];
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
	
	@Override
	public void cleanUp() {
		this.points = null;
		System.gc();
	}
	
	public double sample(double x, double y, double z) {
		int cellx = ((int)x) % width;
		int celly = ((int)y) % height;
		int cellz = ((int)z) % depth;
		if(cellx < 0) cellx = width + cellx;
		if(celly < 0) celly = height + celly;
		if(cellz < 0) cellz = depth + cellz;
		
		int ix = (int)x;
		int iy = (int)y;
		int iz = (int)z;
		int tx,ty,tz;
		double smallestDist = 1000.0;
		for(int i = 0; i < 27; i++) {
			tx = (i % 3) - 1;
			ty = ((i / 3) % 3) - 1;
			tz = (i / 9) - 1;
			int nCellx = ix + tx;
			int nCelly = iy + ty;
			int nCellz = iz + tz;
			int nCellx2 = (cellx + tx) % width;
			int nCelly2 = (celly + ty) % height;
			int nCellz2 = (cellz + tz) % depth;
			if(nCellx2 < 0) nCellx2 = width - 1;
			if(nCelly2 < 0) nCelly2 = height - 1;
			if(nCellz2 < 0) nCellz2 = depth - 1;
			double[] cellPoint = points[nCellz2 * (width * height) + nCelly2 * width + nCellx2];
			double diffx = x - (cellPoint[0] + nCellx);
			double diffy = y - (cellPoint[1] + nCelly);
			double diffz = z - (cellPoint[2] + nCellz);
			double dist = diffx * diffx + diffy * diffy + diffz * diffz;
			if(dist < smallestDist) {
				smallestDist = dist;
			}
		}
		
		return Math.sqrt(smallestDist);
	}
	
	public WorleyNoise clone() {
		WorleyNoise res = new WorleyNoise(this.width, this.height, this.depth);
		if(this.points != null) {
			res.points = new double[width * height * depth][3];
			for(int i = 0; i < width; i++) {
				for(int j = 0; j < height; j++) {
					for(int k = 0; k < depth; k++) {
						int idx = k * (width * height) + j * width + i;
						res.points[idx][0] = this.points[idx][0];
						res.points[idx][1] = this.points[idx][1];
						res.points[idx][2] = this.points[idx][2];
					}
				}
			}
		}
		return res;
	}
}