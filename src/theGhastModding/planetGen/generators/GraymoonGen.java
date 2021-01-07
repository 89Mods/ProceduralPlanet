package theGhastModding.planetGen.generators;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Random;

import javax.imageio.ImageIO;

import edu.cornell.lassp.houle.RngPack.RanMT;
import theGhastModding.planetGen.noise.NoiseConfig;
import theGhastModding.planetGen.utils.NoisemapGenerator;
import theGhastModding.planetGen.noise.OctaveNoise3D;
import theGhastModding.planetGen.noise.OctaveWorley;
import theGhastModding.planetGen.utils.CraterDistributer;
import theGhastModding.planetGen.utils.CraterDistributer.CraterDistributionSettings;
import theGhastModding.planetGen.utils.CraterGenerator;
import theGhastModding.planetGen.utils.CraterGenerator.CraterConfig;
import theGhastModding.planetGen.utils.MapUtils;
import theGhastModding.planetGen.utils.ProgressBars;

public class GraymoonGen {
	
	public static class GraymoonGenSettings {
		public int width = 4096;
		public int height = 2048;
		public int planetRadius = 200000;
		
		public double mariaLatitudeRange = 80;
		public double mariaLongitudeRange = 110;
		public double mariaFadeRange = 30;
		public int mariaCraterCount = 6;
		
		public int smallCraterCount = 8192+128;
		public int hugeCraterCount = 3;
		
		public double craterMaxsize = 128;
		public double craterMinsize = 4;
		public double craterMaxstrength = 0.6;
		public double craterMinstrength = 0.1;
		
		public double mariaCraterMaxsize = 32;
		public double mariaCraterMinsize = 4;
		public double mariaCraterMaxstrength = 0.2;
		public double mariaCraterMinstrength = 0.1;
		
		public int craterFlattenedStart = 14;
		public int craterFlattenedEnd = 28;
		
		public CraterConfig bowlCraterConfig      = new CraterConfig(0, 0, 0.2, 0.4, 1.0, 3.8, -10.0, 0.3, 2.1, 0.1, 0.4, 30, 96, 1.0);
		public CraterConfig flattenedCraterConfig = new CraterConfig(0, 0, 0.125, 0.5, 1.0, 4.8, -0.5, 0.35, 6.1, 0.15, 0.75, 30, 96, 0.9);
		public CraterConfig mariaCraterConfig     = new CraterConfig(0, 0, 0.125, 0.5, 1.0, 4.8, -10.0, 0.0, 2.1, 0.1, 0.4, 1000000, 1000000, 1.0);
		
		public NoiseConfig mariaNoise                = new NoiseConfig(new OctaveNoise3D(16, 16, 16, 5, 2.0, 0.6)).setIsRidged(false).setNoiseStrength(3.0).setNoiseScale(2.0).setDistortStrength(0.25).setNoiseOffset(0.1);
		public NoiseConfig mountainNoise             = new NoiseConfig(new OctaveNoise3D(16, 16, 16, 4, 2.0, 0.6)).setIsRidged(false).setNoiseStrength(1.0).setNoiseScale(0.54).setDistortStrength(0.25).setNoiseOffset(0.375);
		public NoiseConfig groundNoiseLargeDetail    = new NoiseConfig(new OctaveNoise3D(16, 16, 16, 8, 2.0, 0.6)).setIsRidged(false).setNoiseStrength(0.75).setNoiseScale(0.7).setDistortStrength(0.5).setNoiseOffset(0.25);
		public NoiseConfig groundNoiseMediumDetail   = new NoiseConfig(new OctaveNoise3D(24, 24, 24, 6, 2.0, 0.6)).setIsRidged(false).setNoiseStrength(0.2).setNoiseScale(0.3).setDistortStrength(0.75).setNoiseOffset(0.2);
		public NoiseConfig groundNoiseSmallDetail    = new NoiseConfig(new OctaveNoise3D(24, 24, 24, 6, 2.0, 0.6)).setIsRidged(false).setNoiseStrength(0.075).setNoiseScale(0.15).setDistortStrength(0.25).setNoiseOffset(0.15);
		public NoiseConfig mountainsNoise            = new NoiseConfig(new OctaveNoise3D(24, 24, 24, 10, 2.0, 0.65)).setIsRidged(true).setNoiseStrength(1.35).setNoiseScale(0.72).setDistortStrength(0.43).setNoiseOffset(0);
		public NoiseConfig craterMountainsNoise      = new NoiseConfig(new OctaveNoise3D(24, 24, 24, 7, 2.0, 0.6)).setIsRidged(true).setNoiseStrength(1.5).setNoiseScale(0.17).setDistortStrength(0.6).setNoiseOffset(0);
		public NoiseConfig colorNoise                = new NoiseConfig(new OctaveWorley(16, 16, 16, 10, 2.0, 0.75)).setIsRidged(true).setNoiseStrength(1.25).setNoiseScale(0.75).setDistortStrength(0.75).setNoiseOffset(0);
		public NoiseConfig secondColorNoise          = new NoiseConfig(new OctaveNoise3D(16, 16, 16, 3, 2.0, 0.6)).setIsRidged(true).setNoiseStrength(1.0).setNoiseScale(1.15).setDistortStrength(0.25).setNoiseOffset(0.325);
		public NoiseConfig craterRimColorNoise       = new NoiseConfig(new OctaveNoise3D(16, 16, 16, 3, 2.0, 0.5)).setIsRidged(false).setNoiseStrength(2.4).setNoiseScale(0.75).setDistortStrength(0.25).setNoiseOffset(0.275);
		
