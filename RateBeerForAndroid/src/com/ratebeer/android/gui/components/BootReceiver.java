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
package com.ratebeer.android.gui.components;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.googlecode.androidannotations.annotations.Bean;
import com.googlecode.androidannotations.annotations.EReceiver;
import com.googlecode.androidannotations.annotations.SystemService;
import com.ratebeer.android.app.ApplicationSettings;
import com.ratebeer.android.app.location.PassiveLocationUpdateReceiver;
import com.ratebeer.android.gui.components.helpers.Log;

/**
 * Receives a broadcast message when the device has started and is used to manually start/stop the alarm service.
 * @author erickok
 */
@EReceiver
public class BootReceiver extends BroadcastReceiver {

	@Bean
	protected Log Log;
	@SystemService
	protected LocationManager locationManager;

	private static AlarmManager mgr;
	private static PendingIntent pi = null;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME,
				"Boot signal received, starting beermail service");
		startBeerMailAlarm(context);
		startPassiveLocationUpdates(context);
	}

	public static void cancelBeerMailAlarm() {
		if (mgr != null) {
			mgr.cancel(pi);
		}
	}

	public static void startBeerMailAlarm(Context context) {
		if (isBeermailEnabled(context)) {
			// Set up PendingIntent for the alarm service
			mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			Intent i = new Intent(context, AlarmReceiver_.class);
			pi = PendingIntent.getBroadcast(context, 0, i, 0);
			// First intent after a small (2 second) delay and repeat at the user-set intervals
			mgr.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 2000,
					getBeermailUpdateFrequencyInMilliseconds(context), pi);
		}
	}

	private static boolean isBeermailEnabled(Context context) {
		// Copy of ApplicationSettings.isBeermailEnabled(), but we have no Application object in a BroadcastReceiver so
		// we cannot get to the already instantiated ApplicationSettings
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(ApplicationSettings.ENABLE_BEERMAIL,
				true);
	}

	private static int getBeermailUpdateFrequencyInMilliseconds(Context context) {
		return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString(
				ApplicationSettings.BEERMAIL_UPDATEFREQUENCY, "3600")) * 1000;
	}

	@TargetApi(Build.VERSION_CODES.FROYO)
	private void startPassiveLocationUpdates(Context context) {
		if (android.os.Build.VERSION.SDK_INT >= 8) {
			locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER,
					PassiveLocationUpdateReceiver.PASSIVE_MAX_TIME, PassiveLocationUpdateReceiver.PASSIVE_MAX_DISTANCE,
					PendingIntent.getActivity(context, 0, new Intent(context, PassiveLocationUpdateReceiver.class),
							PendingIntent.FLAG_UPDATE_CURRENT));
		}
	}

}
