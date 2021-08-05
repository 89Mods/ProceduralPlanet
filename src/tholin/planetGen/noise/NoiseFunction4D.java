package tholin.planetGen.noise;

public abstract class NoiseFunction4D {
	
	protected int width,height,depth,trength;
	
	public NoiseFunction4D(int width, int height, int depth, int trength) {
		this.width = width;
		this.height = height;
		this.depth = depth;
		this.trength = trength;
	}
	
	public abstract double sample(double x, double y, double z, double w);
	
}