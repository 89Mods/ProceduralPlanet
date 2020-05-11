package theGhastModding.planetGen.generators;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;

import javax.imageio.ImageIO;
import edu.cornell.lassp.houle.RngPack.RanMT;
import theGhastModding.planetGen.noise.NoiseFunction;
import theGhastModding.planetGen.noise.NoiseUtils;
import theGhastModding.planetGen.noise.OctaveNoise3D;
import theGhastModding.planetGen.noise.WorleyNoise;
import theGhastModding.planetGen.utils.SphereUtils;

public class ComplexSurface {
	
	private static double[] RGB(Color c) {
		return new double[] {c.getRed() / 255.0, c.getGreen() / 255.0, c.getBlue() / 255.0};
	}
	
	private static int printed;
	
	private static void printBar() {
		System.out.print("|");
		for(int i = 0; i < 98; i++) System.out.print("-");
		System.out.println("|");
		printed = 0;
	}
	
	private static void printProgress(int i, int max) {
		int percentage = (int)((double)i / (double)max * 100.0);
		if(percentage >= printed) {
			int cnt = percentage - printed;
			for(int j = 0; j < cnt; j++) {
				System.out.print(">");
				printed++;
			}
		}
	}
	
	private static void finishProgress() {
		System.out.println(">");
		printed = 0;
	}
	
