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

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.InstanceState;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.OrmLiteDao;
import com.googlecode.androidannotations.annotations.ViewById;
import com.j256.ormlite.dao.Dao;
import com.ratebeer.android.R;
import com.ratebeer.android.api.command.PostRatingCommand;
import com.ratebeer.android.app.persistance.DatabaseHelper;
import com.ratebeer.android.app.persistance.OfflineRating;
import com.ratebeer.android.gui.components.PosterService;
import com.ratebeer.android.gui.components.RateBeerFragment;
import com.ratebeer.android.gui.fragments.ConfirmDialogFragment.OnDialogResult;
import com.ratebeer.android.gui.wheel.IntegerWheelAdapter;
import com.ratebeer.android.gui.wheel.IntegerWheelView;
import com.ratebeer.android.gui.wheel.OnSelectionChangedListener;

import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;

@EFragment(R.layout.fragment_rate)
@OptionsMenu(R.menu.rate)
public class RateFragment extends RateBeerFragment implements Runnable {

	protected static final int MIN_CHARACTERS = 85;
	private static final long TIMER_DELAY = 750;
	private static final int NO_BEER_ID = -1;
	private static final int NO_OFFLINE_ID = -1;
	private static final int NO_ORIGINAL_RATING_ID = -1;

	@FragmentArg
	@InstanceState
	protected int beerId = NO_BEER_ID;
	@FragmentArg
	@InstanceState
	protected int originalRatingId = NO_ORIGINAL_RATING_ID;
	@FragmentArg
	@InstanceState
	protected String originalRatingDate = null;
	@FragmentArg
	@InstanceState
	protected String beerName = null;
	@FragmentArg
	@InstanceState
	protected int offlineId = NO_OFFLINE_ID;
	@FragmentArg
	@InstanceState
	protected int aroma;
	@FragmentArg
	@InstanceState
	protected int appearance;
	@FragmentArg
	@InstanceState
	protected int taste;
	@FragmentArg
	@InstanceState
	protected int palate;
	@FragmentArg
	@InstanceState
	protected int overall;
	@FragmentArg
	@InstanceState
	protected String comments = null;

	@ViewById(R.id.appearance)
	protected IntegerWheelView appearanceWheel;
	@ViewById(R.id.aroma)
	protected IntegerWheelView aromaWheel;
	@ViewById(R.id.taste)
	protected IntegerWheelView tasteWheel;
	@ViewById(R.id.palate)
	protected IntegerWheelView palateWheel;
	@ViewById(R.id.overall)
	protected IntegerWheelView overallWheel;
	@ViewById(R.id.name)
	protected TextView nameView;
	@ViewById(R.id.total)
	protected TextView totalText;
	@ViewById
	protected TextView customnamelabel, characterCounter, assistanceWords;
	@ViewById
	protected EditText customname;
	@ViewById(R.id.comments)
	protected EditText commentsEdit;
	@ViewById
	protected Button addrating, offlineStatus, assistance;
	@ViewById
	protected CheckBox share;
	private Thread timer;

	@OrmLiteDao(helper = DatabaseHelper.class, model = OfflineRating.class)
	Dao<OfflineRating, Integer> offlineRatingDao;
	
	public RateFragment() {
	}

	/**
	 * Resume editing of an offline rating
	 * @param offlineId The database row ID of the offline rating
	 */
	public static RateFragment buildFromOfflineID(int offlineId) {
		return RateFragment_.builder().offlineId(offlineId).build();
	}

	/**
	 * Start an empty beer rating
	 * @param beerName The name of the beer to be rated
	 * @param beerId The ID of the beer to be rated
	 */
	public static RateFragment buildFromBeer(String beerName, int beerId) {
		return RateFragment_.builder().beerName(beerName).beerId(beerId).build();
	}

	/**
	 * Start a beer rating with the fields already populated
	 * @param beerName The name of the beer that was rated
	 * @param beerId The ID of the beer that was rated
	 * @param aroma The aroma rating
	 * @param appearance The appearance rating
	 * @param taste The taste rating
	 * @param palate The palate rating
	 * @param overall The overall rating
	 * @param comments The rating comments
	 */
	public static RateFragment buildFromConcrete(String beerName, int beerId, int originalRatingId,
			String originalRatingDate, int appearance, int aroma, int taste, int palate, int overall, String comments) {
		return RateFragment_.builder().beerName(beerName).beerId(beerId).originalRatingId(originalRatingId)
				.originalRatingDate(originalRatingDate).appearance(appearance).aroma(aroma).taste(taste).palate(palate)
				.overall(overall).comments(comments).build();
	}

