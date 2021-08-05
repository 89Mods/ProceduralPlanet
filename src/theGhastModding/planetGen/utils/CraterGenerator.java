package theGhastModding.planetGen.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.util.Arrays;
import java.util.Random;

import javax.imageio.ImageIO;

import edu.cornell.lassp.houle.RngPack.RanMT;
import theGhastModding.planetGen.noise.NoiseConfig;
import theGhastModding.planetGen.noise.OctaveNoise2D;
import theGhastModding.planetGen.noise.OctaveNoise3D;
import theGhastModding.planetGen.noise.PerlinNoise3D;

public class CraterGenerator {
	
	public static class CraterConfig {
		
		public double size;
		public double craterStrength,perturbStrength,perturbScale;
		public double p1,p2,floorHeight;
		public double ejectaStrength,ejectaStretch,ejectaPerturbStrength,ejectaPerturbScale,fullPeakSize,ringThreshold,ringFunctMul;
		
		public CraterConfig(double size, double craterStrength, double perturbStrength, double perturbScale, double p1, double p2, double floorHeight, double ejectaStrength, double ejectaStretch, double ejectaPerturbStrength, double ejectaPerturbScale, double fullPeakSize, double ringThreshold, double ringFunctMul) {
			this.size = size;
			this.craterStrength = craterStrength;
			this.perturbStrength = perturbStrength;
			this.perturbScale = perturbScale;
			this.p1 = p1;
			this.p2 = p2;
			this.floorHeight = floorHeight;
			this.ejectaStrength = ejectaStrength;
			this.ejectaStretch = ejectaStretch;
			this.ejectaPerturbStrength = ejectaPerturbStrength;
			this.ejectaPerturbScale = ejectaPerturbScale;
			this.fullPeakSize = fullPeakSize;
			this.ringThreshold = ringThreshold;
			this.ringFunctMul = ringFunctMul;
		}
		
		public CraterConfig() {
			this.size = 0;
		}
		
		public CraterConfig setSize(double size) {
			this.size = size;
			return this;
		}
		
		public CraterConfig setCraterStrength(double craterStrength) {
			this.craterStrength = craterStrength;
			return this;
		}
		
		public CraterConfig setPerturbStrength(double perturbStrength) {
			this.perturbStrength = perturbStrength;
			return this;
		}
		
		public CraterConfig setPerturbScale(double perturbScale) {
			this.perturbScale = perturbScale;
			return this;
		}
		
		public CraterConfig setP1(double p1) {
			this.p1 = p1;
			return this;
		}
		
		public CraterConfig setP2(double p2) {
			this.p2 = p2;
			return this;
		}
		
		public CraterConfig setFloorHeight(double floorHeight) {
			this.floorHeight = floorHeight;
			return this;
		}
		
		public CraterConfig setEjectaStrength(double ejectaStrength) {
			this.ejectaStrength = ejectaStrength;
			return this;
		}
		
		public CraterConfig setEjectaStretch(double ejectaStretch) {
			this.ejectaStretch = ejectaStretch;
			return this;
		}
		
		public CraterConfig setEjectaPerturbStrength(double ejectaPerturbStrength) {
			this.ejectaPerturbStrength = ejectaPerturbStrength;
			return this;
		}
		
		public CraterConfig setEjectaPerturbScale(double ejectaPerturbScale) {
			this.ejectaPerturbScale = ejectaPerturbScale;
			return this;
		}
		
		public CraterConfig setFullPeakSize(double fullPeakSize) {
			this.fullPeakSize = fullPeakSize;
			return this;
		}
		
		public CraterConfig setRingThreshold(double ringThreshold) {
			this.ringThreshold = ringThreshold;
			return this;
		}
		
		public CraterConfig setRingFunctMul(double ringFunctMul) {
			this.ringFunctMul = ringFunctMul;
			return this;
		}
		
		public static CraterConfig genBowlOnlyConfig(double size, double craterStrength, double perturbStrength, double perturbScale, double p1, double p2) {
			return new CraterConfig(size, craterStrength, perturbStrength, perturbScale, p1, p2, -10.0, 0.0, 2.1, 0.1, 0.4, 1000000, 1000000, 1.0);
		}
		
