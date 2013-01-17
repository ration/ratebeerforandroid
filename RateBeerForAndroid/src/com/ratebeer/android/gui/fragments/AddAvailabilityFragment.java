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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.ratebeer.android.R;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.CommandFailureResult;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.api.command.AddAvailabilityCommand;
import com.ratebeer.android.api.command.GetFavouritePlacesCommand;
import com.ratebeer.android.api.command.SearchPlacesCommand;
import com.ratebeer.android.api.command.SearchPlacesCommand.PlaceSearchResult;
import com.ratebeer.android.gui.components.ArrayAdapter;
import com.ratebeer.android.gui.components.PosterService;
import com.ratebeer.android.gui.components.RateBeerActivity;
import com.ratebeer.android.gui.components.RateBeerFragment;
import com.viewpagerindicator.TabPageIndicator;

public class AddAvailabilityFragment extends RateBeerFragment {

	private static final String STATE_BEERID = "beerId";
	private static final String STATE_BEERNAME = "beerName";
	private static final String STATE_FAVOURITES = "favourites";
	private static final String STATE_FINDRESULTS = "findresults";

	private LayoutInflater inflater;
	private ListView favouritesView, findresultsView;
	private EditText placenameText;
	private CheckBox onbottlecanCheck, ontapCheck;
	private Button addavailabilityButton, skipButton, findplaceButton;
	private ViewPager pager;

	protected String beerName;
	protected int beerId;
	private ArrayList<PlaceSearchResult> favourites = new ArrayList<PlaceSearchResult>();
	private ArrayList<PlaceSearchResult> findResults = new ArrayList<PlaceSearchResult>();

	public AddAvailabilityFragment() {
		this(null, -1);
	}

	/**
	 * Allow adding of beer availability info
	 * @param beerName The beer name, or null if not known
	 * @param beerId The beer ID
	 */
	public AddAvailabilityFragment(String beerName, int beerId) {
		this.beerName = beerName;
		this.beerId = beerId;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		this.inflater = inflater;
		return inflater.inflate(R.layout.fragment_addavailability, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		pager = (ViewPager) getView().findViewById(R.id.pager);
		if (pager != null) {
			// Phone layout (using a pager to show the two lists)
			PlacesPagerAdapter cellarPagerAdapter = new PlacesPagerAdapter();
			pager.setAdapter(cellarPagerAdapter);
			favouritesView = cellarPagerAdapter.getFavouritesList();
			findresultsView = cellarPagerAdapter.getFindResultsList();
			placenameText = cellarPagerAdapter.getFindText();
			findplaceButton = cellarPagerAdapter.getFindButton();
			TabPageIndicator titles = (TabPageIndicator) getView().findViewById(R.id.titles);
			titles.setViewPager(pager);
		} else {
			// Tablet layout (showing both lists at the same time)
			favouritesView = (ListView) getView().findViewById(R.id.favourites);
			findresultsView = (ListView) getView().findViewById(R.id.findresults);
			placenameText = (EditText) getView().findViewById(R.id.placename);
			findplaceButton = (Button) getView().findViewById(R.id.findplace);
		}
		favouritesView.setItemsCanFocus(false);
		findresultsView.setItemsCanFocus(false);
		placenameText.addTextChangedListener(onPlaceNameChanged);
		findplaceButton.setOnClickListener(onFindPlace);

		// The bottle/tap checks and buttons are on the bottom and never in a pager
		onbottlecanCheck = (CheckBox) getView().findViewById(R.id.onbottlecan);
		ontapCheck = (CheckBox) getView().findViewById(R.id.ontap);
		addavailabilityButton = (Button) getView().findViewById(R.id.addavailability);
		addavailabilityButton.setOnClickListener(onAddAvailability);
		skipButton = (Button) getView().findViewById(R.id.skip);
		skipButton.setOnClickListener(onSkipClicked);

		if (savedInstanceState != null) {
			beerName = savedInstanceState.getString(STATE_BEERNAME);
			beerId = savedInstanceState.getInt(STATE_BEERID);
			if (savedInstanceState.containsKey(STATE_FAVOURITES)) {
				favourites = savedInstanceState.getParcelableArrayList(STATE_FAVOURITES);
			}
			if (savedInstanceState.containsKey(STATE_FAVOURITES)) {
				findResults = savedInstanceState.getParcelableArrayList(STATE_FINDRESULTS);
			}
		} else {
			refreshFavourites();
		}
		// Publish the current favs and find results, even when they are not loaded yet (and thus still empty)
		publishFavourites(favourites);
		publishFindResults(findResults);

		
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		MenuItem item = menu.add(RateBeerActivity.MENU_REFRESH, RateBeerActivity.MENU_REFRESH, RateBeerActivity.MENU_REFRESH, R.string.app_refresh);
		item.setIcon(R.drawable.ic_action_refresh);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case RateBeerActivity.MENU_REFRESH:
			refreshFavourites();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_BEERNAME, beerName);
		outState.putInt(STATE_BEERID, beerId);
		if (favourites != null) {
			outState.putParcelableArrayList(STATE_FAVOURITES, favourites);
		}
		if (findResults != null) {
			outState.putParcelableArrayList(STATE_FINDRESULTS, findResults);
		}
	}

