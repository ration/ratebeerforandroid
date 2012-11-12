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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.commonsware.cwac.merge.MergeAdapter;
import com.ratebeer.android.R;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.CommandFailureResult;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.api.UserSettings;
import com.ratebeer.android.api.command.GetUserDetailsCommand;
import com.ratebeer.android.api.command.GetUserDetailsCommand.RecentBeerRating;
import com.ratebeer.android.api.command.GetUserDetailsCommand.UserDetails;
import com.ratebeer.android.api.command.ImageUrls;
import com.ratebeer.android.gui.components.ActivityUtil;
import com.ratebeer.android.gui.components.ArrayAdapter;
import com.ratebeer.android.gui.components.RateBeerActivity;
import com.ratebeer.android.gui.components.RateBeerFragment;

public class UserViewFragment extends RateBeerFragment {

	private static final String STATE_USERNAME = "userName";
	private static final String STATE_USERID = "userId";
	private static final String STATE_DETAILS = "details";

	private LayoutInflater inflater;
	private ListView userView;

	private ImageView imageView;
	private TextView nameText, locationText, dates, favStyleText, recentratingslabel;
	private Button beersratedButton, cellarButton;
	private RecentBeerRatingsAdapter recentRatingsAdapter;

	protected String userName;
	protected int userId;
	private UserDetails details = null;

	public UserViewFragment() {
		this(null, -1);
	}