		public void serialize(DataOutputStream out) throws Exception {
			out.writeDouble(size);
			out.writeDouble(craterStrength);
			out.writeDouble(perturbStrength);
			out.writeDouble(perturbScale);
			out.writeDouble(p1);
			out.writeDouble(p2);
			out.writeDouble(floorHeight);
			out.writeDouble(ejectaStrength);
			out.writeDouble(ejectaStretch);
			out.writeDouble(ejectaPerturbStrength);
			out.writeDouble(ejectaPerturbScale);
			out.writeDouble(fullPeakSize);
			out.writeDouble(ringThreshold);
			out.writeDouble(ringFunctMul);
		}
		
		public static CraterConfig deserialize(DataInputStream in) throws Exception {
			CraterConfig res = new CraterConfig();
			res.size = in.readDouble();
			res.craterStrength = in.readDouble();
			res.perturbStrength = in.readDouble();
			res.perturbScale = in.readDouble();
			res.p1 = in.readDouble();
			res.p2 = in.readDouble();
			res.floorHeight = in.readDouble();
			res.ejectaStrength = in.readDouble();
			res.ejectaStretch = in.readDouble();
			res.ejectaPerturbStrength = in.readDouble();
			res.ejectaPerturbScale = in.readDouble();
			res.fullPeakSize = in.readDouble();
			res.ringThreshold = in.readDouble();
			res.ringFunctMul = in.readDouble();
			return res;
		}
		
		@Override
		public String toString() {
			String s = String.format("Size: %#.4f", this.size);
			s += String.format("\nStrength: %#.4f", this.craterStrength);
			s += String.format("\nPerturb Strength: %#.4f", this.perturbStrength);
			s += String.format("\nPerturb Scale: %#.4f", this.perturbScale);
			s += String.format("\nP1: %#.4f", this.p1);
			s += String.format("\nP2: %#.4f", this.p2);
			s += String.format("\nFloor Height: %#.4f", this.floorHeight);
			s += String.format("\nEjecta Strength: %#.4f", this.ejectaStrength);
			s += String.format("\nEjecta Stretch: %#.4f", this.ejectaStretch);
			s += String.format("\nEjecta Perturb Strength: %#.4f", this.ejectaPerturbStrength);
			s += String.format("\nEjecta Perturb Scale: %#.4f", this.ejectaPerturbScale);
			s += String.format("\nFull peak size: %#.4f", this.fullPeakSize);
			s += String.format("\nRing Threshold: %#.4f", this.ringThreshold);
			s += String.format("\nRing funct mul: %#.4f", this.ringFunctMul);
			return s;
		}
		
	}
	
	private TrigonometryCache trigCache;
	private PerlinNoise3D noise3d;
	private NoiseConfig perturbNoiseConfig;
	private int width, height;
	
	public CraterGenerator(int width, int height) {
		this.width = width;
		this.height = height;
		this.noise3d = new PerlinNoise3D(8, 8, 8);
		this.perturbNoiseConfig = new NoiseConfig(noise3d);
		this.trigCache = new TrigonometryCache(width, height);
	}
	
	public boolean genCrater(double[][] map, double[][] craterMap, int ymin, int ymax, double lat, double lon, CraterConfig cc, NoiseConfig peakNoise, Random rng) {
		return genCrater(map, craterMap, map, craterMap, ymin, ymax, lat, lon, cc, peakNoise, rng);
	}
	
