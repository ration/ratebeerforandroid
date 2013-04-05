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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
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
import com.ratebeer.android.api.command.SearchBeersCommand;
import com.ratebeer.android.api.command.SearchBeersCommand.BeerSearchResult;
import com.ratebeer.android.gui.components.PosterService;
import com.ratebeer.android.gui.components.RateBeerFragment;
import com.ratebeer.android.gui.components.helpers.ArrayAdapter;

import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;

@EFragment(R.layout.fragment_addupccode)
@OptionsMenu(R.menu.refresh)
public class AddUpcCodeFragment extends RateBeerFragment {

	@FragmentArg
	@InstanceState
	protected String upcCode;
	@InstanceState
	protected ArrayList<BeerSearchResult> results = null;

	@ViewById(R.id.results)
	protected ListView resultsView;
	@ViewById
	protected EditText beername;
	@ViewById
	protected TextView empty;
	@ViewById
	protected TextView upccode;
	@ViewById(R.id.findbeer)
	protected Button findButton;

	public AddUpcCodeFragment() {
	}
	
	@AfterViews
	public void init() {

		resultsView.setOnItemClickListener(onItemSelected);
		beername.addTextChangedListener(onBeerNameChanged);
		findButton.setOnClickListener(onFindClick);

		if (results != null) {
			publishResults(results);
		} else {
			upccode.setText(upcCode);
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

	@OptionsItem(R.id.menu_refresh)
	protected void refreshResults() {
		// Search for beers with the custom specified name
		if (beername.getText().length() <= 0) {
			Crouton.makeText(getActivity(), R.string.rate_offline_nonamegiven, Style.INFO).show();
			return;
		}
		execute(new SearchBeersCommand(getUser(), beername.getText().toString(), getUser() != null ? getUser()
				.getUserID() : SearchBeersCommand.NO_USER));
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

	}

	private OnItemClickListener onItemSelected = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if (getActivity() == null) {
				return;
			}
			BeerSearchResult item = ((SearchResultsAdapter)resultsView.getAdapter()).getItem(position);
			addUpcCode(item.beerId, item.beerName, upcCode);
			// Also redirect to the beer in question
			load(BeerViewFragment_.builder().beerId(item.beerId).beerName(item.beerName).build());
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
		if (resultsView.getAdapter() == null) {
			resultsView.setAdapter(new SearchResultsAdapter(getActivity(), result));
		} else {
			((SearchResultsAdapter)resultsView.getAdapter()).replace(result);
		}
		resultsView.setVisibility(result.size() == 0? View.GONE: View.VISIBLE);
		empty.setVisibility(result.size() == 0? View.VISIBLE: View.GONE);
	}

	@Override
	public void onTaskFailureResult(CommandFailureResult result) {
		publishException(empty, result.getException());
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
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_beersearchresult, null);
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
			holder.overall.setText((item.overallPerc >= 0? String.format("%.0f", item.overallPerc): "?"));
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
