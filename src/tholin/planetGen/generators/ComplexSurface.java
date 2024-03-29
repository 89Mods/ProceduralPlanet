package tholin.planetGen.generators;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.imageio.ImageIO;
import edu.cornell.lassp.houle.RngPack.RanMT;
import tholin.planetGen.noise.*;
import tholin.planetGen.utils.*;

public class ComplexSurface {
	
	public static class ComplexGenSettings {
		
		public int width = 4096;
		public int height = 2048;
		public int colorMapWidth = width;
		public int colorMapHeight = height;
		public int planetRadius = 600000;
		
		public double baseOceanFactor = -0.1;
		public double basePoleRadius = 0.106;
		public double beachThreshold = 0.015 / 12.0;
		public double mountainsFadeStart = 0.1;
		public double mountainsFadeEnd = 0.25;
		public double hillsFadeStart = 0.11;
		public double hillsFadeEnd = 0.35;
		public double mountainColorFadeStart = 0.15;
		public double mountainColorFadeEnd = 0.3;
		public double basePeaksFadeStart = 0.45;
		public double basePeaksFadeEnd = 0.55;
		public double baseDesertFadeStart = 45;
		public double baseDesertFadeEnd = 20;
		public double baseSnowFadeStart = 0.5;
		public double baseSnowFadeEnd = 0.6;
		public double snowLatitudeFadeDistance = 10.0;
		public double taigaRadiusMul = 1.7;
		
		public double planetTemperature = 0;
		
		public NoiseConfig continentNoise            = new NoiseConfig(new OctaveNoise3D(12, 12, 12, 6 , 2.0, 0.6 )).setIsRidged(false).setNoiseStrength(12).setNoiseScale(1.25).setDistortStrength(0.25);
		public NoiseConfig mountainNoise             = new NoiseConfig(new OctaveNoise3D(16, 16, 16, 6 , 2.0, 0.6 )).setIsRidged(false).setNoiseStrength(1).setNoiseScale(0.18).setDistortStrength(0.25).setNoiseOffset(0.375);
		public NoiseConfig lakeNoiseMul              = new NoiseConfig(new OctaveNoise3D(8,  8,  8,  5 , 2.0, 0.6 )).setIsRidged(false).setNoiseStrength(1).setNoiseScale(1.0).setDistortStrength(0.25).setNoiseOffset(0.375);
		public NoiseConfig lakeNoise                 = new NoiseConfig(new OctaveNoise3D(16, 16, 16, 4 , 2.0, 0.6 )).setIsRidged(false).setNoiseStrength(1).setNoiseScale(0.3).setDistortStrength(0.25).setNoiseOffset(0.375);
		public NoiseConfig desertNoise               = new NoiseConfig(new OctaveNoise3D(8,  8,  8,  5 , 2.0, 0.6 )).setIsRidged(false).setNoiseStrength(1).setNoiseScale(1.0).setDistortStrength(0.25).setNoiseOffset(0.375);
		public NoiseConfig taigaNoise                = new NoiseConfig(new OctaveNoise3D(8,  8,  8,  5 , 2.0, 0.6 )).setIsRidged(false).setNoiseStrength(1).setNoiseScale(1.0).setDistortStrength(0.25).setNoiseOffset(0.375);
		public NoiseConfig groundNoiseLargeDetail    = new NoiseConfig(new OctaveNoise3D(16, 16, 16, 8 , 2.0, 0.7 )).setIsRidged(false).setNoiseStrength(0.75).setNoiseScale(0.5).setDistortStrength(0.2).setNoiseOffset(0.25);
		public NoiseConfig groundNoiseMediumDetail   = new NoiseConfig(new OctaveNoise3D(20, 20, 20, 6 , 2.0, 0.64)).setIsRidged(false).setNoiseStrength(0.2).setNoiseScale(0.14).setDistortStrength(0.2).setNoiseOffset(0.25);
		public NoiseConfig groundNoiseSmallDetail    = new NoiseConfig(new OctaveNoise3D(30, 30, 30, 6 , 2.0, 0.64)).setIsRidged(false).setNoiseStrength(0.075).setNoiseScale(0.085).setDistortStrength(0.2).setNoiseOffset(0.25);
		public NoiseConfig hillNoise                 = new NoiseConfig(new OctaveNoise3D(20, 20, 20, 9 , 2.0, 0.55)).setIsRidged(true ).setNoiseStrength(0.35).setNoiseScale(0.16).setDistortStrength(0.5).setNoiseOffset(0.25);
		public NoiseConfig mountainsNoise            = new NoiseConfig(new OctaveNoise3D(24, 24, 24, 11, 2.0, 0.5 )).setIsRidged(true ).setNoiseStrength(2.9).setNoiseScale(0.17).setDistortStrength(0.6).setNoiseOffset(0); //TODO: Set to 16 octaves before use
		public NoiseConfig colorNoise                = new NoiseConfig(new OctaveNoise3D(20, 20, 20, 6 , 2.0, 0.72)).setIsRidged(true ).setNoiseStrength(1).setNoiseScale(0.25).setDistortStrength(0.5).setNoiseOffset(0);
		public NoiseConfig polesPerturbNoise         = new NoiseConfig(new OctaveNoise3D(16, 16, 16, 3 , 2.0, 0.6 )).setIsRidged(false).setNoiseStrength(1).setNoiseScale(0.5).setDistortStrength(0.35).setNoiseOffset(0);
		public NoiseConfig mountainWorley            = new NoiseConfig(new WorleyNoise(  64, 64, 64               )).setIsRidged(false).setNoiseStrength(1).setNoiseScale(0.015).setDistortStrength(0.01).setNoiseOffset(0);
		
		public double[] biomeColorOceans          = MapUtils.RGB(new Color(55, 98, 171));
		public double[] biomeColorLowlands        = MapUtils.RGB(new Color(131, 188, 46));
		public double[] biomeColorHills           = MapUtils.RGB(new Color(93, 133, 42));
		public double[] biomeColorMountains       = MapUtils.RGB(new Color(167, 167, 167));
		public double[] biomeColorDesert          = MapUtils.RGB(new Color(234, 191, 111));
		public double[] biomeColorDesertHills     = MapUtils.RGB(new Color(178, 145, 83));
		public double[] biomeColorDesertMountains = MapUtils.RGB(new Color(127, 103, 59));
		public double[] biomeColorTaiga           = MapUtils.RGB(new Color(199, 143, 223));
		public double[] biomeColorTaigaHills      = MapUtils.RGB(new Color(113, 81, 127));
		public double[] biomeColorLakes           = MapUtils.RGB(new Color(40, 72, 127));
		public double[] biomeColorNorthPole       = MapUtils.RGB(new Color(255, 255, 255));
		public double[] biomeColorSouthPole       = MapUtils.RGB(new Color(190, 190, 190));
		public double[] biomeColorBeaches         = MapUtils.RGB(new Color(250, 242, 183));
		public double[] biomeColorPeaks           = MapUtils.RGB(new Color(220, 220, 220));
		public double[] biomeColorSnow            = MapUtils.RGB(new Color(220, 220, 220));
		public double[] biomeColorSnowyHills      = MapUtils.RGB(new Color(220, 220, 220));
		
		public double[] lowlandColor   = MapUtils.RGB(new Color(50, 62, 36));
		public double[] hillsColor     = MapUtils.RGB(new Color(51, 56, 41));
		public double[] mountainsColor = MapUtils.RGB(new Color(84, 84, 84));
		public double[] desertColor    = MapUtils.RGB(new Color(132, 116, 87));
		public double[] taigaColor     = MapUtils.RGB(new Color(73, 91, 65));
		public double[] oceansColor    = MapUtils.RGB(new Color(18, 26, 40));
		public double[] polesColor     = MapUtils.RGB(new Color(174, 198, 211));
		public double[] beachesColor   = MapUtils.RGB(new Color(169, 156, 117));
		public double[] peaksColor     = MapUtils.RGB(new Color(174, 198, 211));
		public double[] snowColor      = MapUtils.RGB(new Color(174, 198, 211));
		
