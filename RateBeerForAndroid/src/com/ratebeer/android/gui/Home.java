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

import android.app.SearchManager;
import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.ratebeer.android.R;
import com.ratebeer.android.app.RateBeerForAndroid;
import com.ratebeer.android.gui.components.BeermailService;
import com.ratebeer.android.gui.components.BootReceiver;
import com.ratebeer.android.gui.components.PosterService;
import com.ratebeer.android.gui.components.RateBeerActivity;
import com.ratebeer.android.gui.components.RateBeerFragment;
import com.ratebeer.android.gui.components.helpers.SearchUiHelper;
import com.ratebeer.android.gui.fragments.AboutFragment_;
import com.ratebeer.android.gui.fragments.AddUpcCodeFragment_;
import com.ratebeer.android.gui.fragments.BeerViewFragment_;
import com.ratebeer.android.gui.fragments.DashboardFragment;
import com.ratebeer.android.gui.fragments.DashboardFragment_;
import com.ratebeer.android.gui.fragments.MailViewFragment_;
import com.ratebeer.android.gui.fragments.MailsFragment;
import com.ratebeer.android.gui.fragments.RateFragment_;
import com.ratebeer.android.gui.fragments.SearchFragment_;
import com.ratebeer.android.gui.fragments.SendMailFragment;

@EActivity(R.layout.activity_home)
public class Home extends RateBeerActivity {

	private static final int MENU_PREFERENCES = 10;
	private static final int MENU_ABOUT = 11;

	@AfterViews
	public void init() {
		// Start the background service, if necessary
		BootReceiver.startAlarm(getApplicationContext());

		// Show search directly in action bar on larger screens
		// For phones the DashboardFragment and SearchFragment will show an icon
		new SearchUiHelper(this).addSearchToActionBar(getSupportActionBar());
		
		handleStartIntent();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem pref = menu.add(MENU_PREFERENCES, MENU_PREFERENCES, MENU_PREFERENCES, R.string.home_preferences);
		pref.setIcon(android.R.drawable.ic_menu_preferences);
		MenuItem about = menu.add(MENU_ABOUT, MENU_ABOUT, MENU_ABOUT, R.string.home_about);
		about.setIcon(android.R.drawable.ic_menu_info_details);
		return true;
	}

	@AfterViews
	protected void handleStartIntent() {

		if (getIntent().hasExtra(SearchManager.QUERY)) {
			// Start a search
			load(SearchFragment_.builder().query(getIntent().getStringExtra(SearchManager.QUERY)).build());
		} else if (getIntent().getAction() != null && getIntent().getData() != null && 
			getIntent().getAction().equals(Intent.ACTION_VIEW)) {
			// Open details for some specific beer
			List<String> segments = getIntent().getData().getPathSegments();
			if (segments.size() > 1) {
				try {
					int beerId = Integer.parseInt(segments.get(1));
					load(BeerViewFragment_.builder().beerId(beerId).build());
				} catch (NumberFormatException e) {
					Log.d(RateBeerForAndroid.LOG_NAME, "Invalid ACTION_VIEW Intent data; " + segments.get(1) + " is not a number.");
				}
			}
		} else if (getIntent().getAction() != null && getIntent().getAction().equals(PosterService.ACTION_EDITRATING)) {
			// Open the rating screen for a beer
			load(RateFragment_.buildFromExtras(getIntent().getExtras()));
		} else if (getIntent().getAction() != null && getIntent().getAction().equals(PosterService.ACTION_ADDUPCCODE)) {
			// Open the add UPC code screen again; this assumes the UPC code is given in the extras
			load(AddUpcCodeFragment_.builder().upcCode(getIntent().getStringExtra(PosterService.EXTRA_UPCCODE)).build());
		} else if (getIntent().getAction() != null && getIntent().getAction().equals(BeermailService.ACTION_VIEWBEERMAILS)) {
			// Open the beermails screen
			load(new MailsFragment());
		} else if (getIntent().getAction() != null && getIntent().getAction().equals(BeermailService.ACTION_VIEWBEERMAIL)) {
			// Open the beermail screen to a specific mail
			load(MailViewFragment_.buildFromExtras(getIntent().getExtras()));
		} else if (getIntent().getAction() != null && getIntent().getAction().equals(BeermailService.ACTION_REPLYBEERMAIL)) {
			// Open the beermail reply screen to a specific mail
			load(SendMailFragment.buildFromExtras(getIntent().getExtras()));
		} else {
			// Normal startup; show dashboard
			load(DashboardFragment_.builder().build());
			return;
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// Home button click in the action bar
			Home_.intent(this).flags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET).start();
			return true;
		case MENU_PREFERENCES:
			PreferencesInterface_.intent(this).start();
			return true;
		case MENU_ABOUT:
			load(AboutFragment_.builder().build());
			return true;
		}
		return super.onOptionsItemSelected(item);
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
