package com.ratebeer.android.app.location;

public class LocationUtils {
	
	/**
	 * Calculate (with a reasonable accuracy) the distance between two GPS coordinates. Taken from 
	 * http://stackoverflow.com/a/123305/243165
	 * @param lat1 Latitude of point 1
	 * @param lng1 Longitude of point 1
	 * @param lat2 Latitude of point 2
	 * @param lng2Longitude of point 2
	 * @return The distance between the two points in miles
	 */
	public static double distanceInMiles(double lat1, double lng1, double lat2, double lng2) {
		double earthRadius = 3958.75;
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);
		double sindLat = Math.sin(dLat / 2);
		double sindLng = Math.sin(dLng / 2);
		double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2) * Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double dist = earthRadius * c;
		return dist;
	}
	
}