		public double[] normalColor      = MapUtils.RGB(new Color(85, 85, 85));
		public double[] mountainsColor   = MapUtils.RGB(new Color(101, 101, 101));
		public double[] mariasColor      = MapUtils.RGB(new Color(52, 52, 52));
		public double[] secondaryColor   = MapUtils.RGB(new Color(32, 32, 32));
		
		public double[] craterRimFades          = new double[] {0.4, 0.4, 0.4};
		public double[] mariaCraterRimFades     = new double[] {0.25, 0.25, 0.25};
		public double   craterRimFadeStart      = 0.25;
		public double   craterRimFadeEnd        = 0.55;
		public double   mariaCraterRimFadeStart = 0.0;
		public double   mariaCraterRimFadeEnd   = 0.15;
		
		public double[] normalBiomeColor    = MapUtils.RGB(new Color(150, 150, 150));
		public double[] mountainsBiomeColor = MapUtils.RGB(new Color(180, 180, 180));
		public double[] mariasBiomeColor    = MapUtils.RGB(new Color(80, 80, 80));
		public double[] biomeColorSecondary = MapUtils.RGB(new Color(200, 200, 200));
		
		public GraymoonGenSettings() {
			
		}
		
		public String toString() {
			String s = "Width: " + Integer.toString(width) + "\n";
			s += "Height: " + Integer.toString(height) + "\n";
			s += "Radius: " + Integer.toString(planetRadius) + "\n";
			s += String.format("Maria Latitude Range: %#.4f\n", this.mariaLatitudeRange);
			s += String.format("Maria Longitude Range: %#.4f\n", this.mariaLongitudeRange);
			s += String.format("Maria Fade Range: %#.4f\n", this.mariaFadeRange);
			s += "Maria Shape Crater Count: " + Integer.toString(this.mariaCraterCount) + "\n";
			s += "Small Crater Count: " + Integer.toString(this.smallCraterCount) + "\n";
			s += "Huge Crater Count: " + Integer.toString(this.hugeCraterCount) + "\n";
			s += String.format("Crater Max Size: %#.4f\n", this.craterMaxsize);
			s += String.format("Crater Min Size: %#.4f\n", this.craterMinsize);
			s += String.format("Crater Max Strength: %#.4f\n", this.craterMaxstrength);
			s += String.format("Crater Min Strength: %#.4f\n", this.craterMinstrength);
			s += "Flattened Crater Start Size: " + Integer.toString(this.craterFlattenedStart) + "\n";
			s += "Flattened Crater End Size: " + Integer.toString(this.craterFlattenedEnd) + "\n";
			s += "Bowl Crater Configuration:\n";
			s += "\t" + bowlCraterConfig.toString().replace("\n", "\n\t") + "\n";
			s += "Flattened Crater Configuration:\n";
			s += "\t" + flattenedCraterConfig.toString().replace("\n", "\n\t") + "\n";
			s += "Maria Crater Configuration:\n";
			s += "\t" + mariaCraterConfig.toString().replace("\n", "\n\t") + "\n";
			s += "Maria Noise\n";
			s += "\t" + mariaNoise.toString().replace("\n", "\n\t") + "\n";
			s += "Mountain biome Noise\n";
			s += "\t" + mountainNoise.toString().replace("\n", "\n\t") + "\n";
			s += "Large Detail Ground Noise\n";
			s += "\t" + groundNoiseLargeDetail.toString().replace("\n", "\n\t") + "\n";
			s += "Medium Detail Ground Noise\n";
			s += "\t" + groundNoiseMediumDetail.toString().replace("\n", "\n\t") + "\n";
			s += "Small Detail Ground Noise\n";
			s += "\t" + groundNoiseSmallDetail.toString().replace("\n", "\n\t") + "\n";
			s += "Mountains Noise\n";
			s += "\t" + mountainsNoise.toString().replace("\n", "\n\t") + "\n";
			s += "Crater Mountains Noise\n";
			s += "\t" + craterMountainsNoise.toString().replace("\n", "\n\t") + "\n";
			s += "Color Noise\n";
			s += "\t" + colorNoise.toString().replace("\n", "\n\t") + "\n";
			if(secondaryColor != null) {
				s += "Secondary Color Noise\n";
				s += "\t" + secondColorNoise.toString().replace("\n", "\n\t") + "\n";
			}
			s += "Crater Rim Color Noise\n";
			s += "\t" + craterRimColorNoise.toString().replace("\n", "\n\t") + "\n";
			s += String.format("Base color: %#.4f,%#.4f,%#.4f\n", this.normalColor[0], this.normalColor[1], this.normalColor[2]);
			s += String.format("Mountains color: %#.4f,%#.4f,%#.4f\n", this.mountainsColor[0], this.mountainsColor[1], this.mountainsColor[2]);
			s += String.format("Marias color: %#.4f,%#.4f,%#.4f\n", this.mariasColor[0], this.mariasColor[1], this.mariasColor[2]);
			if(secondaryColor != null) s += String.format("Secondary color: %#.4f,%#.4f,%#.4f\n", this.secondaryColor[0], this.secondaryColor[1], this.secondaryColor[2]);
			s += String.format("Crater Rim Fades: %#.4f,%#.4f,%#.4f\n", this.craterRimFades[0], this.craterRimFades[1], this.craterRimFades[2]);
			s += String.format("Maria Crater Rim Fades: %#.4f,%#.4f,%#.4f\n", this.mariaCraterRimFades[0], this.mariaCraterRimFades[1], this.mariaCraterRimFades[2]);
			s += String.format("Crater Rim Fade Start: %#.4f\n", this.craterRimFadeStart);
			s += String.format("Crater Rim Fade End: %#.4f\n", this.craterRimFadeEnd);
			s += String.format("Maria Crater Rim Fade Start: %#.4f\n", this.mariaCraterRimFadeStart);
			s += String.format("Maria Crater Rim Fade End: %#.4f\n", this.mariaCraterRimFadeEnd);
			s += String.format("Biome color base: %#.4f,%#.4f,%#.4f\n", this.normalBiomeColor[0], this.normalBiomeColor[1], this.normalBiomeColor[2]);
			s += String.format("Biome color mountains: %#.4f,%#.4f,%#.4f\n", this.mountainsBiomeColor[0], this.mountainsBiomeColor[1], this.mountainsBiomeColor[2]);
			s += String.format("Biome color marias: %#.4f,%#.4f,%#.4f\n", this.mariasBiomeColor[0], this.mariasBiomeColor[1], this.mariasBiomeColor[2]);
			if(biomeColorSecondary != null) s += String.format("Biome color secondary: %#.4f,%#.4f,%#.4f", this.biomeColorSecondary[0], this.biomeColorSecondary[1], this.biomeColorSecondary[2]);
			return s;
		}
		
	}
	
