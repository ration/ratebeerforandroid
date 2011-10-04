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
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.commonsware.cwac.merge.MergeAdapter;
import com.ratebeer.android.R;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.CommandFailureResult;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.api.UserSettings;
import com.ratebeer.android.api.command.GetBeerDetailsCommand;
import com.ratebeer.android.api.command.GetBeerImageCommand;
import com.ratebeer.android.api.command.GetRatingsCommand;
import com.ratebeer.android.api.command.GetUserRatingCommand;
import com.ratebeer.android.api.command.PostRatingCommand;
import com.ratebeer.android.api.command.Style;
import com.ratebeer.android.api.command.GetBeerDetailsCommand.BeerDetails;
import com.ratebeer.android.api.command.GetRatingsCommand.BeerRating;
import com.ratebeer.android.api.command.GetUserRatingCommand.OwnBeerRating;
import com.ratebeer.android.app.RateBeerForAndroid;
import com.ratebeer.android.gui.components.ActivityUtil;
import com.ratebeer.android.gui.components.ArrayAdapter;
import com.ratebeer.android.gui.components.PosterService;
import com.ratebeer.android.gui.components.RateBeerActivity;
import com.ratebeer.android.gui.components.RateBeerFragment;
import com.ratebeer.android.gui.fragments.AddToCellarFragment.CellarType;

public class BeerViewFragment extends RateBeerFragment {

	private static final String DECIMAL_FORMATTER = "%.1f";
	private static final String STATE_BEERNAME = "beerName";
	private static final String STATE_BEERID = "beerId";
	private static final String STATE_RATINGSCOUNT = "ratingsCount";
	private static final String STATE_DETAILS = "details";
	private static final String STATE_OWNRATING = "ownRating";
	private static final String STATE_RATINGS = "ratings";
	private static final int UNKNOWN_RATINGS_COUNT = -1;
	private static final int MENU_SHARE = 0;

	private LayoutInflater inflater;
	private ListView beerView;
	//private ListView recentRatingsView;

	private View scoreCard;
	private TextView nameText, brewernameText, noscoreyetText, scoreText, stylepctlText, ratingsText, descriptionText;
	private LinearLayout buttonsbarView, buttonsbar2View;
	private Button abvstyleButton;
	private Button rateThisButton, drinkingThisButton, addAvailabilityButton, havethisButton, wantthisButton;
	private View ownratingRow, ownratinglabel, recentratingslabel;
	private TextView ownratingTotal, ownratingAroma, ownratingAppearance, ownratingTaste, ownratingPalate, 
		ownratingOverall, ownratingUsername, ownratingComments;
	private ImageView imageView;
	private BeerRatingsAdapter ratingsAdapter;

	protected String beerName;
	protected int beerId;
	protected int ratingsCount;
	private BeerDetails details = null;
	private ArrayList<BeerRating> ratings = null;
	private OwnBeerRating ownRating = null;

	public BeerViewFragment() {
		this(null, -1);
	}

	/**
	 * Show a specific beer's details, with the beer name and number of beer ratings known in advance
	 * @param beerName The beer name, or null if not known
	 * @param beerId The beer ID
	 * @param ratings The number of ratings
	 */
	public BeerViewFragment(String beerName, int beerId, int ratings) {
		this.beerName = beerName;
		this.beerId = beerId;
		this.ratingsCount = ratings;
	}

	/**
	 * Show a specific beer's details, with the beer name known in advance
	 * @param beerName The beer name, or null if not known
	 * @param beerId The beer ID
	 */
	public BeerViewFragment(String beerName, int beerId) {
		this(beerName, beerId, UNKNOWN_RATINGS_COUNT);
	}
	
