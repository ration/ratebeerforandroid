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

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.commonsware.cwac.merge.MergeAdapter;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.InstanceState;
import com.googlecode.androidannotations.annotations.ViewById;
import com.ratebeer.android.R;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.CommandFailureResult;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.api.UserSettings;
import com.ratebeer.android.api.command.GetBeerAvailabilityCommand;
import com.ratebeer.android.api.command.GetBeerDetailsCommand;
import com.ratebeer.android.api.command.GetBeerDetailsCommand.BeerDetails;
import com.ratebeer.android.api.command.GetRatingsCommand;
import com.ratebeer.android.api.command.GetRatingsCommand.BeerRating;
import com.ratebeer.android.api.command.GetUserTicksCommand;
import com.ratebeer.android.api.command.GetUserTicksCommand.UserTick;
import com.ratebeer.android.api.command.ImageUrls;
import com.ratebeer.android.api.command.PostRatingCommand;
import com.ratebeer.android.api.command.SearchPlacesCommand.PlaceSearchResult;
import com.ratebeer.android.api.command.Style;
import com.ratebeer.android.app.RateBeerForAndroid;
import com.ratebeer.android.gui.components.PosterService;
import com.ratebeer.android.gui.components.RateBeerActivity;
import com.ratebeer.android.gui.components.RateBeerFragment;
import com.ratebeer.android.gui.components.helpers.ActivityUtil;
import com.ratebeer.android.gui.components.helpers.ArrayAdapter;
import com.ratebeer.android.gui.fragments.AddToCellarFragment.CellarType;
import com.viewpagerindicator.TabPageIndicator;

@EFragment(R.layout.fragment_beerview)
public class BeerViewFragment extends RateBeerFragment {

	private static final String DECIMAL_FORMATTER = "%.1f";
	private static final int UNKNOWN_RATINGS_COUNT = -1;
	private static final int MENU_SHARE = 0;
	private static final int ACTIVITY_CAMERA = 0;
	private static final int ACTIVITY_PICKPHOTO = 1;

	@FragmentArg
	@InstanceState
	protected int beerId;
	@FragmentArg
	@InstanceState
	protected String beerName = null;
	@FragmentArg
	@InstanceState
	protected int ratingsCount = UNKNOWN_RATINGS_COUNT;
	@InstanceState
	protected BeerDetails details = null;
	@InstanceState
	protected ArrayList<BeerRating> ownRatings = null;
	@InstanceState
	protected ArrayList<UserTick> ownTicks = null;
	@InstanceState
	protected ArrayList<BeerRating> recentRatings = new ArrayList<BeerRating>();
	@InstanceState
	protected ArrayList<PlaceSearchResult> availability = new ArrayList<PlaceSearchResult>();

	@ViewById
	protected ViewPager pager;
	@ViewById
	protected TabPageIndicator titles;
	private View scoreCard;
	private TextView nameText, noscoreyetText, scoreText, stylepctlText, ratingsText, descriptionText;
	private Button brewernameButton, abvstyleButton;
	private Button rateThisButton, drinkingThisButton, addAvailabilityButton, havethisButton, wantthisButton, uploadphotoButton;
	private View ownratingRow, ownratinglabel, ticklabel, otherratingslabel;
	private TextView ownratingTotal, ownratingAroma, ownratingAppearance, ownratingTaste, ownratingPalate, 
		ownratingOverall, ownratingUsername, ownratingComments;
	private RatingBar tickBar;
	private ListView availabilityView;
	private TextView availabilityEmpty;
	private BeerRatingsAdapter recentRatingsAdapter;
	private ImageView imageView;
	private DateFormat displayDateFormat;

	public BeerViewFragment() {
	}

	@AfterViews
	public void init() {

		displayDateFormat = android.text.format.DateFormat.getDateFormat(getActivity());
		pager.setAdapter(new BeerPagerAdapter());
		titles.setViewPager(pager);

		if (details != null) {
			publishDetails(details);
			refreshImage(); // Note: always requested, although the image is cached
			publishOwnRating(ownRatings);
			publishOwnTick(ownTicks);
			setRatings(recentRatings);
			setAvailability(availability);
		} else {
			refreshDetails();
			refreshImage();
			refreshOwnRating();
			refreshOwnTick();
			refreshRatings();
			refreshAvailability();
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
			refreshImage();
			refreshOwnRating();
			refreshOwnTick();
			refreshRatings();
			refreshAvailability();
			break;
		case MENU_SHARE:
			// Start a share intent for this beer
			Intent s = new Intent(Intent.ACTION_SEND);
		    s.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			s.setType("text/plain");
			s.putExtra(Intent.EXTRA_TEXT, getString(R.string.details_share, beerName, beerId));
			startActivity(Intent.createChooser(s, getString(R.string.details_sharebeer)));
		}
		return super.onOptionsItemSelected(item);
	}