		public static ComplexGenSettings deserialize(DataInputStream in) throws Exception {
			ComplexGenSettings res = new ComplexGenSettings();
			res.width = in.readInt();
			res.height = in.readInt();
			res.planetRadius = in.readInt();
			res.baseOceanFactor = in.readDouble();
			res.basePoleRadius = in.readDouble();
			res.beachThreshold = in.readDouble();
			res.mountainsFadeStart = in.readDouble();
			res.mountainsFadeEnd = in.readDouble();
			res.hillsFadeStart = in.readDouble();
			res.hillsFadeEnd = in.readDouble();
			res.mountainColorFadeStart = in.readDouble();
			res.mountainColorFadeEnd = in.readDouble();
			res.basePeaksFadeStart = in.readDouble();
			res.basePeaksFadeEnd = in.readDouble();
			res.baseDesertFadeStart = in.readDouble();
			res.baseDesertFadeEnd = in.readDouble();
			res.baseSnowFadeStart = in.readDouble();
			res.baseSnowFadeEnd = in.readDouble();
			res.snowLatitudeFadeDistance = in.readDouble();
			res.planetTemperature = in.readDouble();
			res.colorNoise = NoiseConfig.deserialize(in);
			res.mountainNoise = NoiseConfig.deserialize(in);
			res.lakeNoiseMul = NoiseConfig.deserialize(in);
			res.lakeNoise = NoiseConfig.deserialize(in);
			res.desertNoise = NoiseConfig.deserialize(in);
			res.taigaNoise = NoiseConfig.deserialize(in);
			res.groundNoiseLargeDetail = NoiseConfig.deserialize(in);
			res.groundNoiseMediumDetail = NoiseConfig.deserialize(in);
			res.groundNoiseSmallDetail = NoiseConfig.deserialize(in);
			res.hillNoise = NoiseConfig.deserialize(in);
			res.mountainsNoise = NoiseConfig.deserialize(in);
			res.colorNoise = NoiseConfig.deserialize(in);
			res.polesPerturbNoise = NoiseConfig.deserialize(in);
			res.mountainWorley = NoiseConfig.deserialize(in);
			res.lowlandColor = new double[] {in.readDouble(), in.readDouble(), in.readDouble()};
			res.hillsColor = new double[] {in.readDouble(), in.readDouble(), in.readDouble()};
			res.mountainsColor = new double[] {in.readDouble(), in.readDouble(), in.readDouble()};
			res.desertColor = new double[] {in.readDouble(), in.readDouble(), in.readDouble()};
			res.taigaColor = new double[] {in.readDouble(), in.readDouble(), in.readDouble()};
			res.oceansColor = new double[] {in.readDouble(), in.readDouble(), in.readDouble()};
			res.polesColor = new double[] {in.readDouble(), in.readDouble(), in.readDouble()};
			res.beachesColor = new double[] {in.readDouble(), in.readDouble(), in.readDouble()};
			res.peaksColor = new double[] {in.readDouble(), in.readDouble(), in.readDouble()};
			res.snowColor = new double[] {in.readDouble(), in.readDouble(), in.readDouble()};
			res.biomeColorOceans = new double[] {in.readDouble(), in.readDouble(), in.readDouble()};
			res.biomeColorLowlands = new double[] {in.readDouble(), in.readDouble(), in.readDouble()};
			res.biomeColorHills = new double[] {in.readDouble(), in.readDouble(), in.readDouble()};
			res.biomeColorMountains = new double[] {in.readDouble(), in.readDouble(), in.readDouble()};
			res.biomeColorDesert = new double[] {in.readDouble(), in.readDouble(), in.readDouble()};
			res.biomeColorDesertHills = new double[] {in.readDouble(), in.readDouble(), in.readDouble()};
			res.biomeColorDesertMountains = new double[] {in.readDouble(), in.readDouble(), in.readDouble()};
			res.biomeColorTaiga = new double[] {in.readDouble(), in.readDouble(), in.readDouble()};
			res.biomeColorTaigaHills = new double[] {in.readDouble(), in.readDouble(), in.readDouble()};
			res.biomeColorLakes = new double[] {in.readDouble(), in.readDouble(), in.readDouble()};
			res.biomeColorNorthPole = new double[] {in.readDouble(), in.readDouble(), in.readDouble()};
			res.biomeColorSouthPole = new double[] {in.readDouble(), in.readDouble(), in.readDouble()};
			res.biomeColorBeaches = new double[] {in.readDouble(), in.readDouble(), in.readDouble()};
			res.biomeColorPeaks = new double[] {in.readDouble(), in.readDouble(), in.readDouble()};
			res.biomeColorSnow = new double[] {in.readDouble(), in.readDouble(), in.readDouble()};
			res.biomeColorSnowyHills = new double[] {in.readDouble(), in.readDouble(), in.readDouble()};
			return res;
		}
		
