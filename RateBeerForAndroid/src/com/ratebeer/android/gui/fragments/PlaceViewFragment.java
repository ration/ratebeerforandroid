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
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.commonsware.cwac.merge.MergeAdapter;
import com.google.android.maps.MapView;
import com.ratebeer.android.R;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.api.HttpHelper;
import com.ratebeer.android.api.command.CheckInCommand;
import com.ratebeer.android.api.command.GetCheckinsCommand;
import com.ratebeer.android.api.command.GetCheckinsCommand.CheckedInUser;
import com.ratebeer.android.api.command.GetPlaceDetailsCommand;
import com.ratebeer.android.api.command.GetPlacesCommand.Place;
import com.ratebeer.android.gui.components.ActivityUtil;
import com.ratebeer.android.gui.components.ArrayAdapter;
import com.ratebeer.android.gui.components.MapContainer;
import com.ratebeer.android.gui.components.RateBeerActivity;
import com.ratebeer.android.gui.components.RateBeerFragment;

public class PlaceViewFragment extends RateBeerFragment implements MapContainer {

	private static final String STATE_PLACE = "place";
	private static final String STATE_PLACEID = "placeId";

	private static final int MENU_SHARE = 0;

	private LayoutInflater inflater;
	private ListView placeView;

	private TextView nameText, typeText, ratingText, checkinslabel;
	private Button addressText, phoneText, checkinhereButton;
	private FrameLayout mapFrame;
	private CheckinsAdapter checkedinsAdapter;

	private Place place;
	private int placeId;

	public PlaceViewFragment() {
		this(null);
	}

	/**
	 * Show the details of some place
	 * @param place The place object to show the details of
	 */
	public PlaceViewFragment(Place place) {
		this.place = place;
		this.placeId = place.placeID;
	}

	/**
	 * Show the details of some place, of which we only know the place ID
	 * @param place The place ID to retrieve and show the details of
	 */
	public PlaceViewFragment(int placeId) {
		this.placeId = placeId;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		this.inflater = inflater;
		return inflater.inflate(R.layout.fragment_placeview, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		placeView = (ListView) getView().findViewById(R.id.placeview);
		if (placeView != null) {
			placeView.setAdapter(new PlaceViewAdapter());
			placeView.setItemsCanFocus(true);
		} else {
			// Tablet
			ListView checkinsView = (ListView) getView().findViewById(R.id.checkins);
			checkedinsAdapter = new CheckinsAdapter(getActivity(), new ArrayList<CheckedInUser>());
			checkinsView.setAdapter(checkedinsAdapter);
			initFields(getView());
		}
		
		if (savedInstanceState != null) {
			place = savedInstanceState.getParcelable(STATE_PLACE);
			placeId = savedInstanceState.getInt(STATE_PLACEID);
		}

		if (place != null) {
			setDetails(place);
			refreshCheckins();
		} else {
			// Retrieve the details
			refreshDetails();
			refreshCheckins();
		}
		
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		MenuItem item = menu.add(RateBeerActivity.MENU_REFRESH, RateBeerActivity.MENU_REFRESH, RateBeerActivity.MENU_REFRESH, R.string.app_refresh);
		item.setIcon(R.drawable.ic_action_refresh);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		MenuItem item2 = menu.add(Menu.NONE, MENU_SHARE, MENU_SHARE, R.string.app_share);
		item2.setIcon(R.drawable.ic_action_share);
		item2.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case RateBeerActivity.MENU_REFRESH:
			refreshDetails();
			refreshCheckins();
			break;
		case MENU_SHARE:
			if (place != null) {
				// Start a share intent for this event
				Intent s = new Intent(Intent.ACTION_SEND);
				s.setType("text/plain");
				s.putExtra(Intent.EXTRA_TEXT, getString(R.string.places_share, place.placeName, place.placeID));
				startActivity(Intent.createChooser(s, getString(R.string.places_shareplace)));
			}
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(STATE_PLACE, place);
		outState.putInt(STATE_PLACEID, placeId);
	}

	private void refreshDetails() {
		execute(new GetPlaceDetailsCommand(getRateBeerActivity().getApi(), placeId));
	}

	private void refreshCheckins() {
		execute(new GetCheckinsCommand(getRateBeerActivity().getApi(), placeId));
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

	private void onCheckinClick(int userId, String username) {
		getRateBeerActivity().load(new UserViewFragment(username, userId));
	}
	
	@Override
	public void onTaskSuccessResult(CommandSuccessResult result) {
		if (result.getCommand().getMethod() == ApiMethod.GetPlaceDetails) {
			publishDetails(((GetPlaceDetailsCommand) result.getCommand()).getDetails());
		} else if (result.getCommand().getMethod() == ApiMethod.GetCheckins) {
			publishCheckins(((GetCheckinsCommand) result.getCommand()).getCheckins());
		} else if (result.getCommand().getMethod() == ApiMethod.CheckIn) {
			Toast.makeText(getActivity(), R.string.places_checkedinnow, Toast.LENGTH_LONG).show();
			refreshCheckins();
		}
	}

	private OnClickListener onCheckInClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// Check in to this place
			execute(new CheckInCommand(getRateBeerActivity().getApi(), placeId));
		}
	};

