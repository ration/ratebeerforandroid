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
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.ratebeer.android.R;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.api.HttpHelper;
import com.ratebeer.android.api.command.CheckInCommand;
import com.ratebeer.android.api.command.GetAvailableBeersCommand;
import com.ratebeer.android.api.command.GetAvailableBeersCommand.AvailableBeer;
import com.ratebeer.android.api.command.GetCheckinsCommand;
import com.ratebeer.android.api.command.GetCheckinsCommand.CheckedInUser;
import com.ratebeer.android.api.command.GetPlaceDetailsCommand;
import com.ratebeer.android.api.command.GetPlacesAroundCommand.Place;
import com.ratebeer.android.app.location.SimpleItemizedOverlay;
import com.ratebeer.android.app.location.SimpleItemizedOverlay.OnBalloonClickListener;
import com.ratebeer.android.gui.components.ActivityUtil;
import com.ratebeer.android.gui.components.ArrayAdapter;
import com.ratebeer.android.gui.components.RateBeerActivity;
import com.ratebeer.android.gui.components.RateBeerFragment;
import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitleProvider;

public class PlaceViewFragment extends RateBeerFragment implements OnBalloonClickListener {

	private static final String STATE_PLACEID = "placeId";
	private static final String STATE_PLACE = "place";
	private static final String STATE_CHECKINS= "checkins";
	private static final String STATE_AVAILABLEBEERS = "availableBeers";

	private static final int MENU_SHARE = 0;

	private LayoutInflater inflater;
	private ViewPager pager;
	private TextView nameText, typeText, ratingText;
	private Button addressText, phoneText, checkinhereButton;
	private FrameLayout mapFrame;
	private ListView checkinsView;
	private ListView availableBeersView;
	private TextView availableBeersEmpty;

	private int placeId;
	private Place place;
	private ArrayList<CheckedInUser> checkins = new ArrayList<CheckedInUser>();
	private ArrayList<AvailableBeer> availableBeers = new ArrayList<AvailableBeer>();

	public PlaceViewFragment() {
		this(null);
	}

