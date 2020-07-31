package theGhastModding.planetGen.generators;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import edu.cornell.lassp.houle.RngPack.RanMT;
import theGhastModding.planetGen.noise.NoiseConfig;
import theGhastModding.planetGen.noise.NoiseUtils;
import theGhastModding.planetGen.noise.OctaveNoise3D;

public class NewGasGiantBase {
	
	public static void main(String[] args) {
		try {
			int r1 = 140;
			int g1 = 209;
			int b1 = 246;
			int r2 = 57;
			int g2 = 168;
			int b2 = 227;
			
			OctaveNoise3D noise = new OctaveNoise3D(new RanMT(), 20, 20, 20, 12, 2.0, 0.6);
			NoiseConfig nc = new NoiseConfig(noise, false, 1.5, 1.0, 0.5, 0.125);
			BufferedImage img = new BufferedImage(1920, 1920/2, BufferedImage.TYPE_INT_RGB);
			int bands = 4;
			for(int i = 0; i < img.getHeight(); i++) {
				double x = (i * (double)bands) / img.getHeight();
				double y = 0.5 * (Math.sin(x * 2 * Math.PI) + 1.0);
				for(int j = 0; j < img.getWidth(); j++) {
					double val = NoiseUtils.sampleSpherableNoise(j, i, img.getWidth(), img.getHeight(),nc);
					val += 0.5;
					val = Math.max(0, Math.min(1, val));
					double y2 = y * val;
					int r = (int)(y2 * r2 + (1.0 - y2) * r1);
					int g = (int)(y2 * g2 + (1.0 - y2) * g1);
					int b = (int)(y2 * b2 + (1.0 - y2) * b1);
					//r = g = b = (int)(val * 255.0);
					r = Math.min(255, Math.max(0, r));
					g = Math.min(255, Math.max(0, g));
					b = Math.min(255, Math.max(0, b));
					
					img.setRGB(j, i, b | (g << 8) | (r << 16));
				}
			}
			
			ImageIO.write(img, "png", new File("gasgiant-input.png"));
		}catch(Exception e) {
			System.err.println("Error: ");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
}
