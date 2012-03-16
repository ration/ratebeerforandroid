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
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.ratebeer.android.R;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.CommandFailureResult;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.api.command.SearchBeersCommand;
import com.ratebeer.android.api.command.SearchBeersCommand.BeerSearchResult;
import com.ratebeer.android.gui.components.ArrayAdapter;
import com.ratebeer.android.gui.components.PosterService;
import com.ratebeer.android.gui.components.RateBeerActivity;
import com.ratebeer.android.gui.components.RateBeerFragment;

public class AddUpcCodeFragment extends RateBeerFragment {

	private static final String STATE_UPCCODE = "upcCode";
	private static final String STATE_RESULTS = "results";

	private LayoutInflater inflater;
	private ListView resultsView;
	private EditText beernameText;
	private TextView emptyText, upccodeText;
	private Button findButton;

	protected String upcCode;
	private ArrayList<BeerSearchResult> results = null;

	public AddUpcCodeFragment() {
		this(null);
	}

	/**
	 * Allow adding of a UPC code to the corresponding beer by search for the beer name
	 * @param upcCode The UPC code to add
	 */
	public AddUpcCodeFragment(String upcCode) {
		this.upcCode = upcCode;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		this.inflater = inflater;
		return inflater.inflate(R.layout.fragment_addupccode, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		upccodeText = (TextView) getView().findViewById(R.id.upccode);
		emptyText = (TextView) getView().findViewById(R.id.empty);
		resultsView = (ListView) getView().findViewById(R.id.results);
		resultsView.setOnItemClickListener(onItemSelected);
		beernameText = (EditText) getView().findViewById(R.id.beername);
		beernameText.addTextChangedListener(onBeerNameChanged);
		findButton = (Button) getView().findViewById(R.id.findbeer);
		findButton.setOnClickListener(onFindClick);

		if (savedInstanceState != null) {
			upccodeText.setText(savedInstanceState.getString(STATE_UPCCODE));
			if (savedInstanceState.containsKey(STATE_RESULTS)) {
				results = savedInstanceState.getParcelableArrayList(STATE_RESULTS);
				publishResults(results);
			}
		} else {
			upccodeText.setText(upcCode);
			if (results != null) {
				publishResults(results);
			}
		}
		
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
		outState.putString(STATE_UPCCODE, upcCode);
		if (results != null) {
			outState.putParcelableArrayList(STATE_RESULTS, results);
		}
	}

	private TextWatcher onBeerNameChanged = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {}
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		@Override
		public void afterTextChanged(Editable s) {
			// Enable the find button only when a query is entered
			findButton.setEnabled(s.length() > 0);
		}
	};

	private void refreshResults() {
		// Search for beers with the custom specified name
		if (beernameText.getText().length() <= 0) {
			Toast.makeText(getActivity(), R.string.rate_offline_nonamegiven, Toast.LENGTH_LONG).show();
			return;
		}
		execute(new SearchBeersCommand(getRateBeerActivity().getApi(), beernameText.getText().toString(),
				getRateBeerActivity().getUser() != null ? getRateBeerActivity().getUser().getUserID()
						: SearchBeersCommand.NO_USER));
	}

	private OnClickListener onFindClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			refreshResults();
		}
	};

	private void addUpcCode(int beerId, String beerName, String upcCode) {

		// Use the poster service to add the new availability info
		Intent i = new Intent(PosterService.ACTION_ADDUPCCODE);
		i.putExtra(PosterService.EXTRA_BEERID, beerId);
		i.putExtra(PosterService.EXTRA_BEERNAME, beerName);
		i.putExtra(PosterService.EXTRA_UPCCODE, upcCode);
		getActivity().startService(i);

		// Close this fragment
		getSupportActivity().getSupportFragmentManager().popBackStackImmediate();
		
	}

	private OnItemClickListener onItemSelected = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if (getRateBeerActivity() == null) {
				return;
			}
			BeerSearchResult item = ((SearchResultsAdapter)resultsView.getAdapter()).getItem(position);
			addUpcCode(item.beerId, item.beerName, upcCode);
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