	/**
	 * Show a specific beer's details, without the beer name known in advance
	 * @param beerId The beer ID
	 */
	public BeerViewFragment(int beerId) {
		this(null, beerId);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		this.inflater = inflater;
		return inflater.inflate(R.layout.fragment_beerview, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		beerView = (ListView) getView().findViewById(R.id.beerview);
		if (beerView != null) {
			// Phone
			beerView.setAdapter(new BeerViewAdapter());
			beerView.setItemsCanFocus(true);
		} else {
			// Tablet
			ListView recentRatingsView = (ListView) getView().findViewById(R.id.recentratings);
			ratingsAdapter = new BeerRatingsAdapter(getActivity(), new ArrayList<BeerRating>());
			recentRatingsView.setAdapter(ratingsAdapter);
			initFields(getView());
			// Allow the description field to scroll
			descriptionText.setMovementMethod(ScrollingMovementMethod.getInstance());
		}
		
		if (savedInstanceState != null) {
			beerName = savedInstanceState.getString(STATE_BEERNAME);
			beerId = savedInstanceState.getInt(STATE_BEERID);
			ratingsCount = savedInstanceState.getInt(STATE_RATINGSCOUNT);
			if (savedInstanceState.containsKey(STATE_RATINGS)) {
				ArrayList<BeerRating> savedRatings = savedInstanceState.getParcelableArrayList(STATE_RATINGS);
				publishRatings(savedRatings);
			}
			if (savedInstanceState.containsKey(STATE_DETAILS)) {
				BeerDetails savedDetails = savedInstanceState.getParcelable(STATE_DETAILS);
				publishDetails(savedDetails);
			}
			refreshImage();
			refreshOwnRating();
		} else {
			refreshDetails();
			refreshImage();
			refreshOwnRating();
			refreshRatings();
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
	public boolean onOptionsItemSelected(android.view.MenuItem item) {
		switch (item.getItemId()) {
		case RateBeerActivity.MENU_REFRESH:
			refreshDetails();
			refreshImage();
			refreshOwnRating();
			refreshRatings();
			break;
		case MENU_SHARE:
			// Start a share intent for this beer
			Intent s = new Intent(Intent.ACTION_SEND);
			s.setType("text/plain");
			s.putExtra(Intent.EXTRA_TEXT, getString(R.string.details_share, beerName, beerId));
			startActivity(Intent.createChooser(s, getString(R.string.details_sharebeer)));
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_BEERNAME, beerName);
		outState.putInt(STATE_BEERID, beerId);
		outState.putInt(STATE_RATINGSCOUNT, ratingsCount);
		if (details != null) {
			outState.putParcelable(STATE_DETAILS, details);
		}
		if (ownRating != null) {
			outState.putParcelable(STATE_OWNRATING, ownRating);
		}
		if (ratings != null) {
			outState.putParcelableArrayList(STATE_RATINGS, ratings);
		}
	}

	private void refreshImage() {
		execute(new GetBeerImageCommand(getRateBeerActivity().getApi(), beerId));
	}

	private void refreshDetails() {
		execute(new GetBeerDetailsCommand(getRateBeerActivity().getApi(), beerId));
	}

	private void refreshOwnRating() {
		if (getRateBeerActivity().getUser() != null) {
			execute(new GetUserRatingCommand(getRateBeerActivity().getApi(), beerId));
		}
	}

	private void refreshRatings() {
		execute(new GetRatingsCommand(getRateBeerActivity().getApi(), beerId));
	}

	protected void onRateBeerClick() {
		// Get the original rating if we already rated this beer
		if (ownRating != null) {
			// Start new rating fragment, with pre-populated fields
			getRateBeerActivity().load(new RateFragment(beerName, beerId, ownRating.ratingID, ownRating.origDate, 
					ownRating.appearance, ownRating.aroma, ownRating.taste, ownRating.palate, ownRating.overall, 
					ownRating.comments));
		} else {
			// Start new rating fragment
			getRateBeerActivity().load(new RateFragment(beerName, beerId));
		}
	}

	protected void onDrinkingBeerClick() {
		// Update now drinking status
		Intent i = new Intent(PosterService.ACTION_SETDRINKINGSTATUS);
		i.putExtra(PosterService.EXTRA_NEWSTATUS, beerName);
		i.putExtra(PosterService.EXTRA_BEERID, beerId);
		getActivity().startService(i);
	}

	protected void onAddAvailability() {
		// Open availability adding screen
		getRateBeerActivity().load(new AddAvailabilityFragment(beerName, beerId));
	}

	protected void onWantOrHaveBeerClick(AddToCellarFragment.CellarType cellarType) {
		// Open add to cellar screen
		getRateBeerActivity().load(new AddToCellarFragment(beerName, beerId, cellarType));
	}

	protected void onStyleClick() {
		// Infer style from the name; this is a bit of a hack but the RateBeer API doesn't give the style ID :(
		if (details == null) {
			return;
		}
		Style style = null;
		for (Style s : Style.ALL_STYLES.values()) {
			if (s.getName().equals(details.beerStyle)) {
				style = s;
				break;
			}
		}
		if (style == null) {
			Log.d(RateBeerForAndroid.LOG_NAME, "Wanted to get the beer style of " + beerName + 
					" (#" + beerId + "), which is " + details.beerStyle + " but can't find it in our own list.");
			publishException(null, getString(R.string.error_stylenotfound));
			return;
		}
		// Open style details screen
		getRateBeerActivity().load(new StyleViewFragment(style));
	}
	
	private void onReviewClick(Integer userId, String username) {
		// Start the user details screen
		getRateBeerActivity().load(new UserViewFragment(username, userId));
	}
	
	@Override
	public void onTaskSuccessResult(CommandSuccessResult result) {
		if (result.getCommand().getMethod() == ApiMethod.GetBeerDetails) {
			publishDetails(((GetBeerDetailsCommand) result.getCommand()).getDetails());
		} else if (result.getCommand().getMethod() == ApiMethod.GetBeerImage) {
			setImage(((GetBeerImageCommand) result.getCommand()).getImage());
		} else if (result.getCommand().getMethod() == ApiMethod.GetBeerRatings) {
			publishRatings(((GetRatingsCommand) result.getCommand()).getRatings());
		} else if (result.getCommand().getMethod() == ApiMethod.GetUserRating) {
			publishOwnRating(((GetUserRatingCommand) result.getCommand()).getRating());
		}
	}

	private void publishDetails(BeerDetails details) {
		this.details = details;
		// Update the beer name and title
		this.beerName = details.beerName;
		// Show details
		setDetails(details);
	}
	
	private void publishOwnRating(OwnBeerRating ownRating) {
		this.ownRating = ownRating;
		// Show the rating
		setOwnRating(ownRating);
	}

	private void publishRatings(ArrayList<BeerRating> ratings) {
		this.ratings = ratings;
		setRatings(ratings);
	}

	/**
	 * Overwrites the list of ratings that are shown
	 * @param ratings The list of beer ratings
	 */
	public void setRatings(ArrayList<BeerRating> ratings) {
		ratingsAdapter.replace(ratings);
		recentratingslabel.setVisibility(View.VISIBLE);
	}

	/**
	 * Overrides the beer image shown
	 * @param drawable The bitmap containing image of the beer
	 */
	public void setImage(Drawable drawable) {
		imageView.setImageDrawable(drawable == null? null: drawable);
	}

	/**
	 * Overrides the different textual details about this beer, as shown as header
	 * @param details The beer details object containing the texts to show
	 */
	public void setDetails(BeerDetails details) {
		nameText.setText(details.beerName);
		brewernameText.setText(getString(R.string.details_bybrewer, details.brewerName));
		boolean noScoreYet = details.overallPerc == GetBeerDetailsCommand.NO_SCORE_YET;
		noscoreyetText.setVisibility(noScoreYet? View.VISIBLE: View.GONE);
		scoreCard.setVisibility(noScoreYet? View.GONE: View.VISIBLE);
		scoreText.setText(Integer.toString((int)details.overallPerc));
		stylepctlText.setText(Integer.toString((int)details.stylePerc));
		ratingsText.setText(ratingsCount == UNKNOWN_RATINGS_COUNT? "?": Integer.toString(ratingsCount));
		abvstyleButton.setText(getString(R.string.details_abvstyle, details.beerStyle, String.format(DECIMAL_FORMATTER, details.alcohol)));
		abvstyleButton.setVisibility(View.VISIBLE);
		descriptionText.setText(details.description == null || details.description.equals("")? 
				getString(R.string.details_nodescription): details.description);
		// Only show the buttons bar if we have a signed in user
		UserSettings user = getRateBeerApplication().getSettings().getUserSettings();
		buttonsbarView.setVisibility(user != null? View.VISIBLE: View.GONE);
		addAvailabilityButton.setVisibility(user != null? View.VISIBLE: View.GONE);
		// Only show the cellar buttons bar if we have a signed in premium user
		buttonsbar2View.setVisibility(user != null && user.isPremium()? View.VISIBLE: View.GONE);
	}
	
	public void setOwnRating(OwnBeerRating ownRating) {
		if (ownRating != null) {
			ownratinglabel.setVisibility(View.VISIBLE);
			ownratingRow.setVisibility(View.VISIBLE);
			ownratingTotal.setText(Float.toString(PostRatingCommand.calculateTotal(ownRating.aroma, ownRating.appearance, 
				ownRating.taste, ownRating.palate, ownRating.overall)));
			ownratingAroma.setText(Integer.toString(ownRating.aroma));
			ownratingAppearance.setText(Integer.toString(ownRating.appearance));
			ownratingTaste.setText(Integer.toString(ownRating.taste));
			ownratingPalate.setText(Integer.toString(ownRating.palate));
			ownratingOverall.setText(Integer.toString(ownRating.overall));
			ownratingUsername.setText(getRateBeerActivity().getUser().getUsername());
			ownratingComments.setText(ownRating.comments);
			rateThisButton.setText(R.string.details_viewrerate);
		} else {
			ownratinglabel.setVisibility(View.GONE);
			ownratingRow.setVisibility(View.GONE);
			rateThisButton.setText(R.string.details_ratethis);
		}
		rateThisButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onRateBeerClick();
			}
		});
	}
	