	public boolean genCrater(double[][] baseMap, double[][] baseCraterMap, double[][] map, double[][] craterMap, int ymin, int ymax, double lat, double lon, CraterConfig cc, NoiseConfig peakNoise, Random rng) {
		double baseHeight = baseMap[(int)((lon + 180.0) / 360.0 * width)][(int)((lat + 90.0) / 180.0 * height)];
		double baseHeightCM = craterMap == null ? 0 : baseCraterMap[(int)((lon + 180.0) / 360.0 * width)][(int)((lat + 90.0) / 180.0 * height)];
		double th = cc.craterStrength * 1.1;
		if(cc.floorHeight >= 0) return false;
		if(cc.floorHeight > -1) {
			th *= Math.abs(cc.floorHeight);
		}
		if(baseHeight < th) return false;
		double s = 1.0 / cc.size;
		double perturbStrength = cc.perturbStrength * cc.size;
		double ejectaPerturbStrength = cc.ejectaPerturbStrength * cc.size;
		double ejectaStretch = 1.0 / cc.ejectaStretch;
		
		double enddist = -Math.log((1.0 / 65535.0) / cc.craterStrength) / (s * Math.log(cc.p2)) * Math.max(1, (width / 4096.0));
		
		noise3d.initialize(rng);
		
		lat %= 180.0;
		lon %= 360.0;
		
		perturbNoiseConfig.setIsRidged(false).setNoiseStrength(perturbStrength).setNoiseScale(cc.perturbScale).setDistortStrength(0.125).setNoiseOffset(0);
		double[] noise =  new double[width * 2 + 1];
		double[] eNoise = new double[width * 2 + 1];
		if(perturbStrength <= 0 || cc.perturbScale <= 0) Arrays.fill(noise, 0);
		else {
			for(int i = 0; i < width * 2 + 1; i++) {
				noise[i] =  NoiseUtils.sampleSpherableNoise((double)i / (double)(width * 2 + 1) * 24, 12 + 1, 24, 24, perturbNoiseConfig);
			}
		}
		if(ejectaPerturbStrength <= 0 || cc.ejectaPerturbScale <= 0) Arrays.fill(eNoise, 0);
		else {
			perturbNoiseConfig.setNoiseStrength(ejectaPerturbStrength).setNoiseScale(cc.ejectaPerturbScale);
			for(int i = 0; i < width * 2 + 1; i++) {
				eNoise[i] = NoiseUtils.sampleSpherableNoise((double)i / (double)(width * 2 + 1) * 24, 12 - 1, 24, 24, perturbNoiseConfig);
			}
		}
		
		double sinLat = Math.sin(Math.toRadians(lat));
		double cosLat = Math.cos(Math.toRadians(lat));
		
		double[] diffLongCache = new double[width];
		for(int i = 0; i < width; i++) {
			double longitude = (double)(i - width / 2) / (width / 2.0) * 180.0;
			diffLongCache[i] = Math.cos(Math.toRadians(Math.abs(lon - longitude)));
		}
		
		for(int j = ymin; j < ymax; j++) {
			double latitude = (double)(j - height / 2) / (height / 2.0) * 90.0;
			
			double dist = Maths.gcDistance(lat, lon, latitude, lon);
			dist *= width / 2;
			if(Math.abs(dist) >= enddist) {
				continue;
			}
			
			double sinLatitude = Math.sin(Math.toRadians(latitude));
			double cosLatitude = Math.cos(Math.toRadians(latitude));
			
			double a = sinLat * sinLatitude;
			
			int istart = 0;
			int iend = width;
			//int stepSize = Math.max(1, width / 25);
			//boolean bb = false;
			// Find starting point
			/*if(enddist < 180) {
				for(int i = 0; i < width; i += stepSize) {
					double longitude = (double)(i - width / 2) / (width / 2.0) * 180.0;
					dist = SphereUtils.distance(lat, lon, latitude, longitude);
					dist *= 2048;
					if(!bb && Math.abs(dist) < enddist) {
						istart = Math.max(0, i - stepSize);
						bb = true;
					}
					if(bb && Math.abs(dist) >= enddist) {
						iend = i + stepSize;
						break;Continue on laptop; center of mass position is weighted average
					}
				}
				istart = Math.max(0, istart);
				iend = Math.min(width, iend);
			}*/ // This is broken. Not sure why, but not using this doesn't loose me too much performance.
			
			for(int i = istart; i < iend; i++) {
				double longitude = (double)(i - width / 2) / (width / 2.0) * 180.0;
				
				double b = cosLat * cosLatitude * diffLongCache[i];
				dist = trigCache.fastAcos(a + b) / Math.PI;
				dist *= 2048;
				
				double crater_funct_x = dist;
				if(Math.abs(crater_funct_x) >= enddist) continue;
				
				double angle = Maths.angleFromCoordinate(lat, lon, latitude, longitude);
				
				if(Double.isNaN(angle)) angle = 0.1; // This always happens exactly in the dead-center of the crater. Workaround is to set the angle to some fixed value.
				double h =  noise[(int)(angle / 360.0 * width * 2)];
				double eh = eNoise[(int)(angle / 360.0 * width * 2)];
				
				double ringFunctMul = 1.0;
				ringFunctMul *= cc.ringFunctMul;
				
				double[] func = craterFunct(crater_funct_x, crater_funct_x + h, crater_funct_x + eh, eh, s, cc.p1, cc.p2, map[i][j], baseHeight, cc.craterStrength, cc.ejectaStrength, ejectaStretch, ringFunctMul, cc.floorHeight, peakNoise, latitude, longitude, cc.fullPeakSize, cc.ringThreshold, baseHeightCM, craterMap == null ? 0 : craterMap[i][j]);
				
				if(craterMap != null) craterMap[i][j] = func[1];
				map[i][j] = func[0];
			}
		}
		return true;
	}
	
