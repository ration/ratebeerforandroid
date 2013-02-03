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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

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
import com.ratebeer.android.api.command.AddAvailabilityCommand;
import com.ratebeer.android.api.command.GetFavouritePlacesCommand;
import com.ratebeer.android.api.command.SearchPlacesCommand;
import com.ratebeer.android.api.command.SearchPlacesCommand.PlaceSearchResult;
import com.ratebeer.android.gui.components.PosterService;
import com.ratebeer.android.gui.components.RateBeerFragment;
import com.ratebeer.android.gui.components.helpers.ArrayAdapter;
import com.viewpagerindicator.TabPageIndicator;

@EFragment(R.layout.fragment_addavailability)
@OptionsMenu(R.menu.refresh)
public class AddAvailabilityFragment extends RateBeerFragment {

	@FragmentArg
	@InstanceState
	protected String beerName;
	@FragmentArg
	@InstanceState
	protected int beerId;
	@InstanceState
	protected ArrayList<PlaceSearchResult> favourites = null;
	@InstanceState
	protected ArrayList<PlaceSearchResult> findResults = null;

	@ViewById
	protected ViewPager pager;
	@ViewById
	protected TabPageIndicator titles;
	@ViewById(R.id.favourites)
	protected ListView favouritesView;
	@ViewById(R.id.findresults)
	protected ListView findresultsView;
	@ViewById(R.id.placename)
	protected EditText placenameText;
	@ViewById(R.id.findplace)
	protected Button findplaceButton;

	@ViewById
	protected CheckBox onbottlecan, ontap;
	@ViewById
	protected Button addavailability, skip;

	public AddAvailabilityFragment() {
	}

	@AfterViews
	public void init() {
		
		if (pager != null) {
			// Phone layout (using a pager to show the two lists)
			pager.setAdapter(new PlacesPagerAdapter());
			titles.setViewPager(pager);
		}
		favouritesView.setItemsCanFocus(false);
		findresultsView.setItemsCanFocus(false);
		placenameText.addTextChangedListener(onPlaceNameChanged);
		findplaceButton.setOnClickListener(onFindPlace);

		// The bottle/tap checks and buttons are on the bottom and never in a pager
		addavailability.setOnClickListener(onAddAvailability);
		skip.setOnClickListener(onSkipClicked);

		if (favourites != null && findResults != null) {
			publishFavourites(favourites);
			publishFindResults(findResults);
		} else {
			refreshFavourites();
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
			execute(new SearchPlacesCommand(getUser(), placenameText.getText().toString()));
		}
	};

	@OptionsItem(R.id.menu_refresh)
	protected void refreshFavourites() {
		execute(new GetFavouritePlacesCommand(getUser(), beerId));
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
		@TargetApi(8)
		@SuppressWarnings("deprecation")
		@Override
		public void onClick(View v) {
			
			long[] ids1, ids2;
			if (android.os.Build.VERSION.SDK_INT >= 8) {
				// Use a try {}, because the method name was changed in Android...
				// For Android 2.2+
				ids1 = favouritesView.getCheckedItemIds();
				ids2 = findresultsView.getCheckedItemIds();
				
			} else {
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
			i.putExtra(PosterService.EXTRA_ONBOTTLECAN, onbottlecan.isChecked());
			i.putExtra(PosterService.EXTRA_ONTAP, ontap.isChecked());
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
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_placeforavailability, null);
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

		public PlacesPagerAdapter() {
			pagerFavouritesView = (View) getActivity().getLayoutInflater().inflate(R.layout.fragment_availability_favs, null);
			pagerFindResultsView = (View) getActivity().getLayoutInflater().inflate(R.layout.fragment_availability_other, null);
			favouritesView = (ListView) pagerFavouritesView.findViewById(R.id.favourites);
			findresultsView = (ListView) pagerFindResultsView.findViewById(R.id.findresults);
			placenameText = (EditText) pagerFindResultsView.findViewById(R.id.placename);
			findplaceButton = (Button) pagerFindResultsView.findViewById(R.id.findplace);
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

	}

}
