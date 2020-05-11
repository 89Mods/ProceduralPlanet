package theGhastModding.planetGen.smalltests;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Random;

import javax.imageio.ImageIO;

import theGhastModding.planetGen.utils.CraterGenerator;

public class CraterGeneratorTest {
	
	public static void main(String[] args) {
		try {
			double[][] testImg = new double[4096][2048];
			for(int i = 0; i < testImg.length; i++) Arrays.fill(testImg[i], 0.8);
			BufferedImage testRes = new BufferedImage(4096, 2048, BufferedImage.TYPE_INT_RGB);
			
			Random rng = new Random(("fuck".hashCode() << 6));
			int currCraterCnt = 4;
			double currCraterSize = 2048;
			double currCraterStrength = 0.75;
			int octaves = 7;
			long startTime;
			for(int i = 0; i < octaves; i++) {
				System.out.println((i + 1) + "/" + octaves);
				System.out.println(currCraterCnt);
				startTime = System.currentTimeMillis();
				for(int j = 0; j < currCraterCnt; j++) {
					double lon = rng.nextDouble() * 360.0;
					double lat = rng.nextDouble() * 160.0 + 10.0;
					if(currCraterSize <= 100) {
						CraterGenerator.genBowlCrater(testImg, lat - 90.0, lon - 180.0, (int)currCraterSize, currCraterStrength, 0.25, 0.5, 0.48, 3.6, 4.8, rng);
					}else {
						CraterGenerator.genFlattenedCrater(testImg, lat - 90.0, lon - 180.0, (int)currCraterSize, currCraterStrength, 0.25, 0.5, 0.48, 8, rng);
					}
				}
				System.out.println(String.format("%#.2f", (double)(System.currentTimeMillis() - startTime) / (double)currCraterCnt));
				currCraterCnt *= 2;
				currCraterSize /= 2.0;
				currCraterStrength *= 0.8;
			}
			
			for(int i = 0; i < 4096; i++) {
				for(int j = 0; j < 2048; j++) {
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