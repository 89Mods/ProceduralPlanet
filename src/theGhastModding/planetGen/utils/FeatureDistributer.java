package theGhastModding.planetGen.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Random;

import edu.cornell.lassp.houle.RngPack.RanMT;
import theGhastModding.planetGen.noise.NoiseConfig;
import theGhastModding.planetGen.utils.CraterDistributer.CraterDistributionSettings;
import theGhastModding.planetGen.utils.CraterGenerator.CraterConfig;
import theGhastModding.planetGen.utils.RavineDistributer.RavineDistributionSettings;
import theGhastModding.planetGen.utils.RavineGenerator.RavineConfig;

public class FeatureDistributer {
	
	public static class FeatureDistributerConfig {
		public NoiseConfig craterDistrConfig;
		public NoiseConfig ravineDistrConfig;
		
		public boolean cratersEnabled;
		public boolean ravinesEnabled;
		public boolean useExclusiveFeatureGeneration;
		
		public double craterNoiseThreshold;
		public double ravineNoiseThreshold;
		
		public CraterConfig bowlCraterConfig;
		public CraterConfig flattenedCraterConfig;
		public RavineConfig ravineConfig;
		
		public FeatureDistributerConfig() {
			this.cratersEnabled = false;
			this.ravinesEnabled = false;
			this.useExclusiveFeatureGeneration = false;
		}
		
		public FeatureDistributerConfig(NoiseConfig craterDistrConfig, NoiseConfig ravineDistrConfig, boolean cratersEnabled, boolean ravinesEnabled, boolean useExclusiveFeatureGeneration, double craterNoiseThreshold, double ravineNoiseThreshold, CraterConfig bowlCraterConfig, CraterConfig flattenedCraterConfig, RavineConfig ravineConfig) {
			this.craterDistrConfig = craterDistrConfig;
			this.ravineDistrConfig = ravineDistrConfig;
			this.cratersEnabled = cratersEnabled;
			this.ravinesEnabled = ravinesEnabled;
			this.useExclusiveFeatureGeneration = useExclusiveFeatureGeneration;
			this.craterNoiseThreshold = craterNoiseThreshold;
			this.ravineNoiseThreshold = ravineNoiseThreshold;
			this.bowlCraterConfig = bowlCraterConfig;
			this.flattenedCraterConfig = flattenedCraterConfig;
			this.ravineConfig = ravineConfig;
		}
		
		public FeatureDistributerConfig setCraterDistrConfig(NoiseConfig craterDistrConfig) {
			this.craterDistrConfig = craterDistrConfig;
			return this;
		}
		
		public FeatureDistributerConfig setRavineDistrConfig(NoiseConfig ravineDistrConfig) {
			this.ravineDistrConfig = ravineDistrConfig;
			return this;
		}
		
		public FeatureDistributerConfig setCratersEnabled(boolean cratersEnabled) {
			this.cratersEnabled = cratersEnabled;
			return this;
		}
		
		public FeatureDistributerConfig setRavinesEnabled(boolean ravinesEnabled) {
			this.ravinesEnabled = ravinesEnabled;
			return this;
		}
		
		public FeatureDistributerConfig setUseExclusiveFeatureGeneration(boolean useExclusiveFeatureGeneration) {
			this.useExclusiveFeatureGeneration = useExclusiveFeatureGeneration;
			return this;
		}
		
		public FeatureDistributerConfig setCraterNoiseThreshold(double craterNoiseThreshold) {
			this.craterNoiseThreshold = craterNoiseThreshold;
			return this;
		}
		
		public FeatureDistributerConfig setRavineNoiseThreshold(double ravineNoiseThreshold) {
			this.ravineNoiseThreshold = ravineNoiseThreshold;
			return this;
		}
		
		public FeatureDistributerConfig setBowlCraterConfig(CraterConfig bowlCraterConfig) {
			this.bowlCraterConfig = bowlCraterConfig;
			return this;
		}
		
		public FeatureDistributerConfig setFlattenedCraterConfig(CraterConfig flattenedCraterConfig) {
			this.flattenedCraterConfig = flattenedCraterConfig;
			return this;
		}
		
		public FeatureDistributerConfig setRavineConfig(RavineConfig ravineConfig) {
			this.ravineConfig = ravineConfig;
			return this;
		}
		
		public void serialize(DataOutputStream out) throws Exception {
			out.writeBoolean(craterDistrConfig != null);
			if(craterDistrConfig != null) craterDistrConfig.serialize(out);
			out.writeBoolean(ravineDistrConfig != null);
			if(ravineDistrConfig != null) ravineDistrConfig.serialize(out);
			out.writeBoolean(cratersEnabled);
			out.writeBoolean(ravinesEnabled);
			out.writeBoolean(useExclusiveFeatureGeneration);
			out.writeDouble(craterNoiseThreshold);
			out.writeDouble(ravineNoiseThreshold);
			out.writeBoolean(bowlCraterConfig != null);
			if(bowlCraterConfig != null) bowlCraterConfig.serialize(out);
			out.writeBoolean(flattenedCraterConfig != null);
			if(flattenedCraterConfig != null) flattenedCraterConfig.serialize(out);
			out.writeBoolean(ravineConfig != null);
			if(ravineConfig != null) ravineConfig.serialize(out);
		}
		
