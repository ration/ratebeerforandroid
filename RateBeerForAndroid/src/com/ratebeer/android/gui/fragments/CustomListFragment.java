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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.InstanceState;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.OrmLiteDao;
import com.googlecode.androidannotations.annotations.ViewById;
import com.j256.ormlite.dao.Dao;
import com.ratebeer.android.R;
import com.ratebeer.android.app.persistance.CustomListBeer;
import com.ratebeer.android.app.persistance.DatabaseHelper;
import com.ratebeer.android.gui.components.RateBeerFragment;
import com.ratebeer.android.gui.components.helpers.ArrayAdapter;
import com.ratebeer.android.gui.fragments.ConfirmDialogFragment.OnDialogResult;
import com.ratebeer.android.gui.fragments.CustomListFragment.CustomListBeerComparator.SortBy;

import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;

@EFragment(R.layout.fragment_customlist)
@OptionsMenu(R.menu.customlist)
public class CustomListFragment extends RateBeerFragment {

	private static final int MENU_REMOVE = 10;

	@FragmentArg
	@InstanceState
	protected int customListId;
	@ViewById(R.id.empty)
	protected TextView emptyText;
	@ViewById(R.id.beers)
	protected ListView beersView;
	
	@OrmLiteDao(helper = DatabaseHelper.class, model = CustomListBeer.class)
	Dao<CustomListBeer, Long> customListBeerDao;

	private List<CustomListBeer> beers;
	private SortBy sortBy = SortBy.TimeSaved;
	
	public CustomListFragment() {
	}

	@AfterViews
	public void init() {

		beersView.setOnItemClickListener(onItemSelected);
		registerForContextMenu(beersView);
		loadBeers();
		
	}
	
	@OptionsItem(R.id.menu_addbeer)
	protected void onAddBeer() {
		load(AddBeerToCustomListFragment_.builder().customListId(customListId).build());
	}

	@OptionsItem(R.id.menu_sortby)
	protected void onSort() {
		new CustomListBeersSortDialog(this).show(getActivity().getSupportFragmentManager(), null);
	}
	
	private void loadBeers() {
		
		// Get custom lists from database
		try {
			beers = customListBeerDao.queryForAll();
		} catch (SQLException e) {
			// Not available!
			publishException(emptyText, getString(R.string.custom_notavailable));
			return;
		}
		publishBeers();
		
	}

	private void publishBeers() {
		Collections.sort(beers, new CustomListBeerComparator(sortBy));
		if (beersView.getAdapter() == null) {
			beersView.setAdapter(new CustomListBeersAdapter(getActivity(), beers));
		} else {
			((CustomListBeersAdapter) beersView.getAdapter()).replace(beers);
		}
		beersView.setVisibility(beers.size() == 0 ? View.GONE : View.VISIBLE);
		emptyText.setVisibility(beers.size() == 0 ? View.VISIBLE : View.GONE);
	}

	private OnItemClickListener onItemSelected = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			CustomListBeer item = ((CustomListBeersAdapter)beersView.getAdapter()).getItem(position);
			load(BeerViewFragment_.builder().beerId(item.getBeerId()).beerName(item.getBeerName()).build());
		}
	};

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		menu.add(Menu.NONE, MENU_REMOVE, MENU_REMOVE, R.string.custom_removebeer);
	}
	
	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) item.getMenuInfo();
		final CustomListBeer beer = (CustomListBeer) beersView.getItemAtPosition(acmi.position);
		new ConfirmDialogFragment(new OnDialogResult() {
			@Override
			public void onConfirmed() {
				removeBeer(beer);
				loadBeers();
			}
		}, R.string.custom_confirmremovebeer, beer.getBeerName()).show(getFragmentManager(), "dialog");
		return super.onContextItemSelected(item);
	}

	protected void removeBeer(CustomListBeer beer) {
		try {
			customListBeerDao.delete(beer);
		} catch (SQLException e) {
			Crouton.makeText(getActivity(), R.string.custom_notavailable, Style.ALERT).show();
		}
	}

	public void setSortOrder(SortBy sortBy) {
		this.sortBy = sortBy;
		publishBeers();
	}
	
	private class CustomListBeersAdapter extends ArrayAdapter<CustomListBeer> {

		public CustomListBeersAdapter(Context context, List<CustomListBeer> result) {
			super(context, result);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// Get the right view, using a ViewHolder
			ViewHolder holder;
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_customlistbeer, null);
				holder = new ViewHolder();
				holder.beer = (TextView) convertView.findViewById(R.id.beer);
				holder.count = (TextView) convertView.findViewById(R.id.count);
				holder.overall = (TextView) convertView.findViewById(R.id.overall);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			// Bind the data
			CustomListBeer item = getItem(position);
			holder.beer.setText(item.getBeerName());
			holder.count.setText(Integer.toString(item.getCount()) + " " + getString(R.string.details_ratings));
			holder.overall.setText((item.getRating() >= 0 ? String.format("%.0f", item.getRating()) : "?"));

			return convertView;
		}

	}

	public static class CustomListBeerComparator implements Comparator<CustomListBeer> {

		public enum SortBy {
			Name, RateCount, Score, TimeSaved
		}

		private final SortBy sortBy;

		public CustomListBeerComparator(SortBy sortBy) {
			this.sortBy = sortBy;
		}

		@Override
		public int compare(CustomListBeer lhs, CustomListBeer rhs) {
			switch (sortBy) {
			case Name:
				return lhs.getBeerName().compareTo(rhs.getBeerName());
			case RateCount:
				return -Double.compare(lhs.getCount(), rhs.getCount());
			case Score:
				return -Double.compare(lhs.getRating(), rhs.getRating());
			default:
				return -lhs.getTimeSaved().compareTo(rhs.getTimeSaved());
			}
		}

	}
	protected static class ViewHolder {
		TextView beer, count, overall;
	}

}
