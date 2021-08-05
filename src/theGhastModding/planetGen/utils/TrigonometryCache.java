package theGhastModding.planetGen.utils;

public class TrigonometryCache {
	
	private double[] acosCache;
	private double stride;
	
	public TrigonometryCache(int width, int height) {
		this((int)(Math.sqrt((double)width * (double)width + (double)height * (double)height) * 8.0 * Math.PI));
	}
	
	public TrigonometryCache(int size) {
		double stride = 1.0 / size;
		this.stride = stride;
		acosCache = new double[size + 1];
		for(int i = 0; i < size + 1; i++) {
			acosCache[i] = Math.acos(stride * i);
		}
	}
	
	public double fastAcos(double x) {
		if(x == -1) return Math.PI;
		x %= 1.0;
		boolean b = x < 0;
		if(b) x = -x;
		int indx = (int)(x / stride);
		int indx2 = indx + (indx == acosCache.length - 1 ? 0 : 1);
		double d = (x - (double)(indx * stride)) / stride;
		x = (1.0 - d) * acosCache[indx] + d * acosCache[indx2];
		if(b) x = (Math.PI / 2.0) + ((Math.PI / 2.0) - x);
		return x;
	}
	
}