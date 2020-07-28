package theGhastModding.planetGen.smalltests;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Random;

import javax.imageio.ImageIO;

import theGhastModding.planetGen.noise.NoiseConfig;
import theGhastModding.planetGen.noise.OctaveNoise3D;
import theGhastModding.planetGen.utils.CraterGenerator;
import theGhastModding.planetGen.utils.CraterGenerator.CraterConfig;
import theGhastModding.planetGen.utils.CraterGenerator.CraterDistributionSettings;

public class CraterGeneratorTest {
	
	public static void main(String[] args) {
		try {
			double[][] testImg = new double[2048][1024];
			for(int i = 0; i < testImg.length; i++) Arrays.fill(testImg[i], 0.8);
			BufferedImage testRes = new BufferedImage(testImg.length, testImg[0].length, BufferedImage.TYPE_INT_RGB);
			
			Random rng = new Random(("fuck".hashCode() << 6));
			long startTime;
			final int craterCnt = 2048;
			final double maxsize = 128.0;
			final double minsize = 4.0;
			final double maxStrength = 0.6;
			final double minStrength = 0.05;
			final double flattenedStart = 14;
			final double flattenedEnd   = 28;
			CraterConfig bowlCraterConfig =      new CraterConfig(0, 0, 0.2, 0.4, 1.0, 4.8, -10.0, 0.3, 2.1, 0.1, 0.4, 30, 96, 1.0);
			CraterConfig flattenedCraterConfig = new CraterConfig(0, 0, 0.1, 0.5, 1.0, 4.8, -0.5, 0.35, 6.1, 0.15, 0.75, 30, 96, 0.9);
			OctaveNoise3D mountainsNoise =       new OctaveNoise3D(rng, 24, 24, 24, 6, 2.0, 0.5);
			NoiseConfig nc = new NoiseConfig(mountainsNoise, true, 1.5, 0.17, 0.6, 0.0);
			CraterDistributionSettings cds = new CraterDistributionSettings(craterCnt, minsize, maxsize, minStrength, maxStrength, flattenedStart, flattenedEnd, nc, 0.8);
			startTime = System.currentTimeMillis();
			CraterGenerator.distributeCraters(testImg, bowlCraterConfig, flattenedCraterConfig, cds, rng);
			System.out.println("Took " + ((System.currentTimeMillis() - startTime) / 1000) + "s to generate " + craterCnt + " craters.");
			
			for(int i = 0; i < testImg.length; i++) {
				for(int j = 0; j < testImg[0].length; j++) {
					int col = (int)(testImg[i][j] * 255.0);
					col = Math.max(0, Math.min(255, col));
					testRes.setRGB(i, j, col | (col << 8) | (col << 16));
				}
			}
			ImageIO.write(testRes, "png", new File("craters.png"));
		}catch(Exception e) {
			System.err.println("Error: ");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
}