package tholin.planetGen.utils;

import java.util.*;
import java.util.concurrent.*;

import tholin.planetGen.utils.RavineGenerator.RavineConfig;

public class RavineDistributer {
	
	private static final int maxThreads;
	private static final RavineDistributionRunner[] distributionRunners;
	private static final ThreadPoolExecutor threadPool;
	
	static {
		maxThreads = Math.min(GlobalConfiguration.MAX_THREADS, Runtime.getRuntime().availableProcessors());
		distributionRunners = new RavineDistributionRunner[maxThreads];
		for(int i = 0; i < maxThreads; i++) distributionRunners[i] = new RavineDistributionRunner(i, maxThreads);
		threadPool = (ThreadPoolExecutor)Executors.newCachedThreadPool();
	}
	
	public static class RavineDistributionSettings {
		
		public int ravineCount;
		public double minsize;
		public double maxsize;
		public double minlength;
		public double maxlength;
		public double lengthToSizeScale;
		public double minStrength;
		public double maxStrength;
		public double distributionBias;
		public double rimWidthRNGBias;
		public double rimHeightRNGBias;
		public double rimShapeExponentRNGBias;
		
		public RavineDistributionSettings() {
			super();
		}
		
		public RavineDistributionSettings(int ravineCount, double minsize, double maxsize, double minlength, double maxlength, double lengthToSizeScale, double minStrength, double maxStrength, double distributionBias, double rimWidthRNGBias, double rimHeightRNGBias, double rimShapeExponentRNGBias) {
			super();
			this.ravineCount = ravineCount;
			this.minsize = minsize;
			this.maxsize = maxsize;
			this.minlength = minlength;
			this.maxlength = maxlength;
			this.lengthToSizeScale = lengthToSizeScale;
			this.minStrength = minStrength;
			this.maxStrength = maxStrength;
			this.distributionBias = distributionBias;
			this.rimWidthRNGBias = rimWidthRNGBias;
			this.rimHeightRNGBias = rimHeightRNGBias;
			this.rimShapeExponentRNGBias = rimShapeExponentRNGBias;
		}
		
		public RavineDistributionSettings setRavineCount(int ravineCount) {
			this.ravineCount = ravineCount;
			return this;
		}
		
		public RavineDistributionSettings setMinsize(double minsize) {
			this.minsize = minsize;
			return this;
		}
		
		public RavineDistributionSettings setMaxsize(double maxsize) {
			this.maxsize = maxsize;
			return this;
		}
		
		public RavineDistributionSettings setMinlength(double minlength) {
			this.minlength = minlength;
			return this;
		}
		
		public RavineDistributionSettings setMaxlength(double maxlength) {
			this.maxlength = maxlength;
			return this;
		}
		
		public RavineDistributionSettings setLengthToSizeScale(double lengthToSizeScale) {
			this.lengthToSizeScale = lengthToSizeScale;
			return this;
		}
		
		public RavineDistributionSettings setMinStrength(double minStrength) {
			this.minStrength = minStrength;
			return this;
		}
		
		public RavineDistributionSettings setMaxStrength(double maxStrength) {
			this.maxStrength = maxStrength;
			return this;
		}
		
		public RavineDistributionSettings setRimWidthRNGBias(double rimWidthRNGBias) {
			this.rimWidthRNGBias = rimWidthRNGBias;
			return this;
		}
		
		public RavineDistributionSettings setRimHeightRNGBias(double rimHeightRNGBias) {
			this.rimHeightRNGBias = rimHeightRNGBias;
			return this;
		}
		
		public RavineDistributionSettings setRimShapeExponentRNGBias(double rimShapeExponentRNGBias) {
			this.rimShapeExponentRNGBias = rimShapeExponentRNGBias;
			return this;
		}
	}
	
	private static volatile double[][] mapBuffer = null;
	private static volatile double[][] featureMapBuffer = null;
	private static volatile double[][] ravineMapBuffer = null;
	private static volatile RavineGenerator ravineGenerator = null;
	
	public static class RavineDistributionRunner implements Runnable {
		
		private final int indx,threadCount;
		private double[][] map,featureMap,ravineMap;
		private List<RavineInstance> toGenerate;
		private RavineGenerator generator = null;
		private boolean debugProgress;
		private int from,to;
		
