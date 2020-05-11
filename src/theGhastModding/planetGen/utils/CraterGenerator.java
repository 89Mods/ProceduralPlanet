package theGhastModding.planetGen.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Random;

import javax.imageio.ImageIO;

import edu.cornell.lassp.houle.RngPack.RanMT;
import theGhastModding.planetGen.noise.NoiseUtils;
import theGhastModding.planetGen.noise.OctaveNoise2D;
import theGhastModding.planetGen.noise.PerlinNoise3D;

public class CraterGenerator {
	
	private static boolean genCrater(double[][] map, double lat, double lon, int size, double craterStrength, double perturbStrength, double perturbScale, double s, double p1, double p2, double craterOffset, Random rng) {
		double baseHeight = map[(int)((lon + 180.0) / 360.0 * map.length)][(int)((lat + 90.0) / 180.0 * map[0].length)];
		if(baseHeight < craterStrength * 1.1) return false;
		
		double enddist = -Math.log(0.0035 / craterStrength) / (s * Math.log(p2));
		
		PerlinNoise3D noise3d = new PerlinNoise3D(rng, 8, 8, 8);
		
		lat %= 180.0;
		lon %= 360.0;
		
		double[] noise = new double[map.length * 2 + 1];
		for(int i = 0; i < map.length * 2 + 1; i++) {
			noise[i] = NoiseUtils.sampleSpherableNoise(noise3d, (double)i / (double)(map.length * 2 + 1) * 12, 6, 12, 12, perturbScale, perturbScale, 0.125) * perturbStrength;
		}
		
		double sinLat = Math.sin(Math.toRadians(lat));
		double cosLat = Math.cos(Math.toRadians(lat));
		
		for(int j = 0; j < map[0].length; j++) {
			double latitude = (double)(j - map[0].length / 2) / (map[0].length / 2.0) * 90.0;
			
			double dist = SphereUtils.distance(lat, lon, latitude, lon);
			dist *= map.length / 2;
			if(Math.abs(dist / (size / 2.0) * 6) >= enddist) {
				continue;
			}
			
			double sinLatitude = Math.sin(Math.toRadians(latitude));
			double cosLatitude = Math.cos(Math.toRadians(latitude));
			
			double a = sinLat * sinLatitude;
			
			int istart = 0;
			int iend = map.length;
			int stepSize = map.length / 25;
			boolean bb = false;
			// Find starting point
			for(int i = 0; i < map.length; i += stepSize) {
				double longitude = (double)(i - map.length / 2) / (map.length / 2.0) * 180.0;
				dist = SphereUtils.distance(lat, lon, latitude, longitude);
				dist *= map.length / 2;
				if(!bb && Math.abs(dist / (size / 2.0) * 6) < enddist) {
					istart = Math.max(0, i - stepSize);
					bb = true;
				}
				if(bb && Math.abs(dist / (size / 2.0) * 6) >= enddist) {
					iend = i;
					break;
				}
			}
			
			for(int i = istart; i < iend; i++) {
				double longitude = (double)(i - map.length / 2) / (map.length / 2.0) * 180.0;
				
				double diffLong = Math.toRadians(Math.abs(lon - longitude));
				
				double b = cosLat * cosLatitude * Math.cos(diffLong);
				dist = Math.acos(a + b) / Math.PI;
				dist *= map.length / 2;
				
				double crater_funct_x = dist / (size / 2.0) * 6;
				if(Math.abs(crater_funct_x) >= enddist) continue;
				
				double angle = SphereUtils.angleFromCoordinate(lat, lon, latitude, longitude);
				if(Double.isNaN(angle)) angle = 0.1; // This always happens exactly in the dead-center of the crater. Workaround is to set the angle to some fixed value.
				double h = noise[(int)(angle / 360.0 * map.length * 2)];
				
				crater_funct_x += h;
				
				double func = craterFunct(crater_funct_x, s, p1, p2, map[i][j], baseHeight, craterStrength, craterOffset);
				
				map[i][j] = func;
			}
		}
		return true;
	}
	
	public static boolean genBowlCrater(double[][] map, double lat, double lon, int size, double craterStrength, double perturbStrength, double perturbScale, double s, double p1, double p2, Random rng) {
		return genCrater(map, lat, lon, size, craterStrength, perturbStrength, perturbScale, s, p1, p2, 0.0, rng);
	}
	
	public static boolean genFlattenedCrater(double[][] map, double lat, double lon, int size, double craterStrength, double perturbStrength, double perturbScale, double s, double p2, Random rng) {
		double p1 = 7.0;
		return genCrater(map, lat, lon, size, craterStrength, perturbStrength, perturbScale, s, p1, p2, 0.5, rng);
	}
	
	public static double craterFunct(double x, double s, double p1, double p2, double terrainHeight, double baseHeight, double craterStrength, double craterOffset) {
		double bowl = Math.pow(Math.abs(s * x), p1) - 1.0 + craterOffset * craterStrength;
		double lip = Math.pow(p2, -Math.abs(s * x));
		return Math.min(bowl * craterStrength + baseHeight, lip * craterStrength + terrainHeight);
	}
	
	public static void main(String[] args) {
		try {
			double[][] testImg = new double[4096][2048];
			for(int i = 0; i < testImg.length; i++) Arrays.fill(testImg[i], 0.5);
			BufferedImage testRes = new BufferedImage(4096, 2048, BufferedImage.TYPE_INT_RGB);
			
			RanMT rng = new RanMT().seedCompletely();
			long startTime = System.currentTimeMillis();
			genBowlCrater(testImg, 0, 0, 500, 0.3, 0.25, 0.5, 0.48, 3.6, 4.8, rng);
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
			g.setColor(Color.WHITE);
			for(int i = 0; i < 225; i++) {
				double h = testImg[512 - 112 + i][1024];
				int h2 = (int)(h * 50.0);
				g.drawLine(i, 49, i, 50 - h2 - 1);
			}
			ImageIO.write(testRes, "png", new File("crosssection.png"));
			
			testRes = new BufferedImage(900, 200, BufferedImage.TYPE_INT_RGB);
			OctaveNoise2D noise2d = new OctaveNoise2D(rng, 4, 4, 4, 0.5, 2.0);
			int prevy = Integer.MAX_VALUE;
			g = (Graphics2D)testRes.getGraphics();
			g.setColor(Color.WHITE);
			for(int i = 0; i < testRes.getWidth(); i++) {
				double h = NoiseUtils.sampleSpherableNoise(noise2d, i, 450, 900, 900, 1.0 / 12.0, 1.0 / 12.0, 0.25) + 0.5;
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
	
}