	public static GeneratorResult generate(Random rng, GraymoonGenSettings settings, boolean debugProgress, boolean debugSteps, boolean test) throws Exception {
		GeneratorResult result = new GeneratorResult();
		
		final int width = settings.width;
		final int height = settings.height;
		final double resMul = 200000.0 / (double)settings.planetRadius * 0.85;
		
		double[][] marias        = new double[width][height];
		float[][]  mountainMap   = new float[width][height];
		double[][] finalNoiseMap = new double[width][height];
		
		double[][] tempMap        = new double[width][height];
		double[][] tempMap2       = new double[width][height];
		double[][] craterMap1     = new double[width][height];
		double[][] craterMap2     = new double[width][height];
		double[][] mariaNoiseMuls = new double[width][height];
		
		if(debugProgress) System.out.println("Marias");
		NoisemapGenerator.genNoisemap(rng, marias, settings.mariaNoise, null, resMul, debugProgress);
		for(int i = 0; i < width; i++) {
			double longitude = (double)(i - width / 2) / (width / 2.0) * 180.0;
			double mul = 1;
			if(settings.mariaLongitudeRange < 180) {
				mul = Math.abs(longitude);
				if(mul > settings.mariaLongitudeRange - settings.mariaFadeRange && mul < settings.mariaLongitudeRange) {
					mul = 1.0 - (mul - (settings.mariaLongitudeRange - settings.mariaFadeRange)) / settings.mariaFadeRange;
				}else if(mul >= settings.mariaLongitudeRange) mul = 0;
				else if(mul <= settings.mariaLongitudeRange - settings.mariaFadeRange) mul = 1;
			}
			for(int j = 0; j < height; j++) {
				double latitude = (double)(j - height / 2) / (height / 2.0) * 90.0;
				double mul2 = 1;
				if(settings.mariaLatitudeRange < 90) {
					mul2 = Math.abs(latitude);
					if(mul2 > settings.mariaLatitudeRange - settings.mariaFadeRange && mul2 < settings.mariaLatitudeRange) {
						mul2 = 1.0 - (mul2 - (settings.mariaLatitudeRange - settings.mariaFadeRange)) / settings.mariaFadeRange;
					}else if(mul2 >= settings.mariaLatitudeRange) mul2 = 0;
					else if(mul2 <= settings.mariaLatitudeRange - settings.mariaFadeRange) mul2 = 1;
				}
				
				marias[i][j] = marias[i][j] * mul * mul2;
				marias[i][j] *= marias[i][j];
				marias[i][j] = Math.max(0, Math.min(1, marias[i][j]));
				
			}
		}
		
		for(int i = 0; i < marias.length; i++) {
			for(int j = 0; j < marias[0].length; j++) {
				marias[i][j] = (1.0 - marias[i][j]) * 0.4;
			}
		}
		
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				if(marias[i][j] < 0.23) {
					marias[i][j] = 0.23;
				}else {
					marias[i][j] *= ((marias[i][j] - 0.23) * 3.0) + 1.0;
					marias[i][j] = Math.min(0.4, marias[i][j]);
				}
			}
		}
		
		if(debugProgress) ProgressBars.printBar();
		for(int i = 0; i < width; i++) for(int j = 0; j < height; j++) tempMap[i][j] = 0.4;
		CraterConfig mariaShapeCraterConfig = new CraterConfig();
		mariaShapeCraterConfig.setPerturbStrength(0.25).setPerturbScale(0.35).setP1(1.0).setP2(8.4).setFloorHeight(-0.5);
		mariaShapeCraterConfig.setEjectaStrength(1).setEjectaPerturbScale(0).setEjectaStretch(0).setEjectaPerturbStrength(0);
		mariaShapeCraterConfig.setFullPeakSize(10).setRingThreshold(128).setRingFunctMul(1);
		int attemptCntr = 0;
		CraterGenerator craterGen = new CraterGenerator(width, height);
		for(int i = 0; i < settings.mariaCraterCount; i++) {
			if(debugProgress) ProgressBars.printProgress(i, settings.mariaCraterCount);
			double lat = (rng.nextDouble() * 2 - 1) * (settings.mariaLatitudeRange - settings.mariaFadeRange / 2);
			double lon = (rng.nextDouble() * 2 - 1) * (settings.mariaLongitudeRange - settings.mariaFadeRange / 2);
			int px = (int)((lon + 180.0) / 360.0 * width);
			int py = (int)((lat + 90.0) / 180.0 * height);
			double val = marias[px][py];
			if(val > 0.3 && val < 0.4) {
				mariaShapeCraterConfig.setSize(200 + rng.nextInt(32)).setCraterStrength(0.5);
				craterGen.genCrater(tempMap, null, 0, tempMap[0].length, lat, lon, mariaShapeCraterConfig, null, rng);
			}else {
				i--;
				attemptCntr++;
				if(attemptCntr >= 128) break;
				continue;
			}
		}
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				tempMap[i][j] = Math.max(0.23, tempMap[i][j]);
				marias[i][j] = Math.min(marias[i][j], tempMap[i][j]);
			}
		}
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				mariaNoiseMuls[i][j] = Math.max(0, Math.min(1, (marias[i][j] - 0.23) * 5.882352941));
			}
		}
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				//Post-scale depth because I'm too lazy to re-do all of the above code
				if(marias[i][j] > 0.25) {
					marias[i][j] = (marias[i][j] - 0.25) * 6.6666666;
					marias[i][j] = CraterDistributer.biasFunction(marias[i][j], -0.65);
					marias[i][j] *= marias[i][j];
					marias[i][j] = marias[i][j] / 6.666666 + 0.25;
				}
				
				marias[i][j] /= 0.4;
				marias[i][j] = marias[i][j] * 0.3 + 0.1;
			}
		}
		if(debugProgress) {
			ProgressBars.printProgress(width - 1, width);
			ProgressBars.finishProgress();
		}
		
		if(debugSteps) ImageIO.write(MapUtils.renderMap(marias), "png", new File("marias.png"));
		
		if(debugProgress) System.out.println("Biomes");
		NoisemapGenerator.genNoisemap(rng, tempMap, settings.mountainNoise, null, resMul, debugProgress);
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				double val = tempMap[i][j];
				val = Math.max(0, Math.min(1, Math.abs(val)));
				if(val > 0.44) {
					double h = val - 0.44;
					h = h * 1.785 * 5.0;
					mountainMap[i][j] = (float)h;
					mountainMap[i][j] *= (float)mariaNoiseMuls[i][j];
				}else mountainMap[i][j] = 0;
			}
		}
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				mountainMap[i][j] = Math.min(1, mountainMap[i][j]);
			}
		}
		
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				int r,g,b;
				r = g = b = 0;
				if(mountainMap[i][j] > 0) {
					double mul = (1.0 - Math.max(0, Math.min(1, mountainMap[i][j])));
					g = (int)(g * mul);
					b = (int)(b * mul);
					r = (int)((1.0 - mul) * 255);
				}
				r = Math.max(0, Math.min(255, r));
				g = Math.max(0, Math.min(255, g));
				b = Math.max(0, Math.min(255, b));
				img.setRGB(i, j, b | (g << 8) | (r << 16));
			}
		}
		if(debugSteps) ImageIO.write(img, "png", new File("continents.png"));
		
		if(debugProgress) System.out.println("Ground");
		NoisemapGenerator.genNoisemap(rng, finalNoiseMap, settings.groundNoiseLargeDetail, null, resMul, debugProgress);
		double inMariaMul = 0.15;
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				double mariaMul = mariaNoiseMuls[i][j];
				
				if(mariaMul > 0) {
					double val = finalNoiseMap[i][j];
					val = Math.abs(val);
					val *= 0.4;
					val *= mariaMul;
					finalNoiseMap[i][j] = val;
				}
				if(mariaMul < inMariaMul) {
					mariaMul = inMariaMul - mariaMul;
					double val = finalNoiseMap[i][j];
					val = Math.abs(val);
					val *= 0.3;
					val *= inMariaMul;
					finalNoiseMap[i][j] = val;
				}
			}
		}
		if(debugSteps) ImageIO.write(MapUtils.renderMap(finalNoiseMap), "png", new File("ground.png"));
		
		if(debugProgress) System.out.println("Mountains");
		for(int i = 0; i < width; i++) for(int j = 0; j < height; j++) tempMap2[i][j] = mountainMap[i][j];
		NoisemapGenerator.genNoisemap(rng, tempMap, settings.mountainsNoise, tempMap2, resMul, debugProgress);
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				finalNoiseMap[i][j] += tempMap[i][j] + Math.min(0.2, settings.mariaCraterMaxstrength);
			}
		}
		if(debugSteps) ImageIO.write(MapUtils.renderMap(tempMap), "png", new File("mountains.png"));
		
		if(debugSteps) ImageIO.write(MapUtils.renderMap(finalNoiseMap), "png", new File("complex.png"));
		
		if(debugProgress) {
			System.out.println("Craters");
		}
		boolean[][] craterDistr = new boolean[1024][512];
		boolean[][] mariaCraterDistr = new boolean[1024][512];
		int mariaCnt = 0;
		for(int i = 0; i < 1024; i++) {
			int ix = (int)((double)i / 1024.0 * width);
			for(int j = 0; j < 512; j++) {
				int iy = (int)((double)j / 512.0 * height);
				double val = marias[ix][iy];
				craterDistr[i][j] = val >= 0.36;
				mariaCraterDistr[i][j] = val < 0.36;
				if(val < 0.35) mariaCnt++;
			}
		}
		for(int i = 0; i < width; i++) {
			Arrays.fill(craterMap1[i], 0.0);
			Arrays.fill(craterMap2[i], 0.0);
		}
		double mariaRatio = (double)mariaCnt / (double)(1024 * 512);
		int inMariaCraterCount = (int)(mariaRatio * settings.smallCraterCount);
		double cS = settings.bowlCraterConfig.ringThreshold / settings.craterMaxsize * (settings.craterMaxstrength - settings.craterMinstrength) + settings.craterMinstrength;
		CraterDistributionSettings cds = new CraterDistributionSettings(settings.smallCraterCount - inMariaCraterCount, settings.craterMinsize, settings.bowlCraterConfig.ringThreshold - 1, settings.craterMinstrength, cS, settings.craterFlattenedStart, settings.craterFlattenedEnd, settings.craterMountainsNoise, 0.76);
		CraterDistributer.distributeCraters(craterDistr, finalNoiseMap, craterMap1, settings.bowlCraterConfig, settings.flattenedCraterConfig, cds, resMul, rng, debugProgress);
		cds = new CraterDistributionSettings(settings.hugeCraterCount, settings.bowlCraterConfig.ringThreshold + 0.001, settings.craterMaxsize, cS, settings.craterMaxstrength, settings.craterFlattenedStart, settings.craterFlattenedEnd, settings.craterMountainsNoise, 0.5);
		CraterDistributer.distributeCraters(craterDistr, finalNoiseMap, craterMap1, settings.bowlCraterConfig, settings.flattenedCraterConfig, cds, resMul, rng, debugProgress);
		cds = new CraterDistributionSettings(inMariaCraterCount, settings.mariaCraterMinsize, settings.mariaCraterMaxsize, settings.mariaCraterMinstrength, settings.mariaCraterMaxstrength, 0, 1000000, settings.craterMountainsNoise, 0.7);
		CraterDistributer.distributeCraters(mariaCraterDistr, finalNoiseMap, craterMap2, settings.mariaCraterConfig, settings.mariaCraterConfig, cds, resMul, rng, debugProgress);
		if(debugSteps) {
			ImageIO.write(MapUtils.renderMap(craterMap1), "png", new File("crater_map.png"));
			ImageIO.write(MapUtils.renderMap(craterMap2), "png", new File("crater_map_2.png"));
		}
		
		double min = 0;
		for(double[] d1:finalNoiseMap) for(double d2:d1) if(d2 < min) min = d2;
		
		if(debugProgress) System.out.println("Secondary Noise");
		NoisemapGenerator.genNoisemap(rng, tempMap, settings.groundNoiseMediumDetail, null, resMul, debugProgress);
		NoisemapGenerator.genNoisemap(rng, tempMap2, settings.groundNoiseSmallDetail, null, resMul, debugProgress);
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				finalNoiseMap[i][j] -= min;
				
				double val = tempMap[i][j] + tempMap2[i][j];
				if(marias[i][j] < 0.4) {
					val *= Math.max(0.1, (marias[i][j] / 0.4));
				}
				finalNoiseMap[i][j] += val;
			}
		}
		
		int biggestPixelValue = 0;
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				double v = finalNoiseMap[i][j];
				int col  = (int)(v * 255.0);
				int r,g,b;
				r = g = b = Math.max(0, Math.min(255, col));
				if(r > biggestPixelValue) {
					biggestPixelValue = r;
				}
				img.setRGB(i, j, b | (g << 8) | (r << 16));
			}
		}
		if(debugProgress) System.err.println(biggestPixelValue);
		result.heightmap = img;
		result.heightmap16 = MapUtils.render16bit(finalNoiseMap);
		result.heightmap24 = MapUtils.render24bit(finalNoiseMap);
		result.heightmapRaw = new double[width][height];
		for(int i = 0; i < width; i++) for(int j = 0; j < height; j++) result.heightmapRaw[i][j] = finalNoiseMap[i][j];
		if(debugSteps) {
			ImageIO.write(img, "png", new File("graymoon.png"));
			ImageIO.write(result.heightmap16, "png", new File("graymoon_16.png"));
			ImageIO.write(result.heightmap24, "png", new File("graymoon_24.png"));
		}
		if(debugProgress) System.out.println("Done.");
		
		if(debugProgress) System.out.println("Color Map!");
		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		if(settings.secondaryColor != null) {
			NoisemapGenerator.genNoisemap(rng, tempMap, settings.secondColorNoise, null, resMul, debugProgress);
			for(int i = 0; i < width; i++) {
				for(int j = 0; j < height; j++) {
					mariaNoiseMuls[i][j] = Math.min(1, Math.max(0, (tempMap[i][j] - 0.3) * 1.75)) * mariaNoiseMuls[i][j];
				}
			}
		}
		NoisemapGenerator.genNoisemap(rng, tempMap, settings.colorNoise, null, resMul, debugProgress);
		NoisemapGenerator.genNoisemap(rng, tempMap2, settings.craterRimColorNoise, null, resMul, debugProgress);
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				double mariaMul = (marias[i][j] - 0.23) * 5.882352941;
				if(mariaMul < 0) mariaMul = 0;
				double mountainMul = Math.max(0, Math.min(1, mountainMap[i][j]));
				double[] rgb = new double[] {
						mariaMul * (mountainMul * settings.mountainsColor[0] + (1.0 - mountainMul) * settings.normalColor[0]) + (1.0 - mariaMul) * settings.mariasColor[0],
						mariaMul * (mountainMul * settings.mountainsColor[1] + (1.0 - mountainMul) * settings.normalColor[1]) + (1.0 - mariaMul) * settings.mariasColor[1],
						mariaMul * (mountainMul * settings.mountainsColor[2] + (1.0 - mountainMul) * settings.normalColor[2]) + (1.0 - mariaMul) * settings.mariasColor[2],
				};
				if(settings.secondaryColor != null) {
					rgb[0] = rgb[0] * (1.0 - mariaNoiseMuls[i][j]) + mariaNoiseMuls[i][j] * settings.secondaryColor[0];
					rgb[1] = rgb[1] * (1.0 - mariaNoiseMuls[i][j]) + mariaNoiseMuls[i][j] * settings.secondaryColor[1];
					rgb[2] = rgb[2] * (1.0 - mariaNoiseMuls[i][j]) + mariaNoiseMuls[i][j] * settings.secondaryColor[2];
				}
				double mul = tempMap[i][j];
				mul += 0.5;
				if(mul > mul) mul = 1;
				rgb[0] = mul * rgb[0];
				rgb[1] = mul * rgb[1];
				rgb[2] = mul * rgb[2];
				double heightCol = finalNoiseMap[i][j];
				heightCol = 1.0 + heightCol * 0.15;
				rgb[0] *= heightCol;
				rgb[1] *= heightCol;
				rgb[2] *= heightCol;
				
				if(craterMap1[i][j] > settings.craterRimFadeStart && tempMap2[i][j] > 0) {
					double mmul = (craterMap1[i][j] - settings.craterRimFadeStart) / (settings.craterRimFadeEnd - settings.craterRimFadeStart);
					mmul = Math.min(1, mmul);
					mmul *= tempMap2[i][j];
					rgb[0] *= 1.0 + settings.craterRimFades[0] * mmul;
					rgb[1] *= 1.0 + settings.craterRimFades[1] * mmul;
					rgb[2] *= 1.0 + settings.craterRimFades[2] * mmul;
				}
				if(craterMap2[i][j] > settings.mariaCraterRimFadeStart && tempMap2[i][j] > 0) {
					double mmul = (craterMap2[i][j] - settings.mariaCraterRimFadeStart) / (settings.mariaCraterRimFadeEnd - settings.mariaCraterRimFadeStart);
					mmul = Math.min(1, mmul);
					mmul *= tempMap2[i][j];
					rgb[0] *= 1.0 + settings.mariaCraterRimFades[0] * mmul;
					rgb[1] *= 1.0 + settings.mariaCraterRimFades[1] * mmul;
					rgb[2] *= 1.0 + settings.mariaCraterRimFades[2] * mmul;
				}
				int r = (int)Math.max(0, Math.min(255, rgb[0] * 255.0));
				int g = (int)Math.max(0, Math.min(255, rgb[1] * 255.0));
				int b = (int)Math.max(0, Math.min(255, rgb[2] * 255.0));
				
				img.setRGB(i, j, b | (g << 8) | (r << 16));
			}
		}
		result.colorMap = img;
		if(debugSteps) ImageIO.write(img, "png", new File("colors.png"));
		if(debugProgress) System.out.println("Done.");
		
		if(debugProgress) System.out.println("Biome map");
		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		if(debugProgress) ProgressBars.printBar();
		final double[] base = new double[] {0,0,0};
		for(int i = 0; i < width; i++) {
			if(debugProgress) ProgressBars.printProgress(i, width);
			for(int j = 0; j < height; j++) {
				double[] rgb = base;
				if(marias[i][j] < 0.35) {
					rgb = settings.mariasBiomeColor;
				}else {
					if(mountainMap[i][j] > 0.25) {
						rgb = settings.mountainsBiomeColor;
					}else {
						rgb = settings.normalBiomeColor;
						if(settings.secondaryColor != null && settings.biomeColorSecondary != null && mariaNoiseMuls[i][j] >= 0.2) {
							rgb = settings.biomeColorSecondary;
						}
					}
				}
				int r = (int)Math.max(0, Math.min(255, rgb[0] * 255.0));
				int g = (int)Math.max(0, Math.min(255, rgb[1] * 255.0));
				int b = (int)Math.max(0, Math.min(255, rgb[2] * 255.0));
				
				img.setRGB(i, j, b | (g << 8) | (r << 16));
			}
		}
		if(debugProgress) ProgressBars.finishProgress();
		
		result.biomeMap = img;
		if(debugSteps) ImageIO.write(img, "png", new File("biomes.png"));
		
		if(debugProgress) System.out.println("Done.");
		return result;
	}
	
	public static void main(String[] args) {
		try {
			boolean test = true;
			RanMT rng = test ? new RanMT(fixed_seed) : new RanMT().seedCompletely();
			long name = System.currentTimeMillis();
			if(!test) {
				FileOutputStream fos = new FileOutputStream("past_outputs/" + name + "_seed.txt");
				int cntr = 0;
				for(int i:rng.getLongSeed()) {
					System.out.print(i + ",");
					fos.write((i + ",").getBytes());
					cntr++;
					if(cntr % 16 == 0) {
						System.out.println();
						fos.write("\r\n".getBytes());
						fos.flush();
					}
				}
				System.out.println();
				fos.close();
			}
			
			GraymoonGenSettings settings = new GraymoonGenSettings();
			GeneratorResult res = GraymoonGen.generate(rng, settings, true, true, test);
			ImageIO.write(res.heightmap, "png", new File("past_outputs/" + name + ".png"));
			ImageIO.write(res.heightmap16, "png", new File("past_outputs/" + name + "_16.png"));
			ImageIO.write(res.colorMap, "png", new File("past_outputs/" + name + "_colors.png"));
			ImageIO.write(res.biomeMap, "png", new File("past_outputs/" + name + "_biomes.png"));
			NoisemapGenerator.cleanUp();
			CraterDistributer.cleanUp();
		}catch(Exception e) {
			System.err.println("Error: ");
			e.printStackTrace();
			System.exit(1);
		}
		NoisemapGenerator.cleanUp();
	}
	
	private static final int[] fixed_seed = new int[] { // 2,698,966,514,655,541,623 300 2.6 8 1 42
			178709473,-1820209147,-1540717568,1908574711,-1330883869,-1815092435,-1511910098,-475587614,475696601,687080299,-1102239555,-330026436,-571541577,-38564925,1711489193,-2004524584,
			-1878194481,956220136,401229824,715903529,-1946623472,-1543292916,376460053,-784859263,1603979552,599487667,1414214566,-387590497,127225618,1515583493,497539505,2063931079,
			788031161,-1240627137,301172569,1828194961,-1654965730,-1941123494,430494647,1130532335,-1956194139,557270627,1187674899,-255630121,-853824022,-1527677494,-101317565,-1267605104,
			-1604595848,1094885740,959983985,-197534979,1971590300,1265195917,-519224815,1209004753,924713749,-1891761108,993462313,-1533530616,1827964836,1912347934,-1840068926,-1367811850,
			370508368,1548559157,-1940330240,-978076124,1015684955,1953409481,1903198038,1165995759,349176679,848153625,130062322,-899917908,-228768808,-1028527667,1479105512,224028414,
			2042180512,2026944108,-2096167646,147981347,1072954373,-723151136,-945774769,-1476290879,1988560904,-1239002660,54827945,944691963,1508392521,1705970654,974467288,807684558,
			-665187226,306157620,748065138,-1532702737,1304576018,803476680,-1710167466,-189803991,1083290586,-274539536,-1841809393,-990935768,-1022966124,-1400258784,604760230,1709937878,
			2069145853,-1129015031,-887599903,576330045,1551463852,-1910036139,931388419,-1054286359,-926081777,1486112014,1888634632,-1141311448,781394003,-160672857,-595109672,1208397238,
			773396931,-1462952324,1813228143,1421323038,2890142,-1557336396,45425995,273255033,586075938,1020984466,951407970,1482336546,1247445857,-1884345085,-1095791647,-1186509005,
			821355573,478355598,-37170790,1150625143,746888828,-951152010,184789407,-1813578725,1506723807,503383356,-1487297529,-1020075708,-911795461,1718797122,-779829362,171214604,
			1490878527,1627860283,1638046983,-497681635,-1009817371,114972281,1109098911,1401465679,363952745,-879495803,477571139,399031928,-892729593,339321502,-1140234579,-1035683708,
			-600319259,-1516608370,-73360203,-60591126,1929696151,-970092351,-828376816,941778066,798084357,-1333855987,-651195513,-326749822,-911910031,-1132303692,376186595,-1911055954,
			1286034609,1466465261,1052840668,-89740566,1523788685,2060364654,-69889912,-53589968,-1318380852,-714813861,2058889958,-521011094,-1623842920,1080151902,-1741563322,-760204054,
			1037222437,1851898837,-1234503845,-553480255,94941843,-108583935,1179384772,75942478,-1014812034,402245742,843323197,-1331051466,236867390,1193056385,-1554018413,1705961544,
			422636561,789905820,215479518,181265884,717069905,-646083359,92808030,-1797213246,112481802,392023593,971904472,1264314968,1546574252,-1681808821,-1398503296,-359962942,
			-1630400651,-511066644,-1904317698,1229800633,-1105323898,1817152431,850273056,-171630304,-608748351,1433819718,111321055,1908520019,2029101819,1606671468,8665886,1422833352,
			-1825973226,-48630308,-1434751895,-1624434911,-843825344,-886318590,-869577902,-164920771,393023835,222854126,-1224088912,1469659028,825459356,-1835775245,1702908372,256717332,
			433682318,1229734381,-268648327,1593548147,-1420680412,2028750444,1449140246,1439931297,-1540787757,1045616091,1064419094,-895525559,-275889010,1849427358,457353904,1214285812,
			1031371300,1940770678,145416801,44231175,-474232146,422556268,-112567921,-1811970589,-1879281287,1827118861,-759707052,1586094953,-1980585919,418789434,-1228449849,-484329008,
			610529403,-1699979488,806551931,1239606591,-2102317260,-1133243452,-1154554825,-1557287559,133994077,-1752784221,137580003,503405194,-1674419977,734667510,1535549365,-1918689535,
			1920883428,1299203530,-1475280716,499862066,603471405,1093694702,1480843155,30593655,-1064248373,-516473051,1050625631,1083910283,-276162867,-1910171224,-778109956,2083152139,
			1476636303,-2135528203,-1439074549,-1187665894,1365694915,1240396595,928686191,-1206646444,1253748777,1893885841,-1140562573,1708361494,1865115002,645170209,-117499114,1231716112,
			-850504697,-123055980,1447276491,-1774230974,-1376987307,1214693536,-74863036,-1165968666,-698354203,95199965,178458563,-720927985,700898372,-541073801,428859143,2076035885,
			2074224589,-1480588739,494119494,-1597788075,1839071360,496378795,852936253,835494743,-510327847,-1068495920,316497681,1114570283,-535429286,-2076002301,-1872863631,794815471,
			-755598682,-2104613221,721689016,768974292,-1698950389,-824412840,-1579100343,663343827,1424401543,-1586081360,1914773627,-1726566996,-1603975699,-318327395,1506140911,1705499462,
			-859827886,1218967180,1234199758,943327733,-1480540017,-803986327,-1400240562,400731655,-1935403213,1024636697,-1898851210,1239030548,-283239490,108891752,-124130570,2034362474,
			-608492535,-1032372428,-981555448,1642178535,-832065802,1655484957,1537321868,-397152914,-819393149,582750988,-382553947,-1621253156,-61808493,706416314,978331655,929674515,
			-2001379321,-199859957,-865488979,979566470,-1145538700,-1983597434,2140033786,-1016685249,-1339576564,2064044125,693149309,-1761052398,1715398689,1290737182,1479180613,505690079,
			1012393371,-987082426,-913636935,1249143856,152086448,323212740,1878506635,1500126613,-520972852,1507782139,1777089089,-1090608964,149191153,-2092984136,-178188131,1064942406,
			-1242478209,-1648584394,1059378187,1684413635,36553115,905237451,-395256659,103686977,-305737484,1172438724,1080284711,-1726286915,1518522792,1495441019,2051810217,1236262279,
			-1354787766,2014663908,-1662250419,-1180953263,943143631,1151303801,-2111710372,1539094281,1746264708,1958291952,27096765,48832329,-718659884,-2145893608,1284390124,881037660,
			758792835,-409651760,-1821222060,889728214,1640529192,-165494433,1248990435,-1844169568,-36716046,1014180311,-1007627023,580887954,-314342061,1369749137,1681789581,325262625,
			-35895973,-1802534719,1687159374,-1700524326,133883381,-1030844228,-156100268,1541491840,-39003835,970870269,-1643028850,1494866789,354098314,-1494598349,-1106211077,-1935140145,
			-487718641,-1390145966,-1328673037,-336905500,-2140158260,-717358044,885370101,-1606876267,-931657829,-1411221374,-1560791970,-1194202558,1298788538,-2059194846,713215898,1976662297,
			-372289938,-887534569,1104165701,-1856699288,-1269144134,334570895,-1731344490,-1722633755,-1353146587,-2082791151,449719109,-2116366352,-1025507131,534509044,-100391074,347961981,
			1883339252,-826918298,1371469659,-925986206,881458220,-1159249068,-1479371320,-666762283,-1088657685,-1427025337,2027277639,584736064,-122797840,-1370680285,102767639,-2048674540,
			1446069370,782003717,-810457568,-650846084,443791327,234397457,2137880443,-1380910576,-125724370,-59397311,-1907009801,1057175587,1087639,-1741349711,-1750571015,-946924655,
			-595533243,1017254017,555179709,-1966162423,909949564,-1632594297,-1703739173,2006575245,1971381495,1966759591,2055256329,957274504,1020781392,1976743842,-1445005028,-367537037,
			-1328415576,-542384539,-534549447,1690750706,163846428,-1486382126,849148488,1409382444,-73962282,401421104,-681338489,-1019768986,921956601,1704894708,178254247,-451048968,
	};
	
}
