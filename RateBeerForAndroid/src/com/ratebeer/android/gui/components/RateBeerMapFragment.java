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

import android.os.Bundle;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.googlecode.androidannotations.annotations.EBean;

/**
 * Helper for fragments that contain a MapView. It takes care of calling onCreate, onDestroy, etc. Overriding fragments
 * MUST call setMapView to manage the contained map appropriately.
 * @author Eric Kok
 */
@EBean
public abstract class RateBeerMapFragment extends RateBeerFragment {

	private MapView map = null;
	
	public RateBeerMapFragment() {
	}
	
	/**
	 * Registers a map view contained in this fragment, so its life cycle can be managed
	 * @param mapView The MapView object that should be managed and which can be received using getMapView() or getMap()
	 */
	protected void setMapView(MapView mapView) {
		this.map = mapView;
		if (map != null) {
			// Ideally we should pass the Fragment's savedInstanceState to let MapView reconstruct itself. However, due
			// to https://code.google.com/p/gmaps-api-issues/issues/detail?id=5083 this will cause exceptions on 
			// orientation changes (when the contained Parcelable objects are to be unpacked). Instead the fragemnts 
			// will manually re-initialize and re-populate the map.
			map.onCreate(null);
			try {
				// Force initialization of the Google Play Services
				// See http://stackoverflow.com/questions/13905230/mapview-and-cameraupdate-in-api-v2
				MapsInitializer.initialize(getActivity());
			} catch (GooglePlayServicesNotAvailableException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Returns the actual map object contained in the internally managed MapView
	 * @return A GoogleMap that can be further manipulated to adjust camera, add markers, etc.
	 */
	protected GoogleMap getMap() {
		return map.getMap();
	}

	/**
	 * Returns the now-managed map view tight to this fragment
	 * @return The internally managed MapView view object
	 */
	protected MapView getMapView() {
		return map;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (map != null)
			map.onResume();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if (map != null)
			map.onPause();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (map != null)
			map.onDestroy();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (map != null)
			map.onSaveInstanceState(outState);
	}
	
	@Override
	public void onLowMemory() {
		super.onLowMemory();
		if (map != null)
			map.onLowMemory();
	}
}