	private TextWatcher onPlaceNameChanged = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {}
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		@Override
		public void afterTextChanged(Editable s) {
			// Enable the find button only when a query is entered
			findplaceButton.setEnabled(s.length() > 0);
		}
	};
	
	private OnClickListener onFindPlace = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			execute(new SearchPlacesCommand(getRateBeerActivity().getApi(), placenameText.getText().toString()));
		}
	};

	protected void refreshFavourites() {
		execute(new GetFavouritePlacesCommand(getRateBeerActivity().getApi(), beerId));
	}

	private void clearSelection(ListView listView) {
		final int itemCount = listView.getCount();
		for (int i = 0; i < itemCount; ++i) {
			listView.setItemChecked(i, false);
		}
	}
	
	private OnClickListener onSkipClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// Just close this fragment
			getActivity().getSupportFragmentManager().popBackStackImmediate();
		}
	};
	
	private OnClickListener onAddAvailability = new OnClickListener() {
		@SuppressWarnings("deprecation")
		@Override
		public void onClick(View v) {
			
			long[] ids1, ids2;
			try {
				// Use a try {}, because the method name was changed in Android...
				// For Android 2.2+
				ids1 = favouritesView.getCheckedItemIds();
				ids2 = findresultsView.getCheckedItemIds();
			} catch (NoSuchMethodError e) {
				// For Android 2.1-
				ids1 = favouritesView.getCheckItemIds();
				ids2 = findresultsView.getCheckItemIds();
			}
			if (ids1.length + ids2.length <= 0) {
				publishException(null, getString(R.string.addav_noplacesselected));
				return;
			}
			
			
			// See which favourite places are selected
			int[] selectedFavourites = new int[ids1.length];
			for (int f = 0; f < ids1.length; f++) {
				selectedFavourites[f] = getFavouritesAdapter().getItem((int) ids1[f]).placeId;
			}
			
			// See if a place was searched for and selected
			String extraPlaceName = null;
			int extraPlaceId = AddAvailabilityCommand.NO_EXTRA_PLACE;
			//if (resPos != AdapterView.INVALID_POSITION) {
			if (ids2.length > 0) {
				extraPlaceName = getFindresutlsAdapter().getItem((int) ids2[0]).placeName;
				extraPlaceId = getFindresutlsAdapter().getItem((int) ids2[0]).placeId;
			}

			// Use the poster service to add the new availability info
			Intent i = new Intent(PosterService.ACTION_ADDAVAILABILITY);
			i.putExtra(PosterService.EXTRA_BEERID, beerId);
			i.putExtra(PosterService.EXTRA_BEERNAME, beerName);
			i.putExtra(PosterService.EXTRA_SELECTEDPLACES, selectedFavourites);
			i.putExtra(PosterService.EXTRA_EXTRAPLACENAME, extraPlaceName);
			i.putExtra(PosterService.EXTRA_EXTRAPLACEID, extraPlaceId);
			i.putExtra(PosterService.EXTRA_ONBOTTLECAN, onbottlecanCheck.isChecked());
			i.putExtra(PosterService.EXTRA_ONTAP, ontapCheck.isChecked());
			getActivity().startService(i);

			// Close this fragment
			getActivity().getSupportFragmentManager().popBackStackImmediate();
			
			
		}
	};

	@Override
	public void onTaskSuccessResult(CommandSuccessResult result) {
		if (result.getCommand().getMethod() == ApiMethod.GetFavouritePlaces) {
			publishFavourites(((GetFavouritePlacesCommand) result.getCommand()).getPlaces());
		} else if (result.getCommand().getMethod() == ApiMethod.SearchPlaces) {
			publishFindResults(((SearchPlacesCommand) result.getCommand()).getSearchResults());
		}
	}

	private void publishFavourites(ArrayList<PlaceSearchResult> result) {
		this.favourites = result;
		if (favouritesView.getAdapter() == null && result != null) {
			favouritesView.setAdapter(new CheckablePlacesAdapter(getActivity(), result));
		} else {
			clearSelection(favouritesView);
			((CheckablePlacesAdapter) favouritesView.getAdapter()).replace(result);
		}
	}

	private void publishFindResults(ArrayList<PlaceSearchResult> result) {
		this.findResults = result;
		if (findresultsView.getAdapter() == null && result != null) {
			findresultsView.setAdapter(new CheckablePlacesAdapter(getActivity(), result));
		} else {
			clearSelection(findresultsView);
			((CheckablePlacesAdapter) findresultsView.getAdapter()).replace(result);
		}
	}

	private CheckablePlacesAdapter getFavouritesAdapter() {
		return (CheckablePlacesAdapter) favouritesView.getAdapter();
	}

	private CheckablePlacesAdapter getFindresutlsAdapter() {
		return (CheckablePlacesAdapter) findresultsView.getAdapter();
	}

	@Override
	public void onTaskFailureResult(CommandFailureResult result) {
		publishException(null, result.getException());
	}
	
	private class CheckablePlacesAdapter extends ArrayAdapter<PlaceSearchResult> {

		public CheckablePlacesAdapter(Context context, List<PlaceSearchResult> objects) {
			super(context, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// Get the right view, using a ViewHolder
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.list_item_placeforavailability, null);
				holder = new ViewHolder();
				holder.placeName = (TextView) convertView.findViewById(R.id.placeName);
				holder.city = (TextView) convertView.findViewById(R.id.city);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			// Bind the data
			PlaceSearchResult item = getItem(position);
			holder.placeName.setText(item.placeName);
			holder.city.setText(item.city);
			
			return convertView;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}
	}

	protected static class ViewHolder {
		TextView placeName, city;
	}

	private class PlacesPagerAdapter extends PagerAdapter {

		private View pagerFavouritesView;
		private View pagerFindResultsView;
		private ListView pagerFavouritesList;
		private ListView pagerFindResultsList;
		private EditText pagerFindText;
		private Button pagerFindButton;

		public PlacesPagerAdapter() {
			LayoutInflater inflater = getActivity().getLayoutInflater();
			pagerFavouritesView = (View) inflater.inflate(R.layout.fragment_availability_favs, null);
			pagerFindResultsView = (View) inflater.inflate(R.layout.fragment_availability_other, null);
			pagerFavouritesList = (ListView) pagerFavouritesView.findViewById(R.id.favourites);
			pagerFindResultsList = (ListView) pagerFindResultsView.findViewById(R.id.findresults);
			pagerFindText = (EditText) pagerFindResultsView.findViewById(R.id.placename);
			pagerFindButton = (Button) pagerFindResultsView.findViewById(R.id.findplace);
		}

		public Button getFindButton() {
			return pagerFindButton;
		}

		public EditText getFindText() {
			return pagerFindText;
		}

		public ListView getFavouritesList() {
			return pagerFavouritesList;
		}

		public ListView getFindResultsList() {
			return pagerFindResultsList;
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return getActivity().getString(R.string.addav_favourites).toUpperCase();
			case 1:
				return getActivity().getString(R.string.addav_findaplace).toUpperCase();
			}
			return null;
		}

		@Override
		public Object instantiateItem(View container, int position) {
			switch (position) {
			case 0:
				((ViewPager) container).addView(pagerFavouritesView, 0);
				return pagerFavouritesView;
			case 1:
				((ViewPager) container).addView(pagerFindResultsView, 0);
				return pagerFindResultsView;
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
		public void finishUpdate(View container) {}

		@Override
		public Parcelable saveState() { return null; }

		@Override
		public void startUpdate(View container) {
		}

		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {}

	}

}
