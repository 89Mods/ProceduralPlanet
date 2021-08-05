package tholin.planetGen.noise;

import java.util.*;

public class OctaveNoise3D extends NoiseFunction {
	
	private PerlinNoise3D noise;
	private double persistence;
	private double lacunarity;
	private int octaves;
	
	public OctaveNoise3D(int octaves, double lacunarity, double persistence) {
		this(16, 16, 16, octaves, lacunarity, persistence);
	}
	
	public OctaveNoise3D(int width, int height, int depth, int octaves, double lacunarity, double persistence) {
		super(width, height, depth);
		this.noise = new PerlinNoise3D((int)(width * octaves), (int)(height * octaves), (int)(depth * octaves));
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
		if(x < 0) x = (double)Integer.MAX_VALUE + x;
		if(y < 0) y = (double)Integer.MAX_VALUE + y;
		if(z < 0) z = (double)Integer.MAX_VALUE + z;
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
	
	public OctaveNoise3D clone() {
		OctaveNoise3D res = new OctaveNoise3D(this.width, this.height, this.depth, this.octaves, this.lacunarity, this.persistence);
		res.noise = (PerlinNoise3D)this.noise.clone();
		return res;
	}
}