	/**
	 * Show the details of some place
	 * @param place The place object to show the details of
	 */
	public PlaceViewFragment(Place place) {
		this.place = place;
		if (place != null) {
			this.placeId = place.placeID;
		}
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

		pager = (ViewPager) getView().findViewById(R.id.pager);
		PlacePagerAdapter placePagerAdapter = new PlacePagerAdapter();
		pager.setAdapter(placePagerAdapter);
		TabPageIndicator titles = (TabPageIndicator) getView().findViewById(R.id.titles);
		titles.setViewPager(pager);
		nameText = (TextView) getView().findViewById(R.id.name);
		ratingText = (TextView) getView().findViewById(R.id.rating);
		
		if (savedInstanceState != null) {
			placeId = savedInstanceState.getInt(STATE_PLACEID);
			if (savedInstanceState.containsKey(STATE_PLACE)) {
				place = savedInstanceState.getParcelable(STATE_PLACE);
			}
			if (savedInstanceState.containsKey(STATE_CHECKINS)) {
				checkins = savedInstanceState.getParcelableArrayList(STATE_CHECKINS);
			}
			if (savedInstanceState.containsKey(STATE_AVAILABLEBEERS)) {
				availableBeers = savedInstanceState.getParcelableArrayList(STATE_AVAILABLEBEERS);
			}
		} else if (place != null) {
			// Use the already known details
			refreshCheckins();
			refreshAvailableBeers();
		} else {
			// Retrieve the details too
			refreshDetails();
			refreshCheckins();
			refreshAvailableBeers();
		}
		// Publish the current details, even when it is not loaded yet (and thus still empty)
		publishDetails(place);
		publishCheckins(checkins);
		publishAvailableBeers(availableBeers);
		
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
			refreshAvailableBeers();
			break;
		case MENU_SHARE:
			if (place != null) {
				// Start a share intent for this event
				Intent s = new Intent(Intent.ACTION_SEND);
			    s.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
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
		outState.putInt(STATE_PLACEID, placeId);
		if (place != null) {
			outState.putParcelable(STATE_PLACE, place);
		}
		if (checkins != null) {
			outState.putParcelableArrayList(STATE_CHECKINS, checkins);
		}
		if (availableBeers != null) {
			outState.putParcelableArrayList(STATE_AVAILABLEBEERS, availableBeers);
		}
	}

	private void refreshDetails() {
		execute(new GetPlaceDetailsCommand(getRateBeerActivity().getApi(), placeId));
	}

	private void refreshCheckins() {
		execute(new GetCheckinsCommand(getRateBeerActivity().getApi(), placeId));
	}

	private void refreshAvailableBeers() {
		execute(new GetAvailableBeersCommand(getRateBeerActivity().getApi(), placeId));
		availableBeersEmpty.setText(R.string.details_noavailability);
	}

	private OnClickListener onAddressClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
		    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
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
		    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			intent.setData(Uri.parse("tel:" + place.phoneNumber.replace("-", "").replace("+", "").replace(" ", "").trim()));
			startActivity(intent);
		}
	};

	private void onCheckinClick(int userId, String username) {
		getRateBeerActivity().load(new UserViewFragment(username, userId));
	}

	private void onAvailableBeerClick(int beerId, String beerName) {
		getRateBeerActivity().load(new BeerViewFragment(beerName, beerId));
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
		} else if (result.getCommand().getMethod() == ApiMethod.GetAvailableBeers) {
			publishAvailableBeers(((GetAvailableBeersCommand) result.getCommand()).getAvailableBeers());
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
		if (details == null) {
			return;
		}
		// Show details
		setDetails(details);
	}

	private void publishCheckins(ArrayList<CheckedInUser> checkins) {
		this.checkins = checkins;
		if (checkinsView.getAdapter() == null) {
			checkinsView.setAdapter(new CheckinsAdapter(getActivity(), checkins));
		} else {
			((CheckinsAdapter) checkinsView.getAdapter()).replace(checkins);
		}
	}

	private void publishAvailableBeers(ArrayList<AvailableBeer> availableBeers) {
		this.availableBeers = availableBeers;
		if (availableBeersView.getAdapter() == null) {
			availableBeersView.setAdapter(new AvailableBeersAdapter(getActivity(), availableBeers));
		} else {
			((AvailableBeersAdapter) availableBeersView.getAdapter()).replace(availableBeers);
		}
	}

	private void setDetails(Place details) {
		nameText.setText(place.placeName);
		typeText.setText(PlacesFragment.getPlaceTypeName(getActivity(), place.placeType));
		ratingText.setText(place.avgRating == -1? "?": Integer.toString(place.avgRating));
		String distanceText = place.distance == -1D? "": "\n" + PlacesFragment.getPlaceDistance(getRateBeerActivity(), place.distance);
		addressText.setText(place.address + "\n" + place.city + distanceText);
		phoneText.setText(place.phoneNumber);

		// Get the activity-wide MapView to show on this fragment and center on this place's location
		MapView mapView = getRateBeerActivity().requestMapViewInstance();
		mapView.getController().setCenter(getRateBeerActivity().getPoint(place.latitude, place.longitude));
		mapFrame.addView(mapView);
		final SimpleItemizedOverlay to = PlacesFragment.getPlaceTypeMarker(mapView, place.placeType, this);
		to.addOverlay(new OverlayItem(getRateBeerActivity().getPoint(place.latitude, place.longitude), place.placeName,
				place.avgRating > 0 ? getString(R.string.places_rateandcount, Integer.toString(place.avgRating))
						: getString(R.string.places_notyetrated)));
		mapView.getOverlays().add(to);
		
		// Make fields visible too
		ratingText.setVisibility(View.VISIBLE);
		addressText.setVisibility(View.VISIBLE);
		phoneText.setVisibility(View.VISIBLE);
		checkinhereButton.setVisibility(View.VISIBLE);
		
	}

	@Override
	public void onBalloonClicked(OverlayItem item) {
		// No action, for now
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
			CheckinViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.list_item_checkedinuser, null);
				ActivityUtil.makeListItemClickable(convertView, onRowClick);
				holder = new CheckinViewHolder();
				holder.name = (TextView) convertView.findViewById(R.id.user);
				convertView.setTag(holder);
			} else {
				holder = (CheckinViewHolder) convertView.getTag();
			}

			// Bind the data
			CheckedInUser item = getItem(position);
			holder.name.setTag(item.userID);
			holder.name.setText(item.userName);
			
			return convertView;
		}

	}

	protected static class CheckinViewHolder {
		TextView name;
	}

	private class AvailableBeersAdapter extends ArrayAdapter<AvailableBeer> {

		private final DateFormat dateFormat;

		private OnClickListener onRowClick = new OnClickListener() {
			@Override
			public void onClick(View v) {
				onAvailableBeerClick((Integer)v.findViewById(R.id.beer).getTag(), ((TextView)v.findViewById(R.id.beer)).getText().toString());
			}
		};

		public AvailableBeersAdapter(Context context, List<AvailableBeer> objects) {
			super(context, objects);
			this.dateFormat = android.text.format.DateFormat.getDateFormat(getActivity());
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// Get the right view, using a ViewHolder
			AvailableBeerViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.list_item_availablebeer, null);
				ActivityUtil.makeListItemClickable(convertView, onRowClick);
				holder = new AvailableBeerViewHolder();
				holder.score = (TextView) convertView.findViewById(R.id.score);
				holder.beer = (TextView) convertView.findViewById(R.id.beer);
				holder.timerecorded = (TextView) convertView.findViewById(R.id.timerecorded);
				convertView.setTag(holder);
			} else {
				holder = (AvailableBeerViewHolder) convertView.getTag();
			}

			// Bind the data
			AvailableBeer item = getItem(position);
			if (getActivity() != null) {
				holder.beer.setTag(item.beerId);
				holder.beer.setText(item.beerName);
				holder.score.setText(item.averageRating == -1? "?": Integer.toString(item.averageRating));
				if (item.timeEntered != null) {
					holder.timerecorded.setText(getString(R.string.places_enteredat, dateFormat.format(item.timeEntered)));
				} else {
					holder.timerecorded.setText("");
				}
			}
			
			return convertView;
		}

	}

	protected static class AvailableBeerViewHolder {
		TextView score, beer, timerecorded;
	}

	private class PlacePagerAdapter extends PagerAdapter implements TitleProvider {

		private View pagerDetailsView;
		private View pagerCheckinsView;
		private FrameLayout pagerAvailableBeersFrame;

		public PlacePagerAdapter() {
			LayoutInflater inflater = getActivity().getLayoutInflater();
			pagerDetailsView = (LinearLayout) inflater.inflate(R.layout.fragment_placedetails, null);
			pagerCheckinsView = (LinearLayout) inflater.inflate(R.layout.fragment_placecheckins, null);
			pagerAvailableBeersFrame = (FrameLayout) inflater.inflate(R.layout.fragment_searchlist, null);

			availableBeersEmpty = (TextView) pagerAvailableBeersFrame.findViewById(R.id.empty);
			availableBeersView = (ListView) pagerAvailableBeersFrame.findViewById(R.id.list);
			availableBeersView.setEmptyView(availableBeersEmpty);

			checkinsView = (ListView) pagerCheckinsView.findViewById(R.id.list);
			checkinhereButton = (Button) pagerCheckinsView.findViewById(R.id.checkinhere);
			checkinhereButton.setOnClickListener(onCheckInClick);

			typeText = (TextView) pagerDetailsView.findViewById(R.id.type);
			addressText = (Button) pagerDetailsView.findViewById(R.id.address);
			phoneText = (Button) pagerDetailsView.findViewById(R.id.phone);
			mapFrame = (FrameLayout) pagerDetailsView.findViewById(R.id.map);
			addressText.setOnClickListener(onAddressClick);
			phoneText.setOnClickListener(onPhoneClick);

		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public String getTitle(int position) {
			switch (position) {
			case 0:
				return getActivity().getString(R.string.app_details).toUpperCase();
			case 1:
				return getActivity().getString(R.string.places_checkins).toUpperCase();
			case 2:
				return getActivity().getString(R.string.places_availablebeers).toUpperCase();
			}
			return null;
		}

		@Override
		public Object instantiateItem(View container, int position) {
			switch (position) {
			case 0:
				((ViewPager) container).addView(pagerDetailsView, 0);
				return pagerDetailsView;
			case 1:
				((ViewPager) container).addView(pagerCheckinsView, 0);
				return pagerCheckinsView;
			case 2:
				((ViewPager) container).addView(pagerAvailableBeersFrame, 0);
				return pagerAvailableBeersFrame;
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

		@Override
		public void finishUpdate(View container) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View container) {
		}

		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {
		}

	}

}
