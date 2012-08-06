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

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.SearchRecentSuggestions;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ratebeer.android.R;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.CommandFailureResult;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.api.command.SearchBeersCommand;
import com.ratebeer.android.api.command.SearchBrewersCommand;
import com.ratebeer.android.api.command.SearchBrewersCommand.BrewerSearchResult;
import com.ratebeer.android.api.command.UpcSearchCommand;
import com.ratebeer.android.api.command.SearchBeersCommand.BeerSearchResult;
import com.ratebeer.android.api.command.SearchPlacesCommand;
import com.ratebeer.android.api.command.SearchPlacesCommand.PlaceSearchResult;
import com.ratebeer.android.api.command.SearchUsersCommand;
import com.ratebeer.android.api.command.SearchUsersCommand.UserSearchResult;
import com.ratebeer.android.api.command.UpcSearchCommand.UpcSearchResult;
import com.ratebeer.android.app.RateBeerForAndroid;
import com.ratebeer.android.gui.SearchHistoryProvider;
import com.ratebeer.android.gui.components.ActivityUtil;
import com.ratebeer.android.gui.components.ArrayAdapter;
import com.ratebeer.android.gui.components.RateBeerActivity;
import com.ratebeer.android.gui.components.RateBeerFragment;
import com.ratebeer.android.gui.fragments.ConfirmDialogFragment.OnDialogResult;
import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitleProvider;

public class SearchFragment extends RateBeerFragment {

	private static final String STATE_QUERY = "lastQuery";
	private static final String STATE_BEERRESULTS = "beerResults";
	private static final String STATE_BREWERRESULTS = "brewerResults";
	private static final String STATE_PLACERESULTS = "placeResults";
	private static final String STATE_USERRESULTS = "userResults";
	public static final String ARG_QUERY = "query";
	private static final int MENU_CLEARHISTORY = 0;
	private static final int MENU_SCANBARCODE = 1;
	private static final int MENU_SEARCH = 2;

	public final static String SCAN_INTENT = "com.google.zxing.client.android.SCAN";
	public static final Uri SCANNER_MARKET_URI = Uri.parse("market://search?q=pname:com.google.zxing.client.android");
	private static final int ACTIVITY_BARCODE = 0;

	private LayoutInflater inflater;
	private ViewPager pager;
	private ListView beersView;
	private ListView brewersView;
	private ListView placesView;
	private ListView usersView;

	private String lastQuery = null;
	private boolean startBarcodeScanner = false;
	private ArrayList<BeerSearchResult> beerResults = null;
	private ArrayList<BrewerSearchResult> brewerResults = null;
	private ArrayList<PlaceSearchResult> placeResults = null;
	private ArrayList<UserSearchResult> userResults = null;

	public SearchFragment() {
		this(null);
	}

	public SearchFragment(boolean startBarcodeScanner) {
		this(null);
		this.startBarcodeScanner = startBarcodeScanner;
	}

	public SearchFragment(String query) {
		this.lastQuery = query;
	}

	/*
	 * @Override public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); if
	 * (savedInstanceState == null) { performSearch(); } }
	 */

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		this.inflater = inflater;
		return inflater.inflate(R.layout.fragment_search, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// Get and set up ListViews and the ViewPager
		pager = (ViewPager) getView().findViewById(R.id.pager);
		SearchPagerAdapter cellarPagerAdapter = new SearchPagerAdapter();
		pager.setAdapter(cellarPagerAdapter);
		TabPageIndicator titles = (TabPageIndicator) getView().findViewById(R.id.titles);
		titles.setViewPager(pager);

		beersView = cellarPagerAdapter.getBeersView();
		brewersView = cellarPagerAdapter.getBrewersView();
		placesView = cellarPagerAdapter.getPlacesView();
		usersView = cellarPagerAdapter.getUsersView();
		beersView.setOnItemClickListener(onBeerSelected);
		brewersView.setOnItemClickListener(onBrewerSelected);
		placesView.setOnItemClickListener(onPlaceSelected);
		usersView.setOnItemClickListener(onUserSelected);
		registerForContextMenu(beersView);
		registerForContextMenu(brewersView);
		registerForContextMenu(placesView);
		registerForContextMenu(usersView);

		if (savedInstanceState != null) {
			lastQuery = savedInstanceState.getString(STATE_QUERY);
			if (savedInstanceState.containsKey(STATE_BEERRESULTS)) {
				beerResults = savedInstanceState.getParcelableArrayList(STATE_BEERRESULTS);
			}
			if (savedInstanceState.containsKey(STATE_BREWERRESULTS)) {
				brewerResults = savedInstanceState.getParcelableArrayList(STATE_BREWERRESULTS);
			}
			if (savedInstanceState.containsKey(STATE_PLACERESULTS)) {
				placeResults = savedInstanceState.getParcelableArrayList(STATE_PLACERESULTS);
			}
			if (savedInstanceState.containsKey(STATE_USERRESULTS)) {
				userResults = savedInstanceState.getParcelableArrayList(STATE_USERRESULTS);
			}
		} else if (beerResults == null) {
			// Fresh start: save this query and perform the search
			if (lastQuery != null) {
				// Store query in search history
				SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(),
						SearchHistoryProvider.AUTHORITY, SearchHistoryProvider.MODE);
				suggestions.saveRecentQuery(lastQuery, null);
			}
			performSearch();
		}
		// Publish the current wants and haves view, even when it is not loaded yet (and thus still empty)
		publishBeerResults(beerResults);
		publishBrewerResults(brewerResults);
		publishPlaceResults(placeResults);
		publishUserResults(userResults);

