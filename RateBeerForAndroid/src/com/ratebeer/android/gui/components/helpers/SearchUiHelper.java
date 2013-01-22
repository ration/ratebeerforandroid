package com.ratebeer.android.gui.components.helpers;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.ratebeer.android.R;

public class SearchUiHelper {

	private Activity context;
	private SearchManager searchManager;
	
	public SearchUiHelper(Activity context) {
		this.context = context;
		this.searchManager = (SearchManager) context.getSystemService(Context.SEARCH_SERVICE);
	}

	@TargetApi(8)
	public void addSearchToActionBar(ActionBar actionBar) {
		if (!shouldShowInMenu()) {
			// Set up a SearchView
			SearchView searchView = new SearchView(context);
			searchView.setSearchableInfo(searchManager.getSearchableInfo(context.getComponentName()));
			searchView.setQueryRefinementEnabled(true);
			searchView.setIconifiedByDefault(false);
			searchView.setFocusable(false);
			searchView.setFocusableInTouchMode(false);
			actionBar.setCustomView(searchView);
			actionBar.setDisplayShowCustomEnabled(true);
		}
	}

	@TargetApi(8)
	public void enhanceSearchInMenu(Menu menu) {
		// For phones, the dashboard & search fragments show a search icon in the action bar
		// Note that tablets always show an search input in the action bar through the HomeTablet activity directly
		MenuItem item = menu.findItem(R.id.menu_search);
		if (shouldShowInMenu() && item != null && android.os.Build.VERSION.SDK_INT >= 8) {
			SearchView searchView = new SearchView(context);
			searchView.setSearchableInfo(searchManager.getSearchableInfo(context.getComponentName()));
			searchView.setQueryRefinementEnabled(true);
			item.setActionView(searchView);
		}
	}

	@TargetApi(13)
	private boolean shouldShowInMenu() {
		if (android.os.Build.VERSION.SDK_INT >= 13) { 
			if (context.getResources().getConfiguration().screenWidthDp >= 800) {
				return false; // Already shown as SearchView
			}
		}
		return true;
	}

}
