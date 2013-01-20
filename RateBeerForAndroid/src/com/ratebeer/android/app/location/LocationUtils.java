package com.ratebeer.android.app.location;

import android.content.res.Resources;
import android.location.Location;

import com.google.android.maps.GeoPoint;
import com.ratebeer.android.R;
import com.ratebeer.android.api.command.GetPlacesAroundCommand.Place;
import com.ratebeer.android.app.ApplicationSettings;

public class LocationUtils {

	private static final String DECIMAL_FORMATTER = "%.1f";
	
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

	/**
	 * Calculate the distance to a place from our current location
	 * @param rbActivity The activity to retrieve settings and resources from
	 * @param place The place object to calculate the distance to
	 * @param currentLocation Our current location, or null if not known
	 * @return If our current location is known, the distance between this and the place's location is returned, in
	 * miles or kilometers (according to the user settings)
	 */
	public static String getPlaceDistance(ApplicationSettings settings, Resources resources, Place place,
			Location currentLocation) {
		// Calculate distance
		double distance = LocationUtils.distanceInMiles(place.latitude, place.longitude, currentLocation.getLatitude(),
				currentLocation.getLongitude());
		// Show in KM instead of miles?
		if (settings.showDistanceInKm()) {
			distance *= 1.609344;
		}
		return String.format(DECIMAL_FORMATTER, distance)
				+ (settings.showDistanceInKm() ? resources.getString(R.string.places_km) : resources
						.getString(R.string.places_m));
	}

	/**
	 * Convert latitude and longitude in double format to a GeoPoint object
	 * @param lat The latitude, like 4.542334
	 * @param lon The longitude, like 23.182631
	 * @return The converted GeoPoint object, like (4542334, 23182631)
	 */
	public static GeoPoint getPoint(double lat, double lon) {
		return new GeoPoint((int)(lat * 1E6), (int)(lon * 1E6));
	}

}
