package theGhastModding.planetGen.generators;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

import javax.imageio.ImageIO;

import edu.cornell.lassp.houle.RngPack.RanMT;
import theGhastModding.planetGen.noise.NoiseConfig;
import theGhastModding.planetGen.noise.OctaveNoise3D;
import theGhastModding.planetGen.noise.OctaveWorley;
import theGhastModding.planetGen.utils.CraterGenerator;
import theGhastModding.planetGen.utils.MapUtils;
import theGhastModding.planetGen.utils.NoisemapGenerator;
import theGhastModding.planetGen.utils.ProgressBars;
import theGhastModding.planetGen.utils.CraterGenerator.CraterConfig;
import theGhastModding.planetGen.utils.CraterGenerator.CraterDistributionSettings;

public class AsteroidMoonGen {
	
	public static class AsteroidGenSettings {
		
		public int width                    = 4096;
		public int height                   = 2048;
		public int planetRadius             = 20000;
		public boolean ridgedShape          = false;
		
		public NoiseConfig shapeNoise       = new NoiseConfig(new OctaveNoise3D(16, 16, 16, 3, 2.0, 0.32)).setIsRidged(ridgedShape).setNoiseStrength(1.35).setNoiseScale(2.0).setDistortStrength(0.25).setNoiseOffset(ridgedShape ? 0.078 : 0.25);
		public NoiseConfig groundNoise      = new NoiseConfig(new OctaveNoise3D(16, 16, 16, 4, 2.0, 0.55)).setIsRidged(false).setNoiseStrength(0.35).setNoiseScale(0.85).setDistortStrength(0.5).setNoiseOffset(0.325);
		public NoiseConfig peakNoise        = new NoiseConfig(new OctaveNoise3D(16, 16, 16, 5, 2.0, 0.6)).setIsRidged(false).setNoiseStrength(0.125).setNoiseScale(0.64).setDistortStrength(0.5).setNoiseOffset(0.325);
		public NoiseConfig secondColorNoise = new NoiseConfig(new OctaveNoise3D(16, 16, 16, 2, 2.0, 0.5)).setIsRidged(true).setNoiseStrength(1.0).setNoiseScale(1.15).setDistortStrength(0.25).setNoiseOffset(0.325);
		public NoiseConfig colorNoise       = new NoiseConfig(new OctaveWorley(32, 32, 32, 5, 2.0, 0.6)).setIsRidged(false).setNoiseStrength(1.25).setNoiseScale(1.0).setDistortStrength(0.75).setNoiseOffset(0);
		
		public CraterConfig craterConfig    = CraterConfig.genBowlOnlyConfig(0, 0, 0.1, 0.5, 1.0, 4.8);
		public int craterCount              = 512;
		public double craterMaxsize         = 128;
		public double craterMinsize         = 16;
		public double craterMaxstrength     = 0.2;
		public double craterMinstrength     = 0.05;
		
		public double[] normalColor         = MapUtils.RGB(new Color(53, 53, 53));
		public double[] peaksColor          = MapUtils.RGB(new Color(115, 129, 133));
		public double[] secondaryColor      = MapUtils.RGB(new Color(88, 64, 64));
		
		public double[] biomeColorNormal    = MapUtils.RGB(new Color(53, 53, 53));
		public double[] biomeColorPeaks     = MapUtils.RGB(new Color(115, 115, 115));
		public double[] biomeColorSecondary = null;
		
		public AsteroidGenSettings() {
			
		}
		
	}
	
