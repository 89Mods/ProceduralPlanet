package theGhastModding.planetGen.noise;

import java.util.*;

public class OctaveWorley extends NoiseFunction {
	
	private WorleyNoise noise;
	private double persistence;
	private double lacunarity;
	private int octaves;
	
	public OctaveWorley(int octaves, double lacunarity, double persistence) {
		this(16, 16, 16, octaves, lacunarity, persistence);
	}
	
	public OctaveWorley(int width, int height, int depth, int octaves, double lacunarity, double persistence) {
		super(width, height, depth);
		this.noise = new WorleyNoise((int)(width * octaves), (int)(height * octaves), (int)(depth * octaves));
		this.persistence = persistence;
		this.lacunarity = lacunarity;
		this.octaves = octaves;
	}
	
	@Override
	public void initialize(Random rng) {
		this.noise.initialize(rng);
	}
	
	@Override
	public void cleanUp() {
		this.noise.cleanUp();
	}
	
	public double sample(double x, double y, double z) {
		double finalRes = 0;
		double max = 0;
		
		double currFreq = 1.0;
		double currSc = 1.0;
		for(int i = 0; i < octaves; i++) {
			finalRes += noise.sample(x * currFreq, y * currFreq, z * currFreq) * currSc;
			max += currSc;
			currFreq *= lacunarity;
			currSc *= persistence;
		}
		
		return finalRes / max;
	}
	
	public double getLacunarity() {
		return this.lacunarity;
	}
	
	public double getPersistence() {
		return this.persistence;
	}
	
	public int getOctaves() {
		return this.octaves;
	}
	
	public OctaveWorley clone() {
		OctaveWorley res = new OctaveWorley(this.width, this.height, this.depth, this.octaves, this.lacunarity, this.persistence);
		res.noise = (WorleyNoise)this.noise.clone();
		return res;
	}
}