	/**
	 * Show a specific user's details, with the user name known in advance
	 * @param userName The user name
	 * @param userId The user ID
	 */
	public UserViewFragment(String userName, int userId) {
		this.userName = userName;
		this.userId = userId;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		this.inflater = inflater;
		return inflater.inflate(R.layout.fragment_userview, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		userView = (ListView) getView().findViewById(R.id.userview);
		if (userView != null) {
			// Phone
			userView.setAdapter(new UserViewAdapter());
			userView.setItemsCanFocus(true);
		} else {
			// Tablet
			ListView recentRatingsView = (ListView) getView().findViewById(R.id.recentratings);
			recentRatingsAdapter = new RecentBeerRatingsAdapter(getActivity(), new ArrayList<RecentBeerRating>());
			recentRatingsView.setAdapter(recentRatingsAdapter);
			imageView = (ImageView) getView().findViewById(R.id.image);
			nameText = (TextView) getView().findViewById(R.id.name);
			locationText = (TextView) getView().findViewById(R.id.location);
			dates = (TextView) getView().findViewById(R.id.dates);
			favStyleText = (TextView) getView().findViewById(R.id.favStyle);
			recentratingslabel = (TextView) getView().findViewById(R.id.recentratingslabel);
			beersratedButton = (Button) getView().findViewById(R.id.beersrated);
			beersratedButton.setOnClickListener(onShowAllRatingsClick);
			cellarButton = (Button) getView().findViewById(R.id.cellar);
			cellarButton.setOnClickListener(onViewCellarClick);
		}
		
		if (savedInstanceState != null) {
			userName = savedInstanceState.getString(STATE_USERNAME);
			userId = savedInstanceState.getInt(STATE_USERID);
			if (savedInstanceState.containsKey(STATE_DETAILS)) {
				UserDetails savedDetails = savedInstanceState.getParcelable(STATE_DETAILS);
				publishDetails(savedDetails);
			}
			refreshImage();
		} else {
			refreshDetails();
			refreshImage();
		}

		// Prevent adding of a sign in/out menu option in this screen
		if (getRateBeerActivity().getUser() != null && getRateBeerActivity().getUser().getUserID() == userId) {
			showSignInMenuItem = false;
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
			refreshDetails();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_USERNAME, userName);
		outState.putInt(STATE_USERID, userId);
		if (details != null) {
			outState.putParcelable(STATE_DETAILS, details);
		}
	}

	private void refreshImage() {
		getRateBeerApplication().getImageCache().displayImage(ImageUrls.getUserPhotoUrl(userName), imageView);
	}

	private void refreshDetails() {
		execute(new GetUserDetailsCommand(getRateBeerActivity().getApi(), userId));
	}

	public OnClickListener onShowAllRatingsClick = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			getRateBeerActivity().load(new UserRatingsFragment(userId));
		}
	};

	public OnClickListener onViewCellarClick = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			getRateBeerActivity().load(new CellarViewFragment(userId));
		}
	};
	
	private void onRecentBeerRatingClick(int beerId) {
		getRateBeerActivity().load(new BeerViewFragment(beerId));
	}

	/**
	 * Overrides the different textual details about this event, as shown as header, as well as the list of attendees
	 * @param details The event details object, which includes the list of attendees
	 */
	private void setDetails(UserDetails details) {
		nameText.setText(details.name);
		locationText.setText(details.location);
		dates.setText(getString(R.string.user_dates, details.joined, details.lastSeen));
		favStyleText.setText(getString(R.string.user_favsyle, details.favStyleId >= 0? details.favStyleName: getString(R.string.user_nofavstyle)));
		beersratedButton.setText(getString(R.string.user_beersrated, Integer.toString(details.beerRateCount), 
				Integer.toString(details.placeRateCount), 
				details.avgScoreGiven == null? "-": details.avgScoreGiven, 
				details.avgBeerRated == null? "-": details.avgBeerRated));
		beersratedButton.setVisibility(View.VISIBLE);
		// Show the cellar button only with a signed in premium user
		UserSettings user = getRateBeerApplication().getSettings().getUserSettings();
		cellarButton.setVisibility(user != null && user.isPremium()? View.VISIBLE: View.GONE);
		recentratingslabel.setVisibility(View.VISIBLE);
		recentRatingsAdapter.replace(details.recentBeerRatings);
	}
	
	@Override
	public void onTaskSuccessResult(CommandSuccessResult result) {
		if (result.getCommand().getMethod() == ApiMethod.GetUserDetails) {
			publishDetails(((GetUserDetailsCommand) result.getCommand()).getDetails());
		}
	}

	private void publishDetails(UserDetails details) {
		this.details = details;
		// Update the user name
		this.userName = details.name;
		// Show details
		setDetails(details);
	}
	
	@Override
	public void onTaskFailureResult(CommandFailureResult result) {
		publishException(null, result.getException());
	}

	private class UserViewAdapter extends MergeAdapter {

		public UserViewAdapter() {

			// Set the user detail fields
			View fields = getActivity().getLayoutInflater().inflate(R.layout.fragment_userdetails, null);
			addView(fields);

			imageView = (ImageView) fields.findViewById(R.id.image);
			nameText = (TextView) fields.findViewById(R.id.name);
			locationText = (TextView) fields.findViewById(R.id.location);
			dates = (TextView) fields.findViewById(R.id.dates);
			favStyleText = (TextView) fields.findViewById(R.id.favStyle);
			recentratingslabel = (TextView) fields.findViewById(R.id.recentratingslabel);
			beersratedButton = (Button) fields.findViewById(R.id.beersrated);
			beersratedButton.setOnClickListener(onShowAllRatingsClick);
			cellarButton = (Button) fields.findViewById(R.id.cellar);
			cellarButton.setOnClickListener(onViewCellarClick);
			
			// Set the list of attendees
			recentRatingsAdapter = new RecentBeerRatingsAdapter(getActivity(), new ArrayList<RecentBeerRating>());
			addAdapter(recentRatingsAdapter);
		}

	}
	
	private class RecentBeerRatingsAdapter extends ArrayAdapter<RecentBeerRating> {

		private OnClickListener onRowClick = new OnClickListener() {
			@Override
			public void onClick(View v) {
				onRecentBeerRatingClick((Integer)v.findViewById(R.id.beer).getTag());
			}
		};
		
		public RecentBeerRatingsAdapter(Context context, List<RecentBeerRating> objects) {
			super(context, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// Get the right view, using a ViewHolder
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.list_item_recentbeerrating, null);
				ActivityUtil.makeListItemClickable(convertView, onRowClick);
				holder = new ViewHolder();
				holder.beer = (TextView) convertView.findViewById(R.id.beer);
				holder.style = (TextView) convertView.findViewById(R.id.style);
				holder.score  = (TextView) convertView.findViewById(R.id.score);
				holder.date = (TextView) convertView.findViewById(R.id.date);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			// Bind the data
			RecentBeerRating item = getItem(position);
			holder.beer.setTag(new Integer(item.id));
			holder.beer.setText(item.name);
			holder.style.setText(item.styleName);
			holder.score.setText(item.rating);
			holder.date.setText(item.date);
			
			return convertView;
		}

	}

	protected static class ViewHolder {
		TextView beer, style, score, date;
	}

}