	@Override
	public void onTaskFailureResult(CommandFailureResult result) {
		publishException(null, result.getException());
	}

	private class BeerViewAdapter extends MergeAdapter {

		public BeerViewAdapter() {

			View fields = getActivity().getLayoutInflater().inflate(R.layout.fragment_beerdetails, null);
			addView(fields, true);
			initFields(fields);
			
			// Initialize empty
			ownratinglabel.setVisibility(View.GONE);
			ownratingRow.setVisibility(View.GONE);
			buttonsbarView.setVisibility(View.GONE);
			buttonsbar2View.setVisibility(View.GONE);
			ratingsAdapter = new BeerRatingsAdapter(getActivity(), new ArrayList<BeerRating>());
			addAdapter(ratingsAdapter);
		}

	}
	
	private class BeerRatingsAdapter extends ArrayAdapter<BeerRating> {

		private final DateFormat dateFormat;
		
		private OnClickListener onRowClick = new OnClickListener() {
			@Override
			public void onClick(View v) {
				onReviewClick((Integer)v.findViewById(R.id.total).getTag(), (String)v.findViewById(R.id.username).getTag());
			}
		};

		public BeerRatingsAdapter(Context context, List<BeerRating> objects) {
			super(context, objects);
			this.dateFormat = android.text.format.DateFormat.getDateFormat(getActivity());
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// Get the right view, using a ViewHolder
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.list_item_rating, null);
				ActivityUtil.makeListItemClickable(convertView, onRowClick);
				holder = new ViewHolder();
				holder.total = (TextView) convertView.findViewById(R.id.total);
				holder.aroma = (TextView) convertView.findViewById(R.id.aroma);
				holder.appearance = (TextView) convertView.findViewById(R.id.appearance);
				holder.taste = (TextView) convertView.findViewById(R.id.taste);
				holder.palate = (TextView) convertView.findViewById(R.id.palate);
				holder.overall = (TextView) convertView.findViewById(R.id.overall);
				holder.username = (TextView) convertView.findViewById(R.id.username);
				holder.comments = (TextView) convertView.findViewById(R.id.comments);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			// Bind the data
			BeerRating item = getItem(position);
			holder.total.setTag(new Integer(item.userId));
			holder.total.setText(item.totalScore);
			holder.aroma.setText(item.aroma);
			holder.appearance.setText(item.appearance);
			holder.taste.setText(item.flavor);
			holder.palate.setText(item.mouthfeel);
			holder.overall.setText(item.overall);
			holder.username.setTag(item.userName);
			holder.username.setText(item.userName + " (" + item.rateCount + "), " + item.country);
			holder.comments.setText(item.comments + (item.timeUpdated != null ? " ("
					+ dateFormat.format(item.timeUpdated) + ")" : item.timeEntered != null ? " ("
					+ dateFormat.format(item.timeEntered) + ")" : ""));

