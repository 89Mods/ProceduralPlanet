package theGhastModding.planetGen.utils;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;

import javax.imageio.ImageIO;

public class MapUtils {
	
	public static double[] RGB(Color c) {
		return new double[] {c.getRed() / 255.0, c.getGreen() / 255.0, c.getBlue() / 255.0};
	}
	
	public static void factorInColor(double[] color, double multiplier, double[] newColor) {
		multiplier = Math.min(1, multiplier);
		color[0]   = color[0] * (1.0 - multiplier) + newColor[0] * multiplier;
		color[1]   = color[1] * (1.0 - multiplier) + newColor[1] * multiplier;
		color[2]   = color[2] * (1.0 - multiplier) + newColor[2] * multiplier;
	}
	
	public static void printMap(String file, double[][] map) {
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
	
}