	private void refreshImage() {
		RateBeerForAndroid.getImageCache(getActivity()).displayImage(ImageUrls.getBeerPhotoUrl(beerId), imageView);
	}

	private void refreshDetails() {
		execute(new GetBeerDetailsCommand(getUser(), beerId));
	}

	private void refreshOwnRating() {
		if (getUser() != null) {
			execute(new GetRatingsCommand(getUser(), beerId, getUser().getUserID()));
		}
	}

	private void refreshOwnTick() {
		if (getUser() != null) {
			// TODO: Unfortunately we have to retrieve all the user's ticks, since the RB API is limited...
			execute(new GetUserTicksCommand(getUser(), beerId));
		}
	}

	private void refreshRatings() {
		execute(new GetRatingsCommand(getUser(), beerId));
	}

	private void refreshAvailability() {
		execute(new GetBeerAvailabilityCommand(getUser(), beerId));
		availabilityEmpty.setText(R.string.details_noavailability);
	}

	protected void onRateBeerClick() {
		// Get the original rating if we already rated this beer
		if (ownRatings != null && ownRatings.size() > 0) {
			BeerRating ownRating = ownRatings.get(0);
			// Start new rating fragment, with pre-populated fields
			SimpleDateFormat df = new SimpleDateFormat("M/d/yyyy h:mm:ss a");
			load(RateFragment_.buildFromConcrete(beerName, beerId, ownRating.ratingId,
					df.format(ownRating.timeEntered), ownRating.appearance, ownRating.aroma, ownRating.flavor,
					ownRating.mouthfeel, ownRating.overall, ownRating.comments));
		} else {
			// Start new rating fragment
			load(RateFragment_.buildFromBeer(beerName, beerId));
		}
	}
	
