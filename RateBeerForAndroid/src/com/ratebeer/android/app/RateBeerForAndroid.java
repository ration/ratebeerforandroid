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

import android.app.Application;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.ratebeer.android.api.RateBeerApi;

public class RateBeerForAndroid extends Application {

	public static final String LOG_NAME = "RateBeerForAndroid";

	private ApplicationSettings settings;
	private RateBeerApi api;

	/**
	 * Returns the application-wide user settings
	 * @return The user settings object
	 */
	public ApplicationSettings getSettings() {
		if (settings == null) {
			settings = new ApplicationSettings(getApplicationContext(), 
					PreferenceManager.getDefaultSharedPreferences(this));
		}
		return settings;
	}

	/**
	 * Returns the API to the ratebeer.com website
	 * @return The RateBeer API object
	 */
	public RateBeerApi getApi() {
		if (api == null) {
			api = new RateBeerApi(getSettings());
		}
		return api;
	}

	/**
	 * Returns whether the device is a tablet (or better: whether it can fit a tablet layout)
	 * @param r The application resources
	 * @return True if the device is a tablet, false otherwise
	 */
	public static boolean isTablet(Resources r) {
		//boolean hasLargeScreen = ((r.getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
		boolean hasXLargeScreen = ((r.getConfiguration().screenLayout & 
				Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE);
		return hasXLargeScreen;
	}

}
