package tholin.planetGen.utils;

import java.util.*;

public class Maths {
	
	public static double angleFromCoordinate(double lat1, double long1, double lat2, double long2) {
		double dLon = Math.toRadians(long2 - long1);
		
		double y = Math.sin(dLon) * Math.cos(Math.toRadians(lat2));
		double x = Math.cos(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) - Math.sin(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(dLon);
		
		double brng = Math.atan2(y, x);
		
		brng = Math.toDegrees(brng);
		brng = (brng + 360) % 360;
		brng = 360 - brng;
		
		return brng;
	}
	
	public static double gcDistance(double lat1, double long1, double lat2, double long2) {
		double diffLong = Math.abs(long1 - long2);
		double a = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2));
		double b = Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(diffLong));
		double dist = Math.acos(a + b) / Math.PI;
		return dist;
	}
	
	// Function taken from https://www.youtube.com/watch?v=lctXaT9pxA0
	public static double biasFunction(double x, double bias) {
		// Adjust input to make control feel more linear
		double k = 1.0 - bias;
		k = k * k * k;
		// Equation based on: shadertoy.com/view/Xd2yRd
		return (x * k) / (x * k - x + 1.0);
	}
	
	public static double biasedRNG(Random rng, double bias) {
		if(bias <= 1e-8) return 0;
		return (rng.nextDouble() * 2.0 - 1.0) * bias;
	}
}