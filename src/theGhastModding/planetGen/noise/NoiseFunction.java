package theGhastModding.planetGen.noise;

import java.util.Random;

public abstract class NoiseFunction {
	
	protected int width, height, depth;
	
	public NoiseFunction(int width, int height, int depth) {
		this.width = width;
		this.height = height;
		this.depth = depth;
	}
	
	public abstract double sample(double x, double y, double z);
	
	public abstract void initialize(Random rng);
}