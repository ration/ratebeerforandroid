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
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ratebeer.android.R;
import com.ratebeer.android.api.command.PostRatingCommand;
import com.ratebeer.android.app.persistance.OfflineRating;
import com.ratebeer.android.gui.components.PosterService;
import com.ratebeer.android.gui.components.RateBeerFragment;
import com.ratebeer.android.gui.fragments.ConfirmDialogFragment.OnDialogResult;
import com.ratebeer.android.gui.wheel.IntegerWheelAdapter;
import com.ratebeer.android.gui.wheel.OnSelectionChangedListener;
import com.ratebeer.android.gui.wheel.WheelView;

public class RateFragment extends RateBeerFragment {

	protected static final int MIN_CHARACTERS = 85;
	private static final int MENU_DISCARD = 0;
	private static final String STATE_BEERNAME = "beerName";
	private static final String STATE_BEERID = "beerId";
	private static final String STATE_ORIGRATINGID = "originalRatingID";
	private static final String STATE_ORIGRATINGDATE = "originalRatingDate";
	private static final String STATE_OFFLINEID = "offlineId";
	private static final String STATE_APPEARANCE = "appearance";
	private static final String STATE_AROMA = "aroma";
	private static final String STATE_TASTE = "taste";
	private static final String STATE_PALATE = "palate";
	private static final String STATE_OVERALL = "overall";
	private static final int NO_BEER_ID = -1;
	private static final int NO_OFFLINE_ID = -1;
	private static final int NO_ORIGINAL_RATING_ID = -1;

	private WheelView<Integer> appearanceWheel, aromaWheel, tasteWheel, palateWheel, overallWheel;
	private TextView nameText, customnamelabel, totalText, charsText, assistanceWordsView;
	private EditText customnameEdit, commentsEdit;
	private Button addrating, offlineButton, assistanceButton;
	private CheckBox shareBox;

	private int beerId;
	private int originalRatingId;
	private String originalRatingDate;
	private String beerName;
	private int offlineId;
	private int aromaField;
	private int appearanceField;
	private int tasteField;
	private int palateField;
	private int overallField;
	private String commentsField = null;

	public RateFragment() {
		this(null, NO_BEER_ID, NO_ORIGINAL_RATING_ID, null, NO_OFFLINE_ID);
	}

	/**
	 * resume editing of an offline rating
	 * @param offlineId
	 */
	public RateFragment(int offlineId) {
		this(null, NO_BEER_ID, NO_ORIGINAL_RATING_ID, null, offlineId);
	}

	/**
	 * Start an empty beer rating
	 * @param beerName The name of the beer to be rated
	 * @param beerId The ID of the beer to be rated
	 */
	public RateFragment(String beerName, int beerId) {
		this(beerName, beerId, NO_ORIGINAL_RATING_ID, null, NO_OFFLINE_ID);
	}