	protected void onTickBarUpdated(float rating) {
		int newRating = (int) rating;
		if (rating <= 0.1) {
			// Force everything below 0.1 (since the user might not have the finger all the way at 0) as a tick removal
			newRating = -1;
		}
		if (ownTicks != null && ownTicks.size() > 0 && ownTicks.get(0).liked == rating) {
			// No need to update
			return;
		}
		// Update the user's tick status of this beer
		Intent i = new Intent(PosterService.ACTION_POSTTICK);
		i.putExtra(PosterService.EXTRA_BEERID, beerId);
		i.putExtra(PosterService.EXTRA_BEERNAME, beerName);
		i.putExtra(PosterService.EXTRA_USERID, getUser().getUserID());
		i.putExtra(PosterService.EXTRA_LIKED, newRating);
		i.putExtra(PosterService.EXTRA_MESSENGER, new Messenger(new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// Callback from the poster service; just refresh the tick status
				refreshOwnTick();
			}
		}));
		getActivity().startService(i);
	}

	protected void onDrinkingBeerClick() {
		// Update now drinking status
		Intent i = new Intent(PosterService.ACTION_SETDRINKINGSTATUS);
		i.putExtra(PosterService.EXTRA_NEWSTATUS, beerName);
		i.putExtra(PosterService.EXTRA_BEERID, beerId);
		getActivity().startService(i);
		// Manually set the last update date of the drinking status back, so a visit to the home screen refreshes it
		getSettings().saveUserSettings(new UserSettings(getUser().getUserID(), getUser().getUsername(), 
				getUser().getPassword(), getUser().getDrinkingStatus(), getUser().isPremium(), new Date(1)));
	}

	protected void onAddAvailability() {
		// Open availability adding screen
		load(AddAvailabilityFragment_.builder().beerId(beerId).beerName(beerName).build());
	}

	protected void onWantOrHaveBeerClick(AddToCellarFragment.CellarType cellarType) {
		// Open add to cellar screen
		load(AddToCellarFragment_.builder().beerId(beerId).beerName(beerName).cellarType(cellarType).build());
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
		load(StyleViewFragment_.builder().style(style).build());
	}
	
	protected void onBrewerClick() {
		load(BrewerViewFragment_.builder().brewerId(details.brewerId).build());
	}
	
	private void onReviewClick(Integer userId, String username) {
		// Start the user details screen
		load(UserViewFragment_.builder().userName(username).userId(userId).build());
	}

	protected void onStartPhotoUpload() {
		new ChoosePhotoFragment(this).show(getFragmentManager(), "");
	}
	
	public void onStartPhotoSnapping() {
		// Start an intent to snap a picture
		// http://stackoverflow.com/questions/1910608/android-action-image-capture-intent
		try {
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				
				File file = new File(RateBeerForAndroid.DEFAULT_FILES_DIR + "/photos/" + Integer.toString(beerId) + ".jpg");
				if (!file.exists()) {
					file.getParentFile().mkdirs();
					file.createNewFile();
				}
				
				Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
				startActivityForResult(i, ACTIVITY_CAMERA);
			
			} else {
				publishException(null, getString(R.string.error_nocamera));
			}
		} catch (Exception e) {
			publishException(null, getString(R.string.error_nocamera));
		}
	}
	
	public void onStartPhotoPicking() {
		Intent i = new Intent(Intent.ACTION_GET_CONTENT);
		i.setType("image/*");
		startActivityForResult(i, ACTIVITY_PICKPHOTO);
	}

	// Taken from http://stackoverflow.com/a/4470069/243165
	private String getImagePath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = getActivity().managedQuery(uri, projection, null, null, null);
		if (cursor != null) {
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} else
			return null;
	}

	// NOTE: The @OnActivityResult annotation doesn't work (yet) on Fragments
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		File photo = null;
		switch (requestCode) {
		case ACTIVITY_PICKPHOTO:

			if (data != null && data.getData() != null) {
                String selectedImagePath = getImagePath(data.getData());
                if (selectedImagePath != null) {
                    photo = new File(selectedImagePath);
                } else {
    				photo = new File(data.getData().getPath());
                }
			}
			// Note the fall through
			
		case ACTIVITY_CAMERA:
			
			if (photo == null) {
				photo = new File(RateBeerForAndroid.DEFAULT_FILES_DIR + "/photos/" + Integer.toString(beerId) + ".jpg");
			}
			if (resultCode == Activity.RESULT_OK && photo != null && photo.exists()) {
				// Start an upload task for this photo
				Intent i = new Intent(PosterService.ACTION_UPLOADBEERPHOTO);
				i.putExtra(PosterService.EXTRA_BEERID, beerId);
				i.putExtra(PosterService.EXTRA_BEERNAME, beerName);
				i.putExtra(PosterService.EXTRA_PHOTO, photo);
				getActivity().startService(i);
			}
			break;
		}
	}

	private void onPlaceClick(Integer placeId, String placeName) {
		// Start the user details screen
		load(PlaceViewFragment_.builder().placeId(placeId).build());
	}
	
	@Override
	public void onTaskSuccessResult(CommandSuccessResult result) {
		if (result.getCommand().getMethod() == ApiMethod.GetBeerDetails) {
			publishDetails(((GetBeerDetailsCommand) result.getCommand()).getDetails());
		} else if (result.getCommand().getMethod() == ApiMethod.GetUserTicks) {
			publishOwnTick(((GetUserTicksCommand) result.getCommand()).getUserTicks());
		} else if (result.getCommand().getMethod() == ApiMethod.GetBeerRatings) {
			GetRatingsCommand grc = (GetRatingsCommand) result.getCommand();
			if (grc.getForUserId() == GetRatingsCommand.NO_USER) {
				publishRatings(grc.getRatings());
			} else {
				publishOwnRating(grc.getRatings());
			}
		} else if (result.getCommand().getMethod() == ApiMethod.GetBeerAvailability) {
			publishAvailability(((GetBeerAvailabilityCommand) result.getCommand()).getPlaces());
		}
	}

	private void publishDetails(BeerDetails details) {
		this.details = details;
		if (details == null) {
			return;
		}
		// Update the beer name and title
		this.beerName = details.beerName;
		// Show details
		setDetails(details);
	}
	
	private void publishOwnRating(ArrayList<BeerRating> ownRatings) {
		this.ownRatings = ownRatings;
		// Show the rating
		setOwnRating(this.ownRatings);
	}

	private void publishOwnTick(ArrayList<UserTick> allUserTicks) {
		if (allUserTicks == null) {
			this.ownTicks = null;
			setOwnTick(null);
			return;
		}
		
		// Unfortunately the API can only send all of the user's ticks at once so we have to find the current beer
		this.ownTicks = new ArrayList<GetUserTicksCommand.UserTick>();
		for (UserTick userTick : allUserTicks) {
			if (userTick.beerdId == beerId) {
				// Creat a list with only this beer's user tick in it
				this.ownTicks.add(userTick);
				break;
			}
		}
		// Show the tick
		setOwnTick(this.ownTicks);
	}

	private void publishRatings(ArrayList<BeerRating> ratings) {
		this.recentRatings = ratings;
		setRatings(ratings);
	}

	private void publishAvailability(ArrayList<PlaceSearchResult> places) {
		this.availability = places;
		setAvailability(availability);
	}

	public void setRatings(ArrayList<BeerRating> ratings) {
		// First remove our own rating, if it is in there (otherwise it would show twice)
		if (getUser() != null) {
			BeerRating toRemove = null;
			for (BeerRating rating : ratings) {
				if (rating.userId == getUser().getUserID()) {
					toRemove = rating;
					break;
				}
			}
			if (toRemove != null) {
				ratings.remove(toRemove);
			}
		}
		// Show the new list of ratings
		otherratingslabel.setVisibility(View.VISIBLE);
		recentRatingsAdapter.replace(ratings);
	}

	public void setAvailability(ArrayList<PlaceSearchResult> places) {
		// Show the new list of places that have this beer available
		if (availabilityView.getAdapter() == null) {
			availabilityView.setAdapter(new AvailabilityAdapter(getActivity(), places));
		} else {
			((AvailabilityAdapter) availabilityView.getAdapter()).replace(places);
		}
	}

	/**
	 * Overrides the different textual details about this beer, as shown as header
	 * @param details The beer details object containing the texts to show
	 */
	public void setDetails(BeerDetails details) {
		nameText.setText(details.beerName);
		brewernameButton.setText(getString(R.string.details_bybrewer, details.brewerName));
		brewernameButton.setVisibility(View.VISIBLE);
		boolean noScoreYet = details.overallPerc == GetBeerDetailsCommand.NO_SCORE_YET;
		noscoreyetText.setVisibility(noScoreYet? View.VISIBLE: View.GONE);
		scoreCard.setVisibility(noScoreYet? View.GONE: View.VISIBLE);
		scoreText.setText(Integer.toString((int)details.overallPerc));
		stylepctlText.setText(Integer.toString((int)details.stylePerc));
		ratingsText.setText(ratingsCount == UNKNOWN_RATINGS_COUNT? "?": Integer.toString(ratingsCount));
		abvstyleButton.setText(getString(R.string.details_abvstyle, details.beerStyle, String.format(DECIMAL_FORMATTER, details.alcohol)));
		abvstyleButton.setVisibility(View.VISIBLE);
		descriptionText.setText(Html.fromHtml(details.description == null || details.description.equals("")? 
				getString(R.string.details_nodescription): details.description.replace("\n", "<br />")));
		descriptionText.setMovementMethod(new ScrollingMovementMethod());
		// Only show the buttons bar if we have a signed in user
		drinkingThisButton.setVisibility(getUser() != null? View.VISIBLE: View.GONE);
		addAvailabilityButton.setVisibility(getUser() != null? View.VISIBLE: View.GONE);
		uploadphotoButton.setVisibility(getUser() != null? View.VISIBLE: View.GONE);
		// Only show the cellar buttons bar if we have a signed in premium user
		wantthisButton.setVisibility(getUser() != null && getUser().isPremium()? View.VISIBLE: View.GONE);
		havethisButton.setVisibility(getUser() != null && getUser().isPremium()? View.VISIBLE: View.GONE);
	}

	public void setOwnRating(ArrayList<BeerRating> ownRatings) {
		if (ownRatings == null) {
			// Still loading and we didn't have a retained rating object
			return;
		}
		if (ownRatings.size() > 0) {
			BeerRating ownRating = ownRatings.get(0);
			ownratinglabel.setVisibility(View.VISIBLE);
			ownratingRow.setVisibility(View.VISIBLE);
			ownratingTotal.setText(Float.toString(PostRatingCommand.calculateTotal(ownRating.aroma, ownRating.appearance, 
					ownRating.flavor, ownRating.mouthfeel, ownRating.overall)));
			ownratingAroma.setText(Integer.toString(ownRating.aroma));
			ownratingAppearance.setText(Integer.toString(ownRating.appearance));
			ownratingTaste.setText(Integer.toString(ownRating.flavor));
			ownratingPalate.setText(Integer.toString(ownRating.mouthfeel));
			ownratingOverall.setText(Integer.toString(ownRating.overall));
			ownratingUsername.setText(ownRating.userName + " (" + Integer.toString(ownRating.rateCount) + ")");
			ownratingComments.setText(ownRating.comments + (ownRating.timeUpdated != null ? " ("
					+ displayDateFormat.format(ownRating.timeUpdated) + ")" : ownRating.timeEntered != null ? " ("
					+ displayDateFormat.format(ownRating.timeEntered) + ")" : ""));
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

	public void setOwnTick(ArrayList<UserTick> ownTicks) {
		if (ownTicks == null) {
			// Still loading and we didn't have a retained ticks object
			return;
		}
		if (ownTicks.size() > 0) {
			UserTick ownTick = ownTicks.get(0);
			tickBar.setRating(ownTick.liked);
		}
		ticklabel.setVisibility(View.VISIBLE);
		tickBar.setVisibility(View.VISIBLE);
		tickBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
			@Override
			public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
				if (fromUser)
					onTickBarUpdated(rating);
			}
		});
	}
	
	@Override
	public void onTaskFailureResult(CommandFailureResult result) {
		publishException(null, result.getException());
	}

	private class BeerRatingsAdapter extends ArrayAdapter<BeerRating> {

		private OnClickListener onRowClick = new OnClickListener() {
			@Override
			public void onClick(View v) {
				onReviewClick((Integer)v.findViewById(R.id.total).getTag(), (String)v.findViewById(R.id.username).getTag());
			}
		};

		public BeerRatingsAdapter(Context context, List<BeerRating> objects) {
			super(context, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// Get the right view, using a ViewHolder
			BeerRatingViewHolder holder;
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_rating, null);
				ActivityUtil.makeListItemClickable(convertView, onRowClick);
				holder = new BeerRatingViewHolder();
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
				holder = (BeerRatingViewHolder) convertView.getTag();
			}

			// Bind the data
			BeerRating item = getItem(position);
			holder.total.setTag(Integer.valueOf(item.userId));
			holder.total.setText(item.totalScore);
			holder.aroma.setText(Integer.toString(item.aroma));
			holder.appearance.setText(Integer.toString(item.appearance));
			holder.taste.setText(Integer.toString(item.flavor));
			holder.palate.setText(Integer.toString(item.mouthfeel));
			holder.overall.setText(Integer.toString(item.overall));
			holder.username.setTag(item.userName);
			holder.username.setText(item.userName + " (" + Integer.toString(item.rateCount) + "), " + item.country);
			holder.comments.setText(item.comments + (item.timeUpdated != null ? " ("
					+ displayDateFormat.format(item.timeUpdated) + ")" : item.timeEntered != null ? " ("
					+ displayDateFormat.format(item.timeEntered) + ")" : ""));

			return convertView;
		}

	}

	protected static class BeerRatingViewHolder {
		TextView total, aroma, appearance, taste, palate, overall, username, comments;
	}

	private class AvailabilityAdapter extends ArrayAdapter<PlaceSearchResult> {

		private OnClickListener onRowClick = new OnClickListener() {
			@Override
			public void onClick(View v) {
				onPlaceClick((Integer)v.findViewById(R.id.name).getTag(), ((TextView)v.findViewById(R.id.name)).getText().toString());
			}
		};

		public AvailabilityAdapter(Context context, List<PlaceSearchResult> objects) {
			super(context, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// Get the right view, using a ViewHolder
			AvailabilityViewHolder holder;
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_placesearchresult, null);
				ActivityUtil.makeListItemClickable(convertView, onRowClick);
				holder = new AvailabilityViewHolder();
				holder.name = (TextView) convertView.findViewById(R.id.name);
				holder.city = (TextView) convertView.findViewById(R.id.city);
				convertView.setTag(holder);
			} else {
				holder = (AvailabilityViewHolder) convertView.getTag();
			}

			// Bind the data
			PlaceSearchResult item = getItem(position);
			holder.name.setTag(item.placeId);
			holder.name.setText(item.placeName);
			holder.city.setText(item.city);

			return convertView;
		}

	}

	protected static class AvailabilityViewHolder {
		TextView name, city;
	}

    private class BeerRatingsViewAdapter extends MergeAdapter {

            public BeerRatingsViewAdapter() {

            	// Add the rate button and the views for the user's own rating
                View fields = getActivity().getLayoutInflater().inflate(R.layout.fragment_beerratings, null);
                addView(fields, true);

                rateThisButton = (Button) fields.findViewById(R.id.ratethis);
                otherratingslabel = fields.findViewById(R.id.otherratingslabel);
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
    			ticklabel = fields.findViewById(R.id.ticklabel);
    			tickBar = (RatingBar) fields.findViewById(R.id.tick);

                // Add the recent ratings
                recentRatingsAdapter = new BeerRatingsAdapter(getActivity(), new ArrayList<BeerRating>());
                addAdapter(recentRatingsAdapter);
                
            }

    }
    
	private class BeerPagerAdapter extends PagerAdapter {

		private View pagerDetailsView;
		private ListView pagerRecentRatingsView;
		private View pagerAvailabilityView;

		public BeerPagerAdapter() {
			LayoutInflater inflater = getActivity().getLayoutInflater();
			pagerDetailsView = inflater.inflate(R.layout.fragment_beerdetails, null);
			pagerRecentRatingsView = (ListView) inflater.inflate(R.layout.fragment_pagerlist, null);
			pagerAvailabilityView = inflater.inflate(R.layout.fragment_beeravailability, null);

			// Ratings page
			pagerRecentRatingsView.setAdapter(new BeerRatingsViewAdapter());
			
			// Availability page
			availabilityEmpty = (TextView) pagerAvailabilityView.findViewById(R.id.empty);
			availabilityView = (ListView) pagerAvailabilityView.findViewById(R.id.list);
			availabilityView.setEmptyView(availabilityEmpty);
			addAvailabilityButton = (Button) pagerAvailabilityView.findViewById(R.id.addavailability);
			addAvailabilityButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onAddAvailability();
				}
			});
			
			// Details page
			if (pagerDetailsView.findViewById(R.id.image) != null) {
				// Phone version; beer image, description and style button are on the details page in the pager
				imageView = (ImageView) pagerDetailsView.findViewById(R.id.image);
				abvstyleButton = (Button) pagerDetailsView.findViewById(R.id.abvstyle);
				descriptionText = (TextView) pagerDetailsView.findViewById(R.id.description);
				nameText = (TextView) pagerDetailsView.findViewById(R.id.name);
				brewernameButton = (Button) pagerDetailsView.findViewById(R.id.brewername);
			} else {
				// Tablet version; these fields are not in the pager but directly in the main layout
				imageView = (ImageView) getView().findViewById(R.id.image);
				abvstyleButton = (Button) getView().findViewById(R.id.abvstyle);
				descriptionText = (TextView) getView().findViewById(R.id.description);
				nameText = (TextView) getView().findViewById(R.id.name);
				brewernameButton = (Button) getView().findViewById(R.id.brewername);
			}
			scoreCard = pagerDetailsView.findViewById(R.id.scorecard);
			noscoreyetText = (TextView) pagerDetailsView.findViewById(R.id.noscoreyet);
			scoreText = (TextView) pagerDetailsView.findViewById(R.id.overallpctl);
			stylepctlText = (TextView) pagerDetailsView.findViewById(R.id.stylepctl);
			ratingsText = (TextView) pagerDetailsView.findViewById(R.id.ratings);
			drinkingThisButton = (Button) pagerDetailsView.findViewById(R.id.drinkingthis);
			havethisButton = (Button) pagerDetailsView.findViewById(R.id.havethis);
			wantthisButton = (Button) pagerDetailsView.findViewById(R.id.wantthis);
			uploadphotoButton = (Button) pagerDetailsView.findViewById(R.id.uploadphoto);
			abvstyleButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onStyleClick();
				}
			});
			brewernameButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onBrewerClick();
				}
			});
			drinkingThisButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onDrinkingBeerClick();
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
			uploadphotoButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onStartPhotoUpload();
					// When circumventing the photo taking on debugging: 
					//onActivityResult(ACTIVITY_CAMERA, Activity.RESULT_OK, null);
				}
			});

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
				return getActivity().getString(R.string.details_recentratings).toUpperCase();
			case 2:
				return getActivity().getString(R.string.details_availability).toUpperCase();
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
				((ViewPager) container).addView(pagerRecentRatingsView, 0);
				return pagerRecentRatingsView;
			case 2:
				((ViewPager) container).addView(pagerAvailabilityView, 0);
				return pagerAvailabilityView;
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
