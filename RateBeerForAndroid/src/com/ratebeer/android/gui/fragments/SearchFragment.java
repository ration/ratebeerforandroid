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
import android.provider.SearchRecentSuggestions;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.InstanceState;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.ViewById;
import com.ratebeer.android.R;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.CommandFailureResult;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.api.command.GetAliasedBeerCommand;
import com.ratebeer.android.api.command.SearchBeersCommand;
import com.ratebeer.android.api.command.SearchBeersCommand.BeerSearchResult;
import com.ratebeer.android.api.command.SearchBrewersCommand;
import com.ratebeer.android.api.command.SearchBrewersCommand.BrewerSearchResult;
import com.ratebeer.android.api.command.SearchPlacesCommand;
import com.ratebeer.android.api.command.SearchPlacesCommand.PlaceSearchResult;
import com.ratebeer.android.api.command.SearchUsersCommand;
import com.ratebeer.android.api.command.SearchUsersCommand.UserSearchResult;
import com.ratebeer.android.api.command.UpcSearchCommand;
import com.ratebeer.android.api.command.UpcSearchCommand.UpcSearchResult;
import com.ratebeer.android.gui.SearchHistoryProvider;
import com.ratebeer.android.gui.components.RateBeerFragment;
import com.ratebeer.android.gui.components.helpers.ActivityUtil;
import com.ratebeer.android.gui.components.helpers.ArrayAdapter;
import com.ratebeer.android.gui.components.helpers.SearchUiHelper;
import com.ratebeer.android.gui.fragments.ConfirmDialogFragment.OnDialogResult;
import com.viewpagerindicator.TabPageIndicator;

import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;

@EFragment(R.layout.fragment_search)
@OptionsMenu(R.menu.search)
public class SearchFragment extends RateBeerFragment {

	public final static String SCAN_INTENT = "com.google.zxing.client.android.SCAN";
	public static final Uri SCANNER_MARKET_URI = Uri.parse("market://search?q=pname:com.google.zxing.client.android");
	private static final int ACTIVITY_BARCODE = 0;

	@FragmentArg
	@InstanceState
	protected String query = null;
	@FragmentArg
	@InstanceState
	protected boolean startBarcodeScanner = false;
	@InstanceState
	protected ArrayList<BeerSearchResult> beerResults = null;
	@InstanceState
	protected ArrayList<BrewerSearchResult> brewerResults = null;
	@InstanceState
	protected ArrayList<PlaceSearchResult> placeResults = null;
	@InstanceState
	protected ArrayList<UserSearchResult> userResults = null;

	@ViewById
	protected ViewPager pager;
	@ViewById
	protected TabPageIndicator titles;
	private ListView beersView, brewersView, placesView, usersView;
	private TextView beersEmpty, brewersEmpty, placesEmpty, usersEmpty;

	public SearchFragment() {
	}

	@AfterViews
	public void init() {

		// Get and set up ListViews and the ViewPager
		pager.setAdapter(new SearchPagerAdapter());
		titles.setViewPager(pager);

		beersView.setOnItemClickListener(onBeerSelected);
		brewersView.setOnItemClickListener(onBrewerSelected);
		placesView.setOnItemClickListener(onPlaceSelected);
		usersView.setOnItemClickListener(onUserSelected);
		registerForContextMenu(beersView);
		registerForContextMenu(brewersView);
		registerForContextMenu(placesView);
		registerForContextMenu(usersView);

		if (beerResults != null && brewerResults != null && placeResults != null && userResults != null) {
			publishBeerResults(beerResults);
			publishBrewerResults(brewerResults);
			publishPlaceResults(placeResults);
			publishUserResults(userResults);
		} else {
			// Fresh start: save this query and perform the search
			if (query != null) {
				// Store query in search history
				SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(),
						SearchHistoryProvider.AUTHORITY, SearchHistoryProvider.MODE);
				suggestions.saveRecentQuery(query, null);
			}
			performSearch();
		}

