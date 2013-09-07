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
package com.ratebeer.android.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.preference.PreferenceManager;

import com.googlecode.androidannotations.annotations.EBean;
import com.googlecode.androidannotations.annotations.RootContext;
import com.googlecode.androidannotations.api.Scope;
import com.ratebeer.android.R;
import com.ratebeer.android.api.UserSettings;
import com.ratebeer.android.api.command.Country;
import com.ratebeer.android.api.command.State;
import com.ratebeer.android.app.location.PassiveLocationUpdateReceiver;

@EBean(scope = Scope.Singleton)
public class ApplicationSettings {

	private static final String USER_SETTINGS = "user_settings";
	private static final String LAST_USED_COUNTRY = "last_used_country";
	private static final String LAST_USED_STATE = "last_used_state";
	private static final String DISTANCE_IN_KM = "distance_in_km";
	private static final String SHARE_TEXT = "share_text";
	private static final String IS_FIRST_START = "is_first_start";
	public static final String ENABLE_BEERMAIL = "enable_beermail";
	public static final String BEERMAIL_UPDATEFREQUENCY = "beermail_updatefrequency";
	private static final String BEERMAIL_VIBRATE = "beermail_vibrate";
	private static final String LAST_USER_LOCATION_LAT = "last_user_location_latitude";
	private static final String LAST_USER_LOCATION_LONG = "last_user_location_longitude";
	
	@RootContext
	protected Context context;
	protected SharedPreferences prefs;
	
	public ApplicationSettings(Context context) {
		this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	/**
	 * Whether distances should be displayed in kilometers instead of miles
	 * @return True if km should be used; false otherwise
	 */
	public boolean showDistanceInKm() {
		return prefs.getBoolean(DISTANCE_IN_KM, false);
	}
	
	/**
	 * Get the parameterized text to use when sharing a (new) rating
	 * @return The raw text to use, which should be used with String.format() 
	 */
	public String getRatingShareText() {
		return prefs.getString(SHARE_TEXT, context.getString(R.string.app_ratedwithurl));
	}
	
	/**
	 * Returns the stored user settings
	 * @return The user settings object
	 */
	public UserSettings getUserSettings() {
		return UserSettings.fromSinglePreference(prefs.getString(USER_SETTINGS, null));
	}

	/**
	 * Stores the user settings in the Android preferences
	 * @param user The user settings object to store
	 */
	public void saveUserSettings(UserSettings user) {
		Editor editor = prefs.edit();
		if (user == null) {
			// Remove the user settings
			editor.remove(USER_SETTINGS);
		} else {
			// Store the (updated) user settings
			editor.putString(USER_SETTINGS, user.toSinglePreference());
		}
		editor.commit();
	}

	/**
	 * Returns the last used (user-selected) country
	 * @return The last used country object
	 */
	public Country getLastUsedCountry() {
		return Country.fromSinglePreference(prefs.getString(LAST_USED_COUNTRY, null));
	}

	/**
	 * Stores the last used country in the Android preferences so the app can pre-select it
	 * @param name The country to store
	 */
	public void saveLastUsedCountry(Country country) {
		Editor editor = prefs.edit();
		if (country == null) {
			// Remove the last used country setting
			editor.remove(LAST_USED_COUNTRY);
		} else {
			// Store the last used country settings
			editor.putString(LAST_USED_COUNTRY, country.toSinglePreference());
		}
		editor.commit();
	}

	/**
	 * Returns the last used (user-selected) state
	 * @return The last used state object
	 */
	public State getLastUsedState() {
		return State.fromSinglePreference(prefs.getString(LAST_USED_STATE, null));
	}

	/**
	 * Stores the last used state in the Android preferences so the app can pre-select it
	 * @param name The state to store, or null to remove the setting
	 */
	public void saveLastUsedState(State state) {
		Editor editor = prefs.edit();
		if (state == null) {
			// Remove the last used state setting
			editor.remove(LAST_USED_STATE);
		} else {
			// Store the last used state settings
			editor.putString(LAST_USED_STATE, state.toSinglePreference());
		}
		editor.commit();
	}

	/**
	 * Returns whether this is the first time the application is started
	 * @return True if we cannot find a shared preference about a first start
	 */
	public boolean isFirstStart() {
		return prefs.getBoolean(IS_FIRST_START, true);
	}

	/**
	 * Stores a preference that we started the application
	 */
	public void recordFirstStart() {
		Editor editor = prefs.edit();
		editor.putBoolean(IS_FIRST_START, false);
		editor.commit();
	}

	/**
	 * Whether the background beermail notification service is enabled
	 * @return True if the user enabled background notifications
	 */
	public boolean isBeermailEnabled() {
		return prefs.getBoolean(ENABLE_BEERMAIL, true);
	}

	/**
	 * The frequency in which to check for new beermail
	 * @return The update interval in number of minutes
	 */
	public int getBeermailUpdateFrequency() {
		return Integer.parseInt(prefs.getString(BEERMAIL_UPDATEFREQUENCY, "3600"));
	}

	/**
	 * Whether to vibrate when sending beermail notifications
	 * @return True if the user requested to vibrate on notifications
	 */
	public boolean getVibrateOnNotification() {
		return prefs.getBoolean(BEERMAIL_VIBRATE, false);
	}

	/**
	 * Stores the last user location known so the app can quickly show a map location
	 * @param name The location to store, or null to remove the last location
	 */
	public void saveLastUserLocation(Location location) {
		Editor editor = prefs.edit();
		if (location == null) {
			// Remove the last used user location
			editor.remove(LAST_USER_LOCATION_LAT);
			editor.remove(LAST_USER_LOCATION_LONG);
		} else {
			// Store the last used user location
			editor.putLong(LAST_USER_LOCATION_LAT, (long) (location.getLatitude() * 1E6));
			editor.putLong(LAST_USER_LOCATION_LONG, (long) (location.getLongitude() * 1E6));
		}
		editor.commit();
	}

	/**
	 * Returns the last user location we know about
	 * @return The user's last location, or null if not known at all
	 */
	public Location getLastUserLocation() {
		long latitude = prefs.getLong(LAST_USER_LOCATION_LAT, Long.MIN_VALUE);
		long longitude = prefs.getLong(LAST_USER_LOCATION_LONG, Long.MIN_VALUE);
		if (latitude == Long.MIN_VALUE || longitude == Long.MIN_VALUE)
			return null;
		// A latitude and longitude were stored (as Long values); return a storage Location object
		Location location = new Location(PassiveLocationUpdateReceiver.STORAGE_PROVIDER);
		location.setLatitude((double)(latitude) / 1E6);
		location.setLongitude((double)(longitude) / 1E6);
		return location;
	}

}