	private void publishDetails(Place details) {
		this.place = details;
		// Show details
		setDetails(details);
	}

	private void publishCheckins(ArrayList<CheckedInUser> checkins) {
		checkedinsAdapter.replace(checkins);
	}

	private void setDetails(Place details) {
		nameText.setText(place.placeName);
		typeText.setText(PlacesFragment.getPlaceTypeName(getActivity(), place.placeType));
		ratingText.setText(place.avgRating == -1? "?": Integer.toString(place.avgRating));
		String distanceText = place.distance == -1D? "": "\n" + PlacesFragment.getPlaceDistance(getRateBeerActivity(), place.distance);
		addressText.setText(place.address + "\n" + place.city + distanceText);
		phoneText.setText(place.phoneNumber);

		// Get the activity-wide MapView to show on this fragment and center on this place's location
		MapView mapView = getRateBeerActivity().requestMapViewInstance(this);
		mapView.getController().setCenter(getRateBeerActivity().getPoint(place.latitude, place.longitude));
		mapView.getController().setZoom(15);
		mapFrame.addView(mapView);
		
		// Make fields visible too
		ratingText.setVisibility(View.VISIBLE);
		addressText.setVisibility(View.VISIBLE);
		phoneText.setVisibility(View.VISIBLE);
		checkinslabel.setVisibility(View.VISIBLE);
		checkinhereButton.setVisibility(View.VISIBLE);
		
	}

	private class PlaceViewAdapter extends MergeAdapter {

		public PlaceViewAdapter() {

			// Set the place detail fields
			View fields = getActivity().getLayoutInflater().inflate(R.layout.fragment_placedetails, null);
			addView(fields);
			initFields(fields);
			
			// Set the list of checked in users
			checkedinsAdapter = new CheckinsAdapter(getActivity(), new ArrayList<CheckedInUser>());
			addAdapter(checkedinsAdapter);
		}

	}
	
	private class CheckinsAdapter extends ArrayAdapter<CheckedInUser> {

		private OnClickListener onRowClick = new OnClickListener() {
			@Override
			public void onClick(View v) {
				onCheckinClick((Integer)v.findViewById(R.id.user).getTag(), ((TextView)v.findViewById(R.id.user)).getText().toString());
			}
		};

		public CheckinsAdapter(Context context, List<CheckedInUser> objects) {
			super(context, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// Get the right view, using a ViewHolder
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.list_item_checkedinuser, null);
				ActivityUtil.makeListItemClickable(convertView, onRowClick);
				holder = new ViewHolder();
				holder.name = (TextView) convertView.findViewById(R.id.user);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			// Bind the data
			CheckedInUser item = getItem(position);
			holder.name.setTag(item.userID);
			holder.name.setText(item.userName);
			
			return convertView;
		}

	}

	protected static class ViewHolder {
		TextView name;
	}

	public void initFields(View fields) {
		nameText = (TextView) fields.findViewById(R.id.name);
		typeText = (TextView) fields.findViewById(R.id.type);
		ratingText = (TextView) fields.findViewById(R.id.rating);
		addressText = (Button) fields.findViewById(R.id.address);
		phoneText = (Button) fields.findViewById(R.id.phone);
		mapFrame = (FrameLayout) fields.findViewById(R.id.map);
		addressText.setOnClickListener(onAddressClick);
		phoneText.setOnClickListener(onPhoneClick);
		checkinslabel = (TextView) fields.findViewById(R.id.checkinslabel);
		checkinhereButton = (Button) fields.findViewById(R.id.checkinhere);
		checkinhereButton.setOnClickListener(onCheckInClick);
	}

	@Override
	public void removeMapViewinstance() {
		// Removes the map view from it's container so other fragments can use it
		if (mapFrame.getChildAt(0) != null)
			mapFrame.removeViewAt(0);
	}

}
