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
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SearchView;

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

public class HomeTablet extends RateBeerActivity {

	private static final int MENU_PREFERENCES = 10;
	
	// Number of device-independant pixels that will be added to the left and right side of the content pane if no side pane is shown
	// The right value will be set on orientation initalization or changes
	private float singlePaneMargin;
	private static final float SINGLE_PANE_MARGIN_LANDSCAPE = 200;
	private static final float SINGLE_PANE_MARGIN_PORTRAIT = 25;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, R.layout.activity_tablet);

		// Initial setting of the single-pane layout margin based on the screen orientation
		singlePaneMargin = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE? 
				SINGLE_PANE_MARGIN_LANDSCAPE: SINGLE_PANE_MARGIN_PORTRAIT;
		switchLayout(false);
		
		showSearch();
        
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
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

	protected void showSearch() {
		// Set up a SearchView
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = new SearchView(this);
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		searchView.setQueryRefinementEnabled(true);
		searchView.setIconifiedByDefault(false);
		searchView.setFocusable(false);
		searchView.setFocusableInTouchMode(false);
		getSupportActionBar().setCustomView(searchView);
		getSupportActionBar().setDisplayShowCustomEnabled(true);
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
			Intent i = new Intent(getApplication(), HomeTablet.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
	 * @param contentFragment The fragment to show 
	 */
	public void load(RateBeerFragment contentFragment) {
		load(contentFragment, true);
	}

	/**
	 * Load a new screen into the content fragment
	 * @param contentFragment The fragment to show 
	 * @param addToBackStack Whether to also add this fragment to the backstack 
	 */
	public void load(RateBeerFragment contentFragment, boolean addToBackStack) {
		FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
		//trans.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
		trans.replace(R.id.frag_content, contentFragment);
		switchLayout(false);
		if (getSupportFragmentManager().findFragmentById(R.id.frag_content) != null && addToBackStack) {
			trans.addToBackStack(null);
		}
		trans.commit();
		getSupportActionBar().setDisplayHomeAsUpEnabled(!(contentFragment instanceof DashboardFragment));
	}

	/**
	 * Load a new screen plus a side pane into the content fragment
	 * @param sideFragment The fragment to show in the side pane
	 * @param contentFragment The fragment to show in the content pane
	 */
	@Override
	public void load(RateBeerFragment sideFragment, RateBeerFragment contentFragment) {
		FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
		//trans.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
		trans.replace(R.id.frag_content, contentFragment);
		trans.replace(R.id.frag_side, sideFragment);
		switchLayout(true);
		if (getSupportFragmentManager().findFragmentById(R.id.frag_content) != null) {
			trans.addToBackStack(null);
		}
		trans.commit();
		getSupportActionBar().setDisplayHomeAsUpEnabled(!(contentFragment instanceof DashboardFragment));
	}

	/**
	 * Show or hide the side pane and adjust the content pane's left and right margin accordingly
	 * @param showAsTwoPane Whether to show the side pane
	 */
	private void switchLayout(boolean showAsTwoPane) {
		FrameLayout content = (FrameLayout) findViewById(R.id.frag_content);
		FrameLayout side = (FrameLayout) findViewById(R.id.frag_side);
		final float density = getResources().getDisplayMetrics().density;
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 2F);
		if (showAsTwoPane) {
			side.setVisibility(View.GONE);
			lp.setMargins(0, 0, 0, 0);
		} else {
			side.setVisibility(View.VISIBLE);
			lp.setMargins((int)(density * singlePaneMargin), 0, (int)(density * singlePaneMargin), 0);
		}
		content.setLayoutParams(lp);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		singlePaneMargin = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE? 
				SINGLE_PANE_MARGIN_LANDSCAPE: SINGLE_PANE_MARGIN_PORTRAIT;
	}

}
