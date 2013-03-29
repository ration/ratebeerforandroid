package com.ratebeer.android.app.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;

import com.googlecode.androidannotations.annotations.Bean;
import com.googlecode.androidannotations.annotations.EReceiver;
import com.ratebeer.android.app.ApplicationSettings;

@EReceiver
public class PassiveLocationUpdateReceiver extends BroadcastReceiver  {

	public static final long PASSIVE_MAX_TIME = 3600000; // 1 hour
	public static final long PASSIVE_MAX_DISTANCE = 500; // In meters
	public static final String STORAGE_PROVIDER = "storage";
	
	@Bean
	protected ApplicationSettings applicationSettings;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		// We should have a location in our extras, which is the passively received user location
		if (!intent.hasExtra(LocationManager.KEY_LOCATION_CHANGED))
			return;
		
		// Store the new location
		Location location = (Location) intent.getExtras().get(LocationManager.KEY_LOCATION_CHANGED);
		applicationSettings.saveLastUserLocation(location);
		
	}
	
}
