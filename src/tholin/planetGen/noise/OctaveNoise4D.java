package tholin.planetGen.noise;

import java.util.*;

public class OctaveNoise4D extends NoiseFunction4D {
		
	private PerlinNoise4D noise;
	private double persistence;
	private double lacunarity;
	private int octaves;
	
	public OctaveNoise4D(Random rng, int octaves, double persistence, double lacunarity) {
		this(rng, 8, 8, 8, 8, octaves, persistence, lacunarity);
	}
	
	public OctaveNoise4D(Random rng, int width, int height, int depth, int trength, int octaves, double persistence, double lacunarity) {
		super(width, height, depth, trength);
		this.noise = new PerlinNoise4D(rng, (int)(width + octaves), (int)(height * octaves), (int)(depth * octaves), (int)(trength * octaves));
		this.persistence = persistence;
		this.lacunarity = lacunarity;
		this.octaves = octaves;
	}
	
	public double sample(double x, double y, double z, double w) {
		if(x < 0) x = (double)Integer.MAX_VALUE + x;
		if(y < 0) y = (double)Integer.MAX_VALUE + y;
		if(z < 0) z = (double)Integer.MAX_VALUE + z;
		if(w < 0) w = (double)Integer.MAX_VALUE + w;
		double finalRes = 0;
		double max = 0;
		
		double currFreq = 1.0;
		double currSc = 1.0;
		for(int i = 0; i < octaves; i++) {
			finalRes += noise.sample(x * currFreq, y * currFreq, z * currFreq, w * currFreq) * currSc;
			max += currSc;
			currFreq *= persistence;
			currSc *= lacunarity;
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
	
}