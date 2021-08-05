package tholin.planetGen.smalltests;

import java.awt.image.*;
import java.io.*;

import javax.imageio.ImageIO;
import javax.swing.*;

import edu.cornell.lassp.houle.RngPack.RanMT;
import tholin.planetGen.noise.*;
import tholin.planetGen.utils.*;

public class WorleyTest {
	
	public static void main(String[] args) {
		double[][] noisemap = new double[2048][1024];
		BufferedImage res = new BufferedImage(2048, 1024, BufferedImage.TYPE_INT_RGB);
		RanMT rng = new RanMT(3528608460342643357L);
		//WorleyNoise worley = new WorleyNoise(rng, 32, 32, 32);
		OctaveWorley worley = new OctaveWorley(32, 32, 32, 5, 2.0, 0.5);
		NoiseConfig nc = new NoiseConfig(worley).setIsRidged(false).setNoiseStrength(1.25).setNoiseScale(1).setDistortStrength(0.5).setNoiseOffset(0);
		System.out.println(-33 % 32);
		try {
			long startTime = System.currentTimeMillis();
			NoisemapGenerator.genNoisemap(rng, noisemap, nc, null, 1.0, true);
			System.out.println(System.currentTimeMillis() - startTime);
		} catch(Exception e) {
			System.err.println("Error generating noisemap: ");
			e.printStackTrace();
			System.exit(1);
		}
		//worley.initialize(rng);
		for(int i = 0; i < 2048; i++) {
			for(int j = 0; j < 1024; j++) {
				//noisemap[i][j] = worley.sample(i / 50.0, j / 50.0, 3.0);
				noisemap[i][j] = (noisemap[i][j] - 0.5) * 1.25 + 0.5 * 1.25;
			}
		}
		for(int i = 0; i < 2048; i++) {
			for(int j = 0; j < 1024; j++) {
				noisemap[i][j] = Math.max(0, Math.min(1, noisemap[i][j]));
				int col = (int)(noisemap[i][j] * 255.0);
				res.setRGB(i, j, col | (col << 8) | (col << 16));
			}
		}
		JOptionPane.showMessageDialog(null, new JLabel(new ImageIcon(res)), "a", JOptionPane.INFORMATION_MESSAGE);
		try {
			ImageIO.write(res, "png", new File("worley.png"));
		}catch(Exception e) {
			e.printStackTrace();
		}
		NoisemapGenerator.cleanUp();
	}
	
}