	//IDEA: OpenCL perlin noise
	//IDEA: Use Worley noise in mountain generator
	public static void main(String[] args) {
		try {
			/*
			 * Input Parameters
			 */
			final int width = 4096;
			final int height = 2048;
			final int planetRadius = 600000;
			final double planetCircumference = planetRadius * 2.0 * Math.PI;
			final double resMul = 600000.0 / (double)planetRadius * 0.85;
			final double oceanFactor = 0;
			final double poleRadius = 400000;
			final double beachThreshold = 0.015 / 12.0;
			final double mountainsFadeStart = 0.1;
			final double mountainsFadeEnd = 0.25;
			final double hillsFadeStart = 0.11;
			final double hillsFadeEnd = 0.35;
			final double mountainNoiseScale = 0.17;
			final double mountainWorleyScale = 0.015;
			final double peaksFadeStart = 0.35;
			final double peaksFadeEnd = 0.45;
			
			File pastOutputsFolder = new File("past_outputs");
			if(!pastOutputsFolder.exists()) pastOutputsFolder.mkdirs();
			long name = System.currentTimeMillis();
			boolean test = true;
			RanMT rng = test ? new RanMT(fixed_seed) : new RanMT().seedCompletely();
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
			OctaveNoise3D continentNoise =          new OctaveNoise3D(rng, 16, 16, 16, 4, 2.0, 0.6);
			OctaveNoise3D mountainNoise =           new OctaveNoise3D(rng, 16, 16, 16, 4, 2.0, 0.6);
			OctaveNoise3D lakeNoiseMul =            new OctaveNoise3D(rng, 16, 16, 16, 4, 2.0, 0.6);
			NoiseFunction lakeNoise =               new OctaveNoise3D(rng, 16, 16, 16, 4, 2.0, 0.6);
			OctaveNoise3D desertNoise =             new OctaveNoise3D(rng, 16, 16, 16, 4, 2.0, 0.6);
			OctaveNoise3D taigaNoise =              new OctaveNoise3D(rng, 16, 16, 16, 4, 2.0, 0.6);
			OctaveNoise3D groundNoiseLargeDetail =  new OctaveNoise3D(rng, 16, 16, 16, 6, 2.0, 0.5);
			OctaveNoise3D groundNoiseMediumDetail = new OctaveNoise3D(rng, 20, 20, 20, 6, 2.0, 0.5);
			OctaveNoise3D groundNoiseSmallDetail =  new OctaveNoise3D(rng, 30, 30, 30, 6, 2.0, 0.5);
			OctaveNoise3D hillNoise =               new OctaveNoise3D(rng, 20, 20, 20, 9, 2.0, 0.5);
			OctaveNoise3D mountainsNoise =          new OctaveNoise3D(rng, 24, 24, 24, 11, 2.0, 0.5); //TODO: Set to 16 octaves before use
			OctaveNoise3D colorNoise =              new OctaveNoise3D(rng, 20, 20, 20, 6, 2.0, 0.6);
			OctaveNoise3D polesPerturbNoise =       new OctaveNoise3D(rng, 16, 16, 16, 3, 2.0, 0.6);
			WorleyNoise  mountainWorley =          new WorleyNoise(rng, 64, 64, 64);
			
			double[] biomeColorOceans =          RGB(new Color(55, 98, 171));
			double[] biomeColorLowlands =        RGB(new Color(131, 188, 46));
			double[] biomeColorHills =           RGB(new Color(93, 133, 42));
			double[] biomeColorMountains =       RGB(new Color(167, 167, 167));
			double[] biomeColorDesert =          RGB(new Color(234, 191, 111));
			double[] biomeColorDesertHills =     RGB(new Color(178, 145, 83));
			double[] biomeColorDesertMountains = RGB(new Color(127, 103, 59));
			double[] biomeColorTaiga =           RGB(new Color(199, 143, 223));
			double[] biomeColorTaigaHills =      RGB(new Color(113, 81, 127));
			double[] biomeColorLakes =           RGB(new Color(40, 72, 127));
			double[] biomeColorNorthPole =       RGB(new Color(255, 255, 255));
			double[] biomeColorSouthPole =       RGB(new Color(190, 190, 190));
			double[] biomeColorBeaches =         RGB(new Color(250, 242, 183));
			double[] biomeColorPeaks =           RGB(new Color(220, 220, 220));
			
			double[] lowlandColor =   RGB(new Color(50, 62, 36));
			double[] hillsColor =     RGB(new Color(51, 56, 41));
			double[] mountainsColor = RGB(new Color(84, 84, 84));
			double[] desertColor =    RGB(new Color(132, 116, 87));
			double[] taigaColor =     RGB(new Color(73, 91, 65));
			double[] oceansColor =    RGB(new Color(18, 26, 40));
			double[] polesColor =     RGB(new Color(174, 198, 211));
			double[] beachesColor =   RGB(new Color(169, 156, 117));
			double[] peaksColor =     RGB(new Color(174, 198, 211));
			/*double[] lowlandColor = RGB(new Color(53, 74, 22));
			double[] hillsColor =     RGB(new Color(56, 60, 34));
			double[] mountainsColor = RGB(new Color(84, 84, 84));
			double[] desertColor =    RGB(new Color(153, 121, 65));
			double[] taigaColor =     RGB(new Color(28, 70, 24));
			double[] oceansColor =    RGB(new Color(7, 23, 51));*/
			
			double[][] continentMap =  new double[width][height];
			double[][] distanceMap =   new double[width][height];
			double[][] mountainMap =   new double[width][height];
			double[][] hillMap =       new double[width][height];
			double[][] lakesMap =      new double[width][height];
			double[][] desertMap =     new double[width][height];
			double[][] taigaMap =      new double[width][height];
			double[][] finalNoiseMap = new double[width][height];
			double[][] ground =        new double[width][height];
			double[][] hills =         new double[width][height];
			double[][] mountains =     new double[width][height];
			double[][] lakes =         new double[width][height];
			double[][] poles =         new double[width][height];
			double[][][] colorMap =    new double[width][height][3];
			double[][][] biomeMap =    new double[width][height][3];
			
			System.out.println("Continents & Biomes");
			printBar();
			for(int i = 0; i < width; i++) {
				printProgress(i, width);
				double longitude = (double)(i - width / 2) / (width / 2.0) * 180.0;
				for(int j = 0; j < height; j++) {
					double latitude = (double)(j - height / 2) / (height / 2.0) * 90.0;
					continentMap[i][j] = (NoiseUtils.sampleSpherableNoise(continentNoise, i, j, width, height, 0.85 * resMul, 0.85 * resMul, 0.25) + oceanFactor / 2.0) * 12.0;
					double distance = SphereUtils.distance(latitude, longitude, -90, 0);
					distance = Math.min(distance, SphereUtils.distance(latitude, longitude, 90, 0)) * planetCircumference;
					if(distance <= poleRadius * 1.333) {
						distance = 1.0 - (((poleRadius * 1.333) - distance) / (poleRadius * 1.333));
						distance = distance - (1.0 - distance) * 0.75;
						if(distance < 0) distance = 0;
					}else distance = 1;
					distanceMap[i][j] = continentMap[i][j] / 12.0 * distance;
					continentMap[i][j] = /*1.0 - */Math.max(0, Math.min(1, continentMap[i][j])) * distance;
				}
			}
			finishProgress();
			displayMap("distances.png", distanceMap);
			
			printBar();
			for(int i = 0; i < width; i++) {
				printProgress(i, width);
				double longitude = (double)(i - width / 2) / (width / 2.0) * 180.0;
				for(int j = 0; j < height; j++) {
					if(continentMap[i][j] < 0) continue;
					double latitude = (double)(j - height / 2) / (height / 2.0) * 90.0;
					double distance = SphereUtils.distance(latitude, longitude, -90, 0);
					distance = Math.min(distance, SphereUtils.distance(latitude, longitude, 90, 0)) * planetCircumference;
					double val = (NoiseUtils.sampleSpherableNoise(mountainNoise, i, j, width, height, 0.18 * resMul, 0.18 * resMul, 0.25) + 0.375);
					val = Math.max(0, Math.min(1, Math.abs(val)));
					if(distanceMap[i][j] < hillsFadeEnd && val > 0.30) {
						double h = val - 0.30;
						h = h * 1.428 * 5.0;
						hillMap[i][j] = h;
						hillMap[i][j] *= continentMap[i][j];
						if(distanceMap[i][j] > hillsFadeStart) {
							double amul = (distanceMap[i][j] - hillsFadeStart) / (hillsFadeEnd - hillsFadeStart);
							amul = 1.0 - amul;
							hillMap[i][j] *= amul;
						}
					}
					if(distanceMap[i][j] < mountainsFadeEnd && val > 0.465) {
						double h = val - 0.465;
						h = h * 1.869 * 5.0;
						mountainMap[i][j] = h;
						mountainMap[i][j] *= continentMap[i][j];
						if(distanceMap[i][j] > mountainsFadeStart) {
							double amul = (distanceMap[i][j] - mountainsFadeStart) / (mountainsFadeEnd - mountainsFadeStart);
							amul = 1.0 - amul;
							mountainMap[i][j] *= amul;
						}
					}
					val = (NoiseUtils.sampleSpherableNoise(lakeNoiseMul, i, j, width, height, 1.0 * resMul, 1.0 * resMul, 0.25) + 0.375);
					val = Math.max(0, Math.min(1, Math.abs(val)));
					if(val > 0.43) {
						val = Math.min((val - 0.43) * 2.325 * 5.0, 1.0);
						if(val > 0) {
							val = (NoiseUtils.sampleSpherableNoise(lakeNoise, i, j, width, height, 0.3 * resMul, 0.3 * resMul, 0.25) + 0.375) * val;
							val = Math.max(0, Math.min(1, Math.abs(val)));
							if(val > 0.43) {
								double h = val - 0.43;
								h = h * 2.325 * 5.0;
								double m = Math.max(hillMap[i][j], mountainMap[i][j]);
								lakesMap[i][j] = h * Math.max(1.0 - (2.0 * m), 0);
								lakesMap[i][j] *= continentMap[i][j];
							}
						}
					}
					val = (NoiseUtils.sampleSpherableNoise(desertNoise, i, j, width, height, 1.0 * resMul, 1.0 * resMul, 0.25) + 0.375);
					val = Math.max(0, Math.min(1, Math.abs(val)));
					double x = (double)j / (double)height * 2.0 - 1.0;
					val *= 2.0 * (-x * x) + 1.0;
					if(val > 0.38) {
						double h = val - 0.38;
						h = h * 1.612 * 5.0;
						desertMap[i][j] = h * continentMap[i][j] * (1.0 - Math.max(Math.min(lakesMap[i][j], 1.0), 0.0));
					}
					if(distance <= poleRadius * 1.7) {
						double mul = (poleRadius * 1.7) - (distance - (poleRadius * 1.0)) - (poleRadius * 1.0);
						mul /= (poleRadius * 1.7 - poleRadius * 1.0);
						if(distance <= (poleRadius * 1.0)) mul = 1;
						if(mul > 1) mul = 1;
						
						val = (NoiseUtils.sampleSpherableNoise(taigaNoise, i, j, width, height, 1.0 * resMul, 1.0 * resMul, 0.25) + 0.375);
						val = Math.max(0, Math.min(1, Math.abs(val)));
						if(val > 0.35) {
							double h = val - 0.35;
							h = h * 1.538 * 5.0;
							taigaMap[i][j] = h * continentMap[i][j] * (1.0 - Math.max(Math.min(lakesMap[i][j], 1.0), 0.0)) * (1.0 - Math.max(Math.min(mountainMap[i][j], 1.0), 0.0)) * (1.0 - Math.max(Math.min(desertMap[i][j], 1.0), 0.0));
						}
						taigaMap[i][j] *= mul;
					}
					/*mountainMap[i][j] = (NoiseUtils.sampleSpherableNoise(mountainNoise, i, j, width, height, 0.6, 0.6, 0.5));
					mountainMap[i][j] = hillMap[i][j] = (Math.abs(mountainMap[i][j]) - 0.11) * 12.0;
					hillMap[i][j] += 1.0;
					if(continentMap[i][j] < 0.5) mountainMap[i][j] = hillMap[i][j] = 0.0;
					else {
						mountainMap[i][j] = Math.pow(mountainMap[i][j], 3);
						hillMap[i][j] = Math.pow(hillMap[i][j], 3);
					}*/
				}
			}
			finishProgress();
			
			BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			for(int i = 0; i < width; i++) {
				for(int j = 0; j < height; j++) {
					double v = continentMap[i][j];
					int col = (int)(v * 255.0);
					int r,g,b;
					r = g = b = Math.max(0, Math.min(255, col));
					if(hillMap[i][j] > 0) {
						double mul = (1.0 - Math.max(0, Math.min(1, hillMap[i][j])));
						r = (int)(r * mul);
						g = (int)(g * mul);
					}
					if(mountainMap[i][j] > 0) {
						double mul = (1.0 - Math.max(0, Math.min(1, mountainMap[i][j])));
						g = (int)(g * mul);
						b = (int)(b * mul);
						r = (int)((1.0 - mul) * 255);
					}
					if(lakesMap[i][j] > 0) {
						double mul = (1.0 - Math.max(0, Math.min(1, lakesMap[i][j])));
						g = (int)(r * mul);
					}
					if(desertMap[i][j] > 0) {
						double mul = (1.0 - Math.max(0, Math.min(1, desertMap[i][j])));
						double mulmul = Math.max(0, Math.min(1, hillMap[i][j]));
						r = (int)(r * ((1.0 - mulmul) * mul + mulmul));
						b = (int)(b * ((1.0 - mulmul) * mul + mulmul));
						g = Math.max(g, (int)((1.0 - mul) * 255));
					}
					if(taigaMap[i][j] > 0) {
						double mul = (1.0 - Math.max(0, Math.min(1, taigaMap[i][j])));
						double mulmul = Math.max(0, Math.min(0.5, hillMap[i][j]));
						r = (int)(r * ((1.0 - mulmul) * mul + mulmul));
						b = (int)(b * ((1.0 - mulmul) * mul + mulmul));
						g = Math.max(g, (int)((1.0 - mul) * 128));
					}
					r = Math.max(0, Math.min(255, r));
					g = Math.max(0, Math.min(255, g));
					b = Math.max(0, Math.min(255, b));
					img.setRGB(i, j, b | (g << 8) | (r << 16));
				}
			}
			//JOptionPane.showMessageDialog(null, new JLabel(new ImageIcon(img)), "a", JOptionPane.INFORMATION_MESSAGE);
			ImageIO.write(img, "png", new File("continents.png"));
			//System.exit(0);
			
			System.out.println("Ground");
			printBar();
			for(int i = 0; i < width; i++) {
				printProgress(i, width);
				for(int j = 0; j < height; j++) {
					if(continentMap[i][j] > 0) {
						double val = NoiseUtils.sampleSpherableNoise(groundNoiseLargeDetail, i, j, width, height, 0.25 * resMul, 0.25 * resMul, 0.2) * 0.333;
						val += NoiseUtils.sampleSpherableNoise(groundNoiseMediumDetail, i, j, width, height, 0.17 * resMul, 0.17 * resMul, 0.2) * 0.333;
						val += NoiseUtils.sampleSpherableNoise(groundNoiseSmallDetail, i, j, width, height, 0.07 * resMul, 0.07 * resMul, 0.2) * 0.333;
						val += 0.25;
						val = Math.abs(val);
						val *= 0.25;
						ground[i][j] = val;
					} else {
						ground[i][j] = 0;
					}
				}
			}
			displayMap("ground.png", ground);
			finishProgress();
			
			System.out.println("Hills");
			printBar();
			for(int i = 0; i < width; i++) {
				printProgress(i, width);
				for(int j = 0; j < height; j++) {
					if(hillMap[i][j] > 0) {
						double mul = Math.min(1, hillMap[i][j]);
						double val = NoiseUtils.sampleSpherableNoise(hillNoise, i, j, width, height, 0.17 * resMul, 0.17 * resMul, 0.5);
						val += 0.25;
						val *= mul * 0.4;
						
						hills[i][j] = val;
					}else {
						hills[i][j] = 0;
					}
				}
			}
			displayMap("hills.png", hills);
			finishProgress();
			
			System.out.println("Mountains");
			printBar();
			for(int i = 0; i < width; i++) {
				printProgress(i, width);
				for(int j = 0; j < height; j++) {
					if(mountainMap[i][j] > 0) {
						double mul = Math.min(1, mountainMap[i][j]);
						double val = NoiseUtils.sampleSpherableNoise(mountainsNoise, i, j, width, height, mountainNoiseScale * resMul, mountainNoiseScale * resMul, 0.6);
						val = Math.abs(val) * 2.8;
						
						double mul2 =  Math.max(0, Math.min(1, (1.0 - NoiseUtils.sampleSpherableNoise(mountainWorley, i, j, width, height, mountainWorleyScale * resMul, mountainWorleyScale * resMul, 0.01)) * 2.0 + 0.1));
						
						mountains[i][j] = val * mul * mul2;
					}else {
						mountains[i][j] = 0;
					}
				}
			}
			
			displayMap("mountains.png", mountains);
			printMap("mountains.dat", mountains);
			finishProgress();
			
			System.out.println("Lakes");
			printBar();
			for(int i = 0; i < width; i++) {
				printProgress(i, width);
				for(int j = 0; j < height; j++) {
					if(lakesMap[i][j] > 0) {
						double mul  = Math.max(0, Math.min(1, lakesMap[i][j]));
						lakes[i][j] = 1.0 - Math.min(1, Math.max(0, mul));
					}else {
						lakes[i][j] = 1.0;
					}
				}
			}
			displayMap("lakes.png", lakes);
			finishProgress();
			
			System.out.println("Poles");
			printBar();
			for(int i = 0; i < width; i++) {
				printProgress(i, width);
				double longitude = (double)(i - width / 2) / (width / 2.0) * 180.0;
				for(int j = 0; j < height; j++) {
					double latitude = (double)(j - height / 2) / (height / 2.0) * 90.0;
					double distance = SphereUtils.distance(latitude, longitude, -90, 0) * planetCircumference;
					distance += NoiseUtils.sampleSpherableNoise(polesPerturbNoise, i, 175, width, 360, 0.5, 0.5, 0.35) * poleRadius / 1.333;
					if(distance <= poleRadius) {
						poles[i][j] = 0.1;
					}else {
						poles[i][j] = 0.0;
					}
					distance = SphereUtils.distance(latitude, longitude, 90, 0) * planetCircumference;
					distance += NoiseUtils.sampleSpherableNoise(polesPerturbNoise, i, 185, width, 360, 0.5, 0.5, 0.35) * poleRadius / 1.333;
					if(distance <= poleRadius) {
						poles[i][j] = 0.1001;
					}
				}
			}
			displayMap("poles.png", poles);
			finishProgress();
			
			for(int i = 0; i < width; i++) {
				for(int j = 0; j < height; j++) {
					double mul = Math.min(1, mountainMap[i][j]);
					finalNoiseMap[i][j] = (ground[i][j] + (1.0 - mul) * hills[i][j] + mountains[i][j]) * lakes[i][j] * continentMap[i][j];
					finalNoiseMap[i][j] += poles[i][j];
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
			System.err.println(biggestPixelValue);
			ImageIO.write(img, "png", new File("complex.png"));
			ImageIO.write(img, "png", new File("past_outputs/" + name + ".png"));
			printMap("complex.dat", finalNoiseMap);
			System.out.println("Done.");
			//long name = System.currentTimeMillis();
			
			System.out.println("Color Map!");
			img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			printBar();
			for(int i = 0; i < width; i++) {
				printProgress(i, width);
				double longitude = (double)(i - width / 2) / (width / 2.0) * 180.0;
				for(int j = 0; j < height; j++) {
					double continent   = Math.min(1, continentMap[i][j]);
					double[] rgb = null;
					if(poles[i][j] != 0 && (continent <= 1e-8 || lakesMap[i][j] != 0)) {
						rgb = Arrays.copyOf(polesColor, 3);
					}else {
						if(continent <= 1e-8) {
							colorMap[i][j] = oceansColor;
							continue;
						}
						if(distanceMap[i][j] <= beachThreshold) {
							rgb = Arrays.copyOf(beachesColor, 3);
						}else {
							double latitude = (double)(j - height / 2) / (height / 2.0) * 90.0;
							double distance = SphereUtils.distance(latitude, longitude, -90, 0);
							distance = Math.min(distance, SphereUtils.distance(latitude, longitude, 90, 0)) * planetCircumference;
							
							rgb = Arrays.copyOf(lowlandColor, 3);
							factorIn(rgb, hillMap[i][j], hillsColor);
							factorIn(rgb, taigaMap[i][j], taigaColor);
							double coldnessFactor = 0;
							if(distance <= poleRadius * 2.3) {
								coldnessFactor = (poleRadius * 2.3) - (distance - (poleRadius * 1.333)) - (poleRadius * 1.333);
								coldnessFactor /= (poleRadius * 2.3 - poleRadius * 1.333);
								if(distance <= poleRadius * 1.333) coldnessFactor = 1;
							}
							if(coldnessFactor != 0) factorIn(rgb, coldnessFactor, taigaColor);
							
							if(finalNoiseMap[i][j] >= peaksFadeStart && desertMap[i][j] < 0.05) {
								if(finalNoiseMap[i][j] >= peaksFadeEnd) {
									factorIn(rgb, mountainMap[i][j], peaksColor);
								}else {
									double mmul = (finalNoiseMap[i][j] - peaksFadeStart) / (peaksFadeEnd - peaksFadeStart);
									factorIn(rgb, mountainMap[i][j] * (1.0 - mmul), mountainsColor);
									factorIn(rgb, mountainMap[i][j] * mmul, peaksColor);
								}
							}else factorIn(rgb, mountainMap[i][j], mountainsColor);
							
							factorIn(rgb, desertMap[i][j], desertColor);
							factorIn(rgb, lakesMap[i][j], oceansColor);
						}
						
						factorIn(rgb, poles[i][j] * 0.75, polesColor);
						if(poles[i][j] != 0) {
							factorIn(rgb, 0.75, polesColor);
						}
					}
					
					double mul = NoiseUtils.sampleSpherableNoise(colorNoise, i, j, width, height, 0.25 * resMul, 0.25 * resMul, 0.5);
					mul = Math.abs(mul) - 0.25;
					mul *= 1.25;
					rgb[0] -= mul * rgb[0];
					rgb[1] -= mul * rgb[1];
					rgb[2] -= mul * rgb[2];
					
					colorMap[i][j] = rgb;
					//colorMap[i][j][0] = (1.0 - continent) * oceansColor[0] + continent * rgb[0]; //Try this in-game, see how it looks
					//colorMap[i][j][1] = (1.0 - continent) * oceansColor[1] + continent * rgb[1];
					//colorMap[i][j][2] = (1.0 - continent) * oceansColor[2] + continent * rgb[2];
				}
			}
			finishProgress();
			
			for(int i = 0; i < width; i++) {
				for(int j = 0; j < height; j++) {
					int r = (int)Math.max(0, Math.min(255, colorMap[i][j][0] * 255.0));
					int g = (int)Math.max(0, Math.min(255, colorMap[i][j][1] * 255.0));
					int b = (int)Math.max(0, Math.min(255, colorMap[i][j][2] * 255.0));
					
					img.setRGB(i, j, b | (g << 8) | (r << 16));
				}
			}
			ImageIO.write(img, "png", new File("colors.png"));
			ImageIO.write(img, "png", new File("past_outputs/" + name + "_colors.png"));
			System.out.println("Done.");
			System.out.println("Biome map");
			img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			printBar();
			for(int i = 0; i < width; i++) {
				printProgress(i, width);
				for(int j = 0; j < height; j++) {
					if(poles[i][j] == 0.1) {
						biomeMap[i][j] = biomeColorNorthPole;
						continue;
					}
					if(poles[i][j] == 0.1001) {
						biomeMap[i][j] = biomeColorSouthPole;
						continue;
					}
					if(continentMap[i][j] <= 1e-8) {
						biomeMap[i][j] = biomeColorOceans;
						continue;
					}
					if(lakes[i][j] <= 0.5) {
						biomeMap[i][j] = biomeColorLakes;
						continue;
					}
					biomeMap[i][j] = biomeColorLowlands;
					if(desertMap[i][j] > 0.25) {
						biomeMap[i][j] = biomeColorDesert;
					}
					if(taigaMap[i][j] > 0.25) {
						biomeMap[i][j] = biomeColorTaiga;
					}
					if(hillMap[i][j] > 0.25) {
						biomeMap[i][j] = biomeColorHills;
						if(desertMap[i][j] > 0.25) {
							biomeMap[i][j] = biomeColorDesertHills;
						}
						if(taigaMap[i][j] > 0.25) {
							biomeMap[i][j] = biomeColorTaigaHills;
						}
					}
					if(mountainMap[i][j] > 0.25) {
						biomeMap[i][j] = biomeColorMountains;
						if(desertMap[i][j] > 0.25) {
							biomeMap[i][j] = biomeColorDesertMountains;
						}else if(finalNoiseMap[i][j] > (peaksFadeStart + (peaksFadeEnd - peaksFadeStart) / 2.0)) {
							biomeMap[i][j] = biomeColorPeaks;
						}
					}
					if(distanceMap[i][j] <= beachThreshold) {
						biomeMap[i][j] = biomeColorBeaches;
					}
				}
			}
			finishProgress();
			
			for(int i = 0; i < width; i++) {
				for(int j = 0; j < height; j++) {
					int r = (int)Math.max(0, Math.min(255, biomeMap[i][j][0] * 255.0));
					int g = (int)Math.max(0, Math.min(255, biomeMap[i][j][1] * 255.0));
					int b = (int)Math.max(0, Math.min(255, biomeMap[i][j][2] * 255.0));
					
					img.setRGB(i, j, b | (g << 8) | (r << 16));
				}
			}
			ImageIO.write(img, "png", new File("biomes.png"));
			ImageIO.write(img, "png", new File("past_outputs/" + name + "_biomes.png"));
			System.out.println("Done.");
		}catch(Exception e) {
			System.err.println("Error: ");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private static void factorIn(double[] color, double multiplier, double[] newColor) {
		multiplier = Math.min(1, multiplier);
		color[0]   = color[0] * (1.0 - multiplier) + newColor[0] * multiplier;
		color[1]   = color[1] * (1.0 - multiplier) + newColor[1] * multiplier;
		color[2]   = color[2] * (1.0 - multiplier) + newColor[2] * multiplier;
	}
	
	private static void printMap(String file, double[][] map) {
		try {
			FileOutputStream fos = new FileOutputStream(file);
			for(int i = 0; i < map.length; i++) {
				StringBuilder line = new StringBuilder();
				for(int j = 0; j < map[0].length; j++) {
					line.append(String.format("%.2f\t\t", map[i][j]));
				}
				line.append("\r\n");
				fos.write(line.toString().getBytes());
			}
			fos.close();
		}catch(Exception e) {
			System.err.println("Error saving raw map data: ");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public static void displayMap(String file, double[][] map) throws Exception {
		BufferedImage img = new BufferedImage(map.length, map[0].length, BufferedImage.TYPE_INT_RGB);
		for(int i = 0; i < map.length; i++) {
			for(int j = 0; j < map[0].length; j++) {
				double v = map[i][j];
				int col = (int)(v * 255.0);
				int r,g,b;
				r = g = b = Math.max(0, Math.min(255, col));
				img.setRGB(i, j, b | (g << 8) | (r << 16));
			}
		}
		ImageIO.write(img, "png", new File(file));
	}
	
	private static final int[] fixed_seed = new int[] { // 2,698,966,514,655,541,623 300 2.6 8 1 42
			-622527324,1239914429,514770773,-1898375360,-416462275,-1520028893,1504304309,1916294549,1800914911,856304735,1603761750,463999729,1355714229,-754198837,770975268,258484721,-1120959246,
			-1201056982,920333656,-1388971988,-2094720835,420744416,-627023501,54739687,-1096037725,-304117610,-455877318,699624984,-1349613403,1527481243,1108968277,857277469,-1272719514,
			-612146673,-1484893614,330140126,-1114255746,-1305678805,-1892318042,-1640126690,-1237393255,1977638542,-1041791745,-620520294,434469103,-974927892,-1627607031,-1981150609,1860199139,
			1443083977,2039343073,-1762589228,1718486662,-1969014348,-1427835743,-1532002028,701002293,-832754733,1857468779,-1775566472,-1061145299,-1542400694,947991575,780117247,-1914172970,
			963893315,-189312291,1913813312,-1818537707,-410816324,-1817930812,1917330293,-1805034432,-1620115306,1328707844,-1845898213,682592031,-1143606825,1475988464,505581656,-1838406864,
			259187163,-112612195,1266599446,1871983048,1401823614,-821135381,1192768571,1786113861,1238467158,1539471351,217587751,-1359860622,-27822165,668100381,628743387,982939956,
			-1743814098,-1759526355,1056416035,350363674,248639405,-863311005,-691294743,473952287,-272832121,1185881270,-908081949,1535039166,297063526,1547315745,-1304105178,-892122467,
			-1121846299,1636439228,1821217854,-156530776,1397068554,209349694,-52175104,1172346636,-222297184,-1635024279,1360426314,-2146555053,-266171852,2026728730,169181090,-1004844459,
			983967209,3725674,-1936065424,1446889595,-295577566,610249857,368142740,-1945988830,1721381079,1839167594,1999092121,-1541094221,470036071,859815087,1711905359,-2026004727,
			2081778593,2099640652,146344820,1167537492,-1346658655,537498268,2110670006,-1515454740,-116038003,-699301413,-1472271184,1042100129,-385455190,1929762861,1938137138,1697243955,
			784387006,-813755941,432260710,-1225713973,748542404,-1078118144,-1294484987,319321350,-824169040,747689407,2023833104,-1187910016,1513684837,1845474323,-1175567115,-1673910969,
			153802983,596603920,1791624944,424257647,1487114925,325053642,-1641519023,20118528,-714628417,205479760,-98062017,1216837233,354990984,1646426357,1700958646,-1032001407,
			1197586992,1604986168,-1056146849,32381290,1934746685,-224317171,-2133704066,-620752744,162625217,-325337622,1454972275,120992033,-369523116,1814200845,-160046560,486482665,
			1549347446,1712578030,687251970,1661242739,-894304659,-769344549,102272486,113746835,875949824,-1330953092,801385929,-447447901,1887459688,-81960774,1458169666,1679179601,
			1480882356,-1813181444,672715645,-820634478,-756341352,1974236923,-89528910,1362041216,-1715870705,-1264322033,1119445656,-503272854,979190462,1631836172,-748116659,1957305551,
			-2081388601,-48508869,1948399636,1234752126,316453269,-12418824,685389884,-2064430274,556319491,-665413084,-763031973,309002186,437609535,25420001,2050913487,1853820307,
			-1323446547,-449445933,-1361392352,611954184,1706561085,-614257824,-639842380,-1347255075,1852738776,110662581,1443453216,-169887962,-13831274,-1707956491,1595146707,-1082140603,
			-1076209543,-177527748,-1491246057,1789305822,295353135,170327311,2080254586,-1816754316,1917965862,-1179776547,-342344239,-1764688527,-415155415,750160573,1135103295,-1677215775,
			-656686217,-1351714356,-1630428560,1469618956,858279062,1164750827,-304198971,-960591951,-280115258,-55323708,292391966,-119475036,1679988921,36773925,629844610,1680628459,
			-589741248,-1654590756,367395232,1541238711,-31167896,-2080165435,-290822145,-37147111,1936465620,1779006668,589883228,1407955530,921599363,1294544731,819250030,1980228662,
			-1998217713,-1095027790,-1706438932,782364982,1393348700,1507042684,861344615,98473814,1193225325,-1497676856,474466000,-1676591042,-2144767831,-399199069,-1746627787,-318576179,
			-808898693,-1304980088,484912005,1815864052,699616880,-1257545558,-1596312576,-371567577,-786705107,-622060475,530544194,-692085791,81333028,-1314725839,-803837215,-836864301,
			-396630536,482092554,1268583778,1102314737,1356318863,2017848663,728040402,-986355422,-281832924,-1488999148,1138169072,-1128483081,144571110,69604861,-1283526977,557713939,
			-1864416140,-851139008,1882799500,-1521489271,46282440,1907821148,-627174979,-1009758437,-1003589573,390353238,618763753,691042080,1020133898,-864162233,128259887,2003932655,
			123232766,499944097,1416780117,-1675867186,-824400709,794077453,1926971299,-826477924,2057637859,282925234,-1166556976,-709970322,1150154447,-1138674346,-381931406,1581220092,
			921299292,288947487,-1428041050,209105982,1481454200,805367917,1130087490,1711846459,2094704261,-1062484575,-208522940,-482262555,1307296431,417852178,-1663182056,1072604677,
			-1708254838,-1371350435,2097016341,-1651004594,1771049978,-239846942,-1719522772,663119953,-1583940231,-2089103457,-1763486021,-1334715561,1073243046,-1447738395,1153698726,818959299,
			1385219505,392213442,504612290,605859043,-586051342,1554453443,1494110078,-2103945096,-838832017,-861445634,-397315012,2085894382,-35525721,1075947457,-75298301,696929521,
			-190229393,-542060825,591480515,462563421,-624425619,192692356,962029122,419053617,-1938427573,-1987355825,-1821335232,1452278664,1057849525,1596240674,-742165668,778094555,
			-1986740071,484647987,-529583377,-1392672270,-355606159,-261441939,663090646,1284198642,409535626,-2115547222,1526515968,-596977272,2083609663,2126460322,1023162297,-478122107,
			1162844460,-1239195413,-364175665,-1033432952,1559996494,-681074908,1305335806,2048405296,1597490017,894207317,223587248,1121078306,1903086792,1822766069,1294768924,239624775,
			1913651474,1879238252,490828939,-1486610288,-2055081220,1162893543,1067989796,1629811540,1980160625,-1249445860,1039064020,761651479,-1251199979,1239934427,1872115285,-2087439526,
			278900579,895666648,-1451789994,-1230966470,1317235792,873756157,-888299103,951528016,77302374,-33073453,1138606471,-763293071,-445708303,-1516259415,-126421489,50718901,
			-1380585772,492688557,-1547360580,-1804945875,-1632709464,736083088,-290463115,-249572099,624273660,1213006976,-46164047,-331002576,-428486143,882264500,499325505,2135948668,
			-330195418,-1372421306,-1682998903,1983485732,349765652,960767700,1966352158,583362401,963223080,-581953370,-675451444,-157027540,-1152241513,-1510969343,-723568790,1400856562,
			1158885505,-584251496,-1235082358,-147148903,-1734945164,1080287949,-1251152171,703449747,872048481,-471114171,-1643894921,979917370,1875039568,349713001,358233619,-448433427,
			2056694992,1267811914,-827739488,-103149652,1071076189,756765411,-141292663,-276276377,-676096688,-1404676991,-1975616563,625069069,-1735555425,-1759064740,10965672,876744040,
			1120775730,1082448028,434997167,374127294,159221485,-2144662252,-8084489,-554178049,1799572832,-130077093,495908536,1858962979,-392618586,1762570516,1859657006,-699565742,
			-1520868663,-537326583,-769009688,370845774,-319122121,-1184769172,1005924899,-1998800002,1645496259,-1059794104,1015674670,-1969178067,1527220449,902136222,1616280235,
	};
	
}
