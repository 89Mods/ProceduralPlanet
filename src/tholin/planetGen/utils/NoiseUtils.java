package tholin.planetGen.utils;

import tholin.planetGen.noise.*;

public class NoiseUtils {
	
	public static double sampleSpherableNoise(double x, double y, double width, double height, NoiseConfig nc) {
		double fNX = (x + 0.5) / width;
		double fNY = (y + 0.5) / height;
		double fRdx = fNX * 2 * Math.PI;
		double fRdy = fNY * Math.PI;
		double fYSin = Math.sin(fRdy + Math.PI);
		double a = Math.sin(fRdx) * fYSin;
		double b = Math.cos(fRdx) * fYSin;
		double c = Math.cos(fRdy);
		double offsetx = nc.noiseLongitudeScale < 1 ? 1.55 / nc.noiseLongitudeScale : 1.55;
		double offsety = nc.noiseLatitudeScale < 1 ? 1.55 / nc.noiseLatitudeScale : 1.55;
		double val = distortedNoise(nc.noise, offsetx + a / nc.noiseLongitudeScale + nc.zOffset, offsetx + b / nc.noiseLongitudeScale + nc.zOffset, offsety + c / nc.noiseLatitudeScale + nc.zOffset, nc.distortStrength);
		val = val + nc.noiseOffset;
		if(nc.ridged) val = Math.abs(val);
		return val * nc.noiseStrength;
		//return noise.sampleNorm(1.0 + a / scalex, 2.0 + b / scalex, 3.0 + c / scaley, 1.5, 1.0);
	}// https://gamedev.stackexchange.com/questions/162454/how-to-distort-2d-perlin-noise
	
	public static double distortedNoise(NoiseFunction noise, double x, double y, double z, double distorStrength) {
		double xDistort = distorStrength * distort(noise, x + 2.3, y + 1.7, z + 1.5);
		double yDistort = distorStrength * distort(noise, x + 1.8, y + 1.1, z + 2.2);
		double zDistort = distorStrength * distort(noise, x + 2.7, y + 2.4, z + 1.5);
		
		return noise.sample(x + xDistort, y + yDistort, z + zDistort);
	}
	
	public static double distortedNoise(NoiseFunction4D noise, double x, double y, double z, double w, double distorStrength) {
		double xDistort = distorStrength * distort(noise, x + 2.3, y + 1.7, z + 1.5, w + 2.3);
		double yDistort = distorStrength * distort(noise, x + 1.8, y + 1.1, z + 2.2, w + 2.1);
		double zDistort = distorStrength * distort(noise, x + 2.7, y + 2.4, z + 1.5, w + 1.2);
		double wDistort = distorStrength * distort(noise, x + 1.9, y + 2.8, z + 2.7, w + 1.1);
		
		return noise.sample(x + xDistort, y + yDistort, z + zDistort, w + wDistort);
	}
	
	public static double distort(NoiseFunction noise, double x, double y, double z) {
		double wiggleDensity = 4.7;
		return noise.sample(x * wiggleDensity, y * wiggleDensity, z * wiggleDensity);
	}
	
	public static double distort(NoiseFunction4D noise, double x, double y, double z, double w) {
		double wiggleDensity = 4.7;
		return noise.sample(x * wiggleDensity, y * wiggleDensity, z * wiggleDensity, w * wiggleDensity);
	}
	
	public static double sampleTileableNoise(NoiseFunction4D noise, double x, double y, double width, double height, double scalex, double scaley, double distortStrength) {
		double fNX = x / width;
		double fNY = y / height;
		double fRdx = fNX*2*Math.PI;
		double fRdy = fNY*2*Math.PI;
		double a = Math.sin(fRdx);
		double b = Math.cos(fRdx);
		double c = Math.sin(fRdy);
		double d = Math.cos(fRdy);
		
		double offsetx = scalex < 0 ? 1.55 / scalex : 1.55;
		double offsety = scaley < 0 ? 1.55 / scaley : 1.55;
		
		return distortedNoise(noise, offsetx + a / scalex, offsetx + b / scalex, offsety + c / scaley, offsety + d / scaley, distortStrength);
	}
	
}