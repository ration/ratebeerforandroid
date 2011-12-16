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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.ratebeer.android.R;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.CommandFailureResult;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.api.command.GetPlacesCommand;
import com.ratebeer.android.api.command.GetPlacesCommand.Place;
import com.ratebeer.android.app.location.MyLocation;
import com.ratebeer.android.app.location.MyLocation.LocationResult;
import com.ratebeer.android.gui.components.ArrayAdapter;
import com.ratebeer.android.gui.components.RateBeerActivity;
import com.ratebeer.android.gui.components.RateBeerFragment;

public class PlacesFragment extends RateBeerFragment {

	private static final String DECIMAL_FORMATTER = "%.1f";
	private static final String STATE_PLACES = "places";
	private static final int DEFAULT_RADIUS = 25;
	
	private LayoutInflater inflater;
	private TextView emptyText;
	private ListView placesView;

	private ArrayList<Place> places = null;
	
	public PlacesFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		this.inflater = inflater;
		return inflater.inflate(R.layout.fragment_places, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		emptyText = (TextView) getView().findViewById(R.id.empty);
		placesView = (ListView) getView().findViewById(R.id.places);
		placesView.setOnItemClickListener(onItemSelected);

		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(STATE_PLACES)) {
				places = savedInstanceState.getParcelableArrayList(STATE_PLACES);
			}
		}
		
		if (places != null) {
			publishResults(places);
		} else {
			refreshPlaces();
		}		

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		MenuItem item = menu.add(RateBeerActivity.MENU_REFRESH, RateBeerActivity.MENU_REFRESH, RateBeerActivity.MENU_REFRESH, R.string.app_refresh);
		item.setIcon(R.drawable.ic_action_refresh);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case RateBeerActivity.MENU_REFRESH:
			refreshPlaces();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (places != null) {
			outState.putParcelableArrayList(STATE_PLACES, places);
		}
	}

	private void refreshPlaces() {
		// Get the current location (if possible)
		if (new MyLocation().getLocation(getActivity(), onLocationResult)) {
			// Force the progress indicator to start
			getRateBeerActivity().setProgress(true);
		} else {
			publishException(emptyText, getString(R.string.error_locationnotsupported));
		}
	}

	private LocationResult onLocationResult = new LocationResult() {
		@Override
		public void gotLocation(Location location) {
			if (location == null) {
				getView().post(new Runnable() {
					@Override
					public void run() {
						publishException(emptyText, getString(R.string.error_nolocation));
					}
				});
				return;
			}
			// Now get the places at this location, if this fragment is still bound to an activity
			if (getRateBeerActivity() != null) {
				execute(new GetPlacesCommand(getRateBeerActivity().getApi(), DEFAULT_RADIUS, location.getLatitude(), location.getLongitude()));
			}
		}
	};

	private OnItemClickListener onItemSelected = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Place item = ((PlacesAdapter)placesView.getAdapter()).getItem(position);
			getRateBeerActivity().load(new PlaceViewFragment(item));
		}
	};
	
	@Override
	public void onTaskSuccessResult(CommandSuccessResult result) {
		if (result.getCommand().getMethod() == ApiMethod.GetPlacesAround) {
			publishResults(((GetPlacesCommand) result.getCommand()).getPlaces());
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
		placesView.setVisibility(result.size() == 0 ? View.GONE : View.VISIBLE);
		emptyText.setVisibility(result.size() == 0 ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onTaskFailureResult(CommandFailureResult result) {
		publishException(emptyText, result.getException());
	}
	
	@Override
	protected void publishException(TextView textview, String message) {
		getRateBeerActivity().setProgress(false);
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
				convertView = inflater.inflate(R.layout.list_item_place, null);
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
			holder.distance.setText(getPlaceDistance(getRateBeerActivity(), item.distance));
			holder.city.setText(item.city);
			holder.score.setText(item.avgRating == -1? "?": Integer.toString(item.avgRating));

			return convertView;
		}

	}

	public static String getPlaceDistance(RateBeerActivity rbActivity, double distance) {
		// Show in KM instead of miles?
		if (rbActivity.getSettings().showDistanceInKm()) {
			distance *= 1.609344;
		}
		return String.format(DECIMAL_FORMATTER, distance) + 
			(rbActivity.getSettings().showDistanceInKm()? rbActivity.getString(R.string.places_km): rbActivity.
				getString(R.string.places_m));
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

}