	public static GeneratorResult generate(Random rng, AsteroidGenSettings settings, boolean debugProgress, boolean debugSteps, boolean test) throws Exception {
		GeneratorResult result = new GeneratorResult();
		
		settings.shapeNoise.noise.initialize(rng);
		settings.groundNoise.noise.initialize(rng);
		settings.peakNoise.noise.initialize(rng);
		settings.secondColorNoise.noise.initialize(rng);
		settings.colorNoise.noise.initialize(rng);
		
		// Copy commonly-used config items into local variables to make the code more readable
		final int width     = settings.width;
		final int height    = settings.height;
		final double resMul = (double)settings.planetRadius / 20000.0 * 0.85;
		
		double[][] shapeMap      = new double[width][height];
		double[][] tempMap       = new double[width][height];
		double[][] tempMap2      = new double[width][height];
		double[][] finalNoiseMap = new double[width][height];
		double[][][] colorMap    = new double[width][height][3];
		
		if(debugProgress) System.out.println("Shape");
		NoisemapGenerator.genNoisemap(shapeMap, settings.shapeNoise, null, resMul, debugProgress);
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				shapeMap[i][j] = Math.min(1, Math.max(0, shapeMap[i][j]));
			}
		}
		if(debugSteps) ImageIO.write(MapUtils.renderMap(shapeMap), "png", new File("shape.png"));
		
		if(debugProgress) System.out.println("Ground");
		NoisemapGenerator.genNoisemap(tempMap, settings.groundNoise, null, resMul, debugProgress);
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				finalNoiseMap[i][j] = shapeMap[i][j] + tempMap[i][j];
			}
		}
		if(debugSteps) ImageIO.write(MapUtils.renderMap(tempMap), "png", new File("ground.png"));
		
		if(debugProgress) System.out.println("Peaks");
		NoisemapGenerator.genNoisemap(tempMap, settings.peakNoise, shapeMap, resMul, debugProgress);
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				finalNoiseMap[i][j] += tempMap[i][j];
			}
		}
		if(debugSteps) ImageIO.write(MapUtils.renderMap(tempMap), "png", new File("peaks.png"));
		
		if(debugProgress) System.out.println("Craters");
		CraterDistributionSettings cds = new CraterDistributionSettings(settings.craterCount, settings.craterMinsize, settings.craterMaxsize, settings.craterMinstrength, settings.craterMaxstrength, 0, 1000000, null, 0.7);
		CraterGenerator.distributeCraters(null, finalNoiseMap, settings.craterConfig, settings.craterConfig, cds, rng);
		
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
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
		if(debugProgress) System.err.println(biggestPixelValue);
		result.heightmap = img;
		result.heightmap16 = MapUtils.render16bit(finalNoiseMap);
		result.heightmap24 = MapUtils.render24bit(finalNoiseMap);
		result.heightmapRaw = new double[width][height];
		for(int i = 0; i < width; i++) for(int j = 0; j < height; j++) result.heightmapRaw[i][j] = finalNoiseMap[i][j];
		if(debugSteps) {
			ImageIO.write(img, "png", new File("asteroid.png"));
			ImageIO.write(result.heightmap16, "png", new File("asteroid_16.png"));
			ImageIO.write(result.heightmap24, "png", new File("asteroid_24.png"));
		}
		if(debugProgress) System.out.println("Done.");
		
		if(debugProgress) System.out.println("Color Map!");
		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		NoisemapGenerator.genNoisemap(tempMap, settings.colorNoise, null, resMul, debugProgress);
		NoisemapGenerator.genNoisemap(tempMap2, settings.secondColorNoise, null, resMul, debugProgress);
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				shapeMap[i][j] = Math.min(1, Math.max(0, shapeMap[i][j] * 7.0)) - 0.42;
				if(shapeMap[i][j] < 0) shapeMap[i][j] = 0;
				tempMap2[i][j] = Math.min(1, Math.max(0, tempMap2[i][j] * 1.25));
			}
		}
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				double[] rgb = new double[] {
					settings.normalColor[0] * (1.0 - shapeMap[i][j]) + shapeMap[i][j] * settings.peaksColor[0],
					settings.normalColor[1] * (1.0 - shapeMap[i][j]) + shapeMap[i][j] * settings.peaksColor[1],
					settings.normalColor[2] * (1.0 - shapeMap[i][j]) + shapeMap[i][j] * settings.peaksColor[2],
				};
				rgb[0] = rgb[0] * (1.0 - tempMap2[i][j]) + tempMap2[i][j] * settings.secondaryColor[0];
				rgb[1] = rgb[1] * (1.0 - tempMap2[i][j]) + tempMap2[i][j] * settings.secondaryColor[1];
				rgb[2] = rgb[2] * (1.0 - tempMap2[i][j]) + tempMap2[i][j] * settings.secondaryColor[2];
				double mul = tempMap[i][j];
				mul += 0.5;
				if(mul > mul) mul = 1;
				rgb[0] = mul * rgb[0];
				rgb[1] = mul * rgb[1];
				rgb[2] = mul * rgb[2];
				double heightCol = finalNoiseMap[i][j];
				heightCol *= 0.1;
				rgb[0] += heightCol;
				rgb[1] += heightCol;
				rgb[2] += heightCol;
				
				colorMap[i][j] = rgb;
			}
		}
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				int r = (int)Math.max(0, Math.min(255, colorMap[i][j][0] * 255.0));
				int g = (int)Math.max(0, Math.min(255, colorMap[i][j][1] * 255.0));
				int b = (int)Math.max(0, Math.min(255, colorMap[i][j][2] * 255.0));
				
				img.setRGB(i, j, b | (g << 8) | (r << 16));
			}
		}
		result.colorMap = img;
		if(debugSteps) ImageIO.write(img, "png", new File("colors.png"));
		if(debugProgress) System.out.println("Done.");
		
		if(debugProgress) System.out.println("Biome map");
		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		if(debugProgress) ProgressBars.printBar();
		for(int i = 0; i < width; i++) {
			if(debugProgress) ProgressBars.printProgress(i, width);
			for(int j = 0; j < height; j++) {
				if(settings.biomeColorSecondary != null && tempMap2[i][j] >= 0.75) {
					colorMap[i][j] = settings.biomeColorSecondary;
					continue;
				}
				if(shapeMap[i][j] <= 0.25) {
					colorMap[i][j] = settings.biomeColorNormal;
				}else {
					colorMap[i][j] = settings.biomeColorPeaks;
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
		if(debugProgress) System.out.println("Done.");
		return result;
	}
	
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
			AsteroidGenSettings settings = new AsteroidGenSettings();
			GeneratorResult res = AsteroidMoonGen.generate(rng, settings, true, true, test);
			ImageIO.write(res.heightmap, "png", new File("past_outputs/" + name + ".png"));
			ImageIO.write(res.heightmap16, "png", new File("past_outputs/" + name + "_16.png"));
			ImageIO.write(res.colorMap, "png", new File("past_outputs/" + name + "_colors.png"));
			ImageIO.write(res.biomeMap, "png", new File("past_outputs/" + name + "_biomes.png"));
		}catch(Exception e) {
			System.err.println("Error: ");
			e.printStackTrace();
			System.exit(1);
		}
		NoisemapGenerator.cleanUp();
	}
	
	private static final int[] fixed_seed = new int[] {
			392787231,1307843139,-1720014231,-45524606,914705216,-1128472880,-2123214231,-250052666,1429595437,-1450265519,-538846323,-1391990767,-1039870813,-260475542,2074517471,-1609650416,
			-1644494826,244226914,-1561327355,-672184823,-498229661,-407778021,-1552951788,-542078371,-1963048088,851116207,-1449234750,-1376686645,-876610391,-829674973,-1350219482,769208436,
			-860652299,888962682,-4105012,-1012521198,-859022667,985350654,-173037190,-1944250872,-1343137097,1301031447,267765771,-61138897,-282473156,1820282112,517510017,-171174116,
			-1721107780,671523643,1410521414,1481102616,-1815044038,302716481,1042161739,-1701346151,-1872266385,677585665,1159691522,-135925565,1119414344,-296941068,334132572,1323054779,
			835488717,466521633,1942746458,-445838186,660410989,68962067,-2045815840,-493839530,-1537672448,376078823,-227580109,1970464550,-218074193,650751819,-1780111076,1339475685,
			1731348322,2129684219,2039049303,1336896984,498719056,286586273,1567694674,-231570455,330314079,-1992785923,-272129221,1894042849,-1086187259,1669705786,-595088142,929260350,
			1802323968,-1599800896,1049574804,-1226080807,2022088922,724068060,-1627577360,2061683611,1268480980,-2089747587,-1677028779,-941467382,654313026,-777018410,-2066562242,-49935644,
			1587326147,266581568,-1279611834,1703060718,1523357817,-1745288292,896189781,650404940,-1528874149,73889353,1452071516,2107123217,-930157215,-67746511,113242622,-1960762992,
			771659777,864334167,-1686880288,-2002964579,806384694,-1426433745,1637952515,1463395104,-1691888537,-322889943,-1719180160,125481612,1890059201,-513117405,-336526415,569863949,
			1136800042,-1688034724,614581169,829181544,-1000051814,1490229337,32064593,1281770032,358330320,-602495117,898194912,587751672,1526399846,627126482,1197639285,1672355521,
			-2015780338,-1971526279,-426228008,527177166,-847627640,599725634,1512029767,900816506,1055734361,-2020174473,770324411,-1265928302,599796203,-665475734,-300036887,-1991327690,
			835459665,974748349,-1106371440,-744128878,1689592078,-521057235,-588837184,-1421379191,1915544714,161936081,-1237604567,786178403,-364234493,1506483089,-253039359,1086932648,
			-343421853,1441203766,185392242,1827693763,-230956360,-688531713,1977183491,278690136,-1281678399,-1433962886,-793008672,69364845,613971711,-751893791,-687685357,-1275721258,
			-986452224,824102900,-807181986,-852129386,151701301,-1256982691,-725952940,1609410304,1044017279,-1577879191,231108849,-1617409090,-1545032406,-876941884,432072626,949470601,
			2046999749,883344356,823642851,-2018471681,-1274701199,-475000140,967510667,-557600680,-1595848820,1371798030,-330790486,1749783891,1246308684,1431737306,1633010347,693999812,
			-2046593808,104034857,-698778622,405930934,-1274434523,-1381125655,1113034250,997500611,-326358607,2039585519,1529918826,1289749592,-248857250,1383714724,41449618,1743062967,
			-1930143262,-395702609,-1401742181,-480162224,788516878,-1026457028,942516527,-1817837587,1940888408,-480861854,-605946503,1481717440,15908634,1427022011,1025045425,129759600,
			-1844137255,-738037313,-2009294998,2147402064,1341021706,-1826401212,1603037195,-473000706,-1702222368,152106898,-978997631,-810242227,965544057,-1772934789,-1139678623,-1570588294,
			-2131310052,-907471132,713890700,-920284922,1477128675,55582116,-940139833,1605936020,947279249,862202866,706566851,1492899001,738064093,-2017742483,-707086696,-733087903,
			-2024673606,-564060491,623713597,965596852,-1597133982,-99879196,491050516,872664450,171752338,-1858375653,-625682677,-1430916843,-453743601,2094310182,238659266,1659422448,
			-1076595871,2099661952,549999259,265194981,251702790,1182014825,-103321024,816257127,-1251611500,-932472244,252984561,1154626466,1213976188,-299594194,1492495694,-1953063144,
			-1376407815,1720650081,64993196,-1147168893,1993646675,1601237417,969444082,-596447875,-594771603,2136286274,747759812,-1302221296,-1645345010,-720493197,-1651996493,-72000467,
			180878365,-441556899,2059111283,337361083,1894871857,2081945978,-820117231,1905465952,-995158297,1602628771,782531892,-732342436,-825916313,-1598931016,-70206805,-241222340,
			1657452946,-1082785216,1616728373,-687695580,545820891,-2062689008,-1761904484,1125175397,193088836,291271835,810868308,-1252783605,-739297398,1137825792,1371769342,111994348,
			79448734,1524122180,-2005807019,-252000844,1785130811,1592289817,-1345037853,190096721,-289224174,-1331207371,-1766836675,1716815021,-704866571,-609219099,-2111897473,574053781,
			-1049881251,-1578493640,1782780948,248753874,486999497,227578212,-794963647,-1549233439,-1534642777,977928547,1547301284,1686595986,2106956673,1878270960,-65278811,362418190,
			-57899789,470098573,749366159,-137910008,-1985818540,816080833,553701086,-1531316721,1150434544,1927085563,-44187017,134561624,-183843805,1267433775,69198047,1630850196,
			-1903884340,-1058431273,-1828558218,817753386,239455996,1490626221,311166791,1880854728,-1029402550,-967791976,-2048811764,2073325797,451350915,1854648510,1030697830,-1569863937,
			-146534045,81818008,1259923382,1754044287,283051276,-1235408222,-1159335291,583502011,2043602275,-1601241680,1062714270,-1117041643,1703021077,-1726717562,-186712463,716412592,
			1645287388,-1646451905,471924398,758818897,-346883439,-1606814405,788450497,-257857740,-926682065,-1088581394,-2138006783,686949412,-141445232,1865508602,-1400430518,1065301748,
			-94464453,-2120228998,1990528696,1782711545,1217248079,1958095586,1598199496,1761089894,1059748290,-71327197,504167518,159690490,1706862512,901560216,1752447797,-2091513970,
			295439171,-1663600977,-445072600,802256103,1922357035,1387762966,-188017539,604500316,-605079746,270013435,-76552854,-1238022306,-1389630433,-1472172399,1012512140,130796562,
			-1777862174,-1018995615,-542446874,1125033290,295264701,740630734,-1349399012,1501340773,-594087759,-766809064,1039179582,-1232229320,-618849611,1742261022,-1706837624,-2004571079,
			1518463138,1490397465,2071447978,-145217841,-603327843,-1320624356,-979234784,-537703485,-1651478726,1788990560,-1613973836,1884397414,-445362837,-2095697835,-498518646,1329017592,
			-1352899972,869603552,-2110077259,-796430935,-1213946314,-1791884054,-1018500456,360474067,193363953,36248392,1689910694,-566179896,877535674,-1763760958,-1287791681,1085165623,
			-869337630,939461463,1700828392,1848824445,-880206917,-1424561365,1817260503,824038269,632419074,1784066749,1868073267,-807882742,1553054031,139613237,2086937158,-1832167607,
			-1968687638,715752700,-1806095382,1072664996,55211814,1555500783,505511534,2102712850,288071065,1034530896,-140201297,840353352,-1954366851,-676819731,-803257391,-1417150429,
			-228607672,1071983949,-1282816268,-2104243220,-2097061852,-105049758,855780449,-73915469,1027496112,-112406905,1651054314,-516268621,592662133,-1944665552,-140165510,-1460116011,
			363338503,-526430010,876537143,108561794,-1939995916,1612756842,-431662076,-254165589,414777479,-1883739295,-1179609095,-2088326112,146154335,-1196422344,1202771036,307038214,
	};
	
}