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
package com.ratebeer.android.api.command;

import android.content.Context;
import android.preference.PreferenceManager;

import com.ratebeer.android.api.RateBeerApi;
import com.ratebeer.android.api.UserSettings;
import com.ratebeer.android.app.ApplicationSettings;

public class TestHelper {

	public static RateBeerApi getApi(Context context, boolean withUser) {

		ApplicationSettings settings = new ApplicationSettings(context, PreferenceManager
				.getDefaultSharedPreferences(context));
		if (withUser) {

			// Set up API that connects to mock user rbandroid (156822)
			// This user actually exists on the site and:
			// - has 0 beer or place ratings
			// - has joined Feb 10, 2012 and favourite style Saison
			// http://www.ratebeer.com/View-User-156822.htm
			settings.saveUserSettings(new UserSettings(156822, "rbandroid", "Andr01dAPP", "", false));
			return new RateBeerApi(settings.getUserSettings());

		} else {

			// Set up API without any user credentials
			settings.saveUserSettings(null);
			return new RateBeerApi(settings.getUserSettings());

		}
		
	}

}
