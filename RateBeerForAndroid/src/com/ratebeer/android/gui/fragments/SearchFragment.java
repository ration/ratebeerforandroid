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
import android.provider.SearchRecentSuggestions;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.ratebeer.android.R;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.CommandFailureResult;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.api.command.SearchCommand;
import com.ratebeer.android.api.command.SearchCommand.SearchResult;
import com.ratebeer.android.gui.SearchHistoryProvider;
import com.ratebeer.android.gui.components.ArrayAdapter;
import com.ratebeer.android.gui.components.RateBeerActivity;
import com.ratebeer.android.gui.components.RateBeerFragment;

public class SearchFragment extends RateBeerFragment {

	private static final String STATE_QUERY = "lastQuery";
	private static final String STATE_RESULTS = "results";
	public static final String ARG_QUERY = "query";
	private static final int MENU_CLEARHISTORY = 0;

	private TextView emptyText;
	private ListView beersView;
	private LayoutInflater inflater;

	private String lastQuery = null;
	private ArrayList<SearchResult> results = null;

	public SearchFragment() {
		this(null);
	}
	
	public SearchFragment(String query) {
		this.lastQuery = query;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			performSearch();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		this.inflater = inflater;
		return inflater.inflate(R.layout.fragment_search, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		emptyText = (TextView) getView().findViewById(R.id.empty);
		beersView = (ListView) getView().findViewById(R.id.results);
		beersView.setOnItemClickListener(onItemSelected);

		if (savedInstanceState != null) {
			lastQuery = savedInstanceState.getString(STATE_QUERY);
			if (savedInstanceState.containsKey(STATE_RESULTS)) {
				ArrayList<SearchResult> savedResults = savedInstanceState.getParcelableArrayList(STATE_RESULTS);
				publishResults(savedResults);
			}
		} else {
			if (lastQuery != null) {
				// Store query in search history
				SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity()	, 
					SearchHistoryProvider.AUTHORITY, SearchHistoryProvider.MODE);
	        	suggestions.saveRecentQuery(lastQuery, null);			
			}
			performSearch();
		}
		
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		MenuItem item = menu.add(RateBeerActivity.MENU_REFRESH, RateBeerActivity.MENU_REFRESH, RateBeerActivity.MENU_REFRESH, R.string.app_refresh);
		item.setIcon(R.drawable.ic_action_refresh);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		menu.add(MENU_CLEARHISTORY, MENU_CLEARHISTORY, MENU_CLEARHISTORY, R.string.search_clearhistory);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(android.view.MenuItem item) {
		switch (item.getItemId()) {
		case RateBeerActivity.MENU_REFRESH:
			performSearch();
			break;
		case MENU_CLEARHISTORY:
			SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity()	, 
					SearchHistoryProvider.AUTHORITY, SearchHistoryProvider.MODE);
			suggestions.clearHistory();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_QUERY, lastQuery);
		if (results != null) {
			outState.putParcelableArrayList(STATE_RESULTS, results);
		}
	}

	private void performSearch() {
		if (lastQuery == null && beersView.getAdapter() != null) {
			((SearchResultsAdapter)beersView.getAdapter()).clear();
		} else if (lastQuery != null) {
			execute(new SearchCommand(getRateBeerActivity().getApi(), lastQuery, 
					getRateBeerActivity().getUser() != null? getRateBeerActivity().getUser().getUserID(): 
						SearchCommand.NO_USER));
		}
	}

	private OnItemClickListener onItemSelected = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			SearchResult item = ((SearchResultsAdapter)beersView.getAdapter()).getItem(position);
			getRateBeerActivity().load(new BeerViewFragment(item.beerName, item.beerId, item.rateCount));
		}
	};
	
	@Override
	public void onTaskSuccessResult(CommandSuccessResult result) {
		if (result.getCommand().getMethod() == ApiMethod.Search) {
			publishResults(((SearchCommand)result.getCommand()).getSearchResults());
		}
	}
	
	private void publishResults(ArrayList<SearchResult> result) {
		this.results = result;
		//Collections.sort(result);
		if (beersView.getAdapter() == null) {
			beersView.setAdapter(new SearchResultsAdapter(getActivity(), result));
		} else {
			((SearchResultsAdapter)beersView.getAdapter()).replace(result);
		}
		beersView.setVisibility(result.size() == 0? View.GONE: View.VISIBLE);
		emptyText.setVisibility(result.size() == 0? View.VISIBLE: View.GONE);
	}

	@Override
	public void onTaskFailureResult(CommandFailureResult result) {
		publishException(emptyText, result.getException());
	}
	
	private class SearchResultsAdapter extends ArrayAdapter<SearchResult> {

		public SearchResultsAdapter(Context context, List<SearchResult> objects) {
			super(context, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// Get the right view, using a ViewHolder
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.list_item_searchresult, null);
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
			SearchResult item = getItem(position);
			holder.beer.setText(item.beerName);
			holder.overall.setText((item.overallPerc >= 0? Integer.toString(item.overallPerc): "?"));
			holder.count.setText(Integer.toString(item.rateCount) + " " + getString(R.string.details_ratings));
			holder.rated.setVisibility(item.isRated? View.VISIBLE: View.GONE);
			holder.retired.setVisibility(item.isRetired? View.VISIBLE: View.GONE);
			holder.alias.setVisibility(item.isAlias? View.VISIBLE: View.GONE);
			
			return convertView;
		}
		
	}

	protected static class ViewHolder {
		TextView beer, overall, count, rated, retired, alias;
	}

}
