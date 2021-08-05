package theGhastModding.planetGen.utils;

import java.io.*;
import java.util.*;

import theGhastModding.planetGen.noise.NoiseConfig;

public class RavineGenerator {
	
	public static class RavineConfig {
		public NoiseConfig distortNoiseConfig;
		public double ravineStrength;
		public double shapeExponent;
		public double rimWidth;
		public double rimHeight;
		public double rimShapeExponent;
		public boolean rimShapeFullHyperbolic;
		public NoiseConfig rimNoise;
		//public double floorHeight;
		public double size;
		
		public RavineConfig() {
			
		}
		
		public RavineConfig(NoiseConfig distortNoiseConfig, double ravineStrength, double shapeExponent, double rimWidth, double rimHeight, double rimShapeExponent, boolean rimShapeFullHyperbolic, NoiseConfig rimNoise, double size) {
			super();
			this.distortNoiseConfig = distortNoiseConfig;
			this.ravineStrength = ravineStrength;
			this.shapeExponent = shapeExponent;
			this.rimWidth = rimWidth;
			this.rimHeight = rimHeight;
			this.rimShapeExponent = rimShapeExponent;
			this.rimShapeFullHyperbolic = rimShapeFullHyperbolic;
			this.rimNoise = rimNoise;
			this.size = size;
		}
		
		public RavineConfig setDistortNoiseConfig(NoiseConfig distortNoiseConfig) {
			this.distortNoiseConfig = distortNoiseConfig;
			return this;
		}
		
		public RavineConfig setRavineStrength(double ravineStrength) {
			this.ravineStrength = ravineStrength;
			return this;
		}
		
		public RavineConfig setShapeExponent(double shapeExponent) {
			this.shapeExponent = shapeExponent;
			return this;
		}
		
		public RavineConfig setRimWidth(double rimWidth) {
			this.rimWidth = rimWidth;
			return this;
		}
		
		public RavineConfig setRimHeight(double rimHeight) {
			this.rimHeight = rimHeight;
			return this;
		}
		
		public RavineConfig setRimShapeExponent(double rimShapeExponent) {
			this.rimShapeExponent = rimShapeExponent;
			return this;
		}
		
		public RavineConfig setRimShapeFullHyperbolic(boolean rimShapeFullHyperbolic) {
			this.rimShapeFullHyperbolic = rimShapeFullHyperbolic;
			return this;
		}
		
		public RavineConfig setRimNoise(NoiseConfig rimNoise) {
			this.rimNoise = rimNoise;
			return this;
		}
		
		public RavineConfig setSize(double size) {
			this.size = size;
			return this;
		}
		
		public void serialize(DataOutputStream out) throws Exception {
			distortNoiseConfig.serialize(out);
			out.writeDouble(ravineStrength);
			out.writeDouble(shapeExponent);
			out.writeDouble(rimWidth);
			out.writeDouble(rimHeight);
			out.writeDouble(rimShapeExponent);
			out.writeBoolean(rimShapeFullHyperbolic);
			out.writeBoolean(rimNoise != null);
			if(rimNoise != null) rimNoise.serialize(out);
			out.writeDouble(size);
		}
		
		public static RavineConfig deserialize(DataInputStream in) throws Exception {
			RavineConfig res = new RavineConfig();
			res.distortNoiseConfig = NoiseConfig.deserialize(in);
			res.ravineStrength = in.readDouble();
			res.shapeExponent = in.readDouble();
			res.rimWidth = in.readDouble();
			res.rimHeight = in.readDouble();
			res.rimShapeExponent = in.readDouble();
			res.rimShapeFullHyperbolic = in.readBoolean();
			res.rimNoise = in.readBoolean() ? NoiseConfig.deserialize(in) : null;
			res.size = in.readDouble();
			return res;
		}
		
		@Override
		public String toString() {
			String s = "Distort Noise Configuration:\n";
			s += "\t" + distortNoiseConfig.toString().replace("\n", "\n\t") + "\n";
			s += String.format("Ravine Strength: %#.4f\n", this.ravineStrength);
			s += String.format("Shape Exponent: %#.4f\n", this.shapeExponent);
			s += String.format("Rim Width: %#.4f\n", this.rimWidth);
			s += String.format("Rim Height: %#.4f\n", this.rimHeight);
			s += String.format("Rim Shape Exponent: %#.4f\n", this.rimShapeExponent);
			s += "Rim Shape Full Hyperbolic: " + Boolean.toString(this.rimShapeFullHyperbolic) + "\n";
			s += "Rim Noise Configuration:\n";
			s += "\t" + rimNoise.toString().replace("\n", "\n\t") + "\n";
			s += String.format("Size: %#.4f", this.size);
			return s;
		}
	}
	
	private int width,height;
	private TrigonometryCache trigCache;
	private boolean[][] lineMap;
	private float[][] distanceMap;
	
