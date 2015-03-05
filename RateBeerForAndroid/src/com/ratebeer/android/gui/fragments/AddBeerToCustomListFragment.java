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
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.InstanceState;
import com.googlecode.androidannotations.annotations.ItemClick;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.OrmLiteDao;
import com.googlecode.androidannotations.annotations.ViewById;
import com.j256.ormlite.dao.Dao;
import com.ratebeer.android.R;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.CommandFailureResult;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.api.command.SearchBeersCommand;
import com.ratebeer.android.api.command.SearchBeersCommand.BeerSearchResult;
import com.ratebeer.android.app.persistance.CustomListBeer;
import com.ratebeer.android.app.persistance.DatabaseHelper;
import com.ratebeer.android.gui.components.RateBeerFragment;
import com.ratebeer.android.gui.components.helpers.ArrayAdapter;

import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;

@EFragment(R.layout.fragment_findratedbeer)
@OptionsMenu(R.menu.refresh)
public class AddBeerToCustomListFragment extends RateBeerFragment {

	@FragmentArg
	@InstanceState
	protected int customListId = -1;
	@InstanceState
	protected ArrayList<BeerSearchResult> results = null;

	@ViewById(R.id.empty)
	protected TextView emptyText;
	@ViewById(R.id.findresults)
	protected ListView resultsView;
	@ViewById
	protected EditText beername;

	@OrmLiteDao(helper = DatabaseHelper.class, model = CustomListBeer.class)
	Dao<CustomListBeer, Integer> customListBeerDao;

	public AddBeerToCustomListFragment() {
	}

	@AfterViews
	public void init() {
		if (results != null) {
			publishResults(results);
		}
	}

	@OptionsItem(R.id.menu_refresh)
	protected void refreshResults() {
		// Search for beers with the custom specified name
		if (beername.getText().length() <= 0) {
			Crouton.makeText(getActivity(), R.string.custom_nonamegiven, Style.INFO).show();
			return;
		}
		execute(new SearchBeersCommand(getUser(), beername.getText().toString(), getUser().getUserID()));
	}

	@Click
	protected void findbeerClicked() {
		refreshResults();
	}

	@ItemClick(R.id.findresults)
	protected void onResultSelected(BeerSearchResult item) {
		try {
			if (getActivity() == null) {
				getFragmentManager().popBackStack();
				return;
			}
			// Add the clicked beer to the custom list and close the screen
			CustomListBeer beer = new CustomListBeer(customListId, item.beerId, item.beerName, item.overallPerc,
					item.rateCount);
			customListBeerDao.create(beer);
			getFragmentManager().popBackStack();
		} catch (SQLException e) {
			// No action
		}
	}

	@Override
	public void onTaskSuccessResult(CommandSuccessResult result) {
		if (result.getCommand().getMethod() == ApiMethod.SearchBeers) {
			publishResults(((SearchBeersCommand) result.getCommand()).getSearchResults());
		}
	}

	private void publishResults(ArrayList<BeerSearchResult> result) {
		this.results = result;
		if (resultsView.getAdapter() == null) {
			resultsView.setAdapter(new SearchResultsAdapter(getActivity(), result));
		} else {
			((SearchResultsAdapter) resultsView.getAdapter()).replace(result);
		}
		resultsView.setVisibility(result.size() == 0 ? View.GONE : View.VISIBLE);
		emptyText.setVisibility(result.size() == 0 ? View.VISIBLE : View.GONE);
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
			holder.overall.setText((item.overallPerc >= 0 ? String.format("%.0f", item.overallPerc) : "?"));
			holder.count.setText(Integer.toString(item.rateCount) + " " + getString(R.string.details_ratings));
			holder.rated.setVisibility(item.isRated ? View.VISIBLE : View.GONE);
			holder.retired.setVisibility(item.isRetired ? View.VISIBLE : View.GONE);
			holder.alias.setVisibility(item.isAlias ? View.VISIBLE : View.GONE);

			return convertView;
		}

	}

	protected static class ViewHolder {
		TextView beer, overall, count, rated, retired, alias;
	}

}
