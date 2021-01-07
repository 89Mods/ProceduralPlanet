package theGhastModding.planetGen.generators;

import java.awt.image.BufferedImage;

public class GeneratorResult {
	
	public BufferedImage heightmap   = null;
	public BufferedImage heightmap16 = null;
	public BufferedImage heightmap24 = null;
	public double[][] heightmapRaw   = null;
	public BufferedImage colorMap    = null;
	public BufferedImage biomeMap    = null;
	public BufferedImage oceanMap    = null;
	
	public GeneratorResult() {
		
	}
	
}