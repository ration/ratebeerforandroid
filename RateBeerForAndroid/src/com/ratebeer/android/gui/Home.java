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
package com.ratebeer.android.gui;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentTransaction;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.InstanceState;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.ratebeer.android.R;
import com.ratebeer.android.gui.components.BeermailService;
import com.ratebeer.android.gui.components.BootReceiver;
import com.ratebeer.android.gui.components.PosterService;
import com.ratebeer.android.gui.components.RateBeerActivity;
import com.ratebeer.android.gui.components.RateBeerFragment;
import com.ratebeer.android.gui.components.helpers.SearchUiHelper;
import com.ratebeer.android.gui.fragments.*;

@EActivity(R.layout.activity_home)
public class Home extends RateBeerActivity {

	@InstanceState
	boolean firstStart = true;
	
	@AfterViews
	public void init() {
		// Start the background service, if necessary
		BootReceiver.startBeerMailAlarm(getApplicationContext());

		// Show search directly in action bar on larger screens
		// For phones the DashboardFragment and SearchFragment will show an icon
		new SearchUiHelper(this).addSearchToActionBar(getSupportActionBar());
		
		if (firstStart)
			handleStartIntent();
	}

	protected void handleStartIntent() {

		// Start a search?
		if (getIntent().hasExtra(SearchManager.QUERY)) {
			load(SearchFragment_.builder().query(getIntent().getStringExtra(SearchManager.QUERY)).build());
			return;
		}

		// See if some concrete action was requested (such as from the background poster service)
		String action = getIntent().getAction();
		action = action == null? "": action;
		Uri data = getIntent().getData();
	
		// Open details for some specific beer
		if (action.equals(Intent.ACTION_VIEW) && data != null) {
			List<String> segments = data.getPathSegments();
			if (segments.size() > 1) {
				try {
					int beerId = Integer.parseInt(segments.get(1));
					load(BeerViewFragment_.builder().beerId(beerId).build());
					return;
				} catch (NumberFormatException e) {
					Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "Invalid ACTION_VIEW Intent data; " + segments.get(1)
							+ " is not a number.");
				}
			}
		}

		// Open the rating screen for a beer
		if (action.equals(PosterService.ACTION_EDITRATING)) {
			load(RateFragment_.buildFromExtras(getIntent().getExtras()));
			return;
		}
		
		// Open the add UPC code screen again; this assumes the UPC code is given in the extras
		if (action.equals(PosterService.ACTION_ADDUPCCODE)) {
			load(AddUpcCodeFragment_.builder().upcCode(getIntent().getStringExtra(PosterService.EXTRA_UPCCODE))
					.build());
			return;
		}
		
		// Open the beermails screen
		if (action.equals(BeermailService.ACTION_VIEWBEERMAILS)) {
			load(MailsFragment_.builder().build());
			return;
		}
		
		// Open the beermail screen to a specific mail
		if (action.equals(BeermailService.ACTION_VIEWBEERMAIL)) {
			load(MailViewFragment_.buildFromExtras(getIntent().getExtras()));
			return;
		}

		// Open the beermail send mail screen to reply to a specific mail
		if (action.equals(BeermailService.ACTION_REPLYBEERMAIL)) {
			load(SendMailFragment.buildReplyFromExtras(getIntent().getExtras()));
			return;
		}

		// Open the beermail send mail screen to recover a failed send
		if (action.equals(PosterService.ACTION_SENDMAIL)) {
			load(SendMailFragment.buildFromFailedSend(getIntent().getExtras()));
			return;
		}

		// Open the beermail reply screen to a specific mail
		if (action.equals(BeermailService.ACTION_REPLYBEERMAIL)) {
			load(SendMailFragment.buildReplyFromExtras(getIntent().getExtras()));
			return;
		}
		
		// Normal startup; show dashboard
		load(DashboardFragment_.builder().build());

		firstStart = false;
		
	}

	@SuppressLint("InlinedApi")
	@OptionsItem(android.R.id.home)
	protected void onUp() {
		// Home button click in the action bar
		Home_.intent(this).flags(Intent.FLAG_ACTIVITY_CLEAR_TOP).start();
	}

	/**
	 * Convenience method that calls load(fragment, true)
	 * @param fragment The fragment to show
	 */
	public void load(RateBeerFragment fragment) {
		load(fragment, true);
	}

	/**
	 * Load a new screen into the content fragment
	 * @param fragment The fragment to show
	 * @param addToBackStack Whether to also add this fragment to the backstack 
	 */
	@Override
	public void load(RateBeerFragment fragment, boolean addToBackStack) {
		FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
		//trans.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
		trans.replace(R.id.frag_content, fragment);
		if (getSupportFragmentManager().findFragmentById(R.id.frag_content) != null && addToBackStack) {
			trans.addToBackStack(null);
		}
		trans.commit();
		getSupportActionBar().setDisplayHomeAsUpEnabled(!(fragment instanceof DashboardFragment));
	}

}
