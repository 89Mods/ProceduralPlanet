package tholin.planetGen.utils;

import java.awt.*;
import java.awt.image.*;

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
	
	public static BufferedImage renderMap(double[][] map) throws Exception {
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
		return img;
	}
	
	public static BufferedImage render24bit(double[][] map) throws Exception {
		BufferedImage img = new BufferedImage(map.length, map[0].length, BufferedImage.TYPE_INT_RGB);
		for(int i = 0; i < map.length; i++) {
			for(int j = 0; j < map[0].length; j++) {
				double v = map[i][j];
				int col = (int)(v * 16777215.0);
				col = Math.max(0, Math.min(16777215, col));
				img.setRGB(i, j, col);
			}
		}
		return img;
	}
	
	public static BufferedImage render16bit(double[][] map) throws Exception {
		BufferedImage img = new BufferedImage(map.length, map[0].length, BufferedImage.TYPE_INT_RGB);
		for(int i = 0; i < map.length; i++) {
			for(int j = 0; j < map[0].length; j++) {
				double v = map[i][j];
				int col = (int)(v * 65535.0);
				col = Math.max(0, Math.min(65535, col));
				img.setRGB(i, j, col);
			}
		}
		return img;
	}
	
	// With help from https://github.com/Kopernicus/Kopernicus/blob/master/src/Kopernicus/Utility.cs:L730
	public static BufferedImage generateNormalMap(double[][] map, double planetRadius, double mapMaxHeight, double normalStrength) throws Exception {
		BufferedImage img = new BufferedImage(map.length, map[0].length, BufferedImage.TYPE_INT_ARGB);
		double dS = planetRadius * 2.0 * Math.PI / (double)map.length;
		for(int i = 0; i < map.length; i++) {
			for(int j = 0; j < map[0].length; j++) {
				if(j == 0 || j == map[0].length - 1 || map[i][j] < 0.01) {
					img.setRGB(i, j, 0x80808080);
					continue;
				}
				
				double dX = (map[i == 0 ? map.length - 1 : i - 1][j] - map[(i + 1) % map.length][j]) * mapMaxHeight;
				double dY = (map[i][j + 1] - map[i][j - 1]) * mapMaxHeight;
				
				double slopeX = (1.0 + dX / Math.sqrt(dX * dX + dS * dS) * normalStrength) / 2.0;
				double slopeY = (1.0 + dY / Math.sqrt(dY * dY + dS * dS) * normalStrength) / 2.0;
				
				int c1 = (int)Math.max(0, Math.min(255, slopeX * 255.0));
				int c2 = (int)Math.max(0, Math.min(255, slopeY * 255.0));
				img.setRGB(i, j, (c1 << 24) | (c2 << 16) | (c2 << 8) | (c2 << 0));
			}
		}
		return img;
	}
	
}