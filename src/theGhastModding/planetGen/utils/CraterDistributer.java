package theGhastModding.planetGen.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import theGhastModding.planetGen.noise.NoiseConfig;
import theGhastModding.planetGen.utils.CraterGenerator.CraterConfig;

public class CraterDistributer {
	
	private static final int maxThreads;
	private static final DistributionRunner[] distributionRunners;
	private static final ThreadPoolExecutor threadPool;
	
	static {
		maxThreads = Math.min(GlobalConfiguration.MAX_THREADS, Runtime.getRuntime().availableProcessors());
		distributionRunners = new DistributionRunner[maxThreads];
		for(int i = 0; i < maxThreads; i++) distributionRunners[i] = new DistributionRunner(i, maxThreads);
		threadPool = (ThreadPoolExecutor)Executors.newCachedThreadPool();
	}
	
	// Function taken from https://www.youtube.com/watch?v=lctXaT9pxA0
	public static double biasFunction(double x, double bias) {
		// Adjust input to make control feel more linear
		double k = 1.0 - bias;
		k = k * k * k;
		// Equation based on: shadertoy.com/view/Xd2yRd
		return (x * k) / (x * k - x + 1.0);
	}
	
	public static class CraterDistributionSettings {
		
		public int craterCount;
		public double minsize;
		public double maxsize;
		public double minStrength;
		public double maxStrength;
		public double flattenedStart;
		public double flattenedEnd;
		public NoiseConfig mountainsNoise;
		public double distributionBias;
		
		public double ringFunctRNGBias;
		public double p1RNGBias;
		public double p2RNGBias;
		public double craterStrengthRNGBias;
		public double fullPeakRNGBias;
		
		public CraterDistributionSettings() {
			super();
		}
		
		public CraterDistributionSettings(int craterCount, double minsize, double maxsize, double minStrength, double maxStrength, double flattenedStart, double flattenedEnd, NoiseConfig mountainsNoise, double distributionBias) {
			super();
			this.craterCount = craterCount;
			this.minsize = minsize;
			this.maxsize = maxsize;
			this.minStrength = minStrength;
			this.maxStrength = maxStrength;
			this.flattenedStart = flattenedStart;
			this.flattenedEnd = flattenedEnd;
			this.mountainsNoise = mountainsNoise;
			this.distributionBias = distributionBias;
			this.ringFunctRNGBias = 0;
			this.p1RNGBias = 0;
			this.p2RNGBias = 0;
			this.craterStrengthRNGBias = 0;
			this.fullPeakRNGBias = 0;
		}
		
		public CraterDistributionSettings setCraterCount(int craterCount) {
			this.craterCount = craterCount;
			return this;
		}
		
		public CraterDistributionSettings setMinsize(double minsize) {
			this.minsize = minsize;
			return this;
		}
		
		public CraterDistributionSettings setMaxsize(double maxsize) {
			this.maxsize = maxsize;
			return this;
		}
		
		public CraterDistributionSettings setMinStrength(double minStrength) {
			this.minStrength = minStrength;
			return this;
		}
		
		public CraterDistributionSettings setMaxStrength(double maxStrength) {
			this.maxStrength = maxStrength;
			return this;
		}
		
		public CraterDistributionSettings setFlattenedStart(double flattenedStart) {
			this.flattenedStart = flattenedStart;
			return this;
		}
		
		public CraterDistributionSettings setFlattenedEnd(double flattenedEnd) {
			this.flattenedEnd = flattenedEnd;
			return this;
		}
		
		public CraterDistributionSettings setMountainsNoise(NoiseConfig mountainsNoise) {
			this.mountainsNoise = mountainsNoise;
			return this;
		}
		
		public CraterDistributionSettings setDistributionBias(double distributionBias) {
			this.distributionBias = distributionBias;
			return this;
		}
		
		public CraterDistributionSettings setRingFunctRNGBias(double ringFunctRNGBias) {
			this.ringFunctRNGBias = ringFunctRNGBias;
			return this;
		}
		
		public CraterDistributionSettings setP1RNGBias(double p1rngBias) {
			p1RNGBias = p1rngBias;
			return this;
		}
		
		public CraterDistributionSettings setP2RNGBias(double p2rngBias) {
			p2RNGBias = p2rngBias;
			return this;
		}
		
		public CraterDistributionSettings setCraterStrengthRNGBias(double craterStrengthRNGBias) {
			this.craterStrengthRNGBias = craterStrengthRNGBias;
			return this;
		}
		