	private RateFragment(String beerName, int beerId, int originalRatingId, String originalRatingDate, int offlineId) {
		this.beerName = beerName;
		this.beerId = beerId;
		this.originalRatingId = originalRatingId;
		this.originalRatingDate = originalRatingDate;
		this.offlineId = offlineId;
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
	public RateFragment(String beerName, int beerId, int originalRatingId, String originalRatingDate, int appearance,
			int aroma, int taste, int palate, int overall, String comments) {
		this(beerName, beerId, originalRatingId, originalRatingDate, NO_OFFLINE_ID);
		this.aromaField = aroma;
		this.appearanceField = appearance;
		this.tasteField = taste;
		this.palateField = palate;
		this.overallField = overall;
		this.commentsField = comments;
	}

	/**
	 * Start a beer rating with the fields already populated
	 * @param extras The rating data to populate the fields with, which at least includes the
	 *            PosterService.EXTRA_BEERNAME and PosterService.EXTRA_BEERID
	 */
	public RateFragment(Bundle extras) {
		this(extras.getString(PosterService.EXTRA_BEERNAME), extras.getInt(PosterService.EXTRA_BEERID, NO_BEER_ID),
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

	@SuppressWarnings("unchecked")
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// Initialize wheels
		appearanceWheel = (WheelView<Integer>) getView().findViewById(R.id.appearance);
		appearanceWheel.setAdapter(new IntegerWheelAdapter(onRatingChanged, 1, 5, "0"));
		aromaWheel = (WheelView<Integer>) getView().findViewById(R.id.aroma);
		aromaWheel.setAdapter(new IntegerWheelAdapter(onRatingChanged, 1, 10, "0"));
		tasteWheel = (WheelView<Integer>) getView().findViewById(R.id.taste);
		tasteWheel.setAdapter(new IntegerWheelAdapter(onRatingChanged, 1, 10, "0"));
		palateWheel = (WheelView<Integer>) getView().findViewById(R.id.palate);
		palateWheel.setAdapter(new IntegerWheelAdapter(onRatingChanged, 1, 5, "0"));
		overallWheel = (WheelView<Integer>) getView().findViewById(R.id.overall);
		overallWheel.setAdapter(new IntegerWheelAdapter(onRatingChanged, 1, 20, "0"));

		// Initialize fields
		nameText = (TextView) getView().findViewById(R.id.name);
		customnameEdit = (EditText) getView().findViewById(R.id.customname);
		customnamelabel = (TextView) getView().findViewById(R.id.customnamelabel);
		totalText = (TextView) getView().findViewById(R.id.total);
		charsText = (TextView) getView().findViewById(R.id.character_counter);
		commentsEdit = (EditText) getView().findViewById(R.id.comments);
		addrating = (Button) getView().findViewById(R.id.addrating);
		shareBox = (CheckBox) getView().findViewById(R.id.share);
		assistanceButton = (Button) getView().findViewById(R.id.assistance);
		assistanceWordsView = (TextView) getView().findViewById(R.id.assistance_words);
		offlineButton = (Button) getView().findViewById(R.id.offline_status);
		assistanceButton.setOnClickListener(onAssistanceClick);
		offlineButton.setOnClickListener(onOfflineInfoClicked);
		customnameEdit.addTextChangedListener(onCommentChanged);
		commentsEdit.addTextChangedListener(onCommentChanged);
		addrating.setOnClickListener(onUploadRating);

		// Allow clicking of words for rating assistance
		assistanceWordsView.setMovementMethod(LinkMovementMethod.getInstance());
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
		assistanceWordsView.setText(s);
		
		// Load state (i.e. on orientation changes)
		if (savedInstanceState != null) {
			beerId = savedInstanceState.getInt(STATE_BEERID);
			beerName = savedInstanceState.getString(STATE_BEERNAME);
			originalRatingId = savedInstanceState.getInt(STATE_ORIGRATINGID);
			originalRatingDate = savedInstanceState.getString(STATE_ORIGRATINGDATE);
			offlineId = savedInstanceState.getInt(STATE_OFFLINEID);
			appearanceField = savedInstanceState.getInt(STATE_APPEARANCE);
			aromaField = savedInstanceState.getInt(STATE_AROMA);
			tasteField = savedInstanceState.getInt(STATE_TASTE);
			palateField = savedInstanceState.getInt(STATE_PALATE);
			overallField = savedInstanceState.getInt(STATE_OVERALL);
		}

		// Set up offline storage of rating
		try {
			offlineButton.setText(R.string.rate_offline_availble);
			OfflineRating offline;			
			if (offlineId == NO_OFFLINE_ID && beerId == NO_BEER_ID) {
				// Nothing pre-known; create a new offline rating
				offline = new OfflineRating();
				getRateBeerActivity().getHelper().getOfflineRatingDao().create(offline);
			} else if (offlineId != NO_OFFLINE_ID && beerId == NO_BEER_ID) {
				// Continue editing an offline-only rating
				offline = getRateBeerActivity().getHelper().getOfflineRatingDao().queryForId(offlineId);
			} else if (offlineId == NO_OFFLINE_ID) {
				// Beer ID already known but no offline rating yet; try to create an offline rating
				Map<String, Object> p = new HashMap<String, Object>();
				p.put("beerId", beerId);
				List<OfflineRating> test = getRateBeerActivity().getHelper().getOfflineRatingDao().queryForFieldValues(p);
				if (test != null && test.size() > 0) {
					// Just pick the first; there shouldn't be multiple offline ratings for the same beer
					offline = test.get(0);
				} else {
					// No offline rating yet: create it
					offline = new OfflineRating(beerId, beerName);
					getRateBeerActivity().getHelper().getOfflineRatingDao().create(offline);
				}
			} else {
				// Beer ID known and already an offline rating
				offline = getRateBeerActivity().getHelper().getOfflineRatingDao().queryForId(offlineId);
			}
			if (offline == null) {
				// This offline ID is no longer available; rating probably already uploaded
				Toast.makeText(getActivity(), R.string.rate_offline_notavailable, Toast.LENGTH_LONG).show();
				getSupportFragmentManager().popBackStack();
				return;
			}

			// Fill fields from the stored offline rating
			offlineId = offline.getOfflineId();
			if (savedInstanceState == null) {
				if (offline.getBeerId() != null)
					beerId = offline.getBeerId();
				if (offline.getBeerName() != null)
					beerName = offline.getBeerName();
				if (offline.getOriginalRatingId() != null)
					originalRatingId = offline.getOriginalRatingId();
				if (offline.getOriginalRatingDate() != null)
					originalRatingDate = offline.getOriginalRatingDate();
				if (offline.getAppearance() != null)
					appearanceField = offline.getAppearance();
				if (offline.getAroma() != null)
					aromaField = offline.getAroma();
				if (offline.getTaste() != null)
					tasteField = offline.getTaste();
				if (offline.getPalate() != null)
					palateField = offline.getPalate();
				if (offline.getOverall() != null)
					overallField = offline.getOverall();
				if (offline.getComments() != null)
					commentsField = offline.getComments();
			}
			
			if (beerId == NO_BEER_ID) {
				// Show the offline-only rating screen
				addrating.setText(R.string.rate_offline_findbeer);
				nameText.setVisibility(View.GONE);
				customnameEdit.setText(offline.getBeerName());
			} else {
				// Show the normal rating screen
				customnameEdit.setText(beerName);
				addrating.setText(R.string.rate_addrating);
				customnameEdit.setVisibility(View.GONE);
				customnamelabel.setVisibility(View.GONE);
			}
			
		} catch (SQLException e) {
			offlineButton.setText(R.string.rate_offline_notavailable);
		}

		// Check for user; if there is none we cannot upload the rating yet still use the offline feature
		if (getRateBeerActivity().getUser() == null) {
			addrating.setVisibility(View.GONE);
		}

		// Populate field values if these are now known
		if (aromaField > 0) {
			aromaWheel.getAdapter().setSelectedValue(aromaField);
		}
		if (appearanceField > 0) {
			appearanceWheel.getAdapter().setSelectedValue(appearanceField);
		}
		if (tasteField > 0) {
			tasteWheel.getAdapter().setSelectedValue(tasteField);
		}
		if (palateField > 0) {
			palateWheel.getAdapter().setSelectedValue(palateField);
		}
		if (overallField > 0) {
			overallWheel.getAdapter().setSelectedValue(overallField);
		}
		if (commentsField != null) {
			commentsEdit.setText(commentsField);
		}
		if (beerName != null) {
			nameText.setText(beerName);
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

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		MenuItem item = menu.add(Menu.NONE, MENU_DISCARD, MENU_DISCARD, R.string.rate_offline_discard);
		item.setIcon(R.drawable.ic_action_discard);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_DISCARD:
			new ConfirmDialogFragment(new OnDialogResult() {
				@Override
				public void onConfirmed() { // Delete the offline version of this rating
					if (offlineId != NO_OFFLINE_ID) {
						try {
							getRateBeerActivity().getHelper().getOfflineRatingDao().deleteIds(Arrays.asList(offlineId));
						} catch (SQLException e) {
							// Ignore this; we probably don't have access to a database at all
						}
					}
					getSupportFragmentManager().popBackStackImmediate();
				}
			}, R.string.rate_offline_confirmdiscard).show(getSupportFragmentManager(), "dialog");
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_BEERID, beerId);
		outState.putString(STATE_BEERNAME, beerName);
		outState.putInt(STATE_ORIGRATINGID, originalRatingId);
		outState.putString(STATE_ORIGRATINGDATE, originalRatingDate);
		outState.putInt(STATE_OFFLINEID, offlineId);
		if (appearanceWheel != null && appearanceWheel.getAdapter() != null) {
			outState.putInt(STATE_APPEARANCE, appearanceWheel.getAdapter().getSelectedValue());
		}
		if (aromaWheel != null && aromaWheel.getAdapter() != null) {
			outState.putInt(STATE_AROMA, aromaWheel.getAdapter().getSelectedValue());
		}
		if (tasteWheel != null && tasteWheel.getAdapter() != null) {
			outState.putInt(STATE_TASTE, tasteWheel.getAdapter().getSelectedValue());
		}
		if (palateWheel != null && palateWheel.getAdapter() != null) {
			outState.putInt(STATE_PALATE, palateWheel.getAdapter().getSelectedValue());
		}
		if (overallWheel != null && overallWheel.getAdapter() != null) {
			outState.putInt(STATE_OVERALL, overallWheel.getAdapter().getSelectedValue());
		}
	}

	private OnClickListener onAssistanceClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			assistanceButton.setText(assistanceWordsView.getVisibility() == View.VISIBLE? 
					R.string.rate_showassistance: R.string.rate_hideassistance);
			assistanceWordsView.setVisibility(
					assistanceWordsView.getVisibility() == View.VISIBLE? View.GONE: View.VISIBLE);
		}
	};
	
