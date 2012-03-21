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
import android.os.Bundle;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

import com.ratebeer.android.R;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.CommandFailureResult;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.api.command.Country;
import com.ratebeer.android.api.command.GetTopBeersCommand;
import com.ratebeer.android.api.command.GetTopBeersCommand.TopBeer;
import com.ratebeer.android.api.command.GetTopBeersCommand.TopListType;
import com.ratebeer.android.gui.components.ArrayAdapter;
import com.ratebeer.android.gui.components.RateBeerActivity;
import com.ratebeer.android.gui.components.RateBeerFragment;

public class TopBeersFragment extends RateBeerFragment {

	private static final String DECIMAL_FORMATTER = "%.1f";
	private static final String STATE_TOPLIST = "topList";
	private static final String STATE_COUNTRY = "country";
	private static final String STATE_BEERS = "beers";
	
	private LayoutInflater inflater;
	private TextView emptyText;
	private ListView beersView;
	private TextView countryLabel;
	private Spinner countrySpinner;

	private TopListType topList;
	private Country country = null;
	private ArrayList<TopBeer> beers;

	public TopBeersFragment() {
		this(TopListType.Top50);
	}

	public TopBeersFragment(TopListType topList) {
		this.topList = topList;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		this.inflater = inflater;
		return inflater.inflate(R.layout.fragment_topbeers, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		emptyText = (TextView) getView().findViewById(R.id.empty);
		beersView = (ListView) getView().findViewById(R.id.beers);
		countryLabel = (TextView) getView().findViewById(R.id.countrylabel);
		countrySpinner = (Spinner) getView().findViewById(R.id.country);
		countrySpinner.setOnItemSelectedListener(onCountrySelected);
		beersView.setOnItemClickListener(onItemSelected);

		if (savedInstanceState != null) {
			topList = TopListType.valueOf(savedInstanceState.getString(STATE_TOPLIST));
			if (topList != TopListType.TopByCountry) {
				countryLabel.setVisibility(View.GONE);
				countrySpinner.setVisibility(View.GONE);
			}
			if (savedInstanceState.containsKey(STATE_COUNTRY)) {
				country = Country.ALL_COUNTRIES.get(savedInstanceState.getInt(STATE_COUNTRY));
			}
			populateCountrySpinner();
			if (savedInstanceState.containsKey(STATE_BEERS)) {
				beers = savedInstanceState.getParcelableArrayList(STATE_BEERS);
			}
		} else {
			if (topList == TopListType.TopByCountry) {
				populateCountrySpinner();
			} else {
				countryLabel.setVisibility(View.GONE);
				countrySpinner.setVisibility(View.GONE);
			}
			refreshBeers();
		}
		// Publish the current details, even when it is not loaded yet (and thus still empty)
		publishResults(beers);
		
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
			refreshBeers();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void populateCountrySpinner() {
		// Get an array with all the country names
		Country[] allCountries = Country.ALL_COUNTRIES.values().toArray(new Country[Country.ALL_COUNTRIES.size()]);
		android.widget.ArrayAdapter<Country> adapter = new android.widget.ArrayAdapter<Country>(getActivity(),
			android.R.layout.simple_spinner_item, allCountries);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		countrySpinner.setAdapter(adapter);
		// Select the last used country, if known
		if (getRateBeerActivity().getSettings().getLastUsedCountry() != null) {
			for (int j = 0; j < allCountries.length; j++) {
				if (allCountries[j].getId() == getRateBeerActivity().getSettings().getLastUsedCountry().getId()) {
					countrySpinner.setSelection(j);
					break;
				}
			}
		}
	}

	private OnItemSelectedListener onCountrySelected = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			// Some country was selected; load the new top beers list
			country = (Country) countrySpinner.getSelectedItem();
			getRateBeerActivity().getSettings().saveLastUsedCountry(country);
			refreshBeers();
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_TOPLIST, topList.name());
		if (country != null) {
			outState.putInt(STATE_COUNTRY, (int) countrySpinner.getSelectedItemId());
		}
		if (beers != null) {
			outState.putParcelableArrayList(STATE_BEERS, beers);
		}
	}

	private void refreshBeers() {
		switch (topList) {
		case Top50:
			execute(new GetTopBeersCommand(getRateBeerActivity().getApi()));
			break;
		case TopByCountry:
			if (country != null) {
				execute(new GetTopBeersCommand(getRateBeerActivity().getApi(), country));
			}
			break;
		}
	}

	private OnItemClickListener onItemSelected = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			TopBeer item = ((TopBeersAdapter) beersView.getAdapter()).getItem(position);
			getRateBeerActivity().load(new BeerViewFragment(item.beerName, item.beerId));
		}
	};

	@Override
	public void onTaskSuccessResult(CommandSuccessResult result) {
		if (result.getCommand().getMethod() == ApiMethod.GetTopBeers) {
			publishResults(((GetTopBeersCommand) result.getCommand()).getBeers());
		}
	}

	private void publishResults(ArrayList<TopBeer> result) {
		this.beers = result;
		if (beers == null) {
			beersView.setVisibility(View.GONE);
			emptyText.setVisibility(View.GONE);
			return;
		}
		if (beersView.getAdapter() == null) {
			beersView.setAdapter(new TopBeersAdapter(getActivity(), result));
		} else {
			((TopBeersAdapter) beersView.getAdapter()).replace(result);
		}
		beersView.setVisibility(result.size() == 0 ? View.GONE : View.VISIBLE);
		emptyText.setVisibility(result.size() == 0 ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onTaskFailureResult(CommandFailureResult result) {
		publishException(emptyText, result.getException());
	}

	private class TopBeersAdapter extends ArrayAdapter<TopBeer> {

		public TopBeersAdapter(Context context, List<TopBeer> objects) {
			super(context, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (getRateBeerActivity() == null) {
				return null;
			}
			
			// Get the right view, using a ViewHolder
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.list_item_topbeer, null);
				holder = new ViewHolder();
				holder.order = (TextView) convertView.findViewById(R.id.order);
				holder.beer = (TextView) convertView.findViewById(R.id.beer);
				holder.style = (TextView) convertView.findViewById(R.id.style);
				holder.score = (TextView) convertView.findViewById(R.id.score);
				holder.count = (TextView) convertView.findViewById(R.id.count);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			// Bind the data
			TopBeer item = getItem(position);
			if (getActivity() != null) {
				holder.order.setText(Integer.toString(item.orderNr));
				holder.beer.setText(item.beerName);
				holder.style.setText(item.styleName);
				holder.score.setText(String.format(DECIMAL_FORMATTER, item.score));
				holder.count.setText(Integer.toString(item.rateCount) + " " + getString(R.string.details_ratings));
			}

			return convertView;
		}

	}

	protected static class ViewHolder {
		TextView order, beer, style, score, count;
	}

}
