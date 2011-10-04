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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.maps.MapView;
import com.ratebeer.android.R;
import com.ratebeer.android.api.HttpHelper;
import com.ratebeer.android.api.command.GetPlacesCommand.Place;
import com.ratebeer.android.gui.components.MapContainer;
import com.ratebeer.android.gui.components.RateBeerFragment;

public class PlaceViewFragment extends RateBeerFragment implements MapContainer {

	private static final String STATE_PLACE = "place";

	private static final int MENU_SHARE = 0;

	private TextView nameText, typeText, ratingText;
	private Button addressText, phoneText;
	private FrameLayout mapFrame;

	private Place place;

	public PlaceViewFragment() {
		this(null);
	}

	/**
	 * Show the details of some place
	 * @param place The place object to show the details of
	 */
	public PlaceViewFragment(Place place) {
		this.place = place;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_placeview, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		nameText = (TextView) getView().findViewById(R.id.name);
		typeText = (TextView) getView().findViewById(R.id.type);
		ratingText = (TextView) getView().findViewById(R.id.rating);
		addressText = (Button) getView().findViewById(R.id.address);
		phoneText = (Button) getView().findViewById(R.id.phone);
		mapFrame = (FrameLayout) getView().findViewById(R.id.map);
		addressText.setOnClickListener(onAddressClick);
		phoneText.setOnClickListener(onPhoneClick);
		
		if (savedInstanceState != null) {
			place = savedInstanceState.getParcelable(STATE_PLACE);
		}

		if (place != null) {
			nameText.setText(place.placeName);
			typeText.setText(PlacesFragment.getPlaceTypeName(getActivity(), place.placeType));
			ratingText.setText(place.avgRating == -1? "?": Integer.toString(place.avgRating));
			addressText.setText(place.address + "\n" + place.city + "\n" + PlacesFragment.getPlaceDistance(getRateBeerActivity(), place.distance));
			phoneText.setText(place.phoneNumber);
		}
		
		// Get the activity-wide MapView to show on this fragment and center on this place's location
		MapView mapView = getRateBeerActivity().requestMapViewInstance(this);
		mapView.getController().setCenter(getRateBeerActivity().getPoint(place.latitude, place.longitude));
		mapView.getController().setZoom(15);
		mapFrame.addView(mapView);
		
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		MenuItem item2 = menu.add(Menu.NONE, MENU_SHARE, MENU_SHARE, R.string.app_share);
		item2.setIcon(R.drawable.ic_action_share);
		item2.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(android.view.MenuItem item) {
		switch (item.getItemId()) {
		case MENU_SHARE:
			// Start a share intent for this event
			Intent s = new Intent(Intent.ACTION_SEND);
			s.setType("text/plain");
			s.putExtra(Intent.EXTRA_TEXT, getString(R.string.places_share, place.placeName, place.placeID));
			startActivity(Intent.createChooser(s, getString(R.string.places_shareplace)));
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(STATE_PLACE, place);
	}

	private OnClickListener onAddressClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			try {
				intent.setData(Uri.parse("geo:" + place.latitude + "," + place.longitude + 
						"?q=" + URLEncoder.encode(place.placeName, HttpHelper.UTF8)));
			} catch (UnsupportedEncodingException e) {
			}
			startActivity(intent);
		}
	};

	private OnClickListener onPhoneClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(Intent.ACTION_DIAL);
			intent.setData(Uri.parse("tel:" + place.phoneNumber.replace("-", "").replace("+", "").replace(" ", "").trim()));
			startActivity(intent);
		}
	};

	@Override
	public void removeMapViewinstance() {
		// Removes the map view from it's container so other fragments can use it
		if (mapFrame.getChildAt(0) != null)
			mapFrame.removeViewAt(0);
	}

}
