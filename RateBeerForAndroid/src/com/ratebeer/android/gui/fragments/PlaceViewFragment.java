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
import android.location.Location;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.InstanceState;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.ViewById;
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
import com.ratebeer.android.app.location.LocationUtils;
import com.ratebeer.android.app.location.SimpleItemizedOverlay;
import com.ratebeer.android.app.location.SimpleItemizedOverlay.OnBalloonClickListener;
import com.ratebeer.android.gui.components.RateBeerActivity;
import com.ratebeer.android.gui.components.RateBeerFragment;
import com.ratebeer.android.gui.components.helpers.ActivityUtil;
import com.ratebeer.android.gui.components.helpers.ArrayAdapter;
import com.viewpagerindicator.TabPageIndicator;

import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;

@EFragment(R.layout.fragment_placeview)
@OptionsMenu({R.menu.refresh, R.menu.share})
public class PlaceViewFragment extends RateBeerFragment implements OnBalloonClickListener {
	
	@FragmentArg
	@InstanceState
	protected int placeId;
	@FragmentArg
	@InstanceState
	protected Place place = null;
	@FragmentArg
	@InstanceState
	protected Location currentLocation = null;
	@InstanceState
	protected ArrayList<CheckedInUser> checkins = null;
	@InstanceState
	protected ArrayList<AvailableBeer> availableBeers = null;
	
	@ViewById
	protected ViewPager pager;
	@ViewById
	protected TabPageIndicator titles;
	@ViewById(R.id.name)
	protected TextView nameText;
	@ViewById(R.id.rating)
	protected TextView ratingText;
	private TextView typeText;
	private Button addressText, phoneText, checkinhereButton;
	private FrameLayout mapFrame;
	private ListView checkinsView;
	private ListView availableBeersView;
	private TextView availableBeersEmpty;

	public PlaceViewFragment() {
	}
	
	@AfterViews
	public void init() {

		pager.setAdapter(new PlacePagerAdapter());
		titles.setViewPager(pager);
		
		if (checkins != null && availableBeers != null && place != null) {
			publishDetails(place);
			publishCheckins(checkins);
			publishAvailableBeers(availableBeers);
		} else if (place != null) {
			// Use the already known details
			publishDetails(place);
			refreshCheckins();
			refreshAvailableBeers();
		} else {
			// Retrieve the details too
			refreshDetails();
			refreshCheckins();
			refreshAvailableBeers();
		}
		
	}

	@OptionsItem(R.id.menu_refresh)
	protected void onRefresh() {
		refreshDetails();
		refreshCheckins();
		refreshAvailableBeers();
	}
	
	@OptionsItem(R.id.menu_share)
	protected void onShare() {
		if (place != null) {
			// Start a share intent for this event
			Intent s = new Intent(Intent.ACTION_SEND);
		    s.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			s.setType("text/plain");
			s.putExtra(Intent.EXTRA_TEXT, getString(R.string.places_share, place.placeName, place.placeID));
			startActivity(Intent.createChooser(s, getString(R.string.places_shareplace)));
		}
	}

	private void refreshDetails() {
		execute(new GetPlaceDetailsCommand(getUser(), placeId));
	}

	private void refreshCheckins() {
		execute(new GetCheckinsCommand(getUser(), placeId));
	}

	private void refreshAvailableBeers() {
		execute(new GetAvailableBeersCommand(getUser(), placeId));
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
				// UTF-8 always exists
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

	private void onCheckinClick(int userId, String userName) {
		load(UserViewFragment_.builder().userId(userId).userName(userName).build());
	}

	private void onAvailableBeerClick(int beerId, String beerName) {
		load(BeerViewFragment_.builder().beerId(beerId).beerName(beerName).build());
	}
	
	@Override
	public void onTaskSuccessResult(CommandSuccessResult result) {
		if (result.getCommand().getMethod() == ApiMethod.GetPlaceDetails) {
			publishDetails(((GetPlaceDetailsCommand) result.getCommand()).getDetails());
		} else if (result.getCommand().getMethod() == ApiMethod.GetCheckins) {
			publishCheckins(((GetCheckinsCommand) result.getCommand()).getCheckins());
		} else if (result.getCommand().getMethod() == ApiMethod.CheckIn) {
			Crouton.makeText(getActivity(), R.string.places_checkedinnow, Style.CONFIRM).show();
			refreshCheckins();
		} else if (result.getCommand().getMethod() == ApiMethod.GetAvailableBeers) {
			publishAvailableBeers(((GetAvailableBeersCommand) result.getCommand()).getAvailableBeers());
		}
	}

	private OnClickListener onCheckInClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// Check in to this place
			execute(new CheckInCommand(getUser(), placeId));
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
		String distanceText = place.distance == -1D ? "" : "\n"
				+ LocationUtils.getPlaceDistance(getSettings(), getResources(), place, currentLocation);
		addressText.setText(place.address + "\n" + place.city + distanceText);
		phoneText.setText(place.phoneNumber);

		// Get the activity-wide MapView to show on this fragment and center on this place's location
		MapView mapView = ((RateBeerActivity)getActivity()).requestMapViewInstance();
		mapView.getController().setCenter(LocationUtils.getPoint(place.latitude, place.longitude));
		mapFrame.addView(mapView);
		final SimpleItemizedOverlay to = PlacesFragment.getPlaceTypeMarker(mapView, place.placeType, this);
		to.addOverlay(new OverlayItem(LocationUtils.getPoint(place.latitude, place.longitude), place.placeName,
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
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_checkedinuser, null);
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

			if (getActivity() != null) {
				return convertView;
			}
			// Get the right view, using a ViewHolder
			AvailableBeerViewHolder holder;
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_availablebeer, null);
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
			holder.beer.setTag(item.beerId);
			holder.beer.setText(item.beerName);
			holder.score.setText(item.averageRating == -1? "?": Integer.toString(item.averageRating));
			if (item.timeEntered != null) {
				holder.timerecorded.setText(getString(R.string.places_enteredat, dateFormat.format(item.timeEntered)));
			} else {
				holder.timerecorded.setText("");
				}
			
			return convertView;
		}

	}

	protected static class AvailableBeerViewHolder {
		TextView score, beer, timerecorded;
	}

	private class PlacePagerAdapter extends PagerAdapter {

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
		public CharSequence getPageTitle(int position) {
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

	}

}
