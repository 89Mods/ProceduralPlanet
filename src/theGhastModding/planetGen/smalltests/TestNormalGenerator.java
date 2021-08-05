package theGhastModding.planetGen.smalltests;

import java.awt.image.*;
import java.io.*;

import javax.imageio.ImageIO;

import theGhastModding.planetGen.utils.*;

public class TestNormalGenerator {
	
	public static void main(String[] args) {
		try {
			BufferedImage inImg = ImageIO.read(new File("Esker_Height.png"));
			double[][] hm = new double[inImg.getWidth()][inImg.getHeight()];
			for(int i = 0; i < inImg.getWidth(); i++) {
				for(int j = 0; j < inImg.getHeight(); j++) {
					hm[i][j] = (double)(inImg.getRGB(i, j) & 0xFF) / 255.0;
				}
			}
			BufferedImage normalMap = MapUtils.generateNormalMap(hm, 304000, 15200, 0.3333);
			ImageIO.write(normalMap, "png", new File("Esker_Normal_gen.png"));
		}catch(Exception e) {
			System.err.println("Error: ");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
}