	/**
	 * Start a beer rating with the fields already populated
	 * @param extras The rating data to populate the fields with, which at least includes the
	 * PosterService.EXTRA_BEERNAME and PosterService.EXTRA_BEERID
	 */
	public static RateFragment buildFromExtras(Bundle extras) {
		// Assume there is an extra containing the BeerMail object
		return RateFragment_.buildFromConcrete(extras.getString(PosterService.EXTRA_BEERNAME),
				extras.getInt(PosterService.EXTRA_BEERID, NO_BEER_ID),
				extras.getInt(PosterService.EXTRA_ORIGRATINGID, NO_ORIGINAL_RATING_ID),
				extras.getString(PosterService.EXTRA_ORIGRATINGDATE), extras.getInt(PosterService.EXTRA_AROMA, -1),
				extras.getInt(PosterService.EXTRA_APPEARANCE, -1), extras.getInt(PosterService.EXTRA_TASTE, -1),
				extras.getInt(PosterService.EXTRA_PALATE, -1), extras.getInt(PosterService.EXTRA_OVERALL, -1),
				extras.getString(PosterService.EXTRA_COMMENT));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_rate, container, false);
	}

	@AfterViews
	public void init() {

		// Initialize wheels
		appearanceWheel.setAdapter(new IntegerWheelAdapter(onRatingChanged, 1, 5, "0"));
		aromaWheel.setAdapter(new IntegerWheelAdapter(onRatingChanged, 1, 10, "0"));
		tasteWheel.setAdapter(new IntegerWheelAdapter(onRatingChanged, 1, 10, "0"));
		palateWheel.setAdapter(new IntegerWheelAdapter(onRatingChanged, 1, 5, "0"));
		overallWheel.setAdapter(new IntegerWheelAdapter(onRatingChanged, 1, 20, "0"));

		// Initialize fields
		assistance.setOnClickListener(onAssistanceClick);
		offlineStatus.setOnClickListener(onOfflineInfoClicked);
		customname.addTextChangedListener(onCommentChanged);
		commentsEdit.addTextChangedListener(onCommentChanged);
		addrating.setOnClickListener(onUploadRating);

		// Allow clicking of words for rating assistance
		assistanceWords.setMovementMethod(LinkMovementMethod.getInstance());
		String t = getString(R.string.rate_assistance_words);
		SpannableString s = new SpannableString(t);
		int start = 0;
		while (start >= 0) {
			int comma = t.indexOf(", ", start);
			if (comma > 0) {
				s.setSpan(linkWord(t.substring(start, comma)), start, comma, 0);
				start = comma + 2;
			} else {
				s.setSpan(linkWord(t.substring(start, t.length())), start, t.length(), 0);
				break;
			}
		}
		assistanceWords.setText(s);
		
		// Set up offline storage of rating
		try {
			offlineStatus.setText(R.string.rate_offline_availble);
			OfflineRating offline;
			if (offlineId == NO_OFFLINE_ID && beerId == NO_BEER_ID) {
				// Nothing pre-known; create a new offline rating
				offline = new OfflineRating();
				offlineRatingDao.create(offline);
			} else if (offlineId != NO_OFFLINE_ID && beerId == NO_BEER_ID) {
				// Continue editing an offline-only rating
				offline = offlineRatingDao.queryForId(offlineId);
			} else if (offlineId == NO_OFFLINE_ID) {
				// Beer ID already known but no offline rating yet; try to create an offline rating
				Map<String, Object> p = new HashMap<String, Object>();
				p.put("beerId", beerId);
				List<OfflineRating> test = offlineRatingDao.queryForFieldValues(p);
				if (test != null && test.size() > 0) {
					// Just pick the first; there shouldn't be multiple offline ratings for the same beer
					offline = test.get(0);
				} else {
					// No offline rating yet: create it
					offline = new OfflineRating(beerId, beerName);
					offlineRatingDao.create(offline);
				}
			} else {
				// Beer ID known and already an offline rating
				offline = offlineRatingDao.queryForId(offlineId);
			}
			if (offline == null) {
				// This offline ID is no longer available; rating probably already uploaded
				Crouton.makeText(getActivity(), R.string.rate_offline_notavailable, Style.ALERT).show();
				getFragmentManager().popBackStack();
				return;
			}

			// Fill fields from the stored offline rating
			offlineId = offline.getOfflineId();
			if (offline.getBeerId() != null)
				beerId = offline.getBeerId();
			if (offline.getBeerName() != null)
				beerName = offline.getBeerName();
			if (offline.getOriginalRatingId() != null)
				originalRatingId = offline.getOriginalRatingId();
			if (offline.getOriginalRatingDate() != null)
				originalRatingDate = offline.getOriginalRatingDate();
			if (offline.getAppearance() != null)
				appearance = offline.getAppearance();
			if (offline.getAroma() != null)
				aroma = offline.getAroma();
			if (offline.getTaste() != null)
				taste = offline.getTaste();
			if (offline.getPalate() != null)
				palate = offline.getPalate();
			if (offline.getOverall() != null)
				overall = offline.getOverall();
			if (offline.getComments() != null)
				comments = offline.getComments();
			
			if (beerId == NO_BEER_ID) {
				// Show the offline-only rating screen
				addrating.setText(R.string.rate_offline_findbeer);
				nameView.setVisibility(View.GONE);
				customname.setText(offline.getBeerName());
			} else {
				// Show the normal rating screen
				customname.setText(beerName);
				addrating.setText(R.string.rate_addrating);
				customname.setVisibility(View.GONE);
				customnamelabel.setVisibility(View.GONE);
			}
			
		} catch (SQLException e) {
			offlineStatus.setText(R.string.rate_offline_notavailable);
		}

		// Check for user; if there is none we cannot upload the rating yet still use the offline feature
		if (getUser() == null) {
			addrating.setVisibility(View.GONE);
		}

		// Populate field values if these are now known
		if (aroma > 0) {
			aromaWheel.getAdapter().setSelectedValue(aroma);
		}
		if (appearance > 0) {
			appearanceWheel.getAdapter().setSelectedValue(appearance);
		}
		if (taste > 0) {
			tasteWheel.getAdapter().setSelectedValue(taste);
		}
		if (palate > 0) {
			palateWheel.getAdapter().setSelectedValue(palate);
		}
		if (overall > 0) {
			overallWheel.getAdapter().setSelectedValue(overall);
		}
		if (comments != null) {
			commentsEdit.setText(comments);
		}
		if (beerName != null) {
			nameView.setText(beerName);
		}
		
	}

	private ClickableSpan linkWord(final String word) {
		return new ClickableSpan() {
			@Override
			public void onClick(View widget) {
				// Add the word to the comments
				String current = commentsEdit.getText().toString();
				if (current.length() > 0 && !current.endsWith(" ")) {
					current += " ";
				}
				String toAdd;
				if (current.length() == 0 || current.endsWith(". ") || current.endsWith("? ") || current.endsWith("! ")) {
					// Start of new sentence; start with a capital letter
					toAdd = word.substring(0, 1).toUpperCase() + word.substring(1, word.length());
				} else {
					// Continue a sentence
					if (!current.endsWith("and ") && !current.endsWith(", ") && current.length() > 1) {
						current = current.substring(0, current.length() - 1) + ", ";
					}
					toAdd = word;
				}
				commentsEdit.setText(current + toAdd);
				commentsEdit.setSelection(commentsEdit.getText().length());
			}
		};
	}

	@OptionsItem(R.id.menu_discard)
	protected void onDiscardRating() {
		new ConfirmDialogFragment(new OnDialogResult() {
			@Override
			public void onConfirmed() { // Delete the offline version of this rating
				if (offlineId != NO_OFFLINE_ID) {
					try {
						offlineRatingDao.deleteIds(Arrays.asList(offlineId));
					} catch (SQLException e) {
						// Ignore this; we probably don't have access to a database at all
					}
				}
				getFragmentManager().popBackStackImmediate();
			}
		}, R.string.rate_offline_confirmdiscard).show(getFragmentManager(), "dialog");
	}

	private OnClickListener onAssistanceClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			assistance.setText(assistanceWords.getVisibility() == View.VISIBLE ? R.string.rate_showassistance
					: R.string.rate_hideassistance);
			assistanceWords.setVisibility(assistanceWords.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
		}
	};
	
	private OnClickListener onOfflineInfoClicked = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			load(TextInfoFragment_.builder().title(getString(R.string.rate_offline_title))
					.info(getString(R.string.rate_offline_info)).build());
		}
	};

	/*
	 * @Override public void onResume() { super.onResume(); getRateBeerActivity().getActionBar().setTitle(beerName);
	 * getRateBeerActivity().getActionBar().withHomeButton(); }
	 */

	private OnSelectionChangedListener<Integer> onRatingChanged = new OnSelectionChangedListener<Integer>() {
		@Override
		public void onSelectionChanged(Integer newSelection) {
			int appearance = appearanceWheel.getAdapter().getSelectedValue();
			int aroma = aromaWheel.getAdapter().getSelectedValue();
			int taste = tasteWheel.getAdapter().getSelectedValue();
			int palate = palateWheel.getAdapter().getSelectedValue();
			int overall = overallWheel.getAdapter().getSelectedValue();
			totalText.setText(Float.toString(PostRatingCommand
					.calculateTotal(aroma, appearance, taste, palate, overall)));
			timer = new Thread(RateFragment.this);
			timer.start();
		}
	};

	private TextWatcher onCommentChanged = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			timer = new Thread(RateFragment.this);
			timer.start();
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			if (timer != null && timer.isAlive()) {
				timer.interrupt();
			}
		}

		@Override
		public void afterTextChanged(Editable s) {
			if (getActivity() == null) 
				return;
			int left = MIN_CHARACTERS - commentsEdit.getText().length();
			if (commentsEdit.getText().length() == 0) {
				characterCounter.setText(R.string.rate_commenttooshort);
			} else if (left >= 0) {
				characterCounter.setText(getString(R.string.rate_charstogo, Integer.toString(left)));
			} else {
				characterCounter.setText(R.string.rate_commentok);
			}
		}
	};

	/**
	 * Implements a small timer to delay the loading of the example RSS feed
	 */
	@Override
	public void run() {
		try {
			Thread.sleep(TIMER_DELAY);
			// If not interrupted...
			if (getActivity() == null)
				return;
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// Store the updated offline rating in the database
					saveOfflineRating();
				}
			});
		} catch (InterruptedException e) {
		}
	}
	
	private void saveOfflineRating() {
		// Gather the rating data
		String customName = customname.getText().toString();
		int appearance = appearanceWheel.getAdapter().getSelectedValue();
		int aroma = aromaWheel.getAdapter().getSelectedValue();
		int taste = tasteWheel.getAdapter().getSelectedValue();
		int palate = palateWheel.getAdapter().getSelectedValue();
		int overall = overallWheel.getAdapter().getSelectedValue();
		String comments = commentsEdit.getText().toString();
		try {
			if (getActivity() == null) {
				offlineStatus.setText(R.string.rate_offline_notavailable);
				return;
			}
			// Get the offline rating from the database
			OfflineRating offline = offlineRatingDao.queryForId(offlineId);
			if (offline == null) {
				offlineStatus.setText(R.string.rate_offline_notavailable);
				return;
			}
			// Update the databse object
			offline.update(beerId, customName, originalRatingId, originalRatingDate, appearance, aroma, taste, palate, overall, comments);
			offlineRatingDao.update(offline);
			offlineStatus.setText(R.string.rate_offline_availble);
		} catch (SQLException e) {
			offlineStatus.setText(R.string.rate_offline_notavailable);
		}
	}

	private OnClickListener onUploadRating = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (beerId == NO_BEER_ID) {
				findRatedBeer();
			} else {
				postRating();
			}
		}
	};

	protected void findRatedBeer() {
		load(FindRatedBeerFragment_.builder().offlineId(offlineId).build());
	}

	protected void postRating() {
		// Try to submit rating to the poster service
		String comment = commentsEdit.getText().toString();
		if (comment != null && comment.length() > MIN_CHARACTERS) {

			try {
				int appearance = appearanceWheel.getAdapter().getSelectedValue();
				int aroma = aromaWheel.getAdapter().getSelectedValue();
				int taste = tasteWheel.getAdapter().getSelectedValue();
				int palate = palateWheel.getAdapter().getSelectedValue();
				int overall = overallWheel.getAdapter().getSelectedValue();

				// Use the poster service to post this new rating
				Intent i = new Intent(PosterService.ACTION_POSTRATING);
				i.putExtra(PosterService.EXTRA_BEERID, beerId);
				i.putExtra(PosterService.EXTRA_OFFLINEID, offlineId);
				i.putExtra(PosterService.EXTRA_ORIGRATINGID, originalRatingId);
				i.putExtra(PosterService.EXTRA_ORIGRATINGDATE, originalRatingDate);
				i.putExtra(PosterService.EXTRA_BEERNAME, beerName);
				i.putExtra(PosterService.EXTRA_AROMA, aroma);
				i.putExtra(PosterService.EXTRA_APPEARANCE, appearance);
				i.putExtra(PosterService.EXTRA_TASTE, taste);
				i.putExtra(PosterService.EXTRA_PALATE, palate);
				i.putExtra(PosterService.EXTRA_OVERALL, overall);
				i.putExtra(PosterService.EXTRA_COMMENT, comment);
				getActivity().startService(i);

				// Share this rating?
				if (share.isChecked()) {
					Intent s = new Intent(Intent.ACTION_SEND);
					s.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
					s.setType("text/plain");
					s.putExtra(Intent.EXTRA_TEXT, String.format(getSettings().getRatingShareText(),
							Integer.toString(beerId), beerName,
							PostRatingCommand.calculateTotal(aroma, appearance, taste, palate, overall),
							Integer.toString(getUser().getUserID())));
					startActivity(Intent.createChooser(s, getString(R.string.app_sharerating)));
				}

				// Close this fragment and open add availability screen
				getFragmentManager().popBackStack();
				load(AddAvailabilityFragment_.builder().beerId(beerId).beerName(beerName).build());

			} catch (NumberFormatException e) {
				publishException(null, getText(R.string.rate_ratingnotcompleted).toString());
			}
		} else {
			publishException(null, getText(R.string.rate_commenttooshort).toString());
		}
	}

}
