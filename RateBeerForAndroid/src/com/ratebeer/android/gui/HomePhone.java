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
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.ratebeer.android.R;
import com.ratebeer.android.app.RateBeerForAndroid;
import com.ratebeer.android.gui.components.BeermailService;
import com.ratebeer.android.gui.components.PosterService;
import com.ratebeer.android.gui.components.RateBeerActivity;
import com.ratebeer.android.gui.components.RateBeerFragment;
import com.ratebeer.android.gui.fragments.AddUpcCodeFragment;
import com.ratebeer.android.gui.fragments.BeerViewFragment;
import com.ratebeer.android.gui.fragments.DashboardFragment;
import com.ratebeer.android.gui.fragments.MailViewFragment;
import com.ratebeer.android.gui.fragments.MailsFragment;
import com.ratebeer.android.gui.fragments.RateFragment;
import com.ratebeer.android.gui.fragments.SearchFragment;
import com.ratebeer.android.gui.fragments.SendMailFragment;

public class HomePhone extends RateBeerActivity {

	private static final int MENU_PREFERENCES = 10;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, R.layout.activity_phone);
	}

	@Override
	public void initialize(Bundle savedInstanceState) {
		
		if (savedInstanceState == null) {
			if (getIntent() != null && getIntent().hasExtra(SearchManager.QUERY)) {
				// Start a search
				load(new SearchFragment(getIntent().getStringExtra(SearchManager.QUERY)));
			} else if (getIntent() != null && getIntent().getAction() != null && getIntent().getData() != null && 
				getIntent().getAction().equals(Intent.ACTION_VIEW)) {
				// Open details for some specific beer
				List<String> segments = getIntent().getData().getPathSegments();
				if (segments.size() > 1) {
					try {
						int beerId = Integer.parseInt(segments.get(1));
						load(new BeerViewFragment(beerId));
					} catch (NumberFormatException e) {
						Log.d(RateBeerForAndroid.LOG_NAME, "Invalid ACTION_VIEW Intent data; " + segments.get(1) + " is not a number.");
					}
				}
			} else if (getIntent() != null && getIntent().getAction() != null && getIntent().getAction().equals(PosterService.ACTION_EDITRATING)) {
				// Open the rating screen for a beer
				load(new RateFragment(getIntent().getExtras()));
			} else if (getIntent() != null && getIntent().getAction() != null && getIntent().getAction().equals(PosterService.ACTION_ADDUPCCODE)) {
				// Open the add UPC code screen again; this assumes the UPC code is given in the extras
				load(new AddUpcCodeFragment(getIntent().getStringExtra(PosterService.EXTRA_UPCCODE)));
			} else if (getIntent() != null && getIntent().getAction() != null && getIntent().getAction().equals(BeermailService.ACTION_VIEWBEERMAILS)) {
				// Open the beermails screen
				load(new MailsFragment());
			} else if (getIntent() != null && getIntent().getAction() != null && getIntent().getAction().equals(BeermailService.ACTION_VIEWBEERMAIL)) {
				// Open the beermail screen to a specific mail
				load(new MailViewFragment(getIntent().getExtras()));
			} else if (getIntent() != null && getIntent().getAction() != null && getIntent().getAction().equals(BeermailService.ACTION_REPLYBEERMAIL)) {
				// Open the beermail reply screen to a specific mail
				load(new SendMailFragment(getIntent().getExtras()));
			} else {
				// Normal startup; show dashboard
				load(new DashboardFragment());
			}
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem pref = menu.add(MENU_PREFERENCES, MENU_PREFERENCES, MENU_PREFERENCES, R.string.home_preferences);
		pref.setIcon(android.R.drawable.ic_menu_preferences);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// Home button click in the action bar
			Intent i = new Intent(getApplication(), HomePhone.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			startActivity(i);
			return true;
		case MENU_PREFERENCES:
			startActivity(new Intent(getApplication(), PreferencesInterface.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Load a new screen into the content fragment and add it to the backstack
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

	@Override
	public void load(RateBeerFragment leftFragment, RateBeerFragment rightFragment) {
		load(leftFragment);
	}

}