	public RavineGenerator(int width, int height) {
		this.width = width;
		this.height = height;
		this.trigCache = new TrigonometryCache(width, height);
		this.lineMap = new boolean[width][height];
		this.distanceMap = new float[width][height];
	}
	
	/*
	 * map is the hm
	 * baseRavineMap is a map of all ravines, and must be the same for every ravine generated on the same hm, as it is used to calculate blending between ravines. Must be initialized to all 0.75.
	 * ravineMap is an output that has the newly generated ravine added to it as well, but is not used for blending, and can thus be different from baseRavineMap before generation.
	 * 	This is so that a map pre-populated with other features (i.e. craters) can be input as the baseRavineMap for the ravine to blend with, while ravineMap can be used
	 * 	to create a map of just all ravines on a heightmap. Useful for generating color- or biome-maps later on.
	 * overlayRavine specifies if the current ravine follows the terrain shape and can thus "dig into" existing ravines.
	 */
	public void genRavine(double[][] map, double[][] featureMap, double[][] ravineMap, double lat1, double lon1, double lat2, double lon2, int ymin, int ymax, boolean overlayRavine, RavineConfig config, Random rng) {
		float ravineGenWidth = 1.0f + (float)config.rimWidth;
		for(int i = 0; i < distanceMap.length; i++) Arrays.fill(distanceMap[i], ravineGenWidth);
		drawLine(lat1, lon1, lat2, lon2, config.distortNoiseConfig, rng);
		
		int pxHeight = (int)Math.ceil(ravineGenWidth * config.size * ((double)height / 1024.0)) + 1;
		int lStart = Math.max(0, ymin - pxHeight);
		int lEnd = Math.min(height - 1, ymax + pxHeight);
		for(int i1 = 0; i1 < width; i1++) {
			for(int j1 = lStart; j1 < lEnd; j1++) {
				if(!lineMap[i1][j1]) continue;
				int currDim = 1;
				double lat = (j1 - map[0].length / 2.0) / (map[0].length / 2.0) * 90.0;
				double lon = (i1 - map.length / 2.0) / (map.length / 2.0) * 180.0;
				double sinLat = Math.sin(Math.toRadians(lat));
				double cosLat = Math.cos(Math.toRadians(lat));
				while(true) {
					boolean allOutOfRange = true;
					int sYa;
					for(int j = 0; j < currDim; j++) {
						int sY = sYa = (int)(j1 - (currDim / 2.0) + j);
						if(sY < 0) sY = -sY;
						if(sY >= map[0].length) sY = map[0].length - 1 - (sY - map[0].length);
						
						double localLat = (double)(sY - map[0].length / 2) / (map[0].length / 2.0) * 90.0;
						
						double sinLatitude = Math.sin(Math.toRadians(localLat));
						double cosLatitude = Math.cos(Math.toRadians(localLat));
						double a = sinLat * sinLatitude;
						
						for(int k = 0; k < currDim; k++) {
							if(j != 0 && j != (currDim - 1)) if(k != 0 && k != (currDim - 1)) continue;
							int sX = (int)(i1 - (currDim / 2.0) + k);
							sX %= map.length;
							if(sX < 0) sX += map.length;
							if(sYa < 0) {
								sX += map.length / 2;
								sX %= map.length;
							}
							if(sYa >= map[0].length) {
								sX += map.length / 2;
								sX %= map.length;
							}
							double localLon = (double)(sX - map.length / 2) / (map.length / 2.0) * 180.0;
							
							double c = Math.cos(Math.toRadians(Math.abs(localLon - lon)));
							double b = cosLat * cosLatitude * c;
							
							double dist = trigCache.fastAcos(a + b) / Math.PI;
							dist *= 2048;
							
							if(dist <= config.size * ravineGenWidth) {
								allOutOfRange = false;
								distanceMap[sX][sY] = Math.min(distanceMap[sX][sY], (float)(dist / config.size));
							}
						}
					}
					if(allOutOfRange) break;
					currDim += 2;
					if(currDim >= map.length / 2) break;
				}
			}
		}
		
		double genWidthPow = Math.pow(ravineGenWidth, config.shapeExponent) - 1.0;
		for(int i = 0; i < width; i++) {
			for(int j = ymin; j < ymax; j++) {
				if(distanceMap[i][j] == ravineGenWidth) {
					distanceMap[i][j] = 1.0f;
				}else {
					//https://www.desmos.com/calculator/wr6tisnn6h
					distanceMap[i][j] = (float)Math.pow(distanceMap[i][j], config.shapeExponent);
					if(distanceMap[i][j] > 1 && distanceMap[i][j] != genWidthPow) {
						float f = distanceMap[i][j] - 1.0f;
						distanceMap[i][j] = 1.0f;
						double x = f / genWidthPow;
						if(config.rimShapeFullHyperbolic) x = x * 2.0 - 1.0;
						f = (float)((1.0 - Math.pow(x, config.rimShapeExponent)) * config.rimHeight);
						double h = config.rimNoise == null ? 1.0 : NoiseUtils.sampleSpherableNoise(i, j, width, height, config.rimNoise);
						distanceMap[i][j] += f * (float)h;
					}
					
					double prev = featureMap[i][j];
					if(overlayRavine) {
						featureMap[i][j] -= (1.0 - distanceMap[i][j]) * config.ravineStrength;
					}else {
						if(distanceMap[i][j] < 1.0) featureMap[i][j] = Math.min(featureMap[i][j], 0.75 - (1.0 - distanceMap[i][j]) * config.ravineStrength); //If featureMap > 0.75, this returns a much higher diff later
						if(distanceMap[i][j] > 1.0) {
							double mul = 1;
							if(featureMap[i][j] < 0.75) {
								double a = 0.75 - featureMap[i][j];
								mul = 1.0 - Math.min(1, a / (config.ravineStrength * 0.64));
							}
							featureMap[i][j] += (distanceMap[i][j] - 1.0) * config.ravineStrength * mul;
						}
					}
					
					double diff = featureMap[i][j] - prev;
					map[i][j] += diff;
					if(ravineMap != null) ravineMap[i][j] += diff;
				}
			}
		}
	}
	
