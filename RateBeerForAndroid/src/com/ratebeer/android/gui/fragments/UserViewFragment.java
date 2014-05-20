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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.commonsware.cwac.merge.MergeAdapter;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.InstanceState;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.ViewById;
import com.ratebeer.android.R;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.CommandFailureResult;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.api.command.GetUserDetailsCommand;
import com.ratebeer.android.api.command.GetUserDetailsCommand.RecentBeerRating;
import com.ratebeer.android.api.command.GetUserDetailsCommand.UserDetails;
import com.ratebeer.android.api.command.ImageUrls;
import com.ratebeer.android.app.RateBeerForAndroid;
import com.ratebeer.android.gui.components.RateBeerFragment;
import com.ratebeer.android.gui.components.helpers.ActivityUtil;
import com.ratebeer.android.gui.components.helpers.ArrayAdapter;

@EFragment(R.layout.fragment_userview)
@OptionsMenu(R.menu.refresh)
public class UserViewFragment extends RateBeerFragment {

	@FragmentArg
	@InstanceState
	protected String userName = null;
	@FragmentArg
	@InstanceState
	protected int userId = -1;
	@InstanceState
	protected UserDetails details = null;

	@ViewById(R.id.userview)
	protected ListView userView;
	@ViewById(R.id.image)
	protected ImageView imageView;
	private TextView nameText, locationText, dates, favStyleText, recentratingslabel;
	private Button beersratedButton, cellarButton;
	private RecentBeerRatingsAdapter recentRatingsAdapter;
	private DateFormat displayDateFormat;


	public UserViewFragment() {
	}

	@AfterViews
	public void init() {

		displayDateFormat = android.text.format.DateFormat.getDateFormat(getActivity());
		userView.setAdapter(new UserViewAdapter());
		userView.setItemsCanFocus(true);
		
		if (details != null) {
			publishDetails(details);
			refreshImage();
		} else {
			refreshDetails();
			refreshImage();
		}

		// Prevent adding of a sign in/out menu option in this screen if the user views its own profile
		if (getUser() != null && getUser().getUserID() == userId) {
			showSignInMenuItem = false;
		}
		
	}

	private void refreshImage() {
		RateBeerForAndroid.getImageCache(getActivity()).displayImage(ImageUrls.getUserPhotoUrl(userName), imageView);
		imageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onShowFullScreenPhoto();
			}
		});
	}

	@OptionsItem(R.id.menu_refresh)
	protected void refreshDetails() {
		execute(new GetUserDetailsCommand(getUser(), userId));
	}

	protected void onShowFullScreenPhoto() {
		// Open the photo in a separate full screen image fragment
		load(FullScreenImageFragment_.builder().photoLowResUrl(ImageUrls.getUserPhotoUrl(userName))
				.photoHighResUrl(ImageUrls.getUserPhotoHighResUrl(userName)).build());
	}

	public OnClickListener onShowAllRatingsClick = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			load(UserRatingsFragment_.builder().userId(userId).build());
		}
	};

	public OnClickListener onViewCellarClick = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			load(CellarViewFragment_.builder().userId(userId).build());
		}
	};
	
	private void onRecentBeerRatingClick(int beerId) {
		load(BeerViewFragment_.builder().beerId(beerId).build());
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
		cellarButton.setVisibility(getUser() != null && getUser().isPremium()? View.VISIBLE: View.GONE);
		recentratingslabel.setVisibility(View.VISIBLE);
		recentRatingsAdapter.replace(details.recentBeerRatings);
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

			if (imageView == null) // This is already set if in tablet mode
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
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_recentbeerrating, null);
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
			holder.beer.setTag(Integer.valueOf(item.id));
			holder.beer.setText(item.name);
			holder.style.setText(item.styleName);
			holder.score.setText(item.rating);
			holder.date.setText(item.date == null? "": displayDateFormat.format(item.date));
			
			return convertView;
		}

	}

	protected static class ViewHolder {
		TextView beer, style, score, date;
	}

}