	// Thanks to www.iquilezles.org/www/articles/smin/smin.htm and https://www.youtube.com/watch?v=lctXaT9pxA0
	public static double smoothMin(double a, double b, double k) {
		if(k == 0) return Math.min(a, b);
		double h = Math.max(0, Math.min(1, (b - a + k) / (2 * k)));
		return a * h + b * (1.0 - h) - k * h * (1.0 - h);
	}
	
	public double[] craterFunct(double x1, double x2, double ejectaX, double ejectaPerturb, double s, double p1, double p2, double terrainHeight, double baseHeight, double craterStrength, double ejectaStrength, double ejectaStretch, double ringFunctMul, double floorHeight, NoiseConfig peakNoise, double lat, double lon, double fullPeakSize, double ringThreshold, double baseHeightCM, double terrainHeightCM) {
		double rS = 1.0 / s;
		
		double peak = 0;
		if(ejectaStrength > 0) {
			if(rS <= ringThreshold) {
				double p = Math.min(1, rS / fullPeakSize);
				double x_a = Math.abs(ejectaX * ejectaStretch);
				peak = x_a * (1.0 / p) < Math.PI ? (Math.cos(x_a * (1.0 / p)) + 1.0) * p * ejectaStrength : 0;
			}else {
				double ringS = 1.0 / s;
				double rS2 = ringFunctMul / s;
				double m1 = Math.max(0, Math.min(1, (ringS - 10.0) / 80.0));
				double p = rS2 * ejectaStretch * (0.5 * m1) - Math.max(0, 1.0 - Math.min(1.0, (rS2 - 10.0) * 0.1)) * Math.PI;
				double peakX = Math.abs(ejectaX * ejectaStretch) - p;
				peak = ejectaStrength > 0 && peakX <= Math.PI && peakX >= -Math.PI ? (Math.cos(peakX) + 1.0) * ejectaStrength : 0;
			}
			if(peak >= 1e-8) {
				double n = NoiseUtils.sampleSpherableNoise(lon + 180.0, lat + 90.0, 360.0, 180.0, peakNoise);
				peak *= n;
			}
		}
		
		double absSX = Math.abs(s * x1);
		double bowl = Math.pow(absSX, p1) - 1.0;
		bowl = smoothMin(bowl, floorHeight, -0.15);
			
		double lip = Math.pow(p2, -Math.abs(s * x2));
		return new double[] {smoothMin((bowl + peak) * craterStrength + baseHeight, lip * craterStrength + terrainHeight, s < 4 ? 0 : 0.05), smoothMin(bowl * craterStrength + baseHeightCM, lip * craterStrength + terrainHeightCM, s < 4 ? 0 : 0.05)};
	}
	
