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
package com.ratebeer.android.gui.fragments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.InstanceState;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.ViewById;
import com.ratebeer.android.R;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.CommandFailureResult;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.api.command.GetPlacesAroundCommand;
import com.ratebeer.android.api.command.GetPlacesAroundCommand.Place;
import com.ratebeer.android.app.location.LocationUtils;
import com.ratebeer.android.app.location.MyLocation;
import com.ratebeer.android.app.location.MyLocation.LocationResult;
import com.ratebeer.android.app.location.TouchableMapViewPager;
import com.ratebeer.android.gui.components.RateBeerActivity;
import com.ratebeer.android.gui.components.RateBeerMapFragment;
import com.ratebeer.android.gui.components.SelectLocationDialog;
import com.ratebeer.android.gui.components.SelectLocationDialog.OnLocationSelectedListener;
import com.ratebeer.android.gui.components.helpers.ArrayAdapter;
import com.viewpagerindicator.TabPageIndicator;

@EFragment(R.layout.fragment_places)
@OptionsMenu(R.menu.places)
public class PlacesFragment extends RateBeerMapFragment implements OnLocationSelectedListener {

	private static final int DEFAULT_RADIUS = 25;
	private static final String GEOCODER_PROVIDER = "geocoder";

	@InstanceState
	protected ArrayList<Place> places = null;
	@InstanceState
	protected Location lastLocation = null;
	
	@ViewById
	protected TouchableMapViewPager pager;
	@ViewById
	protected TabPageIndicator titles;
	private ListView placesView;
	private Map<String, Place> markersMap = new HashMap<String, GetPlacesAroundCommand.Place>();

	public PlacesFragment() {
	}

	@AfterViews
	public void init() {

		pager.setAdapter(new PlacesPagerAdapter());
		titles.setViewPager(pager);
		titles.setOnPageChangeListener(pager.getOnPageChangeListener());

		if (places != null) {
			publishResults(places);
		}
		
		// Retrieve already with a (old) stored location
		lastLocation = getSettings().getLastUserLocation();
		if (lastLocation != null) {
			execute(new GetPlacesAroundCommand(getUser(), DEFAULT_RADIUS, 
					lastLocation.getLatitude(), lastLocation.getLongitude()));
		}
		
		// Always trigger a location update as well
		refreshPlaces();

	}

	@OptionsItem(R.id.menu_location)
	protected void onSelectNewLocation() {
		new SelectLocationDialog(this).show(getFragmentManager(), "SelectLocationDialog");
	}

	// Implements OnLocationSelectedListener
	@Override
	public void onStartLocationSearch(String query) {
		// Try to find a location for the user query, using a Geocoder
		try {
			List<Address> point = new Geocoder(getActivity()).getFromLocationName(query, 1);
			if (point.size() <= 0) {
				// Cannot find address: give an error
				publishException(null, getString(R.string.error_nolocation));
			} else {
				// Found a location! Now look for places
				lastLocation = new Location(GEOCODER_PROVIDER);
				lastLocation.setLongitude(point.get(0).getLongitude());
				lastLocation.setLatitude(point.get(0).getLatitude());
				execute(new GetPlacesAroundCommand(getUser(), DEFAULT_RADIUS, lastLocation.getLatitude(),
						lastLocation.getLongitude()));
			}
		} catch (IOException e) {
			// Can't connect to Geocoder server: give an error
			publishException(null, getString(R.string.error_nolocation));
		}		
	}
	
	// Implements OnLocationSelectedListener
	@Override
	public void onUseCurrentLocation() {
		refreshPlaces();
	}
	
	@OptionsItem(R.id.menu_refresh)
	protected void refreshPlaces() {
		// Get the current location (if possible)
		if (new MyLocation().getLocation(getActivity(), onLocationResult)) {
			// Force the progress indicator to start
			((RateBeerActivity)getActivity()).setProgress(true);
		} else {
			publishException(null, getString(R.string.error_locationnotsupported));
		}
	}

	private LocationResult onLocationResult = new LocationResult() {

		@Override
		public void gotLocation(Location location) {
			if (location == null && getView() != null) {
				getView().post(new Runnable() {
					@Override
					public void run() {
						if (getActivity() != null) {
							publishException(null, getString(R.string.error_nolocation));
						}
					}
				});
				return;
			}
			// Now get the places at this location, if this fragment is still bound to an activity
			PlacesFragment.this.lastLocation = location;
			if (getActivity() != null && PlacesFragment.this.lastLocation != null) {
				getView().post(new Runnable() {
					@Override
					public void run() {
						getSettings().saveLastUserLocation(lastLocation);
						execute(new GetPlacesAroundCommand(getUser(), DEFAULT_RADIUS, 
								lastLocation.getLatitude(), lastLocation.getLongitude()));
					}
				});
			}
		}
	};