		public static FeatureDistributerConfig deserialize(DataInputStream in) throws Exception {
			FeatureDistributerConfig res = new FeatureDistributerConfig();
			res.craterDistrConfig = in.readBoolean() ? NoiseConfig.deserialize(in) : null;
			res.ravineDistrConfig = in.readBoolean() ? NoiseConfig.deserialize(in) : null;
			res.cratersEnabled = in.readBoolean();
			res.ravinesEnabled = in.readBoolean();
			res.useExclusiveFeatureGeneration = in.readBoolean();
			res.craterNoiseThreshold = in.readDouble();
			res.ravineNoiseThreshold = in.readDouble();
			res.bowlCraterConfig = in.readBoolean() ? CraterConfig.deserialize(in) : null;
			res.flattenedCraterConfig = in.readBoolean() ? CraterConfig.deserialize(in) : null;
			res.ravineConfig = in.readBoolean() ? RavineConfig.deserialize(in) : null;
			return res;
		}
	}
	
	public static void distributeFeatures(FeatureDistributerConfig config, int distrWidth, int distrHeight, double[][] map, double[][] featureMap, double[][] craterMap, double[][] ravineMap, boolean[][] craterDistributionMap, boolean[][] ravineDistributionMap, CraterDistributionSettings craterDistSettings, RavineDistributionSettings ravineDistSettings, double planetSizeScale, Random rng, boolean debugProgress) {
		double[][] temp = new double[distrWidth][distrHeight];
		if(config.useExclusiveFeatureGeneration && config.craterDistrConfig != null) {
			NoisemapGenerator.genNoisemap(new RanMT().seedCompletely(rng.nextLong()), temp, config.craterDistrConfig, null, planetSizeScale, debugProgress);
		}
		if(config.cratersEnabled && craterDistSettings.craterCount > 0) {
			boolean[][] finalCraterDistr = new boolean[distrWidth][distrHeight];
			if(config.useExclusiveFeatureGeneration && craterDistributionMap != null) for(int i = 0; i < distrWidth; i++) for(int j = 0; j < distrHeight; j++) finalCraterDistr[i][j] = craterDistributionMap[i][j] & (config.craterDistrConfig != null || temp[i][j] >= config.craterNoiseThreshold);
			else if(craterDistributionMap != null) for(int i = 0; i < distrWidth; i++) for(int j = 0; j < distrHeight; j++) finalCraterDistr[i][j] = craterDistributionMap[i][j];
			if(!config.useExclusiveFeatureGeneration && config.craterDistrConfig != null) {
				NoisemapGenerator.genNoisemap(new RanMT().seedCompletely(rng.nextLong()), temp, config.craterDistrConfig, null, planetSizeScale, debugProgress);
				for(int i = 0; i < distrWidth; i++) {
					for(int j = 0; j < distrHeight; j++) {
						finalCraterDistr[i][j] &= temp[i][j] >= config.craterNoiseThreshold;
					}
				}
			}
			double[][] temp2 = new double[featureMap.length][featureMap[0].length];
			for(int i = 0; i < featureMap.length; i++) for(int j = 0; j < featureMap[0].length; j++) temp2[i][j] = featureMap[i][j];
			CraterDistributer.distributeCraters(finalCraterDistr, map, featureMap, config.bowlCraterConfig, config.flattenedCraterConfig == null ? config.bowlCraterConfig : config.flattenedCraterConfig, craterDistSettings, planetSizeScale, new RanMT().seedCompletely(rng.nextLong()), debugProgress);
			//I hate how this works. Absolutely hate it. But got no other choice here.
			for(int i = 0; i < featureMap.length; i++) {
				for(int j = 0; j < featureMap[0].length; j++) {
					craterMap[i][j] += (featureMap[i][j] - temp2[i][j]);
				}
			}
		}
		for(int i = 0; i < featureMap.length; i++) {
			for(int j = 0; j < featureMap[0].length; j++) {
				featureMap[i][j] = Math.min(0.75, featureMap[i][j]);
			}
		}
		if(config.ravinesEnabled && ravineDistSettings.ravineCount > 0) {
			boolean[][] finalRavineDistr = new boolean[distrWidth][distrHeight];
			if(config.useExclusiveFeatureGeneration && craterDistributionMap != null) for(int i = 0; i < distrWidth; i++) for(int j = 0; j < distrHeight; j++) finalRavineDistr[i][j] = !craterDistributionMap[i][j] & (config.craterDistrConfig != null || temp[i][j] < config.craterNoiseThreshold);
			else if(ravineDistributionMap != null) for(int i = 0; i < distrWidth; i++) for(int j = 0; j < distrHeight; j++) finalRavineDistr[i][j] = ravineDistributionMap[i][j];
			if(!config.useExclusiveFeatureGeneration && config.ravineDistrConfig != null) {
				NoisemapGenerator.genNoisemap(new RanMT().seedCompletely(rng.nextLong()), temp, config.ravineDistrConfig, null, planetSizeScale, debugProgress);
				for(int i = 0; i < distrWidth; i++) {
					for(int j = 0; j < distrHeight; j++) {
						finalRavineDistr[i][j] &= temp[i][j] >= config.ravineNoiseThreshold;
					}
				}
			}
			RavineDistributer.distributeRavines(finalRavineDistr, map, featureMap, ravineMap, config.ravineConfig, ravineDistSettings, planetSizeScale, new RanMT().seedCompletely(rng.nextLong()), debugProgress);
		}
	}
	
	public static void cleanUp() {
		CraterDistributer.cleanUp();
		RavineDistributer.cleanUp();
	}
}