	private void drawLine(double lat1, double lon1, double lat2, double lon2, NoiseConfig distortNoise, Random rng) {
		for(int i = 0; i < width; i++) Arrays.fill(lineMap[i], false);
		double[] xyz1 = latlonToXyz(lat1, lon1);
		double[] xyz2 = latlonToXyz(lat2, lon2);
		double[] latlon = new double[2];
		
		double stepX = xyz2[0] - xyz1[0];
		double stepY = xyz2[1] - xyz1[1];
		double stepZ = xyz2[2] - xyz1[2];
		double len = Math.sqrt(stepX * stepX + stepY * stepY + stepZ * stepZ);
		stepX /= len;
		stepY /= len;
		stepZ /= len;
		double l = Math.sqrt(width * height);
		stepX /= l;
		stepY /= l;
		stepZ /= l;
		int steps = (int)(len * l);
		
		double nX = xyz1[1] * xyz2[2] - xyz1[2] * xyz2[1];
		double nY = xyz1[2] * xyz2[0] - xyz1[0] * xyz2[2];
		double nZ = xyz1[0] * xyz2[1] - xyz1[1] * xyz2[0];
		len = Math.sqrt(nX * nX + nY * nY + nZ * nZ);
		nX /= len;
		nY /= len;
		nZ /= len;
		
		double[] offsets = new double[steps];
		distortNoise.noise.initialize(rng);
		double dist = Maths.gcDistance(lat1, lon1, lat2, lon2);
		for(int i = 0; i < steps; i++) {
			offsets[i] = distortNoise.noiseOffset + distortNoise.noiseStrength * distortNoise.noise.sample(1.0 + distortNoise.zOffset, (double)i / (double)steps * distortNoise.noiseLatitudeScale * dist, 0.0) * 0.5;
			if(distortNoise.ridged) offsets[i] = Math.abs(offsets[i]) - 0.5 * distortNoise.noiseStrength;
		}
		
		double currX = xyz1[0];
		double currY = xyz1[1];
		double currZ = xyz1[2];
		for(int i = 0; i < steps; i++){
			xyzToLatlon(currX + nX * offsets[i], currY + nY * offsets[i], currZ + nZ * offsets[i], latlon);
			
			int imgX = (int)((latlon[1] + 180.0) / 360.0 * width) % width;
			int imgY = (int)((latlon[0] + 90.0) / 180.0 * height);
			if(imgY < 0) {
				imgY = -imgY;
				imgX += width / 2;
				imgX %= width;
			}
			if(imgY >= height) {
				imgY = height - 1 - (imgY - height);
				imgX += width / 2;
				imgX %= width;
			}
			lineMap[imgX][imgY] = true;
			
			currX += stepX;
			currY += stepY;
			currZ += stepZ;
		}
	}
	
	private static double[] latlonToXyz(double lat, double lon) {
		double[] ret = new double[3];
		lat += 90.0;
		lon += 270.0;
		
		final double rad = 10.0;
		ret[0] = rad * Math.cos(Math.toRadians(lon)) * Math.sin(Math.toRadians(lat));
		ret[2] = rad * Math.sin(Math.toRadians(lon)) * Math.sin(Math.toRadians(lat));
		ret[1] = rad * Math.cos(Math.toRadians(lat));
		
		return ret;
	}
	
	private static void xyzToLatlon(double x, double y, double z, double[] res) {
		double dist = Math.sqrt(x * x + y * y + z * z);
		x /= dist;
		y /= dist;
		z /= dist;
		
		res[0] = Math.acos(y) * (180.0 / Math.PI) - 90.0; //Latitude
		res[1] = ((270 + (Math.atan2(z, x)) * 180 / Math.PI) % 360) - 180; //Longitude
	}
	
	public int getWidth() {
		return this.width;
	}
	
	public int getHeight() {
		return this.height;
	}
}