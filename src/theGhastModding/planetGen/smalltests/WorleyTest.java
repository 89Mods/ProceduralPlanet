package theGhastModding.planetGen.smalltests;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import edu.cornell.lassp.houle.RngPack.RanMT;
import theGhastModding.planetGen.noise.NoiseUtils;
import theGhastModding.planetGen.noise.OctaveWorley;

public class WorleyTest {
	
	public static void main(String[] args) {
		double[][] noisemap = new double[2048][1024];
		BufferedImage res = new BufferedImage(2048, 1024, BufferedImage.TYPE_INT_RGB);
		RanMT rng = new RanMT(3528608460342643357L);
		//WorleyNoise worley = new WorleyNoise(rng, 32, 32, 32);
		OctaveWorley worley = new OctaveWorley(rng, 32, 32, 32, 5, 2.0, 0.5);
		System.out.println(-33 % 32);
		for(int i = 0; i < 2048; i++) {
			for(int j = 0; j < 1024; j++) {
				//noisemap[i][j] = worley.sample(i / 50.0, j / 50.0, 3.0);
				noisemap[i][j] = (NoiseUtils.sampleSpherableNoise(worley, i, j, 2048, 1024, 1, 1, 0.5) - 0.5) * 1.25 + 0.5 * 1.25;
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
	}
	
}