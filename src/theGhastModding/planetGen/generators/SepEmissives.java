package theGhastModding.planetGen.generators;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class SepEmissives {
	
	public static void main(String[] args) {
		try {
			BufferedImage in = ImageIO.read(new File("mercador.png"));
			BufferedImage out = new BufferedImage(in.getWidth(), in.getHeight(), BufferedImage.TYPE_INT_RGB);
			BufferedImage emissives = new BufferedImage(in.getWidth(), in.getHeight(), BufferedImage.TYPE_INT_ARGB);
			//Add polar hexagon
			double size = 1.0 / 5.0;
			int res = in.getWidth();
			for(int i = 0; i < res; i++) {
				for(int j = 0; j < res; j++) {
					double x = (i / (double)res - 0.5);
					double z = (j / (double)res - 0.5);
					if(!isInsideHexagon(x, z)) continue;
					x *= size;
					z *= size;
					double[] latlong = latlong(x, 1.0, z);
					int imgX = (int)((latlong[1] + 180.0) / 360.0 * in.getWidth());
					int imgY = (int)(latlong[0] / 180.0 * in.getHeight());
					if(imgX < 0 || imgY < 0) continue;
					if((out.getRGB(imgX, imgY) & 0x00FFFFFF) != 0) continue;
					int rgb = in.getRGB(imgX, imgY);
					int r = (rgb >> 16) & 0xFF;
					int g = (rgb >> 8) & 0xFF;
					int b = (rgb >> 0) & 0xFF;
					r = (int)(r * 0.7);
					g = (int)(g * 0.7);
					b = (int)(b * 0.7);
					if(r > 255) r = 255; // Remove and set multiplier to 1.5 and let it multiply multiple times per pixel for crazy colors
					if(g > 255) g = 255;
					if(b > 255) b = 255;
					out.setRGB(imgX, imgY, b | (g << 8) | (r << 16));
				}
			}
			for(int i = 0; i < 90; i++) {
				for(int j = 1; j < in.getWidth(); j++) {
					if((out.getRGB(j, i) & 0x00FFFFFF) == 0) {
						out.setRGB(j, i, out.getRGB(j - 1, i));
					}
				}
			}
			for(int i = 0; i < in.getWidth(); i++) {
				for(int j = 0; j < in.getHeight(); j++) { //TODO: Set starting value to 45 after re-enabling polar hexagon
					if((out.getRGB(i, j) & 0x00FFFFFF) == 0) {
						int rgb = in.getRGB(i, j);
						int r = (rgb >> 16) & 0xFF;
						int g = (rgb >> 8) & 0xFF;
						int b = (rgb >> 0) & 0xFF;
						float brightness = Color.RGBtoHSB(r, g, b, null)[2];
						//double emAlpha = Math.max(0, Math.min(brightness / 0.75, 1.0) - 0.5) * (1.0 / 0.5);
						double emAlpha = brightness;
						int a = (int)(emAlpha * 255.0);
						a = Math.max(0, Math.min(255, a));
						emissives.setRGB(i, j,  0x00FFFFFF | (a << 24));
						double mul = Math.max(0, Math.min(1.0, 1.0 - emAlpha + 0.1));
						if(mul >= 0.95) out.setRGB(i, j, b | (g << 8) | (r << 16));
						else {
							r = (int)(r * mul + (1.0 - mul) * 115);
							g = (int)(g * mul + (1.0 - mul) * 63);
							b = (int)(b * mul + (1.0 - mul) * 75);
							//r = (int)(r * mul);
							//b = g = (int)(g * mul);
						}
						out.setRGB(i, j, b | (g << 8) | (r << 16));
					}
				}
			}
			
			ImageIO.write(out, "png", new File("mercador_fixed.png"));
			ImageIO.write(emissives, "png", new File("emissives.png"));
		}catch(Exception e) {
			System.err.println("Error: ");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private static boolean isInsideHexagon(double x, double y) {
		return Math.abs(y) < Math.sqrt(3) * Math.min(0.5 - Math.abs(x), 0.25);
	}
	
	public static double[] latlong(double x, double y, double z) {
		double dist = Math.sqrt(x * x + y * y + z * z);
		x /= dist;
		y /= dist;
		z /= dist;
		
		double latitude = Math.acos(y) * 57.29577951308232;
		double longitude = ((270 + (Math.atan2(x , z)) * 180 / Math.PI) % 360) -180;
		
		return new double[] {latitude, longitude};// 		return heightmap.sampleHeightmap((longitude + 90.0) / 360.0, latitude / 180.0);
	}
	
}