	private OnClickListener onOfflineInfoClicked = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			getRateBeerActivity().load(
				new TextInfoFragment(getString(R.string.rate_offline_title), getString(R.string.rate_offline_info)));
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
			storeOfflineRating();
		}
	};

	private TextWatcher onCommentChanged = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			int left = MIN_CHARACTERS - commentsEdit.getText().length();
			if (commentsEdit.getText().length() == 0) {
				charsText.setText(R.string.rate_commenttooshort);
			} else if (left >= 0) {
				charsText.setText(getString(R.string.rate_charstogo, Integer.toString(left)));
			} else {
				charsText.setText(R.string.rate_commentok);
			}
			storeOfflineRating();
		}
	};

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

	protected void storeOfflineRating() {
		String customName = customnameEdit.getText().toString();
		int appearance = appearanceWheel.getAdapter().getSelectedValue();
		int aroma = aromaWheel.getAdapter().getSelectedValue();
		int taste = tasteWheel.getAdapter().getSelectedValue();
		int palate = palateWheel.getAdapter().getSelectedValue();
		int overall = overallWheel.getAdapter().getSelectedValue();
		String comments = commentsEdit.getText().toString();
		try {
			if (getRateBeerActivity() == null) {
				offlineButton.setText(R.string.rate_offline_notavailable);
				return;
			}
			OfflineRating offline = getRateBeerActivity().getHelper().getOfflineRatingDao().queryForId(offlineId);
			if (offline == null) {
				offlineButton.setText(R.string.rate_offline_notavailable);
				return;
			}
			offline.update(beerId, customName, originalRatingId, originalRatingDate, appearance, aroma, taste, palate, overall, comments);
			getRateBeerActivity().getHelper().getOfflineRatingDao().update(offline);
			offlineButton.setText(R.string.rate_offline_availble);
		} catch (SQLException e) {
			offlineButton.setText(R.string.rate_offline_notavailable);
		}
	}

	protected void findRatedBeer() {
		getRateBeerActivity().load(new FindRatedBeerFragment(offlineId));
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
				if (shareBox.isChecked()) {
					Intent s = new Intent(Intent.ACTION_SEND);
					s.setType("text/plain");
					s.putExtra(Intent.EXTRA_TEXT, String.format(getRateBeerActivity().getSettings()
							.getRatingShareText(), Integer.toString(beerId), beerName, PostRatingCommand
							.calculateTotal(aroma, appearance, taste, palate, overall), Integer
							.toString(getRateBeerActivity().getUser().getUserID())));
					startActivity(Intent.createChooser(s, getString(R.string.app_sharerating)));
				}

				// Close this fragment and open add availability screen
				getSupportFragmentManager().popBackStack();
				getRateBeerActivity().load(new AddAvailabilityFragment(beerName, beerId));

			} catch (NumberFormatException e) {
				publishException(null, getText(R.string.rate_ratingnotcompleted).toString());
			}
		} else {
			publishException(null, getText(R.string.rate_commenttooshort).toString());
		}
	}

}