	private OnItemClickListener onItemSelected = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Place item = ((PlacesAdapter)placesView.getAdapter()).getItem(position);
			load(PlaceViewFragment_.builder().place(item).currentLocation(lastLocation).build());
		}
	};
	
	@Override
	public void onTaskSuccessResult(CommandSuccessResult result) {
		if (result.getCommand().getMethod() == ApiMethod.GetPlacesAround) {
			publishResults(((GetPlacesAroundCommand) result.getCommand()).getPlaces());
		}
	}

	private void publishResults(ArrayList<Place> result) {
		this.places = result;
		//Collections.sort(result, new PlacesComparator(sortOrder));
		if (placesView.getAdapter() == null) {
			placesView.setAdapter(new PlacesAdapter(getActivity(), result));
		} else {
			((PlacesAdapter) placesView.getAdapter()).replace(result);
		}

		if (lastLocation != null && getMap() != null) {

			// TODO: Show user location
			LocationUtils.initGoogleMap(getMap(), lastLocation.getLatitude(), lastLocation.getLongitude());
			for (Place place : places) {
				Marker m = getMap().addMarker(new MarkerOptions()
					.position(new LatLng(place.latitude, place.longitude))
					.title(place.placeName)
					.snippet(LocationUtils.getPlaceSnippet(getActivity(), place))
					.icon(BitmapDescriptorFactory.defaultMarker(LocationUtils.getPlaceColour(place))));
				markersMap.put(m.getId(), place);
			}
			getMap().setOnInfoWindowClickListener(onInfoWindowClicked);
			getMap().setMyLocationEnabled(true);

		}
		
	}

	private OnInfoWindowClickListener onInfoWindowClicked = new OnInfoWindowClickListener() {
		@Override
		public void onInfoWindowClick(Marker marker) {
			load(PlaceViewFragment_.builder().place(markersMap.get(marker.getId())).currentLocation(lastLocation).build());
		}
	};
	
	@Override
	public void onTaskFailureResult(CommandFailureResult result) {
		publishException(null, result.getException());
	}
	
	@Override
	protected void publishException(TextView textview, String message) {
		((RateBeerActivity)getActivity()).setProgress(false);
		super.publishException(textview, message);
	}

	private class PlacesAdapter extends ArrayAdapter<Place> {

		public PlacesAdapter(Context context, List<Place> objects) {
			super(context, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// Get the right view, using a ViewHolder
			ViewHolder holder;
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_place, null);
				holder = new ViewHolder();
				holder.placeName = (TextView) convertView.findViewById(R.id.placeName);
				holder.placeType = (TextView) convertView.findViewById(R.id.placeType);
				holder.distance = (TextView) convertView.findViewById(R.id.distance);
				holder.city = (TextView) convertView.findViewById(R.id.city);
				holder.score = (TextView) convertView.findViewById(R.id.score);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			// Bind the data
			Place item = getItem(position);
			holder.placeName.setText(item.placeName);
			holder.placeType.setText(getPlaceTypeName(getActivity(), item.placeType));
			holder.distance.setText(LocationUtils.getPlaceDistance(getSettings(), getResources(), item, lastLocation));
			holder.city.setText(item.city);
			holder.score.setText(item.avgRating == -1? "?": Integer.toString(item.avgRating));

			return convertView;
		}

	}

	public static String getPlaceTypeName(Context context, int placeType) {
		switch (placeType) {
		case 1:
			return context.getString(R.string.places_brewpub);
		case 2:
			return context.getString(R.string.places_bar);
		case 3:
			return context.getString(R.string.places_beerstore);
		case 4:
			return context.getString(R.string.places_restaurant);
		case 5:
			return context.getString(R.string.places_brewer);
		default:
			return context.getString(R.string.places_unknown);
		}
	}
	
	protected static class ViewHolder {
		TextView placeName, placeType, distance, city, score;
	}

	private class PlacesPagerAdapter extends PagerAdapter {

		private ListView pagerListView;
		private FrameLayout pagerMapView;

		public PlacesPagerAdapter() {
			LayoutInflater inflater = getActivity().getLayoutInflater();
			pagerListView = (ListView) inflater.inflate(R.layout.fragment_pagerlist, null);
			pagerMapView = (FrameLayout) inflater.inflate(R.layout.fragment_placesmap, null);
			
			placesView = pagerListView;
			placesView.setOnItemClickListener(onItemSelected);
			
			setMapView((MapView) pagerMapView.findViewById(R.id.map_nearby));
		}
		
		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return getActivity().getString(R.string.places_nearby).toUpperCase(Locale.getDefault());
			case 1:
				return getActivity().getString(R.string.places_map).toUpperCase(Locale.getDefault());
			}
			return null;
		}

		@Override
		public Object instantiateItem(View container, int position) {
			switch (position) {
			case 0:
				((ViewPager) container).addView(pagerListView, 0);
				return pagerListView;
			case 1:
				((ViewPager) container).addView(pagerMapView, 0);
				return pagerMapView;
			}
			return null;
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			((ViewPager) container).removeView((View) object);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == (View) object;
		}

	}

}
