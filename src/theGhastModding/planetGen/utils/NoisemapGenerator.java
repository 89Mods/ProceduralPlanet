package theGhastModding.planetGen.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import theGhastModding.planetGen.noise.NoiseConfig;

public class NoisemapGenerator {
	
	private static final int maxThreads;
	private static final NoisemapRunner[] noisemapRunners;
	private static final ThreadPoolExecutor threadPool;
	
	static {
		maxThreads = Runtime.getRuntime().availableProcessors();
		noisemapRunners = new NoisemapRunner[maxThreads];
		for(int i = 0; i < maxThreads; i++) noisemapRunners[i] = new NoisemapRunner(i, maxThreads);
		threadPool = (ThreadPoolExecutor)Executors.newCachedThreadPool();
	}
	
	//Cache synchronization issues be like...
	private static volatile double[][] mapBuffer = null;
	
	public synchronized static void genNoisemap(double[][] mapOutput, NoiseConfig noiseConfig, double[][] noiseMul, double planetSizeScale, boolean debugProgress) {
		if(mapBuffer == null || mapBuffer.length < mapOutput.length || mapBuffer[0].length < mapOutput[0].length) {
			mapBuffer = new double[mapOutput.length][mapOutput[0].length];
		}
		double oldLatScale = noiseConfig.noiseLatitudeScale;
		double oldLonScale = noiseConfig.noiseLongitudeScale;
		noiseConfig.noiseLatitudeScale *= planetSizeScale;
		noiseConfig.noiseLongitudeScale *= planetSizeScale;
		try {
			for(int i = 0; i < maxThreads; i++) {
				noisemapRunners[i].prepare(mapBuffer, mapOutput.length, mapOutput[0].length, noiseConfig, noiseMul, debugProgress && i == 0);
				threadPool.submit(noisemapRunners[i]);
			}
			while(threadPool.getActiveCount() > 0) {
				try { Thread.sleep(240); } catch(Exception e) { e.printStackTrace(); }
			}
			for(int i = 0; i < mapOutput.length; i++) {
				System.arraycopy(mapBuffer[i], 0, mapOutput[i], 0, mapOutput[i].length);
			}
		} finally {
			noiseConfig.noiseLatitudeScale = oldLatScale;
			noiseConfig.noiseLongitudeScale = oldLonScale;
		}
	}
	
	public static void cleanUp() {
		threadPool.shutdown();
	}
	
	private static class NoisemapRunner implements Runnable {
		
		private int indx,threadCount;
		private NoiseConfig noiseConfig;
		private double[][] mapOutput,noiseMul;
		private boolean debugProgress;
		private int from,to,height;
		
		public NoisemapRunner(int indx, int threadCount) {
			this.indx = indx;
			this.threadCount = threadCount;
		}
		
		public void prepare(double[][] mapOutput, int width, int height, NoiseConfig noiseConfig, double[][] noiseMul, boolean debugProgress) {
			this.mapOutput = mapOutput;
			this.noiseConfig = noiseConfig;
			this.noiseMul = noiseMul;
			this.debugProgress = debugProgress;
			this.height = height;
			this.from = (int)((double)width / (double)threadCount * (double)indx);
			this.to = indx == threadCount - 1 ? width : (int)((double)width / (double)threadCount * (double)(indx + 1));
		}
		
		@Override
		public void run() {
			if(debugProgress) ProgressBars.printBar();
			for(int i = 0; i < to - from; i++) {
				if(debugProgress) ProgressBars.printProgress(i, to - from);
				for(int j = 0; j < height; j++) {
					if(noiseMul != null && noiseMul[i + from][j] <= 0) mapOutput[i + from][j] = 0;
					else {
						mapOutput[i + from][j] = NoiseUtils.sampleSpherableNoise(i + from, j, mapOutput.length, mapOutput[0].length, noiseConfig);
						if(noiseMul != null) mapOutput[i + from][j] *= noiseMul[i + from][j];
					}
				}
			}
			if(debugProgress) ProgressBars.finishProgress();
		}
		
	}
	
}