		if (startBarcodeScanner) {
			startScanner();
			// Don't start again when returning to this screen
			startBarcodeScanner = false;
		}

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if (getActivity() != null && !RateBeerForAndroid.isTablet(getResources())) {
			// For phones, the dashboard & search fragments show a search icon in the action bar
			// Note that tablets always show an search input in the action bar through the HomeTablet activity directly
			MenuItem item = menu.add(Menu.NONE, MENU_SEARCH, Menu.NONE, R.string.home_search);
			item.setIcon(R.drawable.ic_action_search);
			item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		}
		MenuItem item = menu.add(Menu.NONE, RateBeerActivity.MENU_REFRESH, Menu.NONE, R.string.app_refresh);
		item.setIcon(R.drawable.ic_action_refresh);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		MenuItem item2 = menu.add(Menu.NONE, MENU_SCANBARCODE, Menu.NONE, R.string.search_barcodescanner);
		item2.setIcon(R.drawable.ic_action_barcode);
		item2.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		
		menu.add(MENU_CLEARHISTORY, MENU_CLEARHISTORY, MENU_CLEARHISTORY, R.string.search_clearhistory);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case RateBeerActivity.MENU_REFRESH:
			performSearch();
			break;
		case MENU_CLEARHISTORY:
			SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(),
					SearchHistoryProvider.AUTHORITY, SearchHistoryProvider.MODE);
			suggestions.clearHistory();
			break;
		case MENU_SCANBARCODE:
			startScanner();
			break;
		case MENU_SEARCH:
			// Open standard search interface
			getActivity().onSearchRequested();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void startScanner() {
		// Test to see if the ZXing barcode scanner is available that can handle the SCAN intent
		Intent scan = new Intent(SCAN_INTENT);
		scan.addCategory(Intent.CATEGORY_DEFAULT);
		scan.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		if (ActivityUtil.isIntentAvailable(getActivity(), scan)) {
			// Ask the barcode scanner to allow the user to scan some code
			startActivityForResult(scan, ACTIVITY_BARCODE);
		} else {
			// Show a message if the user should install the barcode scanner for this feature
			new ConfirmDialogFragment(new OnDialogResult() {
				@Override
				public void onConfirmed() {
					Intent install = new Intent(Intent.ACTION_VIEW, SCANNER_MARKET_URI);
					if (ActivityUtil.isIntentAvailable(getActivity(), install)) {
						startActivity(install);
					} else {
						Toast.makeText(getActivity(), R.string.app_nomarket, Toast.LENGTH_LONG).show();
					}
				}
			}, R.string.app_scannernotfound, "").show(getSupportFragmentManager(), "installscanner");
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_QUERY, lastQuery);
		if (beerResults != null) {
			outState.putParcelableArrayList(STATE_BEERRESULTS, beerResults);
		}
		if (brewerResults != null) {
			outState.putParcelableArrayList(STATE_BREWERRESULTS, brewerResults);
		}
		if (placeResults != null) {
			outState.putParcelableArrayList(STATE_PLACERESULTS, placeResults);
		}
		if (userResults != null) {
			outState.putParcelableArrayList(STATE_USERRESULTS, userResults);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == ACTIVITY_BARCODE && resultCode == Activity.RESULT_OK) {

			// Get scan results code
			String contents = data.getStringExtra("SCAN_RESULT");
			// String formatName = data.getStringExtra("SCAN_RESULT_FORMAT");

			// Start lookup for this code
			execute(new UpcSearchCommand(getRateBeerApplication().getApi(), contents));

		}
	}

	private void performSearch() {
		if (lastQuery == null) {
			if (beersView.getAdapter() != null) {
				((BeerSearchResultsAdapter) beersView.getAdapter()).clear();
			}
			if (brewersView.getAdapter() != null) {
				((BrewerSearchResultsAdapter) brewersView.getAdapter()).clear();
			}
			if (placesView.getAdapter() != null) {
				((BeerSearchResultsAdapter) placesView.getAdapter()).clear();
			}
			if (usersView.getAdapter() != null) {
				((BeerSearchResultsAdapter) usersView.getAdapter()).clear();
			}
		} else {
			execute(new SearchBeersCommand(getRateBeerActivity().getApi(), lastQuery,
					getRateBeerActivity().getUser() != null ? getRateBeerActivity().getUser().getUserID()
							: SearchBeersCommand.NO_USER));
			execute(new SearchBrewersCommand(getRateBeerActivity().getApi(), lastQuery));
			execute(new SearchPlacesCommand(getRateBeerActivity().getApi(), lastQuery));
			execute(new SearchUsersCommand(getRateBeerActivity().getApi(), lastQuery));
		}
	}

	private OnItemClickListener onBeerSelected = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			BeerSearchResult item = ((BeerSearchResultsAdapter) beersView.getAdapter()).getItem(position);
			if (item.isAlias) {
				// Unfortunately this is the only possible workaround for now to prohibit viewing an aliased beer as
				// if it were a normal one (see issue 8)
				Toast.makeText(getActivity(), getString(R.string.search_aliasedbeer), Toast.LENGTH_LONG).show();
				return;
			}
			getRateBeerActivity().load(new BeerViewFragment(item.beerName, item.beerId, item.rateCount));
		}
	};

	private OnItemClickListener onBrewerSelected = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			BrewerSearchResult item = ((BrewerSearchResultsAdapter) brewersView.getAdapter()).getItem(position);
			getRateBeerActivity().load(new BrewerViewFragment(item.brewerId));
		}
	};

	private OnItemClickListener onPlaceSelected = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			PlaceSearchResult item = ((PlaceSearchResultsAdapter) placesView.getAdapter()).getItem(position);
			getRateBeerActivity().load(new PlaceViewFragment(item.placeId));
		}
	};

	private OnItemClickListener onUserSelected = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			UserSearchResult item = ((UserSearchResultsAdapter) usersView.getAdapter()).getItem(position);
			getRateBeerActivity().load(new UserViewFragment(item.userName, item.userId));
		}
	};

	@Override
	public void onTaskSuccessResult(CommandSuccessResult result) {
		if (result.getCommand().getMethod() == ApiMethod.SearchBeers) {
			publishBeerResults(((SearchBeersCommand) result.getCommand()).getSearchResults());
		} else if (result.getCommand().getMethod() == ApiMethod.SearchBrewers) {
			publishBrewerResults(((SearchBrewersCommand) result.getCommand()).getSearchResults());
		} else if (result.getCommand().getMethod() == ApiMethod.SearchPlaces) {
			publishPlaceResults(((SearchPlacesCommand) result.getCommand()).getSearchResults());
		} else if (result.getCommand().getMethod() == ApiMethod.SearchUsers) {
			publishUserResults(((SearchUsersCommand) result.getCommand()).getSearchResults());
		} else if (result.getCommand().getMethod() == ApiMethod.UpcSearch) {
			// See if there were any results
			UpcSearchCommand command = (UpcSearchCommand) result.getCommand();
			List<UpcSearchResult> results = command.getUpcSearchResults();
			if (results.size() > 0) {
				// Beer found: redirect to beer details
				getRateBeerActivity().load(new BeerViewFragment(results.get(0).beerName, results.get(0).beerId));
			} else {
				noBarcodeResult(command.getSearchedUpcCode());
			}
		}
	}

	private void noBarcodeResult(final String code) {
		// Report that there were no results and ask the user to specify this beer instead
		new ConfirmDialogFragment(new OnDialogResult() {
			@Override
			public void onConfirmed() {
				getSupportFragmentManager().popBackStack();
				getRateBeerActivity().load(new AddUpcCodeFragment(code), false);
			}
		}, R.string.search_nobeerswithbarcode, code).show(getSupportFragmentManager(), "addupccode");
	}

	private void publishBeerResults(ArrayList<BeerSearchResult> result) {
		this.beerResults = result;
		if (result == null) {
			// Show empty result
			result = new ArrayList<BeerSearchResult>();
			return;
		}
		if (beersView.getAdapter() == null) {
			beersView.setAdapter(new BeerSearchResultsAdapter(getActivity(), result));
		} else {
			((BeerSearchResultsAdapter) beersView.getAdapter()).replace(result);
		}
	}

	private void publishBrewerResults(ArrayList<BrewerSearchResult> result) {
		this.brewerResults = result;
		if (result == null) {
			// Show empty result
			result = new ArrayList<BrewerSearchResult>();
			return;
		}
		if (brewersView.getAdapter() == null) {
			brewersView.setAdapter(new BrewerSearchResultsAdapter(getActivity(), result));
		} else {
			((BrewerSearchResultsAdapter) brewersView.getAdapter()).replace(result);
		}
	}

	private void publishPlaceResults(ArrayList<PlaceSearchResult> result) {
		this.placeResults = result;
		if (result == null) {
			// Show empty result
			result = new ArrayList<PlaceSearchResult>();
			return;
		}
		if (placesView.getAdapter() == null) {
			placesView.setAdapter(new PlaceSearchResultsAdapter(getActivity(), result));
		} else {
			((PlaceSearchResultsAdapter) placesView.getAdapter()).replace(result);
		}
	}

	private void publishUserResults(ArrayList<UserSearchResult> result) {
		this.userResults = result;
		if (result == null) {
			// Show empty result
			result = new ArrayList<UserSearchResult>();
			return;
		}
		if (usersView.getAdapter() == null) {
			usersView.setAdapter(new UserSearchResultsAdapter(getActivity(), result));
		} else {
			((UserSearchResultsAdapter) usersView.getAdapter()).replace(result);
		}
	}

	@Override
	public void onTaskFailureResult(CommandFailureResult result) {
		publishException(null, result.getException());
	}

	private class BeerSearchResultsAdapter extends ArrayAdapter<BeerSearchResult> {

		public BeerSearchResultsAdapter(Context context, List<BeerSearchResult> objects) {
			super(context, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// Get the right view, using a ViewHolder
			BeerViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.list_item_beersearchresult, null);
				holder = new BeerViewHolder();
				holder.beer = (TextView) convertView.findViewById(R.id.beer);
				holder.overall = (TextView) convertView.findViewById(R.id.overall);
				holder.count = (TextView) convertView.findViewById(R.id.count);
				holder.rated = (TextView) convertView.findViewById(R.id.rated);
				holder.retired = (TextView) convertView.findViewById(R.id.retired);
				holder.alias = (TextView) convertView.findViewById(R.id.alias);
				convertView.setTag(holder);
			} else {
				holder = (BeerViewHolder) convertView.getTag();
			}

			// Bind the data
			BeerSearchResult item = getItem(position);
			if (getActivity() != null) {
				holder.beer.setText(item.beerName);
				holder.overall.setText((item.overallPerc >= 0 ? Integer.toString(item.overallPerc) : "?"));
				holder.count.setText(Integer.toString(item.rateCount) + " " + getString(R.string.details_ratings));
				holder.rated.setVisibility(item.isRated ? View.VISIBLE : View.GONE);
				holder.retired.setVisibility(item.isRetired ? View.VISIBLE : View.GONE);
				holder.alias.setVisibility(item.isAlias ? View.VISIBLE : View.GONE);
			}

			return convertView;
		}

	}

	protected static class BeerViewHolder {
		TextView beer, overall, count, rated, retired, alias;
	}

	private class BrewerSearchResultsAdapter extends ArrayAdapter<BrewerSearchResult> {

		public BrewerSearchResultsAdapter(Context context, List<BrewerSearchResult> objects) {
			super(context, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// Get the right view, using a ViewHolder
			BrewerViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.list_item_placesearchresult, null);
				holder = new BrewerViewHolder();
				holder.name = (TextView) convertView.findViewById(R.id.name);
				holder.city = (TextView) convertView.findViewById(R.id.city);
				convertView.setTag(holder);
			} else {
				holder = (BrewerViewHolder) convertView.getTag();
			}

			// Bind the data
			BrewerSearchResult item = getItem(position);
			if (getActivity() != null) {
				holder.name.setText(item.brewerName);
				holder.city.setText(item.city + ", " + item.country);
			}

			return convertView;
		}

	}

	protected static class BrewerViewHolder {
		TextView name, city;
	}

	private class PlaceSearchResultsAdapter extends ArrayAdapter<PlaceSearchResult> {

		public PlaceSearchResultsAdapter(Context context, List<PlaceSearchResult> objects) {
			super(context, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// Get the right view, using a ViewHolder
			PlaceViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.list_item_placesearchresult, null);
				holder = new PlaceViewHolder();
				holder.name = (TextView) convertView.findViewById(R.id.name);
				holder.city = (TextView) convertView.findViewById(R.id.city);
				convertView.setTag(holder);
			} else {
				holder = (PlaceViewHolder) convertView.getTag();
			}

			// Bind the data
			PlaceSearchResult item = getItem(position);
			if (getActivity() != null) {
				holder.name.setText(item.placeName);
				holder.city.setText(item.city);
			}

			return convertView;
		}

	}

	protected static class PlaceViewHolder {
		TextView name, city;
	}

	private class UserSearchResultsAdapter extends ArrayAdapter<UserSearchResult> {

		public UserSearchResultsAdapter(Context context, List<UserSearchResult> objects) {
			super(context, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// Get the right view, using a ViewHolder
			UserViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.list_item_usersearchresult, null);
				holder = new UserViewHolder();
				holder.name = (TextView) convertView.findViewById(R.id.name);
				holder.ratings = (TextView) convertView.findViewById(R.id.city);
				convertView.setTag(holder);
			} else {
				holder = (UserViewHolder) convertView.getTag();
			}

			// Bind the data
			UserSearchResult item = getItem(position);
			if (getActivity() != null) {
				holder.name.setText(item.userName);
				holder.ratings.setText(getString(R.string.search_ratings, Integer.toString(item.ratings)));
			}

			return convertView;
		}

	}

	protected static class UserViewHolder {
		TextView name, ratings;
	}

	private class SearchPagerAdapter extends PagerAdapter implements TitleProvider {

		private ListView pagerBeersView;
		private ListView pagerBrewersView;
		private ListView pagerPlacesView;
		private ListView pagerUsersView;

		public SearchPagerAdapter() {
			LayoutInflater inflater = getActivity().getLayoutInflater();
			pagerBeersView = (ListView) inflater.inflate(R.layout.fragment_pagerlist, null);
			pagerBrewersView = (ListView) inflater.inflate(R.layout.fragment_pagerlist, null);
			pagerPlacesView = (ListView) inflater.inflate(R.layout.fragment_pagerlist, null);
			pagerUsersView = (ListView) inflater.inflate(R.layout.fragment_pagerlist, null);
		}

		public ListView getBeersView() {
			return pagerBeersView;
		}

		public ListView getBrewersView() {
			return pagerBrewersView;
		}

		public ListView getPlacesView() {
			return pagerPlacesView;
		}

		public ListView getUsersView() {
			return pagerUsersView;
		}

		@Override
		public int getCount() {
			return 4;
		}

		@Override
		public String getTitle(int position) {
			switch (position) {
			case 0:
				return getActivity().getString(R.string.search_beers).toUpperCase();
			case 1:
				return getActivity().getString(R.string.search_brewers).toUpperCase();
			case 2:
				return getActivity().getString(R.string.search_places).toUpperCase();
			case 3:
				return getActivity().getString(R.string.search_users).toUpperCase();
			}
			return null;
		}

		@Override
		public Object instantiateItem(View container, int position) {
			switch (position) {
			case 0:
				((ViewPager) container).addView(pagerBeersView, 0);
				return pagerBeersView;
			case 1:
				((ViewPager) container).addView(pagerBrewersView, 0);
				return pagerBrewersView;
			case 2:
				((ViewPager) container).addView(pagerPlacesView, 0);
				return pagerPlacesView;
			case 3:
				((ViewPager) container).addView(pagerUsersView, 0);
				return pagerUsersView;
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

		@Override
		public void finishUpdate(View container) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View container) {
		}

		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {
		}

	}

}