		if (startBarcodeScanner) {
			startScanner();
			// Don't start again when returning to this screen
			startBarcodeScanner = false;
		}

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		new SearchUiHelper(getActivity()).enhanceSearchInMenu(menu);
	}

	@OptionsItem(R.id.menu_search)
	protected void onStartSearch() {
		// Open standard search interface
		// Note that this method is only called on API < 8 as SearchView is used for API level >= 8 (via
		// ActionBarSherlock)
		getActivity().onSearchRequested();
	}

	@OptionsItem(R.id.menu_clearhistory)
	protected void onClearHistory() {
		SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(),
				SearchHistoryProvider.AUTHORITY, SearchHistoryProvider.MODE);
		suggestions.clearHistory();
	}

	@OptionsItem(R.id.menu_scanbarcode)
	protected void startScanner() {
		// Test to see if the ZXing barcode scanner is available that can handle the SCAN intent
		Intent scan = new Intent(SCAN_INTENT);
		scan.addCategory(Intent.CATEGORY_DEFAULT);
		scan.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		if (ActivityUtil.isIntentAvailable(getActivity(), scan)) {
			// Ask the barcode scanner to allow the user to scan some code
			try {
				startActivityForResult(scan, ACTIVITY_BARCODE);
			} catch (Exception e) {
				// Can't start the barcode scanner, for example with a SecurityException, even though it is there
				Crouton.makeText(getActivity(), R.string.app_notsupported, Style.INFO).show();
			}
		} else {
			// Show a message if the user should install the barcode scanner for this feature
			new ConfirmDialogFragment(new OnDialogResult() {
				@Override
				public void onConfirmed() {
					Intent install = new Intent(Intent.ACTION_VIEW, SCANNER_MARKET_URI);
					if (ActivityUtil.isIntentAvailable(getActivity(), install)) {
						startActivity(install);
					} else {
						Crouton.makeText(getActivity(), R.string.app_nomarket, Style.INFO).show();
					}
				}
			}, R.string.app_scannernotfound, "").show(getFragmentManager(), "installscanner");
		}
	}

	// NOTE: The OnActivityResult annotation is not supported in Fragments
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == ACTIVITY_BARCODE && resultCode == Activity.RESULT_OK) {

			// Get scan results code
			String contents = data.getStringExtra("SCAN_RESULT");
			// String formatName = data.getStringExtra("SCAN_RESULT_FORMAT");

			// Start lookup for this code
			execute(new UpcSearchCommand(getUser(), contents));

		}
	}

	@OptionsItem(R.id.menu_refresh)
	protected void performSearch() {
		if (query == null) {
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
			execute(new SearchBeersCommand(getUser(), query, getUser() != null ? getUser().getUserID()
					: SearchBeersCommand.NO_USER));
			execute(new SearchBrewersCommand(getUser(), query));
			execute(new SearchPlacesCommand(getUser(), query));
			execute(new SearchUsersCommand(getUser(), query));
		}
	}

	private OnItemClickListener onBeerSelected = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			BeerSearchResult item = ((BeerSearchResultsAdapter) beersView.getAdapter()).getItem(position);
			if (item.isAlias) {
				// No aliased beer id: try to parse it instead
				Crouton.makeText(getActivity(), R.string.search_aliasedbeer_redirect, Style.INFO).show();
				execute(new GetAliasedBeerCommand(getUser(), item.beerId));
				return;
			}
			load(BeerViewFragment_.builder().beerName(item.beerName).beerId(item.beerId).ratingsCount(item.rateCount)
					.build());
		}
	};

	private OnItemClickListener onBrewerSelected = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			BrewerSearchResult item = ((BrewerSearchResultsAdapter) brewersView.getAdapter()).getItem(position);
			load(BrewerViewFragment_.builder().brewerId(item.brewerId).build());
		}
	};

	private OnItemClickListener onPlaceSelected = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			PlaceSearchResult item = ((PlaceSearchResultsAdapter) placesView.getAdapter()).getItem(position);
			load(PlaceViewFragment_.builder().placeId(item.placeId).build());
		}
	};

	private OnItemClickListener onUserSelected = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			UserSearchResult item = ((UserSearchResultsAdapter) usersView.getAdapter()).getItem(position);
			load(UserViewFragment_.builder().userName(item.userName).userId(item.userId).build());
		}
	};

	@Override
	public void onTaskSuccessResult(CommandSuccessResult result) {
		if (result.getCommand().getMethod() == ApiMethod.SearchBeers) {
			publishBeerResults(((SearchBeersCommand) result.getCommand()).getSearchResults());
			beersEmpty.setText(R.string.search_nobeers);
		} else if (result.getCommand().getMethod() == ApiMethod.SearchBrewers) {
			publishBrewerResults(((SearchBrewersCommand) result.getCommand()).getSearchResults());
			brewersEmpty.setText(R.string.search_nobrewers);
		} else if (result.getCommand().getMethod() == ApiMethod.SearchPlaces) {
			publishPlaceResults(((SearchPlacesCommand) result.getCommand()).getSearchResults());
			placesEmpty.setText(R.string.search_nopalces);
		} else if (result.getCommand().getMethod() == ApiMethod.SearchUsers) {
			publishUserResults(((SearchUsersCommand) result.getCommand()).getSearchResults());
			usersEmpty.setText(R.string.search_nousers);
		} else if (result.getCommand().getMethod() == ApiMethod.UpcSearch) {
			// See if there were any results
			UpcSearchCommand command = (UpcSearchCommand) result.getCommand();
			List<UpcSearchResult> results = command.getUpcSearchResults();
			if (results.size() > 0) {
				// Beer found: redirect to beer details
				load(BeerViewFragment_.builder().beerName(results.get(0).beerName).beerId(results.get(0).beerId)
						.build());
			} else {
				noBarcodeResult(command.getSearchedUpcCode());
			}
		} else if (result.getCommand().getMethod() == ApiMethod.GetAliasedBeer) {
			// Alias found: redirect to beer details
			load(BeerViewFragment_.builder().beerId(((GetAliasedBeerCommand) result.getCommand()).getAliasedBeerId())
					.build());
		}
	}

	private void noBarcodeResult(final String code) {
		// Report that there were no results and ask the user to specify this beer instead
		new ConfirmDialogFragment(new OnDialogResult() {
			@Override
			public void onConfirmed() {
				getFragmentManager().popBackStack();
				load(AddUpcCodeFragment_.builder().upcCode(code).build());
			}
		}, R.string.search_nobeerswithbarcode, code).show(getFragmentManager(), "addupccode");
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
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_beersearchresult, null);
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
			holder.beer.setText(item.beerName);
			holder.overall.setText((item.overallPerc >= 0 ? Integer.toString(item.overallPerc) : "?"));
			holder.count.setText(Integer.toString(item.rateCount) + " " + getString(R.string.details_ratings));
			holder.rated.setVisibility(item.isRated ? View.VISIBLE : View.GONE);
			holder.retired.setVisibility(item.isRetired ? View.VISIBLE : View.GONE);
			holder.alias.setVisibility(item.isAlias ? View.VISIBLE : View.GONE);

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
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_placesearchresult, null);
				holder = new BrewerViewHolder();
				holder.name = (TextView) convertView.findViewById(R.id.name);
				holder.city = (TextView) convertView.findViewById(R.id.city);
				convertView.setTag(holder);
			} else {
				holder = (BrewerViewHolder) convertView.getTag();
			}

			// Bind the data
			BrewerSearchResult item = getItem(position);
			holder.name.setText(item.brewerName);
			holder.city.setText(item.city + ", " + item.country);

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
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_placesearchresult, null);
				holder = new PlaceViewHolder();
				holder.name = (TextView) convertView.findViewById(R.id.name);
				holder.city = (TextView) convertView.findViewById(R.id.city);
				convertView.setTag(holder);
			} else {
				holder = (PlaceViewHolder) convertView.getTag();
			}

			// Bind the data
			PlaceSearchResult item = getItem(position);
			holder.name.setText(item.placeName);
			holder.city.setText(item.city);

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
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_usersearchresult, null);
				holder = new UserViewHolder();
				holder.name = (TextView) convertView.findViewById(R.id.name);
				holder.ratings = (TextView) convertView.findViewById(R.id.city);
				convertView.setTag(holder);
			} else {
				holder = (UserViewHolder) convertView.getTag();
			}

			// Bind the data
			UserSearchResult item = getItem(position);
			holder.name.setText(item.userName);
			holder.ratings.setText(getString(R.string.search_ratings, Integer.toString(item.ratings)));

			return convertView;
		}

	}

	protected static class UserViewHolder {
		TextView name, ratings;
	}

	private class SearchPagerAdapter extends PagerAdapter {

		private FrameLayout pagerBeersFrame;
		private FrameLayout pagerBrewersFrame;
		private FrameLayout pagerPlacesFrame;
		private FrameLayout pagerUsersFrame;

		public SearchPagerAdapter() {
			LayoutInflater inflater = getActivity().getLayoutInflater();
			pagerBeersFrame = (FrameLayout) inflater.inflate(R.layout.fragment_searchlist, null);
			beersEmpty = (TextView) pagerBeersFrame.findViewById(R.id.empty);
			beersView = (ListView) pagerBeersFrame.findViewById(R.id.list);
			beersView.setEmptyView(beersEmpty);
			pagerBrewersFrame = (FrameLayout) inflater.inflate(R.layout.fragment_searchlist, null);
			brewersEmpty = (TextView) pagerBrewersFrame.findViewById(R.id.empty);
			brewersView = (ListView) pagerBrewersFrame.findViewById(R.id.list);
			brewersView.setEmptyView(brewersEmpty);
			pagerPlacesFrame = (FrameLayout) inflater.inflate(R.layout.fragment_searchlist, null);
			placesEmpty = (TextView) pagerPlacesFrame.findViewById(R.id.empty);
			placesView = (ListView) pagerPlacesFrame.findViewById(R.id.list);
			placesView.setEmptyView(placesEmpty);
			pagerUsersFrame = (FrameLayout) inflater.inflate(R.layout.fragment_searchlist, null);
			usersEmpty = (TextView) pagerUsersFrame.findViewById(R.id.empty);
			usersView = (ListView) pagerUsersFrame.findViewById(R.id.list);
			usersView.setEmptyView(usersEmpty);
		}

		@Override
		public int getCount() {
			return 4;
		}

		@Override
		public CharSequence getPageTitle(int position) {
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
				((ViewPager) container).addView(pagerBeersFrame, 0);
				return pagerBeersFrame;
			case 1:
				((ViewPager) container).addView(pagerBrewersFrame, 0);
				return pagerBrewersFrame;
			case 2:
				((ViewPager) container).addView(pagerPlacesFrame, 0);
				return pagerPlacesFrame;
			case 3:
				((ViewPager) container).addView(pagerUsersFrame, 0);
				return pagerUsersFrame;
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