		public RavineDistributionRunner(int indx, int threadCount) {
			this.indx = indx;
			this.threadCount = threadCount;
		}
		
		public void prepare(double[][] map, double[][] featureMap, double[][] ravineMap, List<RavineInstance> toGenerate, int width, int height, boolean debugProgress) {
			this.map = map;
			this.featureMap = featureMap;
			this.ravineMap = ravineMap;
			this.toGenerate = toGenerate;
			if(generator == null || generator.getWidth() != width || generator.getHeight() != height) generator = new RavineGenerator(width, height);
			this.debugProgress = debugProgress;
			this.from = (int)((double)height / (double)threadCount * (double)indx);
			this.to = indx == threadCount - 1 ? height : (int)((double)height / (double)threadCount * (double)(indx + 1));
		}
		
		@Override
		public void run() {
			if(debugProgress) ProgressBars.printBar();
			Random rng = new Random();
			for(int i = 0; i < toGenerate.size(); i++) {
				if(debugProgress) ProgressBars.printProgress(i, toGenerate.size());
				RavineInstance ravine = toGenerate.get(i);
				rng.setSeed(ravine.rngSeed);
				generator.genRavine(this.map, this.featureMap, this.ravineMap, ravine.lat1, ravine.lon1, ravine.lat2, ravine.lon2, from, to, ravine.overlayRavine, ravine.finalRavineConfig, rng);
			}
			if(debugProgress) ProgressBars.finishProgress();
		}
	}
	
	private static class RavineInstance {
		public RavineConfig finalRavineConfig;
		public long rngSeed;
		public double lat1,lon1,lat2,lon2;
		public boolean overlayRavine;
		
		public RavineInstance(RavineConfig finalRavineConfig, long rngSeed, double lat1, double lon1, double lat2, double lon2, boolean overlayRavine) {
			super();
			this.finalRavineConfig = finalRavineConfig;
			this.rngSeed = rngSeed;
			this.lat1 = lat1;
			this.lon1 = lon1;
			this.lat2 = lat2;
			this.lon2 = lon2;
			this.overlayRavine = overlayRavine;
		}
	}
	
	public static List<RavineInstance> ravinesToGenerate = new ArrayList<RavineInstance>();
	