	public static void main(String[] args) {
		try {
			double[][] testImg = new double[4096][2048];
			for(int i = 0; i < testImg.length; i++) Arrays.fill(testImg[i], 0.75);
			BufferedImage testRes = new BufferedImage(4096, 2048, BufferedImage.TYPE_INT_RGB);
			
			RanMT rng = new RanMT().seedCompletely();
			OctaveNoise3D mountainsNoise =          new OctaveNoise3D(24, 24, 24, 6, 2.0, 0.5);
			mountainsNoise.initialize(rng);
			long startTime = System.currentTimeMillis();
			NoiseConfig nc = new NoiseConfig(mountainsNoise, true, 3.8, 0.17, 0.6, 0.0, 0.0);
			CraterGenerator generator = new CraterGenerator(testImg.length, testImg[0].length);
			CraterConfig cc = new CraterConfig(5, 0.3, 0.2, 0.4, 1.0, 4.8, -10.0, 0.25, 4.2, 0.1, 0.4, 30, 75, 1.0);
			generator.genCrater(testImg, null, 0, testImg[0].length, 0, 0, cc, nc, rng);
			cc.setSize(15);
			generator.genCrater(testImg, null, 0, testImg[0].length, 0, 2.5, cc, nc, rng);
			cc.setSize(32);
			generator.genCrater(testImg, null, 0, testImg[0].length, 0, 7.5, cc, nc, rng);
			cc.setSize(35).setEjectaPerturbScale(1);
			generator.genCrater(testImg, null, 0, testImg[0].length, 0, 17.5, cc, nc, rng);
			cc.setSize(116).setEjectaStretch(8.4);
			generator.genCrater(testImg, null, 0, testImg[0].length, 0, 37.5, cc, nc, rng);
			//genBowlCrater(testImg, 0, 10, 500, 0.3, 0.25, 0.5, 0.48, 3.6, 4.8, rng);
			//genFlattenedCrater(testImg, 25, 50, 500, 0.4, 0.25, 0.5, 0.48, 8, rng);
			System.err.println(System.currentTimeMillis() - startTime);
			
			for(int i = 0; i < 4096; i++) {
				for(int j = 0; j < 2048; j++) {
					int col = (int)(testImg[i][j] * 255.0);
					col = Math.max(0, Math.min(255, col));
					testRes.setRGB(i, j, col | (col << 8) | (col << 16));
				}
			}
			ImageIO.write(testRes, "png", new File("craters.png"));
			
			testRes = new BufferedImage(225, 50, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = (Graphics2D)testRes.getGraphics();
			g.setColor(Color.DARK_GRAY);
			g.fillRect(0, 0, testRes.getWidth(), testRes.getHeight());
			g.setColor(Color.WHITE);
			for(int i = 0; i < 225; i++) {
				//double h = testImg[512 - 112 + i][1024];
				double h = Maths.biasFunction(i / 255.0, 0.5);
				int h2 = (int)(h * 50.0);
				g.drawLine(i, 49, i, 50 - h2 - 1);
			}
			ImageIO.write(testRes, "png", new File("crosssection.png"));
			
			testRes = new BufferedImage(900, 200, BufferedImage.TYPE_INT_RGB);
			OctaveNoise2D noise2d = new OctaveNoise2D(4, 4, 4, 0.5, 2.0);
			noise2d.initialize(rng);
			int prevy = Integer.MAX_VALUE;
			g = (Graphics2D)testRes.getGraphics();
			g.setColor(Color.WHITE);
			nc = new NoiseConfig(noise2d, false, 1.0, 1.0 / 12.0, 0.25, 0.5, 0.0);
			for(int i = 0; i < testRes.getWidth(); i++) {
				double h = NoiseUtils.sampleSpherableNoise(i, 450, 900, 900, nc);
				if(h < 0) continue;
				int y = testRes.getHeight() - (int)(h * testRes.getHeight()) - 1;
				if(prevy != Integer.MAX_VALUE) g.drawLine(i - 1, prevy, i, y);
				prevy = y;
			}
			ImageIO.write(testRes, "png", new File("noise1d.png"));
		}catch(Exception e) {
			System.err.println("Error: ");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public int getWidth() {
		return this.width;
	}
	
	public int getHeight() {
		return this.height;
	}
}
