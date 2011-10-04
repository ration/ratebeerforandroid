/*
    This file is part of RateBeer For Android.
    
    RateBeer for Android is free software: you can redistribute it 
    and/or modify it under the terms of the GNU General Public 
    License as published by the Free Software Foundation, either 
    version 3 of the License, or (at your option) any later version.

    RateBeer for Android is distributed in the hope that it will be 
    useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
    of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with RateBeer for Android.  If not, see 
    <http://www.gnu.org/licenses/>.
 */
package com.ratebeer.android.app.location;

import java.util.Timer;
import java.util.TimerTask;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * Gets the current location of the user in a somewhat robust and precise way
 * Code by Fedor: http://stackoverflow.com/questions/3145089/what-is-the-simplest-and-most-robust-way-to-get-the-users-current-location-in-an/3145655#3145655
 */
public class MyLocation {

	private Timer timer1;
	private LocationManager lm;
	private LocationResult locationResult;
	private boolean gps_enabled = false;
	private boolean network_enabled = false;

	public boolean getLocation(Context context, LocationResult result) {
		// I use LocationResult callback class to pass location value from
		// MyLocation to user code.
		locationResult = result;
		if (lm == null)
			lm = (LocationManager) context
					.getSystemService(Context.LOCATION_SERVICE);

		// exceptions will be thrown if provider is not permitted.
		try {
			gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
		} catch (Exception ex) {
		}
		try {
			//network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			network_enabled = false;
			for (String provider : lm.getAllProviders()) {
				if (LocationManager.NETWORK_PROVIDER.equals(provider)) {
					network_enabled = true;
					break;
				}
			}
		} catch (Exception ex) {
		}

		// don't start listeners if no provider is enabled
		if (!gps_enabled && !network_enabled)
			return false;

		if (gps_enabled)
			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
					locationListenerGps);
		if (network_enabled)
			lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
					locationListenerNetwork);
		timer1 = new Timer();
		timer1.schedule(new GetLastLocation(), 20000);
		return true;
	}

	private LocationListener locationListenerGps = new LocationListener() {
		public void onLocationChanged(Location location) {
			timer1.cancel();
			locationResult.gotLocation(location);
			lm.removeUpdates(this);
			lm.removeUpdates(locationListenerNetwork);
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};

	private LocationListener locationListenerNetwork = new LocationListener() {
		public void onLocationChanged(Location location) {
			timer1.cancel();
			locationResult.gotLocation(location);
			lm.removeUpdates(this);
			lm.removeUpdates(locationListenerGps);
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};

	private class GetLastLocation extends TimerTask {
		@Override
		public void run() {
			lm.removeUpdates(locationListenerGps);
			lm.removeUpdates(locationListenerNetwork);

			Location net_loc = null, gps_loc = null;
			if (gps_enabled)
				gps_loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (network_enabled)
				net_loc = lm
						.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

			// if there are both values use the latest one
			if (gps_loc != null && net_loc != null) {
				if (gps_loc.getTime() > net_loc.getTime())
					locationResult.gotLocation(gps_loc);
				else
					locationResult.gotLocation(net_loc);
				return;
			}

			if (gps_loc != null) {
				locationResult.gotLocation(gps_loc);
				return;
			}
			if (net_loc != null) {
				locationResult.gotLocation(net_loc);
				return;
			}
			locationResult.gotLocation(null);
		}
	}

	public static abstract class LocationResult {
		public abstract void gotLocation(Location location);
	}

}