		public CraterDistributionSettings setFullPeakRNGBias(double fullPeakRNGBias) {
			this.fullPeakRNGBias = fullPeakRNGBias;
			return this;
		}
		
	}
	
	private static volatile double[][] mapBuffer = null;
	private static volatile double[][] craterMapBuffer = null;
	
	private static class DistributionRunner implements Runnable {
		
		private final int indx,threadCount;
		private double[][] map,craterMap,mapOutput,craterMapOutput;
		private List<CraterInstance> toGenerate;
		private CraterGenerator generator;
		private boolean debugProgress;
		private int from,to;
		
		public DistributionRunner(int indx, int threadCount) {
			this.indx = indx;
			this.threadCount = threadCount;
		}
		
		public void prepare(double[][] map, double[][] craterMap, double[][] mapOutput, double[][] craterMapOutput, List<CraterInstance> toGenerate, CraterGenerator generator, int width, int height, boolean debugProgress) {
			this.map = map;
			this.craterMap = craterMap;
			this.mapOutput = mapOutput;
			this.craterMapOutput = craterMapOutput;
			this.toGenerate = toGenerate;
			this.generator = generator;
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
				CraterInstance crater = toGenerate.get(i);
				rng.setSeed(crater.rngSeed);
				generator.genCrater(map, craterMap, mapOutput, craterMapOutput, from, to, crater.latitude, crater.longitude, crater.finalCraterConfig, crater.mountainsNoise, rng);
			}
			if(debugProgress) ProgressBars.finishProgress();
		}
		
	}
	
	private static double biasedRNG(Random rng, double bias) {
		if(bias <= 1e-8) return 0;
		return (rng.nextDouble() * 2.0 - 1.0) * bias;
	}
	
	private static class CraterInstance {
		
		public CraterConfig finalCraterConfig;
		public long rngSeed; // The genCrater function uses some RNG to work. To make sure the craters look identical on all threads, I am using a workaround using Java's dumb RNG and including a seed for it with each crater on the list of craters to be generated.
		public double latitude,longitude;
		public NoiseConfig mountainsNoise;
		
		public CraterInstance(CraterConfig finalCraterConfig, long rngSeed, double latitude, double longitude, NoiseConfig mountainsNoise) {
			super();
			this.finalCraterConfig = finalCraterConfig;
			this.rngSeed = rngSeed;
			this.latitude = latitude;
			this.longitude = longitude;
			this.mountainsNoise = mountainsNoise;
		}
		
	}
	
	public static List<CraterInstance> cratersToGenerate = new ArrayList<CraterInstance>();
	
	public static void distributeCraters(boolean[][] distributionMap, double[][] map, double[][] craterMap, CraterConfig bowlCraterConfig, CraterConfig flattenedCraterConfig, CraterDistributionSettings settings, double planetSizeScale, Random rng, boolean debugProgress) {
		int maxTries = settings.craterCount;
		int tries = 0;
		cratersToGenerate.clear();
		for(int i = 0; i < settings.craterCount; i++) {
			double lat = rng.nextDouble() * 160.0 + 10.0;
			double lon = rng.nextDouble() * 360.0;
			boolean shouldPlace = distributionMap == null ? true : distributionMap[(int)(lon / 360.0 * distributionMap.length)][(int)(lat / 180.0 * distributionMap[0].length)];
			if(!shouldPlace) {
				i--;
				tries++;
				if(tries >= maxTries) break;
				continue;
			}
			double size = rng.nextDouble();
			size = biasFunction(size, settings.distributionBias);
			double cSize = size * (settings.maxsize - settings.minsize);
			cSize += settings.minsize;
			double floorHeight = -1.0;
			double bMul = 1.0;
			double fMul = 0.0;
			if(cSize > settings.flattenedStart) {
				fMul = Math.min(1, (cSize - settings.flattenedStart) / (settings.flattenedEnd - settings.flattenedStart));
				bMul = 1.0 - fMul;
			}
			floorHeight = fMul * flattenedCraterConfig.floorHeight + bMul * bowlCraterConfig.floorHeight;
			CraterConfig finalCraterConfig = new CraterConfig();
			finalCraterConfig.setFloorHeight(floorHeight);
			finalCraterConfig.setSize(cSize);
			finalCraterConfig.setCraterStrength(size * (settings.maxStrength - settings.minStrength) + settings.minStrength + biasedRNG(rng, settings.craterStrengthRNGBias));
			finalCraterConfig.setP1(bowlCraterConfig.p1 * bMul + flattenedCraterConfig.p1 * fMul + biasedRNG(rng, settings.p1RNGBias));
			finalCraterConfig.setP2(bowlCraterConfig.p2 * bMul + flattenedCraterConfig.p2 * fMul + biasedRNG(rng, settings.p2RNGBias));
			finalCraterConfig.setPerturbStrength(bowlCraterConfig.perturbStrength * bMul + flattenedCraterConfig.perturbStrength * fMul);
			finalCraterConfig.setPerturbScale(bowlCraterConfig.perturbScale * bMul + flattenedCraterConfig.perturbScale * fMul);
			finalCraterConfig.setEjectaStrength(bowlCraterConfig.ejectaStrength * bMul + flattenedCraterConfig.ejectaStrength * fMul);
			finalCraterConfig.setEjectaStretch(bowlCraterConfig.ejectaStretch * bMul + flattenedCraterConfig.ejectaStretch * fMul);
			finalCraterConfig.setEjectaPerturbStrength(bowlCraterConfig.ejectaPerturbStrength * bMul + flattenedCraterConfig.ejectaPerturbStrength * fMul);
			finalCraterConfig.setEjectaPerturbScale(bowlCraterConfig.ejectaPerturbScale * bMul + flattenedCraterConfig.ejectaPerturbScale * fMul);
			finalCraterConfig.setRingFunctMul(bowlCraterConfig.ringFunctMul * bMul + flattenedCraterConfig.ringFunctMul * fMul + biasedRNG(rng, settings.ringFunctRNGBias));
			finalCraterConfig.setFullPeakSize(bowlCraterConfig.fullPeakSize + biasedRNG(rng, settings.fullPeakRNGBias));
			finalCraterConfig.setRingThreshold(bowlCraterConfig.ringThreshold);
			cratersToGenerate.add(new CraterInstance(finalCraterConfig, rng.nextLong(), lat - 90.0, lon - 180.0, settings.mountainsNoise));
			//generator.genCrater(map, craterMap, 0, map[0].length, lat - 90.0, lon - 180.0, finalCraterConfig, settings.mountainsNoise, rng);
		}
		if(mapBuffer == null || mapBuffer.length < map.length || mapBuffer[0].length < map[0].length) {
			mapBuffer = new double[map.length][map[0].length];
		}
		if(craterMap != null && (craterMapBuffer == null || craterMapBuffer.length < craterMap.length || craterMapBuffer[0].length < craterMap[0].length)) {
			craterMapBuffer = new double[craterMap.length][craterMap[0].length];
		}
		double oldLatScale = 0,oldLonScale = 0;
		if(settings.mountainsNoise != null) {
			settings.mountainsNoise.noise.initialize(rng);
			oldLatScale = settings.mountainsNoise.noiseLatitudeScale;
			oldLonScale = settings.mountainsNoise.noiseLongitudeScale;
			settings.mountainsNoise.noiseLatitudeScale *= planetSizeScale;
			settings.mountainsNoise.noiseLongitudeScale *= planetSizeScale;
		}
		try {
			for(int i = 0; i < map.length; i++) {
				System.arraycopy(map[i], 0, mapBuffer[i], 0, mapBuffer[i].length);
				if(craterMap != null) System.arraycopy(craterMap[i], 0, craterMapBuffer[i], 0, craterMapBuffer[i].length);
			}
			for(int i = 0; i < maxThreads; i++) {
				distributionRunners[i].prepare(map, craterMap, mapBuffer, craterMap == null ? null : craterMapBuffer, cratersToGenerate, new CraterGenerator(map.length, map[0].length), map.length, map[0].length, debugProgress && i == 0);
				threadPool.submit(distributionRunners[i]);
			}
			while(threadPool.getActiveCount() > 0) {
				try { Thread.sleep(240); } catch(Exception e) { e.printStackTrace(); }
			}
			for(int i = 0; i < map.length; i++) {
				System.arraycopy(mapBuffer[i], 0, map[i], 0, map[i].length);
				if(craterMap != null) System.arraycopy(craterMapBuffer[i], 0, craterMap[i], 0, craterMap[i].length);
			}
		}finally {
			if(settings.mountainsNoise != null) {
				settings.mountainsNoise.noiseLatitudeScale = oldLatScale;
				settings.mountainsNoise.noiseLongitudeScale = oldLonScale;
				settings.mountainsNoise.noise.cleanUp();
			}
		}
	}
	
	public static void cleanUp() {
		threadPool.shutdown();
	}
	
}
