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
package com.ratebeer.android.gui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.ratebeer.android.R;
import com.ratebeer.android.app.ApplicationSettings;
import com.ratebeer.android.app.RateBeerForAndroid;
import com.ratebeer.android.gui.components.BootReceiver;

public class PreferencesInterface extends SherlockPreferenceActivity {

	private SharedPreferences prefs;

	public PreferencesInterface() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_USE_LOGO,
				ActionBar.DISPLAY_SHOW_TITLE);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		addPreferencesFromResource(R.xml.pref_interface);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(onPreferenceChanged);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// Home button click in the action bar
			Intent startActivity = new Intent(this, HomePhone.class);
			startActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			startActivity(startActivity);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		prefs.registerOnSharedPreferenceChangeListener(onPreferenceChanged);
		super.onResume();
	}

	@Override
	protected void onPause() {
		prefs.unregisterOnSharedPreferenceChangeListener(onPreferenceChanged);
		super.onPause();
	}

	private OnSharedPreferenceChangeListener onPreferenceChanged = new OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			if (key.equals(ApplicationSettings.ENABLE_BEERMAIL)
					|| key.equals(ApplicationSettings.BEERMAIL_UPDATEFREQUENCY)) {
				// If a beermail setting was changed, start/stop the service accordingly
				if (getRateBeerApplication().getSettings().isBeermailEnabled()) {
					BootReceiver.startAlarm(getApplicationContext());
				} else {
					BootReceiver.cancelAlarm();
				}
			}
		}
	};

	public RateBeerForAndroid getRateBeerApplication() {
		return (RateBeerForAndroid) getApplication();
	}

}