		public void serialize(DataOutputStream out) throws Exception {
			out.writeInt(width);
			out.writeInt(height);
			out.writeInt(planetRadius);
			out.writeDouble(baseOceanFactor);
			out.writeDouble(basePoleRadius);
			out.writeDouble(beachThreshold);
			out.writeDouble(mountainsFadeStart);
			out.writeDouble(mountainsFadeEnd);
			out.writeDouble(hillsFadeStart);
			out.writeDouble(hillsFadeEnd);
			out.writeDouble(mountainColorFadeStart);
			out.writeDouble(mountainColorFadeEnd);
			out.writeDouble(basePeaksFadeStart);
			out.writeDouble(basePeaksFadeEnd);
			out.writeDouble(basePeaksFadeStart);
			out.writeDouble(baseDesertFadeStart);
			out.writeDouble(baseDesertFadeEnd);
			out.writeDouble(baseSnowFadeStart);
			out.writeDouble(baseSnowFadeEnd);
			out.writeDouble(snowLatitudeFadeDistance);
			out.writeDouble(planetTemperature);
			continentNoise.serialize(out);
			mountainNoise.serialize(out);
			lakeNoiseMul.serialize(out);
			lakeNoise.serialize(out);
			desertNoise.serialize(out);
			taigaNoise.serialize(out);
			groundNoiseLargeDetail.serialize(out);
			groundNoiseMediumDetail.serialize(out);
			groundNoiseSmallDetail.serialize(out);
			hillNoise.serialize(out);
			mountainsNoise.serialize(out);
			colorNoise.serialize(out);
			polesPerturbNoise.serialize(out);
			mountainWorley.serialize(out);
			out.writeDouble(lowlandColor[0]); out.writeDouble(lowlandColor[1]); out.writeDouble(lowlandColor[2]);
			out.writeDouble(hillsColor[0]); out.writeDouble(hillsColor[1]); out.writeDouble(hillsColor[2]);
			out.writeDouble(mountainsColor[0]); out.writeDouble(mountainsColor[1]); out.writeDouble(mountainsColor[2]);
			out.writeDouble(desertColor[0]); out.writeDouble(desertColor[1]); out.writeDouble(desertColor[2]);
			out.writeDouble(taigaColor[0]); out.writeDouble(taigaColor[1]); out.writeDouble(taigaColor[2]);
			out.writeDouble(oceansColor[0]); out.writeDouble(oceansColor[1]); out.writeDouble(oceansColor[2]);
			out.writeDouble(polesColor[0]); out.writeDouble(polesColor[1]); out.writeDouble(polesColor[2]);
			out.writeDouble(beachesColor[0]); out.writeDouble(beachesColor[1]); out.writeDouble(beachesColor[2]);
			out.writeDouble(peaksColor[0]); out.writeDouble(peaksColor[1]); out.writeDouble(peaksColor[2]);
			out.writeDouble(snowColor[0]); out.writeDouble(snowColor[1]); out.writeDouble(snowColor[2]);
			out.writeDouble(biomeColorOceans[0]); out.writeDouble(biomeColorOceans[1]); out.writeDouble(biomeColorOceans[2]);
			out.writeDouble(biomeColorLowlands[0]); out.writeDouble(biomeColorLowlands[1]); out.writeDouble(biomeColorLowlands[2]);
			out.writeDouble(biomeColorHills[0]); out.writeDouble(biomeColorHills[1]); out.writeDouble(biomeColorHills[2]);
			out.writeDouble(biomeColorMountains[0]); out.writeDouble(biomeColorMountains[1]); out.writeDouble(biomeColorMountains[2]);
			out.writeDouble(biomeColorDesert[0]); out.writeDouble(biomeColorDesert[1]); out.writeDouble(biomeColorDesert[2]);
			out.writeDouble(biomeColorDesertHills[0]); out.writeDouble(biomeColorDesertHills[1]); out.writeDouble(biomeColorDesertHills[2]);
			out.writeDouble(biomeColorDesertMountains[0]); out.writeDouble(biomeColorDesertMountains[1]); out.writeDouble(biomeColorDesertMountains[2]);
			out.writeDouble(biomeColorTaiga[0]); out.writeDouble(biomeColorTaiga[1]); out.writeDouble(biomeColorTaiga[2]);
			out.writeDouble(biomeColorTaigaHills[0]); out.writeDouble(biomeColorTaigaHills[1]); out.writeDouble(biomeColorTaigaHills[2]);
			out.writeDouble(biomeColorLakes[0]); out.writeDouble(biomeColorLakes[1]); out.writeDouble(biomeColorLakes[2]);
			out.writeDouble(biomeColorNorthPole[0]); out.writeDouble(biomeColorNorthPole[1]); out.writeDouble(biomeColorNorthPole[2]);
			out.writeDouble(biomeColorSouthPole[0]); out.writeDouble(biomeColorSouthPole[1]); out.writeDouble(biomeColorSouthPole[2]);
			out.writeDouble(biomeColorBeaches[0]); out.writeDouble(biomeColorBeaches[1]); out.writeDouble(biomeColorBeaches[2]);
			out.writeDouble(biomeColorPeaks[0]); out.writeDouble(biomeColorPeaks[1]); out.writeDouble(biomeColorPeaks[2]);
			out.writeDouble(biomeColorSnow[0]); out.writeDouble(biomeColorSnow[1]); out.writeDouble(biomeColorSnow[2]);
			out.writeDouble(biomeColorSnowyHills[0]); out.writeDouble(biomeColorSnowyHills[1]); out.writeDouble(biomeColorSnowyHills[2]);
		}
		
		public ComplexGenSettings() {
			
		}
	}
	
	private static double sigmoid(double x) {
		return 1.0 / (1.0 + Math.exp(-x));
	}
	
	public int currStep = 0;
	