			return convertView;
		}

	}

	protected static class ViewHolder {
		TextView total, aroma, appearance, taste, palate, overall, username, comments;
	}

	public void initFields(View fields) {
		imageView = (ImageView) fields.findViewById(R.id.image);
		abvstyleButton = (Button) fields.findViewById(R.id.abvstyle);
		descriptionText = (TextView) fields.findViewById(R.id.description);
		nameText = (TextView) fields.findViewById(R.id.name);
		brewernameText = (TextView) fields.findViewById(R.id.brewername);
		scoreCard = fields.findViewById(R.id.scorecard);
		noscoreyetText = (TextView) fields.findViewById(R.id.noscoreyet);
		scoreText = (TextView) fields.findViewById(R.id.overallpctl);
		stylepctlText = (TextView) fields.findViewById(R.id.stylepctl);
		ratingsText = (TextView) fields.findViewById(R.id.ratings);
		buttonsbarView = (LinearLayout) fields.findViewById(R.id.buttonsbar);
		rateThisButton = (Button) fields.findViewById(R.id.ratethis);
		drinkingThisButton = (Button) fields.findViewById(R.id.drinkingthis);
		addAvailabilityButton = (Button) fields.findViewById(R.id.addavailability);
		buttonsbar2View = (LinearLayout) fields.findViewById(R.id.buttonsbar2);
		havethisButton = (Button) fields.findViewById(R.id.havethis);
		wantthisButton = (Button) fields.findViewById(R.id.wantthis);
		ownratinglabel = fields.findViewById(R.id.ownratinglabel);
		ownratingRow = fields.findViewById(R.id.ownrating);
		ownratingTotal = (TextView) fields.findViewById(R.id.total);
		ownratingAroma = (TextView) fields.findViewById(R.id.aroma);
		ownratingAppearance = (TextView) fields.findViewById(R.id.appearance);
		ownratingTaste = (TextView) fields.findViewById(R.id.taste);
		ownratingPalate = (TextView) fields.findViewById(R.id.palate);
		ownratingOverall = (TextView) fields.findViewById(R.id.overall);
		ownratingUsername = (TextView) fields.findViewById(R.id.username);
		ownratingComments = (TextView) fields.findViewById(R.id.comments);
		recentratingslabel = fields.findViewById(R.id.recentratingslabel);
		abvstyleButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onStyleClick();
			}
		});
		drinkingThisButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onDrinkingBeerClick();
			}
		});
		addAvailabilityButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onAddAvailability();
			}
		});
		havethisButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onWantOrHaveBeerClick(CellarType.Have);
			}
		});
		wantthisButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onWantOrHaveBeerClick(CellarType.Want);
			}
		});
	}

}
