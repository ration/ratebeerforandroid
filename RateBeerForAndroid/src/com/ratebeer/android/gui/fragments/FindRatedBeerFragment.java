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

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.InstanceState;
import com.googlecode.androidannotations.annotations.OrmLiteDao;
import com.googlecode.androidannotations.annotations.ViewById;
import com.j256.ormlite.dao.Dao;
import com.ratebeer.android.R;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.CommandFailureResult;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.api.command.SearchBeersCommand;
import com.ratebeer.android.api.command.SearchBeersCommand.BeerSearchResult;
import com.ratebeer.android.app.persistance.DatabaseHelper;
import com.ratebeer.android.app.persistance.OfflineRating;
import com.ratebeer.android.gui.components.ArrayAdapter;
import com.ratebeer.android.gui.components.RateBeerActivity;
import com.ratebeer.android.gui.components.RateBeerFragment;

import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;

@EFragment(R.layout.fragment_findratedbeer)
public class FindRatedBeerFragment extends RateBeerFragment {

	@FragmentArg
	@InstanceState
	protected int offlineId = -1;
	@InstanceState
	protected ArrayList<BeerSearchResult> results = null;
	
	@ViewById(R.id.empty)
	protected TextView emptyText;
	@ViewById(R.id.findresults)
	protected ListView resultsView;
	@ViewById
	protected EditText beername;
	protected Button findbeer;

	@OrmLiteDao(helper = DatabaseHelper.class, model = OfflineRating.class)
	Dao<OfflineRating, Integer> offlineRatingDao;
	
	public FindRatedBeerFragment() {
	}

	@AfterViews
	public void init() {

		resultsView.setOnItemClickListener(onItemSelected);
		findbeer.setOnClickListener(onFindClick);

		if (results != null) {
			publishResults(results);
		} else {
			try {

				// Get the offline rating's custom name and set it as default name search
				OfflineRating offline = offlineRatingDao.queryForId(offlineId);
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
		Crouton.makeText(getActivity(), R.string.rate_offline_notavailable, Style.ALERT).show();
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

	private void refreshResults() {
		// Search for beers with the custom specified name
		if (beername.getText().length() <= 0) {
			Crouton.makeText(getActivity(), R.string.rate_offline_nonamegiven, Style.INFO).show();
			return;
		}
		execute(new SearchBeersCommand(getUser(), beername.getText().toString(), getUser().getUserID()));
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
				if (getActivity() == null) {
					cancelScreen();
				}
				// Update the stored offline rating with the found beer ID and close the screen
				OfflineRating offline = offlineRatingDao.queryForId(offlineId);
				if (item.isRated) {
					Crouton.makeText(getActivity(), R.string.rate_offline_alreadyrated, Style.ALERT).show();
				} else {
					offline.update(item.beerId, item.beerName);
					offlineRatingDao.update(offline);
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

			if (getActivity() != null) {
				return convertView;
			}

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
