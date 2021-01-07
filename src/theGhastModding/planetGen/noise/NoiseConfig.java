package theGhastModding.planetGen.noise;

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
	
}