	public GeneratorResult generate(Random sRng, ComplexGenSettings settings, boolean debugProgress, boolean debugSteps) throws Exception {
		GeneratorResult result = new GeneratorResult();
		currStep = 0;
		final int width = settings.width;
		final int height = settings.height;
		final double resMul = 600000.0 / (double)settings.planetRadius * 0.85;
		final double planetTemperature = settings.planetTemperature;
		
		double temperatureMultiplier = 1.0 - sigmoid(planetTemperature * 3.0);
		if(planetTemperature > 0.85) {
			double mul = (planetTemperature - 0.85) / 0.15;
			if(mul > 1) mul = 1;
			temperatureMultiplier *= 1.0 - mul;
		}
		if(planetTemperature < -0.85) {
			double mul = (Math.abs(planetTemperature) - 0.85) / 0.15;
			if(mul > 1) mul = 1;
			temperatureMultiplier += mul * (1.0 - temperatureMultiplier);
		}
		final double poleRadius = planetTemperature > 0 ? settings.basePoleRadius - Math.max(0, (1.0 - temperatureMultiplier - 0.5) / 0.25) * settings.basePoleRadius : settings.basePoleRadius + (temperatureMultiplier - 0.5) / 0.5 * ((Math.PI / 5.5) - settings.basePoleRadius);
		final double oceanFactor = settings.baseOceanFactor + (planetTemperature > 0.0 ? (0.5 - temperatureMultiplier) * 1.4 : 0.0);
		final double desertFadeStart = planetTemperature > 0.0 ? ((90 - settings.baseDesertFadeEnd) * (1.0 - temperatureMultiplier)) + settings.baseDesertFadeStart : settings.baseDesertFadeStart;
		final double desertFadeEnd = planetTemperature > 0.0 ? Math.max(settings.baseDesertFadeEnd, ((90 - settings.baseDesertFadeEnd) * (1.0 - temperatureMultiplier)) - (settings.baseDesertFadeStart - settings.baseDesertFadeEnd)) : settings.baseDesertFadeEnd;
		final double desertOffset = planetTemperature > 0.0 ? planetTemperature / 1.0 * 0.375 : 0.0;
		final double peaksFadeStart = settings.basePeaksFadeStart + 0.225 * planetTemperature;
		final double peaksFadeEnd = settings.basePeaksFadeEnd + 0.225 * planetTemperature;
		final double snowFadeEnd = settings.baseSnowFadeEnd - (temperatureMultiplier - 0.5) * 2.0 * settings.baseSnowFadeEnd * 1.8;
		final double snowFadeStart = snowFadeEnd - (settings.baseSnowFadeEnd - settings.baseSnowFadeStart);
		final double taigaOffset = planetTemperature < 0.0 ? -planetTemperature : 0.0;
		final double snowLatitudeFadeEnd = 90.0 - poleRadius * 1.25 * 180.0;
		final double snowLatitudeFadeStart = snowLatitudeFadeEnd - settings.snowLatitudeFadeDistance;
		
		File pastOutputsFolder = new File("past_outputs");
		if(!pastOutputsFolder.exists()) pastOutputsFolder.mkdirs();
		settings.polesPerturbNoise.noise.initialize(new RanMT().seedCompletely(sRng));
		
		/*double[] lowlandColor = RGB(new Color(53, 74, 22));
		double[] hillsColor =     RGB(new Color(56, 60, 34));
		double[] mountainsColor = RGB(new Color(84, 84, 84));
		double[] desertColor =    RGB(new Color(153, 121, 65));
		double[] taigaColor =     RGB(new Color(28, 70, 24));
		double[] oceansColor =    RGB(new Color(7, 23, 51));*/
		
		double[][] continentMap  = new double[width][height];
		float [][] distanceMap   = new float [width][height];
		double[][] mountainMap   = new double[width][height];
		double[][] hillMap       = new double[width][height];
		double[][] lakesMap      = new double[width][height];
		float [][] desertMap     = new float [width][height];
		float [][] taigaMap      = new float [width][height];
		float [][] snowMap       = new float [width][height];
		double[][] finalNoiseMap = new double[width][height];
		byte  [][] poles         = new byte  [width][height];
		double[][][] colorMap    = new double[settings.colorMapWidth][settings.colorMapHeight][3];
		
		double[][] tempMap  = new double[width][height];
		double[][] tempMap2 = new double[width][height];
		
		currStep++;
		if(debugProgress) System.out.println("Continents & Biomes");
		double oldNoiseOffset = settings.continentNoise.noiseOffset;
		settings.continentNoise.setNoiseOffset(oceanFactor / 2.0); //TODO: Find a way to do this without permanently modifying this NoiseConfig
		NoisemapGenerator.genNoisemap(new RanMT().seedCompletely(sRng), continentMap, settings.continentNoise, null, resMul, debugProgress);
		settings.continentNoise.setNoiseOffset(oldNoiseOffset);
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				distanceMap[i][j] = (float)(continentMap[i][j] / 12.0);
				continentMap[i][j] = /*1.0 - */Math.max(0, Math.min(1, continentMap[i][j]));
			}
		}
		
		currStep++;
		NoisemapGenerator.genNoisemap(new RanMT().seedCompletely(sRng), tempMap, settings.mountainNoise, null, resMul, debugProgress);
		for(int i = 0; i < width; i++) {
			double longitude = (double)(i - width / 2) / (width / 2.0) * 180.0;
			for(int j = 0; j < height; j++) {
				if(continentMap[i][j] < 0) continue;
				double latitude = (double)(j - height / 2) / (height / 2.0) * 90.0;
				double distance = Maths.gcDistance(latitude, longitude, -90, 0);
				distance = Math.min(distance, Maths.gcDistance(latitude, longitude, 90, 0));
				double val = tempMap[i][j];
				val = Math.max(0, Math.min(1, Math.abs(val)));
				if(distanceMap[i][j] < settings.hillsFadeEnd && val > 0.34) {
					double h = val - 0.34;
					h = h * 1.515151 * 5.0;
					hillMap[i][j] = h;
					hillMap[i][j] *= continentMap[i][j];
					if(distanceMap[i][j] > settings.hillsFadeStart) {
						double amul = (distanceMap[i][j] - settings.hillsFadeStart) / (settings.hillsFadeEnd - settings.hillsFadeStart);
						amul = 1.0 - amul;
						hillMap[i][j] *= amul;
					}
				}
				if(distanceMap[i][j] < settings.mountainsFadeEnd && val > 0.475) {
					double h = val - 0.475;
					h = h * 1.904 * 5.0;
					mountainMap[i][j] = h;
					mountainMap[i][j] *= continentMap[i][j];
					if(distanceMap[i][j] > settings.mountainsFadeStart) {
						double amul = (distanceMap[i][j] - settings.mountainsFadeStart) / (settings.mountainsFadeEnd - settings.mountainsFadeStart);
						amul = 1.0 - amul;
						mountainMap[i][j] *= amul;
					}
				}
			}
		}
		currStep++;
		NoisemapGenerator.genNoisemap(new RanMT().seedCompletely(sRng), tempMap, settings.lakeNoiseMul, null, resMul, debugProgress);
		currStep++;
		NoisemapGenerator.genNoisemap(new RanMT().seedCompletely(sRng), tempMap2, settings.lakeNoise, null, resMul, debugProgress);
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				if(continentMap[i][j] < 0) continue;
				double val = tempMap[i][j];
				val = Math.max(0, Math.min(1, Math.abs(val)));
				if(val > 0.43) {
					val = Math.min((val - 0.43) * 2.325 * 5.0, 1.0);
					if(val > 0) {
						val = tempMap2[i][j] * val;
						val = Math.max(0, Math.min(1, Math.abs(val)));
						double mul = 1.0;
						if(planetTemperature > 0.125) {
							if(planetTemperature > 0.65) mul = 0;
							else {
								mul = (planetTemperature - 0.125) / (0.65 - 0.125);
								mul = 1.0 - mul;
							}
						}
						val *= mul;
						if(val > 0.43) {
							double h = val - 0.43;
							h = h * 2.325 * 5.0;
							double m = Math.max(hillMap[i][j], mountainMap[i][j]);
							lakesMap[i][j] = h * Math.max(1.0 - (2.0 * m), 0);
							lakesMap[i][j] *= continentMap[i][j];
						}
					}
				}
			}
		}
		currStep++;
		NoisemapGenerator.genNoisemap(new RanMT().seedCompletely(sRng), tempMap, settings.desertNoise, null, resMul, debugProgress);
		currStep++;
		NoisemapGenerator.genNoisemap(new RanMT().seedCompletely(sRng), tempMap2, settings.taigaNoise, null, resMul, debugProgress);
		for(int i = 0; i < width; i++) {
			double longitude = (double)(i - width / 2) / (width / 2.0) * 180.0;
			for(int j = 0; j < height; j++) {
				if(continentMap[i][j] < 0) continue;
				double latitude = (double)(j - height / 2) / (height / 2.0) * 90.0;
				double distance = Maths.gcDistance(latitude, longitude, -90, 0);
				distance = Math.min(Maths.gcDistance(latitude, longitude, 90, 0), distance);
				double val = tempMap[i][j];
				val = Math.max(0, Math.min(1, Math.abs(val)));
				double mul = 0.0;
				if(Math.abs(latitude) <= desertFadeStart) {
					if(Math.abs(latitude) <= desertFadeEnd) mul = 1.0;
					else {
						mul = 1.0 - (Math.abs(latitude) - desertFadeEnd) / (desertFadeStart - desertFadeEnd);
					}
				}
				val *= mul;
				val += desertOffset;
				if(planetTemperature < 0) {
					if(planetTemperature < -0.25) mul = 0;
					mul = Math.abs(planetTemperature) / 0.25;
					mul = 1.0 - mul;
					val *= mul;
				}
				
				if(val > 0.38) {
					double h = val - 0.38;
					h = h * 1.612 * 5.0;
					desertMap[i][j] = (float)(h * continentMap[i][j] * (1.0 - Math.max(Math.min(lakesMap[i][j], 1.0), 0.0)));
				}
				if(distance <= poleRadius * settings.taigaRadiusMul) {
					mul = (poleRadius * settings.taigaRadiusMul) - (distance - (poleRadius * 1.0)) - (poleRadius * 1.0);
					mul /= (poleRadius * settings.taigaRadiusMul - poleRadius * 1.0);
					if(distance <= (poleRadius * 1.0)) mul = 1;
					if(mul > 1) mul = 1;
					
					val = tempMap2[i][j];
					val = Math.max(0, Math.min(1, Math.abs(val)));
					val += taigaOffset;
					if(val > 0.35) {
						double h = val - 0.35;
						h = h * 1.538 * 5.0;
						taigaMap[i][j] = (float)(h * continentMap[i][j] * (1.0 - Math.max(Math.min(lakesMap[i][j], 1.0), 0.0)) * (1.0 - Math.max(Math.min(mountainMap[i][j], 1.0), 0.0)) * (1.0 - Math.max(Math.min(desertMap[i][j], 1.0), 0.0)));
					}
					taigaMap[i][j] *= mul;
				}
				snowMap[i][j] = 0.0f;
				if(distanceMap[i][j] > snowFadeEnd) {
					snowMap[i][j] = 1.0f;
				}else if(distanceMap[i][j] > snowFadeStart) {
					snowMap[i][j] = (float)((distanceMap[i][j] - snowFadeStart) / (snowFadeEnd - snowFadeStart));
				}
				if(Math.abs(latitude) < snowLatitudeFadeEnd) {
					if(Math.abs(latitude) < snowLatitudeFadeStart) snowMap[i][j] = 0;
					else {
						snowMap[i][j] *= 1.0 - (snowLatitudeFadeEnd - Math.abs(latitude)) / (snowLatitudeFadeEnd - snowLatitudeFadeStart);
					}
				}
				/*mountainMap[i][j] = (NoiseUtils.sampleSpherableNoise(mountainNoise, i, j, width, height, 0.6, 0.6, 0.5));
				mountainMap[i][j] = hillMap[i][j] = (Math.abs(mountainMap[i][j]) - 0.11) * 12.0;
				hillMap[i][j] += 1.0;
				if(continentMap[i][j] < 0.5) mountainMap[i][j] = hillMap[i][j] = 0.0;
				else {
					mountainMap[i][j] = Math.pow(mountainMap[i][j], 3);
					hillMap[i][j] = Math.pow(hillMap[i][j], 3);
				}*/
			}
		}
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				hillMap[i][j] = Math.min(1, hillMap[i][j]);
				mountainMap[i][j] = Math.min(1, mountainMap[i][j]);
				desertMap[i][j] = Math.min(1, desertMap[i][j]);
				taigaMap[i][j] = Math.min(1, taigaMap[i][j]);
				snowMap[i][j] = Math.min(1, snowMap[i][j]);
			}
		}
		
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		if(debugSteps) {
			for(int i = 0; i < width; i++) {
				for(int j = 0; j < height; j++) {
					double v = continentMap[i][j];
					int col = (int)(v * 255.0);
					int r,g,b;
					r = g = b = Math.max(0, Math.min(255, col));
					if(hillMap[i][j] > 0) {
						double mul = (1.0 - Math.max(0, Math.min(1, hillMap[i][j])));
						r = (int)(r * mul);
						g = (int)(g * mul);
					}
					if(mountainMap[i][j] > 0) {
						double mul = (1.0 - Math.max(0, Math.min(1, mountainMap[i][j])));
						g = (int)(g * mul);
						b = (int)(b * mul);
						r = (int)((1.0 - mul) * 255);
					}
					if(lakesMap[i][j] > 0) {
						double mul = (1.0 - Math.max(0, Math.min(1, lakesMap[i][j])));
						g = (int)(r * mul);
					}
					if(desertMap[i][j] > 0) {
						double mul = (1.0 - Math.max(0, Math.min(1, desertMap[i][j])));
						double mulmul = Math.max(0, Math.min(1, hillMap[i][j]));
						r = (int)(r * ((1.0 - mulmul) * mul + mulmul));
						b = (int)(b * ((1.0 - mulmul) * mul + mulmul));
						g = Math.max(g, (int)((1.0 - mul) * 255));
					}
					if(taigaMap[i][j] > 0) {
						double mul = (1.0 - Math.max(0, Math.min(1, taigaMap[i][j])));
						double mulmul = Math.max(0, Math.min(0.5, hillMap[i][j]));
						r = (int)(r * ((1.0 - mulmul) * mul + mulmul));
						b = (int)(b * ((1.0 - mulmul) * mul + mulmul));
						g = Math.max(g, (int)((1.0 - mul) * 128));
					}
					r = Math.max(0, Math.min(255, r));
					g = Math.max(0, Math.min(255, g));
					b = Math.max(0, Math.min(255, b));
					img.setRGB(i, j, b | (g << 8) | (r << 16));
				}
			}
			//JOptionPane.showMessageDialog(null, new JLabel(new ImageIcon(img)), "a", JOptionPane.INFORMATION_MESSAGE);
			ImageIO.write(img, "png", new File("continents.png"));
			//System.exit(0);
		}
		
		currStep++;
		if(debugProgress) System.out.println("Ground");
		NoisemapGenerator.genNoisemap(new RanMT().seedCompletely(sRng), finalNoiseMap, settings.groundNoiseLargeDetail, continentMap, resMul, debugProgress);
		currStep++;
		NoisemapGenerator.genNoisemap(new RanMT().seedCompletely(sRng), tempMap, settings.groundNoiseMediumDetail, continentMap, resMul, debugProgress);
		for(int i = 0; i < width; i++) for(int j = 0; j < height; j++) finalNoiseMap[i][j] += tempMap[i][j];
		currStep++;
		NoisemapGenerator.genNoisemap(new RanMT().seedCompletely(sRng), tempMap, settings.groundNoiseSmallDetail, continentMap, resMul, debugProgress);
		for(int i = 0; i < width; i++) for(int j = 0; j < height; j++) finalNoiseMap[i][j] += tempMap[i][j];
		
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				if(continentMap[i][j] > 0) {
					finalNoiseMap[i][j] = Math.abs(finalNoiseMap[i][j]) * 0.15;
				}
			}
		}
		if(debugSteps) ImageIO.write(MapUtils.renderMap(finalNoiseMap), "png", new File("ground.png"));
		
		currStep++;
		if(debugProgress) System.out.println("Hills");
		NoisemapGenerator.genNoisemap(new RanMT().seedCompletely(sRng), tempMap, settings.hillNoise, hillMap, resMul, debugProgress);
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				double mul = Math.min(1, mountainMap[i][j]);
				finalNoiseMap[i][j] += tempMap[i][j] * (1.0 - mul);
			}
		}
		if(debugSteps) ImageIO.write(MapUtils.renderMap(tempMap), "png", new File("hills.png"));
		
		currStep++;
		if(debugProgress) System.out.println("Mountains");
		NoisemapGenerator.genNoisemap(new RanMT().seedCompletely(sRng), tempMap, settings.mountainsNoise, mountainMap, resMul, debugProgress);
		currStep++;
		NoisemapGenerator.genNoisemap(new RanMT().seedCompletely(sRng), tempMap2, settings.mountainWorley, mountainMap, resMul, debugProgress);
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				if(mountainMap[i][j] > 0) {
					tempMap[i][j] *= Math.max(0, Math.min(1, (1.0 - tempMap2[i][j]) * 2.0 + 0.1));
				}
			}
		}
		for(int i = 0; i < width; i++) for(int j = 0; j < height; j++) finalNoiseMap[i][j] += tempMap[i][j];
		if(debugSteps) ImageIO.write(MapUtils.renderMap(tempMap), "png", new File("mountains.png"));
		
		currStep++;
		if(debugProgress) {
			System.out.println("Lakes");
			ProgressBars.printBar();
		}
		for(int i = 0; i < width; i++) {
			if(debugProgress) ProgressBars.printProgress(i, width);
			for(int j = 0; j < height; j++) {
				if(lakesMap[i][j] > 0) {
					double mul  = Math.max(0, Math.min(1, lakesMap[i][j]));
					tempMap[i][j] = 1.0 - Math.min(1, Math.max(0, mul));
				}else {
					tempMap[i][j] = 1.0;
				}
			}
		}
		for(int i = 0; i < width; i++) for(int j = 0; j < height; j++) finalNoiseMap[i][j] *= (tempMap[i][j] * continentMap[i][j]);
		if(debugSteps) ImageIO.write(MapUtils.renderMap(tempMap), "png", new File("lakes.png"));
		if(debugProgress) ProgressBars.finishProgress();
		
		currStep++;
		if(debugProgress) {
			System.out.println("Poles");
			ProgressBars.printBar();
		}
		double oldLatScale = settings.polesPerturbNoise.noiseLatitudeScale;
		double oldLonScale = settings.polesPerturbNoise.noiseLongitudeScale;
		settings.polesPerturbNoise.noiseLatitudeScale *= resMul;
		settings.polesPerturbNoise.noiseLongitudeScale *= resMul;
		try {
			for(int i = 0; i < width; i++) {
				if(debugProgress) ProgressBars.printProgress(i, width);
				double longitude = (double)(i - width / 2) / (width / 2.0) * 180.0;
				for(int j = 0; j < height; j++) {
					double latitude = (double)(j - height / 2) / (height / 2.0) * 90.0;
					double distance = Maths.gcDistance(latitude, longitude, -90, 0);
					distance += NoiseUtils.sampleSpherableNoise(i, 175, width, 360, settings.polesPerturbNoise) * Math.min(poleRadius, 0.275) / 1.333;
					if(distance <= poleRadius) {
						poles[i][j] = 1;
					}else {
						poles[i][j] = 0;
					}
					distance = Maths.gcDistance(latitude, longitude, 90, 0);
					distance += NoiseUtils.sampleSpherableNoise(i, 185, width, 360, settings.polesPerturbNoise) * Math.min(poleRadius, 0.275) / 1.333;
					if(distance <= poleRadius) {
						poles[i][j] = 2;
					}
				}
			}
		} finally {
			settings.polesPerturbNoise.noiseLatitudeScale = oldLatScale;
			settings.polesPerturbNoise.noiseLongitudeScale = oldLonScale;
		}
		for(int i = 0; i < width; i++) for(int j = 0; j < height; j++) {
			tempMap[i][j] = poles[i][j] == 0 ? 0 : 0.01;
			finalNoiseMap[i][j] = Math.max(finalNoiseMap[i][j], tempMap[i][j]);
		}
		if(debugSteps) ImageIO.write(MapUtils.renderMap(tempMap), "png", new File("poles.png"));
		if(debugProgress) ProgressBars.finishProgress();
		
		int biggestPixelValue = 0;
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				double v = finalNoiseMap[i][j];
				int col  = (int)(v * 255.0);
				int r,g,b;
				r = g = b = Math.max(0, Math.min(255, col));
				if(r > biggestPixelValue) {
					biggestPixelValue = r;
				}
				img.setRGB(i, j, b | (g << 8) | (r << 16));
			}
		}
		if(debugProgress) System.out.println(biggestPixelValue);
		result.heightmap = img;
		result.heightmap16 = MapUtils.render16bit(finalNoiseMap);
		result.heightmap24 = MapUtils.render24bit(finalNoiseMap);
		result.heightmapRaw = new double[width][height];
		for(int i = 0; i < width; i++) for(int j = 0; j < height; j++) result.heightmapRaw[i][j] = finalNoiseMap[i][j];
		if(debugSteps) {
			ImageIO.write(img, "png", new File("complex.png"));
			ImageIO.write(result.heightmap16, "png", new File("complex_16.png"));
			ImageIO.write(result.heightmap24, "png", new File("complex_24.png"));
		}
		if(debugProgress) System.out.println("Done.");
		//long name = System.currentTimeMillis();
		
		currStep++;
		if(debugProgress) System.out.println("Color Map!");
		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		if(settings.colorMapWidth != width || settings.colorMapHeight != height) tempMap = new double[settings.colorMapWidth][settings.colorMapHeight];
		NoisemapGenerator.genNoisemap(new RanMT().seedCompletely(sRng), tempMap, settings.colorNoise, null, resMul, debugProgress);
		for(int i = 0; i < settings.colorMapWidth; i++) {
			int i2 = settings.colorMapWidth == width ? i : (int)((double)i / (double)(settings.colorMapWidth - 1) * (double)(width - 1));
			double longitude = (double)(i - width / 2) / (width / 2.0) * 180.0;
			for(int j = 0; j < settings.colorMapHeight; j++) {
				int j2 = settings.colorMapHeight == height ? j : (int)((double)j / (double)(settings.colorMapHeight - 1) * (double)(height - 1));
				double continent   = Math.min(1, continentMap[i2][j2]);
				double[] rgb = null;
				if(poles[i2][j2] != 0 && (continent <= 1e-8 || lakesMap[i2][j2] != 0)) {
					rgb = Arrays.copyOf(settings.polesColor, 3);
				}else {
					if(continent <= 1e-8) {
						colorMap[i][j] = settings.oceansColor;
						continue;
					}
					if(snowMap[i2][j2] >= 1.0 - 1e-8) {
						rgb = Arrays.copyOf(settings.snowColor, 3);
					}else if(distanceMap[i2][j2] <= settings.beachThreshold) {
						rgb = Arrays.copyOf(settings.beachesColor, 3);
					}else {
						double latitude = (double)(j - height / 2) / (height / 2.0) * 90.0;
						double distance = Maths.gcDistance(latitude, longitude, -90, 0);
						distance = Math.min(distance, Maths.gcDistance(latitude, longitude, 90, 0));
						
						rgb = Arrays.copyOf(settings.lowlandColor, 3);
						factorIn(rgb, hillMap[i2][j2], settings.hillsColor);
						factorIn(rgb, taigaMap[i2][j2], settings.taigaColor);
						double coldnessFactor = 0;
						if(distance <= poleRadius * 2.3) {
							coldnessFactor = (poleRadius * 2.3) - (distance - (poleRadius * 1.333)) - (poleRadius * 1.333);
							coldnessFactor /= (poleRadius * 2.3 - poleRadius * 1.333);
							if(distance <= poleRadius * 1.333) coldnessFactor = 1;
						}
						if(planetTemperature < 0) {
							coldnessFactor = Math.max(coldnessFactor, Math.min(1, planetTemperature / 0.75));
						}
						if(coldnessFactor != 0) factorIn(rgb, coldnessFactor, settings.taigaColor);
						
						double mountainFactor = (finalNoiseMap[i2][j2] - settings.mountainColorFadeStart) / (settings.mountainColorFadeEnd - settings.mountainColorFadeStart);
						mountainFactor = Math.max(0, Math.min(1, mountainFactor));
						if(finalNoiseMap[i2][j2] >= peaksFadeStart && desertMap[i2][j2] < 0.05) {
							if(finalNoiseMap[i2][j2] >= peaksFadeEnd) {
								factorIn(rgb, mountainMap[i2][j2], settings.peaksColor);
							}else {
								double mmul = (finalNoiseMap[i2][j2] - peaksFadeStart) / (peaksFadeEnd - peaksFadeStart);
								factorIn(rgb, mountainFactor * (1.0 - mmul), settings.mountainsColor);
								factorIn(rgb, mountainMap[i2][j2] * mmul, settings.peaksColor);
							}
						}else factorIn(rgb, mountainFactor, settings.mountainsColor);
						
						factorIn(rgb, desertMap[i2][j2], settings.desertColor);
						
						if(snowMap[i2][j2] > 0) {
							factorIn(rgb, snowMap[i2][j2], settings.snowColor);
						}
						
						factorIn(rgb, lakesMap[i2][j2] * 1000.0, settings.oceansColor);
					}
					
					factorIn(rgb, (poles[i2][j2] == 0 ? 0 : 0.01) * 0.75, settings.polesColor);
					if(poles[i2][j2] != 0) {
						factorIn(rgb, 0.75, settings.polesColor);
					}
				}
				
				double mul = tempMap[i][j];
				mul -= 0.25;
				mul *= 1.25;
				rgb[0] -= mul * rgb[0];
				rgb[1] -= mul * rgb[1];
				rgb[2] -= mul * rgb[2];
				
				colorMap[i][j] = rgb;
				//colorMap[i][j][0] = (1.0 - continent) * oceansColor[0] + continent * rgb[0]; //Try this in-game, see how it looks
				//colorMap[i][j][1] = (1.0 - continent) * oceansColor[1] + continent * rgb[1];
				//colorMap[i][j][2] = (1.0 - continent) * oceansColor[2] + continent * rgb[2];
			}
		}
		
		for(int i = 0; i < settings.colorMapWidth; i++) {
			for(int j = 0; j < settings.colorMapHeight; j++) {
				int r = (int)Math.max(0, Math.min(255, colorMap[i][j][0] * 255.0));
				int g = (int)Math.max(0, Math.min(255, colorMap[i][j][1] * 255.0));
				int b = (int)Math.max(0, Math.min(255, colorMap[i][j][2] * 255.0));
				
				img.setRGB(i, j, b | (g << 8) | (r << 16));
			}
		}
		result.colorMap = img;
		if(debugSteps) ImageIO.write(img, "png", new File("colors.png"));
		currStep++;
		if(debugProgress) {
			System.out.println("Done.");
			System.out.println("Biome map");
			ProgressBars.printBar();
		}
		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		if(settings.colorMapWidth < width || settings.colorMapHeight < height) colorMap = new double[width][height][3];
		for(int i = 0; i < width; i++) {
			if(debugProgress) ProgressBars.printProgress(i, width);
			for(int j = 0; j < height; j++) {
				if(poles[i][j] == 1) {
					colorMap[i][j] = settings.biomeColorNorthPole;
					continue;
				}
				if(poles[i][j] == 2) {
					colorMap[i][j] = settings.biomeColorSouthPole;
					continue;
				}
				if(continentMap[i][j] <= 1e-8) {
					colorMap[i][j] = settings.biomeColorOceans;
					continue;
				}
				if(lakesMap[i][j] > 0.1) {
					colorMap[i][j] = settings.biomeColorLakes;
					continue;
				}
				if(snowMap[i][j] > 0.5) {
					colorMap[i][j] = settings.biomeColorSnow;
					if(hillMap[i][j] > 0.25) {
						colorMap[i][j] = settings.biomeColorSnowyHills;
					}
					if(mountainMap[i][j] > 0.25) {
						colorMap[i][j] = settings.biomeColorPeaks;
					}
					continue;
				}
				colorMap[i][j] = settings.biomeColorLowlands;
				if(desertMap[i][j] > 0.25) {
					colorMap[i][j] = settings.biomeColorDesert;
				}
				if(taigaMap[i][j] > 0.25) {
					colorMap[i][j] = settings.biomeColorTaiga;
				}
				if(hillMap[i][j] > 0.25) {
					colorMap[i][j] = settings.biomeColorHills;
					if(desertMap[i][j] > 0.25) {
						colorMap[i][j] = settings.biomeColorDesertHills;
					}
					if(taigaMap[i][j] > 0.25) {
						colorMap[i][j] = settings.biomeColorTaigaHills;
					}
				}
				if(mountainMap[i][j] > 0.25) {
					colorMap[i][j] = settings.biomeColorMountains;
					if(desertMap[i][j] > 0.25) {
						colorMap[i][j] = settings.biomeColorDesertMountains;
					}else if(finalNoiseMap[i][j] > (peaksFadeStart + (peaksFadeEnd - peaksFadeStart) / 2.0)) {
						colorMap[i][j] = settings.biomeColorPeaks;
					}
				}
				if(distanceMap[i][j] <= settings.beachThreshold) {
					colorMap[i][j] = settings.biomeColorBeaches;
				}
			}
		}
		if(debugProgress) ProgressBars.finishProgress();
		
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				int r = (int)Math.max(0, Math.min(255, colorMap[i][j][0] * 255.0));
				int g = (int)Math.max(0, Math.min(255, colorMap[i][j][1] * 255.0));
				int b = (int)Math.max(0, Math.min(255, colorMap[i][j][2] * 255.0));
				
				img.setRGB(i, j, b | (g << 8) | (r << 16));
			}
		}
		result.biomeMap = img;
		
		if(debugSteps) ImageIO.write(img, "png", new File("biomes.png"));
		
		settings.polesPerturbNoise.noise.cleanUp();
		
		currStep++;
		if(debugProgress) {
			System.out.println("Done.");
			System.out.println("Ocean map");
		}
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				continentMap[i][j] = finalNoiseMap[i][j] > 0 ? 0.0 : 1.0;
			}
		}
		NoisemapGenerator.genNoisemap(new RanMT().seedCompletely(sRng), tempMap2, settings.groundNoiseMediumDetail, continentMap, resMul, debugProgress);
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				continentMap[i][j] = 1.0 - continentMap[i][j];
				continentMap[i][j] = Math.max(0, Math.min(1, continentMap[i][j]));
			}
		}
		currStep++;
		PostProcessingEffects.gaussianBlur(continentMap, mountainMap, 1.0 / (double)continentMap.length, 1.0 / (double)continentMap[0].length, (int)(32.0 * (double)width / 4096.0));
		for(int i = 0; i < width; i++) for(int j = 0; j < height; j++) {
			if(continentMap[i][j] > 0.5) {
				continue;
			}else {
				continentMap[i][j] = Math.min(253.0 / 255.0, mountainMap[i][j] * 1.8);
			}
			continentMap[i][j] += tempMap2[i][j] * 0.2;
		}
		
		result.oceanMap = MapUtils.renderMap(continentMap);
		if(debugSteps) ImageIO.write(result.oceanMap, "png", new File("oceans.png"));
		
		currStep++;
		if(debugProgress) System.out.println("Done.");
		
		return result;
	}
	
	public int getTotalSteps(ComplexGenSettings settings) {
		return 19;
	}
	
	public int getCurrentStep() {
		return this.currStep;
	}
	
	//IDEA: OpenCL perlin noise, Change noise stretch using another noise function.
	public static void main(String[] args) {
		try {
			long name = System.currentTimeMillis();
			boolean test = true;
			RanMT rng = test ? new RanMT(fixed_seed) : new RanMT().seedCompletely();
			
			if(!test) {
				FileOutputStream fos = new FileOutputStream("past_outputs/" + name + "_seed.txt");
				int cntr = 0;
				for(int i:rng.getLongSeed()) {
					System.out.print(i + ",");
					fos.write((i + ",").getBytes());
					cntr++;
					if(cntr % 16 == 0) {
						System.out.println();
						fos.write("\r\n".getBytes());
						fos.flush();
					}
				}
				System.out.println();
				fos.close();
			}
			ComplexGenSettings settings = new ComplexGenSettings();
			GeneratorResult res = new ComplexSurface().generate(rng, settings, true, true);
			ImageIO.write(res.heightmap, "png", new File("past_outputs/" + name + ".png"));
			ImageIO.write(res.heightmap16, "png", new File("past_outputs/" + name + "_16.png"));
			ImageIO.write(res.colorMap, "png", new File("past_outputs/" + name + "_colors.png"));
			ImageIO.write(res.biomeMap, "png", new File("past_outputs/" + name + "_biomes.png"));
			NoisemapGenerator.cleanUp();
			CraterDistributer.cleanUp();
		}catch(Exception e) {
			System.err.println("Error: ");
			e.printStackTrace();
			System.exit(1);
		}
		NoisemapGenerator.cleanUp();
	}
	
	private static void factorIn(double[] color, double multiplier, double[] newColor) {
		multiplier = Math.min(1, multiplier);
		color[0]   = color[0] * (1.0 - multiplier) + newColor[0] * multiplier;
		color[1]   = color[1] * (1.0 - multiplier) + newColor[1] * multiplier;
		color[2]   = color[2] * (1.0 - multiplier) + newColor[2] * multiplier;
	}
	
	private static final int[] fixed_seed = new int[] { // 2,698,966,514,655,541,623 300 2.6 8 1 42
			-622527324,1239914429,514770773,-1898375360,-416462275,-1520028893,1504304309,1916294549,1800914911,856304735,1603761750,463999729,1355714229,-754198837,770975268,258484721,-1120959246,
			-1201056982,920333656,-1388971988,-2094720835,420744416,-627023501,54739687,-1096037725,-304117610,-455877318,699624984,-1349613403,1527481243,1108968277,857277469,-1272719514,
			-612146673,-1484893614,330140126,-1114255746,-1305678805,-1892318042,-1640126690,-1237393255,1977638542,-1041791745,-620520294,434469103,-974927892,-1627607031,-1981150609,1860199139,
			1443083977,2039343073,-1762589228,1718486662,-1969014348,-1427835743,-1532002028,701002293,-832754733,1857468779,-1775566472,-1061145299,-1542400694,947991575,780117247,-1914172970,
			963893315,-189312291,1913813312,-1818537707,-410816324,-1817930812,1917330293,-1805034432,-1620115306,1328707844,-1845898213,682592031,-1143606825,1475988464,505581656,-1838406864,
			259187163,-112612195,1266599446,1871983048,1401823614,-821135381,1192768571,1786113861,1238467158,1539471351,217587751,-1359860622,-27822165,668100381,628743387,982939956,
			-1743814098,-1759526355,1056416035,350363674,248639405,-863311005,-691294743,473952287,-272832121,1185881270,-908081949,1535039166,297063526,1547315745,-1304105178,-892122467,
			-1121846299,1636439228,1821217854,-156530776,1397068554,209349694,-52175104,1172346636,-222297184,-1635024279,1360426314,-2146555053,-266171852,2026728730,169181090,-1004844459,
			983967209,3725674,-1936065424,1446889595,-295577566,610249857,368142740,-1945988830,1721381079,1839167594,1999092121,-1541094221,470036071,859815087,1711905359,-2026004727,
			2081778593,2099640652,146344820,1167537492,-1346658655,537498268,2110670006,-1515454740,-116038003,-699301413,-1472271184,1042100129,-385455190,1929762861,1938137138,1697243955,
			784387006,-813755941,432260710,-1225713973,748542404,-1078118144,-1294484987,319321350,-824169040,747689407,2023833104,-1187910016,1513684837,1845474323,-1175567115,-1673910969,
			153802983,596603920,1791624944,424257647,1487114925,325053642,-1641519023,20118528,-714628417,205479760,-98062017,1216837233,354990984,1646426357,1700958646,-1032001407,
			1197586992,1604986168,-1056146849,32381290,1934746685,-224317171,-2133704066,-620752744,162625217,-325337622,1454972275,120992033,-369523116,1814200845,-160046560,486482665,
			1549347446,1712578030,687251970,1661242739,-894304659,-769344549,102272486,113746835,875949824,-1330953092,801385929,-447447901,1887459688,-81960774,1458169666,1679179601,
			1480882356,-1813181444,672715645,-820634478,-756341352,1974236923,-89528910,1362041216,-1715870705,-1264322033,1119445656,-503272854,979190462,1631836172,-748116659,1957305551,
			-2081388601,-48508869,1948399636,1234752126,316453269,-12418824,685389884,-2064430274,556319491,-665413084,-763031973,309002186,437609535,25420001,2050913487,1853820307,
			-1323446547,-449445933,-1361392352,611954184,1706561085,-614257824,-639842380,-1347255075,1852738776,110662581,1443453216,-169887962,-13831274,-1707956491,1595146707,-1082140603,
			-1076209543,-177527748,-1491246057,1789305822,295353135,170327311,2080254586,-1816754316,1917965862,-1179776547,-342344239,-1764688527,-415155415,750160573,1135103295,-1677215775,
			-656686217,-1351714356,-1630428560,1469618956,858279062,1164750827,-304198971,-960591951,-280115258,-55323708,292391966,-119475036,1679988921,36773925,629844610,1680628459,
			-589741248,-1654590756,367395232,1541238711,-31167896,-2080165435,-290822145,-37147111,1936465620,1779006668,589883228,1407955530,921599363,1294544731,819250030,1980228662,
			-1998217713,-1095027790,-1706438932,782364982,1393348700,1507042684,861344615,98473814,1193225325,-1497676856,474466000,-1676591042,-2144767831,-399199069,-1746627787,-318576179,
			-808898693,-1304980088,484912005,1815864052,699616880,-1257545558,-1596312576,-371567577,-786705107,-622060475,530544194,-692085791,81333028,-1314725839,-803837215,-836864301,
			-396630536,482092554,1268583778,1102314737,1356318863,2017848663,728040402,-986355422,-281832924,-1488999148,1138169072,-1128483081,144571110,69604861,-1283526977,557713939,
			-1864416140,-851139008,1882799500,-1521489271,46282440,1907821148,-627174979,-1009758437,-1003589573,390353238,618763753,691042080,1020133898,-864162233,128259887,2003932655,
			123232766,499944097,1416780117,-1675867186,-824400709,794077453,1926971299,-826477924,2057637859,282925234,-1166556976,-709970322,1150154447,-1138674346,-381931406,1581220092,
			921299292,288947487,-1428041050,209105982,1481454200,805367917,1130087490,1711846459,2094704261,-1062484575,-208522940,-482262555,1307296431,417852178,-1663182056,1072604677,
			-1708254838,-1371350435,2097016341,-1651004594,1771049978,-239846942,-1719522772,663119953,-1583940231,-2089103457,-1763486021,-1334715561,1073243046,-1447738395,1153698726,818959299,
			1385219505,392213442,504612290,605859043,-586051342,1554453443,1494110078,-2103945096,-838832017,-861445634,-397315012,2085894382,-35525721,1075947457,-75298301,696929521,
			-190229393,-542060825,591480515,462563421,-624425619,182692356,962029112,419053617,-1938427573,-1987355825,-1821335232,1452278664,1057849525,1596240674,-742165668,778094555,
			-1986740071,484647987,-529583377,-1392672270,-355606159,-261441939,663090646,1284198642,409535626,-2115547222,1526515968,-596977272,2083609663,2126460322,1023162297,-478122107,
			1162844460,-1239195413,-364175665,-1033432952,1559996494,-681074908,1305335806,2048405296,1597490017,894207317,223587248,1121078306,1903086792,1822766069,1294768924,239624775,
			1913651474,1879238252,490828939,-1486610288,-2055081220,1262893543,1067989796,1629811540,1980160625,-1249445860,1039064020,761651479,-1251199979,1239934427,1872115285,-2087439526,
			278900579,895666648,-1451789994,-1230966470,1317235792,873756157,-888299103,951528016,77302374,-33073453,1138606471,-763293071,-445708303,-1516259415,-126421489,50718901,
			-1380585772,492688557,-1547360580,-1804945875,-1632709464,736083088,-290463115,-249572099,624273660,1213006976,-46164047,-331002576,-428486143,882264500,499325505,2135948668,
			-330195418,-1372421306,-1682998903,1983485732,349765652,960767700,1966352158,583362401,963223080,-581953370,-675451444,-157027540,-1152241513,-1510969343,-723568790,1400856562,
			1158885505,-584251496,-1235082358,-147148903,-1734945164,1080287949,-1251152171,703449747,872048481,-471114171,-1643894921,979917370,1875039568,349713001,358233619,-448433427,
			2056694992,1267811914,-827739488,-103149652,1071076189,756765411,-141292663,-276276377,-676096688,-1404676991,-1975616563,625069069,-1735555425,-1759064740,10965672,876744040,
			1120775730,1082448028,434997167,374127294,159221485,-2144662252,-8084489,-554178049,1799572832,-130077093,495908536,1858962979,-392618586,1762570516,1859657006,-699565742,
			-1520868663,-537326583,-769009688,370845774,-319122121,-1184769172,1005924899,-1998800002,1645496259,-1059794104,1015674670,-1969178067,1527220449,902136222,1616280235,
	};
	
}
