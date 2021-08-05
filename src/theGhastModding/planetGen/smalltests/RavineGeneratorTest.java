package theGhastModding.planetGen.smalltests;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.imageio.ImageIO;

import theGhastModding.planetGen.noise.*;
import theGhastModding.planetGen.utils.*;

public class RavineGeneratorTest {
	
	public static void main(String[] args) {
		try {
			double[][] testImg = new double[2048][1024];
			double[][] ravineMap = new double[testImg.length][testImg[0].length];
			double[][] temp = new double[testImg.length][testImg[0].length];
			for(int i = 0; i < testImg.length; i++) {
				Arrays.fill(testImg[i], 0.25);
				Arrays.fill(ravineMap[i], 0.75);
			}
			BufferedImage testRes = new BufferedImage(testImg.length, testImg[0].length, BufferedImage.TYPE_INT_RGB);
			Random rng = new Random("fixed_seed".hashCode());
			RavineGenerator rgen = new RavineGenerator(testImg.length, testImg[0].length);
			
			NoiseConfig groundNoiseLargeDetail = new NoiseConfig(new OctaveNoise3D(16, 16, 16, 8, 2.0, 0.6)).setIsRidged(false).setNoiseStrength(0.8).setNoiseScale(0.7).setDistortStrength(0.5).setNoiseOffset(0.25);
			NoisemapGenerator.genNoisemap(rng, temp, groundNoiseLargeDetail, null, 0.9, true);
			for(int i = 0; i < testImg.length; i++) for(int j = 0; j < testImg[0].length; j++) testImg[i][j] += temp[i][j];
			NoiseConfig groundNoiseMediumDetail = new NoiseConfig(new OctaveNoise3D(24, 24, 24, 6, 2.0, 0.6)).setIsRidged(false).setNoiseStrength(0.25).setNoiseScale(0.3).setDistortStrength(0.75).setNoiseOffset(0.2);
			NoisemapGenerator.genNoisemap(rng, temp, groundNoiseMediumDetail, null, 0.9, true);
			for(int i = 0; i < testImg.length; i++) for(int j = 0; j < testImg[0].length; j++) testImg[i][j] += temp[i][j];
			
			//rgen.genRavine(testImg, 30, 90, 55, 150, 24, rng);
			
			RavineGenerator.RavineConfig config = new RavineGenerator.RavineConfig();
			config.distortNoiseConfig = new NoiseConfig(new OctaveNoise2D(8, 3, 6, 1.6, 0.6), false, 20, 1.57, 0, 0, 0);
			config.rimNoise = new NoiseConfig(new OctaveNoise3D(24, 24, 24, 10, 2.0, 0.65)).setIsRidged(true).setNoiseStrength(1.0 / 0.23).setNoiseScale(0.72).setDistortStrength(0.43).setNoiseOffset(0);
			config.rimNoise.noise.initialize(rng);
			config.rimShapeFullHyperbolic = false;
			ProgressBars.printBar();
			long startTime = System.currentTimeMillis();
			for(int i = 0; i < 100; i++) {
				ProgressBars.printProgress(i, 100);
				double lat1 = rng.nextDouble() * 150.0 - 70.0;
				double lon1 = rng.nextDouble() * 360.0 - 180.0;
				double lat2,lon2;
				do {
					lat2 = rng.nextDouble() * 150.0 - 70.0;
					lon2 = rng.nextDouble() * 360.0 - 180.0;
				}while(Maths.gcDistance(lat1, lon1, lat2, lon2) > 0.5 || Maths.angleFromCoordinate(lat1, lon1, lat2, lon2) < 10);
				//config.size = 2.0 + dist * 6.0 + rng.nextDouble() * 16.0;
				config.size = 2.0 + Maths.gcDistance(lat1, lon1, lat2, lon2) * 6.0 + rng.nextDouble() * 12.0;
				config.ravineStrength = 0.35 * (config.size / 35.0);
				config.shapeExponent = 1.4 + rng.nextDouble() * 0.2 - 0.1;
				config.rimHeight = 0.25 + rng.nextDouble() * 0.2 - 0.1;
				config.rimWidth = 0.5 + rng.nextDouble() * 0.15;
				config.rimShapeExponent = 2.0 + rng.nextDouble() * 0.4 - 0.2;
				//System.out.println("[" + lat1 + "," + lon1 + "], [" + lat2 + "," + lon2 + "] " + SphereUtils.angleFromCoordinate(lat1, lon1, lat2, lon2));
				rgen.genRavine(testImg, ravineMap, null, lat1, lon1, lat2, lon2, 0, testImg[0].length, false, config, rng);
			}
			
			config.size = 32;
			config.ravineStrength = 0.4;
			config.rimHeight = 0.25;
			config.rimWidth = 0.5;
			config.rimShapeExponent = 2.0;
			config.shapeExponent = 2.1;
			config.distortNoiseConfig.setNoiseStrength(0);
			rgen.genRavine(testImg, ravineMap, null, 0, -25, 0, 25, 0, testImg[0].length, false, config, rng);
			config.size = 8;
			config.ravineStrength = 0.075;
			config.distortNoiseConfig.setNoiseStrength(0.1);
			rgen.genRavine(testImg, ravineMap, null, -10, 0, 10, 5, 0, testImg[0].length, true, config, rng);
			//rgen.genRavine(testImg, ravineMap, 40, -170, 40, 170, config, rng);
			//rgen.genRavine(testImg, ravineMap, 70, -140, 70, 140, config, rng);
			//rgen.genRavine(testImg, ravineMap, 50, -170, 50, 10, config, rng);
			//rgen.genRavine(testImg, ravineMap, -85, -35.7, -20, 0, config, rng);
			
			/*for(int i = 0; i < testRes.getWidth(); i++) {
				for(int j = 0; j < testRes.getHeight(); j++) {
					testRes.setRGB(i, j, rgen.lineMap[i][j] ? 0xFFFFFF : 0x333333);
				}
			}
			ImageIO.write(testRes, "png", new File("lines.png"));*/
			
			BufferedImage crossSection = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
			Graphics gr = crossSection.getGraphics();
			gr.setColor(Color.WHITE);
			for(int i = 0; i < crossSection.getWidth() + 1; i++) {
				double h = ravineMap[testImg.length / 2 + i - (crossSection.getWidth() / 2 + 1)][testImg[0].length / 2];
				h = Math.max(0, Math.min(1, h));
				int hi = crossSection.getHeight() - 1 - (int)(h * crossSection.getHeight());
				if(i != 0) gr.drawLine(i, crossSection.getHeight() - 1, i, hi);
			}
			ImageIO.write(crossSection, "png", new File("ravine_cross_section.png"));
			gr.dispose();
			
			ProgressBars.finishProgress();
			config.rimNoise.noise.cleanUp();
			startTime = System.currentTimeMillis() - startTime;
			System.out.println("Finished in " + Double.toString((double)startTime / 1000.0) + " seconds (" + Double.toString((double)startTime / 1000.0 / 60.0) + " mins)");
			
			ImageIO.write(MapUtils.renderMap(testImg), "png", new File("ravines.png"));
			ImageIO.write(MapUtils.render16bit(testImg), "png", new File("ravines_16.png"));
			ImageIO.write(MapUtils.renderMap(ravineMap), "png", new File("ravines_a.png"));
			
			double[] normalColor = MapUtils.RGB(new Color(85, 85, 85));
			double[] ravineColor = MapUtils.RGB(new Color(115, 46, 26));
			NoiseConfig colorNoise = new NoiseConfig(new OctaveWorley(16, 16, 16, 6, 2.0, 0.75)).setIsRidged(true).setNoiseStrength(1.25).setNoiseScale(0.75).setDistortStrength(0.75).setNoiseOffset(0);
			double[][][] colormap = new double[testImg.length][testImg[0].length][3];
			NoisemapGenerator.genNoisemap(rng, temp, colorNoise, null, 0.9, true);
			for(int i = 0; i < testImg.length; i++) {
				for(int j = 0; j < testImg[0].length; j++) {
					double a = Math.min(1.0, ravineMap[i][j] + 0.25);
					
					temp[i][j] += 0.5;
					colormap[i][j][0] = temp[i][j] * (normalColor[0] * a + (1.0 - a) * ravineColor[0]);
					colormap[i][j][1] = temp[i][j] * (normalColor[1] * a + (1.0 - a) * ravineColor[1]);
					colormap[i][j][2] = temp[i][j] * (normalColor[2] * a + (1.0 - a) * ravineColor[2]);
				}
			}
			for(int i = 0; i < testImg.length; i++) {
				for(int j = 0; j < testImg[0].length; j++) {
					int r = (int)(colormap[i][j][0] * 255);
					r = Math.max(0, Math.min(255, r));
					int g = (int)(colormap[i][j][1] * 255);
					g = Math.max(0, Math.min(255, g));
					int b = (int)(colormap[i][j][2] * 255);
					b = Math.max(0, Math.min(255, b));
					testRes.setRGB(i, j, (r << 16) | (g << 8) | (b << 0));
				}
			}
			ImageIO.write(testRes, "png", new File("ravines_colors.png"));
			NoisemapGenerator.cleanUp();
			TrigonometryCache cache = new TrigonometryCache(1024);
			BufferedImage acosTest = new BufferedImage(512, 512, BufferedImage.TYPE_INT_RGB);
			for(int i = 0; i < 512; i++) {
				double x = (i - 256) / 256.0;
				double y = cache.fastAcos(x);
				int iy = (int)(y / 3.141 * acosTest.getHeight());
				if(iy >= acosTest.getHeight()) iy = acosTest.getHeight() - 1;
				if(iy < 0) iy = 0;
				acosTest.setRGB(i, acosTest.getHeight() - 1 - iy, 0xFFFFFF);
			}
			ImageIO.write(acosTest, "png", new File("acos.png"));
		}catch(Exception e) {
			System.err.println("Error: ");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
}