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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.ratebeer.android.R;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import com.actionbarsherlock.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.CommandFailureResult;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.api.command.SearchBeersCommand;
import com.ratebeer.android.api.command.SearchBeersCommand.BeerSearchResult;
import com.ratebeer.android.app.persistance.OfflineRating;
import com.ratebeer.android.gui.components.ArrayAdapter;
import com.ratebeer.android.gui.components.RateBeerActivity;
import com.ratebeer.android.gui.components.RateBeerFragment;

public class FindRatedBeerFragment extends RateBeerFragment {

	private static final String STATE_OFFLINEID = "offlineId";
	private static final String STATE_RESULTS = "results";
	
	private LayoutInflater inflater;
	private TextView emptyText;
	private ListView resultsView;
	private EditText beername;
	private Button findbeer;

	private final int offlineId;
	private ArrayList<BeerSearchResult> results = null;

	public FindRatedBeerFragment() {
		this(-1);
	}
	
	public FindRatedBeerFragment(int offlineId) {
		this.offlineId = offlineId;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		this.inflater = inflater;
		return inflater.inflate(R.layout.fragment_findratedbeer, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		emptyText = (TextView) getView().findViewById(R.id.empty);
		resultsView = (ListView) getView().findViewById(R.id.findresults);
		resultsView.setOnItemClickListener(onItemSelected);
		beername = (EditText) getView().findViewById(R.id.beername);
		findbeer = (Button) getView().findViewById(R.id.findbeer);
		findbeer.setOnClickListener(onFindClick);

		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(STATE_RESULTS)) {
				ArrayList<BeerSearchResult> savedResults = savedInstanceState.getParcelableArrayList(STATE_RESULTS);
				publishResults(savedResults);
			}
		} else {
			try {

				// Get the offline rating's custom name and set it as default name search
				OfflineRating offline = getRateBeerActivity().getHelper().getOfflineRatingDao().queryForId(offlineId);
				if (offline == null) {
					// No offline rating found for this offline ID: cancel this screen
					cancelScreen();
					return;
				}
				beername.setText(offline.getBeerName());
				refreshResults();
				
			} catch (SQLException e) {
				cancelScreen();
				return;
			}
		}		

	}

	private void cancelScreen() {
		Toast.makeText(getActivity(), R.string.rate_offline_notavailable, Toast.LENGTH_LONG).show();
		getFragmentManager().popBackStack();
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
			refreshResults();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_OFFLINEID, offlineId);
		if (results != null) {
			outState.putParcelableArrayList(STATE_RESULTS, results);
		}
	}

	private void refreshResults() {
		// Search for beers with the custom specified name
		if (beername.getText().length() <= 0) {
			Toast.makeText(getActivity(), R.string.rate_offline_nonamegiven, Toast.LENGTH_LONG).show();
			return;
		}
		execute(new SearchBeersCommand(getRateBeerActivity().getApi(), beername.getText().toString(), 
				getRateBeerActivity().getUser().getUserID()));
	}

	private OnClickListener onFindClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			refreshResults();
		}
	};

	private OnItemClickListener onItemSelected = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			BeerSearchResult item = ((SearchResultsAdapter)resultsView.getAdapter()).getItem(position);
			
			try {
				if (getRateBeerActivity() == null) {
					cancelScreen();
				}
				// Update the stored offline rating with the found beer ID and close the screen
				OfflineRating offline = getRateBeerActivity().getHelper().getOfflineRatingDao().queryForId(offlineId);
				if (item.isRated) {
					Toast.makeText(getActivity(), R.string.rate_offline_alreadyrated, Toast.LENGTH_LONG).show();
				} else {
					offline.update(item.beerId, item.beerName);
					getRateBeerActivity().getHelper().getOfflineRatingDao().update(offline);
					getFragmentManager().popBackStack();
				}
			} catch (SQLException e) {
				cancelScreen();
			}
			
		}
	};
	
	@Override
	public void onTaskSuccessResult(CommandSuccessResult result) {
		if (result.getCommand().getMethod() == ApiMethod.SearchBeers) {
			publishResults(((SearchBeersCommand)result.getCommand()).getSearchResults());
		}
	}
	
	private void publishResults(ArrayList<BeerSearchResult> result) {
		this.results = result;
		//Collections.sort(result);
		if (resultsView.getAdapter() == null) {
			resultsView.setAdapter(new SearchResultsAdapter(getActivity(), result));
		} else {
			((SearchResultsAdapter)resultsView.getAdapter()).replace(result);
		}
		resultsView.setVisibility(result.size() == 0? View.GONE: View.VISIBLE);
		emptyText.setVisibility(result.size() == 0? View.VISIBLE: View.GONE);
	}

	@Override
	public void onTaskFailureResult(CommandFailureResult result) {
		publishException(emptyText, result.getException());
	}
	
	private class SearchResultsAdapter extends ArrayAdapter<BeerSearchResult> {

		public SearchResultsAdapter(Context context, List<BeerSearchResult> objects) {
			super(context, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// Get the right view, using a ViewHolder
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.list_item_beersearchresult, null);
				holder = new ViewHolder();
				holder.beer = (TextView) convertView.findViewById(R.id.beer);
				holder.overall = (TextView) convertView.findViewById(R.id.overall);
				holder.count = (TextView) convertView.findViewById(R.id.count);
				holder.rated = (TextView) convertView.findViewById(R.id.rated);
				holder.retired = (TextView) convertView.findViewById(R.id.retired);
				holder.alias = (TextView) convertView.findViewById(R.id.alias);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			// Bind the data
			BeerSearchResult item = getItem(position);
			if (getActivity() != null) {
				holder.beer.setText(item.beerName);
				holder.overall.setText((item.overallPerc >= 0? Integer.toString(item.overallPerc): "?"));
				holder.count.setText(Integer.toString(item.rateCount) + " " + getString(R.string.details_ratings));
				holder.rated.setVisibility(item.isRated? View.VISIBLE: View.GONE);
				holder.retired.setVisibility(item.isRetired? View.VISIBLE: View.GONE);
				holder.alias.setVisibility(item.isAlias? View.VISIBLE: View.GONE);
			}
			
			return convertView;
		}
		
	}

	protected static class ViewHolder {
		TextView beer, overall, count, rated, retired, alias;
	}

}
