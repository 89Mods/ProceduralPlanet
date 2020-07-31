package theGhastModding.planetGen.utils;

import theGhastModding.planetGen.noise.NoiseConfig;

public class NoisemapGenerator {
	
	public static void genNoisemap(double[][] mapOutput, NoiseConfig noiseConfig, double[][] noiseMul, boolean debugProgress) throws Exception {
		int width = mapOutput.length;
		int height = mapOutput[0].length;
		if(debugProgress) ProgressBars.printBar();
		for(int i = 0; i < width; i++) {
			if(debugProgress) ProgressBars.printProgress(i, width);
			for(int j = 0; j < height; j++) {
				if(noiseMul != null && noiseMul[i][j] <= 0) mapOutput[i][j] = 0;
				else {
					mapOutput[i][j] = NoiseUtils.sampleSpherableNoise(i, j, width, height, noiseConfig);
					if(noiseMul != null) mapOutput[i][j] *= noiseMul[i][j];
				}
			}
		}
		if(debugProgress) ProgressBars.finishProgress();
	}
	
}