	public static void distributeRavines(boolean[][] distributionMap, double[][] map, double[][] featureMap, double[][] ravineMap, RavineConfig ravineConfig, RavineDistributionSettings settings, double planetSizeScale, Random rng, boolean debugProgress) {
		int maxTries = settings.ravineCount * 2;
		int tries = 0;
		int totalGenerated = 0;
		ravinesToGenerate.clear();
		for(int i = 0; i < settings.ravineCount; i++) {
			double targetLen = Maths.biasFunction(rng.nextDouble(), settings.distributionBias) * (settings.maxlength - settings.minlength - 0.005) + settings.minlength;
			
			double lat = rng.nextDouble() * 160.0 + 10.0;
			double lon = rng.nextDouble() * 360.0;
			double lat2,lon2,len;
			//TODO: Abort if this gets stuck.
			do {
				lat2 = rng.nextDouble() * 160.0 + 10.0;
				lon2 = rng.nextDouble() * 360.0;
				len = Maths.gcDistance(lat, lon, lat2, lon2);
			}while(len - targetLen > 0.01 || len - targetLen < 0 || Double.isNaN(len));
			
			boolean shouldPlace = distributionMap == null ? true : distributionMap[(int)(lon / 360.0 * distributionMap.length)][(int)(lat / 180.0 * distributionMap[0].length)];
			shouldPlace &= distributionMap == null ? true : distributionMap[(int)(lon2 / 360.0 * distributionMap.length)][(int)(lat2 / 180.0 * distributionMap[0].length)];
			if(!shouldPlace) {
				i--;
				tries++;
				if(tries >= maxTries) break;
				continue;
			}
			totalGenerated++;
			double iSize = Math.pow((len - settings.minlength) / (settings.maxlength - settings.minlength), settings.lengthToSizeScale);
			double size = iSize * (settings.maxsize - settings.minsize) + settings.minsize;
			double strength = iSize * (settings.maxStrength - settings.minStrength) + settings.minStrength;
			
			RavineConfig finalRavineConfig = new RavineConfig();
			finalRavineConfig.setDistortNoiseConfig(ravineConfig.distortNoiseConfig.clone());
			finalRavineConfig.setRavineStrength(strength);
			finalRavineConfig.setShapeExponent(ravineConfig.shapeExponent + Maths.biasedRNG(rng, settings.rimShapeExponentRNGBias));
			finalRavineConfig.setRimWidth(ravineConfig.rimWidth + Maths.biasedRNG(rng, settings.rimWidthRNGBias));
			finalRavineConfig.setRimHeight(ravineConfig.rimHeight + Maths.biasedRNG(rng, settings.rimHeightRNGBias));
			finalRavineConfig.setRimNoise(ravineConfig.rimNoise);
			finalRavineConfig.setSize(size);
			ravinesToGenerate.add(new RavineInstance(finalRavineConfig, rng.nextLong(), lat, lon, lat2, lon2, false));
		}
		if(ravineGenerator == null || mapBuffer.length != map.length || mapBuffer[0].length != map[0].length) {
			ravineGenerator = new RavineGenerator(map.length, map[0].length);
		}
		if(map != null && (mapBuffer == null || mapBuffer.length < map.length || mapBuffer[0].length < map[0].length)) {
			mapBuffer = new double[map.length][map[0].length];
		}
		//TODO: Make the feature map actually optional (and check if it isn't already)
		if(featureMap != null && (featureMapBuffer == null || featureMapBuffer.length < featureMap.length || featureMapBuffer[0].length < featureMap[0].length)) {
			featureMapBuffer = new double[featureMap.length][featureMap[0].length];
		}
		if(ravineMap != null && (ravineMapBuffer == null || ravineMapBuffer.length < ravineMap.length || ravineMapBuffer[0].length < ravineMap[0].length)) {
			ravineMapBuffer = new double[ravineMap.length][ravineMap[0].length];
		}
		double oldLatScale = 0,oldLonScale = 0;
		if(ravineConfig.rimNoise != null) {
			ravineConfig.rimNoise.noise.initialize(rng);
			oldLatScale = ravineConfig.rimNoise.noiseLatitudeScale;
			oldLonScale = ravineConfig.rimNoise.noiseLongitudeScale;
			ravineConfig.rimNoise.noiseLatitudeScale *= planetSizeScale;
			ravineConfig.rimNoise.noiseLongitudeScale *= planetSizeScale;
		}
		try {
			for(int i = 0; i < map.length; i++) {
				System.arraycopy(map[i], 0, mapBuffer[i], 0, map[i].length);
				if(featureMap != null) System.arraycopy(featureMap[i], 0, featureMapBuffer[i], 0, featureMap[i].length);
				if(ravineMap != null) System.arraycopy(ravineMap[i], 0, ravineMapBuffer[i], 0, ravineMap[i].length);
			}
			for(int i = 0; i < maxThreads; i++) {
				distributionRunners[i].prepare(mapBuffer, featureMap == null ? null : featureMapBuffer, ravineMap == null ? null : ravineMapBuffer, ravinesToGenerate, map.length, map[0].length, debugProgress && i == 0);
				threadPool.submit(distributionRunners[i]);
			}
			while(threadPool.getActiveCount() > 0) {
				try { Thread.sleep(240); } catch(Exception e) { e.printStackTrace(); }
			}
			for(int i = 0; i < map.length; i++) {
				System.arraycopy(mapBuffer[i], 0, map[i], 0, map[i].length);
				if(featureMap != null) System.arraycopy(featureMapBuffer[i], 0, featureMap[i], 0, featureMap[i].length);
				if(ravineMap != null) System.arraycopy(ravineMapBuffer[i], 0, ravineMap[i], 0, ravineMap[i].length);
			}
			System.out.println("Tried to generate " + settings.ravineCount + " ravines, actually generated " + totalGenerated);
		}finally {
			if(ravineConfig.rimNoise != null) {
				ravineConfig.rimNoise.noiseLatitudeScale = oldLatScale;
				ravineConfig.rimNoise.noiseLongitudeScale = oldLonScale;
				ravineConfig.rimNoise.noise.cleanUp();
			}
		}
	}
	
	public static void cleanUp() {
		threadPool.shutdown();
	}
}