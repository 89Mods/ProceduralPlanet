package theGhastModding.planetGen.noise;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class NoiseConfig {
	
	public NoiseFunction noise;
	public boolean ridged = false;
	public double noiseStrength;
	public double noiseLatitudeScale;
	public double noiseLongitudeScale;
	public double distortStrength;
	public double noiseOffset;
	public double zOffset;
	
	public NoiseConfig() {
		this(null, false, 1.0, 1.0, 0.0, 0.0, 0.0);
	}
	
	public NoiseConfig(NoiseFunction noise) {
		this(noise, false, 1.0, 1.0, 0.0, 0.0, 0.0);
	}
	
	public NoiseConfig(NoiseFunction noise, boolean ridged, double noiseStrength, double noiseScale, double distortStrength, double noiseOffset, double zOffset) {
		super();
		this.noise = noise;
		this.ridged = ridged;
		this.noiseStrength = noiseStrength;
		this.noiseLatitudeScale = noiseLongitudeScale = noiseScale;
		this.distortStrength = distortStrength;
		this.noiseOffset = noiseOffset;
		this.zOffset = zOffset;
	}
	
	public NoiseConfig setNoiseFunction(NoiseFunction noise) {
		this.noise = noise;
		return this;
	}
	
	public NoiseConfig setIsRidged(boolean ridged) {
		this.ridged = ridged;
		return this;
	}
	
	public NoiseConfig setNoiseStrength(double noiseStrength) {
		this.noiseStrength = noiseStrength;
		return this;
	}
	
	public NoiseConfig setNoiseScale(double noiseScale) {
		this.noiseLatitudeScale = noiseLongitudeScale = noiseScale;
		return this;
	}
	
	public NoiseConfig setNoiseLatitudeScale(double noiseLatitudeScale) {
		this.noiseLatitudeScale = noiseLatitudeScale;
		return this;
	}
	
	public NoiseConfig setNoiseLongitudeScale(double noiseLongitudeScale) {
		this.noiseLongitudeScale = noiseLongitudeScale;
		return this;
	}
	
	public NoiseConfig setDistortStrength(double distortStrength) {
		this.distortStrength = distortStrength;
		return this;
	}
	
	public NoiseConfig setNoiseOffset(double noiseOffset) {
		this.noiseOffset = noiseOffset;
		return this;
	}
	
	public NoiseConfig setZOffset(double zOffset) {
		this.zOffset = zOffset;
		return this;
	}
	
	public void serialize(DataOutputStream out) throws Exception {
		//Can only account for noise functions defined in this library for now...
		int width;
		int height;
		int depth = 1;
		int octaves = 1;
		double lacunarity = 0;
		double persistence = 0;
		boolean isWorley = false;
		if(noise instanceof PerlinNoise2D) {
			width = noise.getWidth();
			height = noise.getHeight();
		}else
		if(noise instanceof PerlinNoise3D) {
			width = noise.getWidth();
			height = noise.getHeight();
			depth = noise.getDepth();
		}else
		if(noise instanceof WorleyNoise) {
			isWorley = true;
			width = noise.getWidth();
			height = noise.getHeight();
			depth = noise.getDepth();
		}else
		if(noise instanceof OctaveNoise2D) {
			width = noise.getWidth();
			height = noise.getHeight();
			octaves = ((OctaveNoise2D)noise).getOctaves();
			lacunarity = ((OctaveNoise2D)noise).getLacunarity();
			persistence = ((OctaveNoise2D)noise).getPersistence();
		}else
		if(noise instanceof OctaveNoise3D) {
			width = noise.getWidth();
			height = noise.getHeight();
			depth = noise.getDepth();
			octaves = ((OctaveNoise3D)noise).getOctaves();
			lacunarity = ((OctaveNoise3D)noise).getLacunarity();
			persistence = ((OctaveNoise3D)noise).getPersistence();
		}else
		if(noise instanceof OctaveWorley) {
			isWorley = true;
			width = noise.getWidth();
			height = noise.getHeight();
			depth = noise.getDepth();
			octaves = ((OctaveWorley)noise).getOctaves();
			lacunarity = ((OctaveWorley)noise).getLacunarity();
			persistence = ((OctaveWorley)noise).getPersistence();
		}else {
			throw new Exception("Unknown noise function. Cannot serialize.");
		}
		out.writeBoolean(isWorley);
		out.writeInt(width);
		out.writeInt(height);
		out.writeInt(depth);
		out.writeInt(octaves);
		out.writeDouble(lacunarity);
		out.writeDouble(persistence);
		
		out.writeBoolean(ridged);
		out.writeDouble(noiseStrength);
		out.writeDouble(noiseLatitudeScale);
		out.writeDouble(noiseLongitudeScale);
		out.writeDouble(distortStrength);
		out.writeDouble(noiseOffset);
		out.writeDouble(zOffset);
	}
	
	public static NoiseConfig deserialize(DataInputStream in) throws Exception {
		NoiseConfig conf = new NoiseConfig();
		boolean isWorley = in.readBoolean();
		int width = in.readInt();
		int height = in.readInt();
		int depth = in.readInt();
		int octaves = in.readInt();
		double lacunarity = in.readDouble();
		double persistence = in.readDouble();
		if(isWorley) {
			if(octaves == 1) {
				conf.noise = new WorleyNoise(width, height, depth);
			}else {
				conf.noise = new OctaveWorley(width, height, depth, octaves, lacunarity, persistence);
			}
		}else {
			if(depth == 1) {
				if(octaves == 1) {
					conf.noise = new PerlinNoise2D(width, height);
				}else {
					conf.noise = new OctaveNoise2D(width, height, octaves, lacunarity, persistence);
				}
			}else {
				if(octaves == 1) {
					conf.noise = new PerlinNoise3D(width, height, depth);
				}else {
					conf.noise = new OctaveNoise3D(width, height, depth, octaves, lacunarity, persistence);
				}
			}
		}
		conf.ridged = in.readBoolean();
		conf.noiseStrength = in.readDouble();
		conf.noiseLatitudeScale = in.readDouble();
		conf.noiseLongitudeScale = in.readDouble();
		conf.distortStrength = in.readDouble();
		conf.noiseOffset = in.readDouble();
		conf.zOffset = in.readDouble();
		return conf;
	}
	
	public String toString() {
		String s = "Ridged: " + Boolean.toString(ridged) + "\n";
		s += String.format("Strength: %#.4f\n", this.noiseStrength);
		s += String.format("Latitude Scale: %#.4f\n", this.noiseLatitudeScale);
		s += String.format("Longitude Scale: %#.4f\n", this.noiseLongitudeScale);
		s += String.format("Distort Strength: %#.4f\n", this.distortStrength);
		s += String.format("Offset: %#.4f\n", this.noiseOffset);
		s += String.format("Z-Offset: %#.4f", this.zOffset);
		return s;
	}
	
	public NoiseConfig clone() {
		NoiseConfig res = new NoiseConfig(this.noise.clone(), this.ridged, this.noiseStrength, 0, this.distortStrength, this.noiseOffset, this.zOffset);
		res.noiseLatitudeScale = this.noiseLatitudeScale;
		res.noiseLongitudeScale = this.noiseLongitudeScale;
		return res;
	}
}