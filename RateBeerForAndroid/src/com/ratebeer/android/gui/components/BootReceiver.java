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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import com.ratebeer.android.app.ApplicationSettings;
import com.ratebeer.android.app.RateBeerForAndroid;

/**
 * Receives a broadcast message when the device has started and is used to manually start/stop the alarm service.
 * @author erickok
 */
public class BootReceiver extends BroadcastReceiver {

	private static AlarmManager mgr;
	private static PendingIntent pi = null;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(RateBeerForAndroid.LOG_NAME, "Boot signal received, starting beermail service");
		startAlarm(context);
	}

	public static void cancelAlarm() {
		if (mgr != null) {
			mgr.cancel(pi);
		}
	}

	public static void startAlarm(Context context) {
		if (isBeermailEnabled(context)) {
			// Set up PendingIntent for the alarm service
			mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			Intent i = new Intent(context, AlarmReceiver.class);
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

}
