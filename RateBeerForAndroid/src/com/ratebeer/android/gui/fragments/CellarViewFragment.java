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
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.InstanceState;
import com.googlecode.androidannotations.annotations.ViewById;
import com.ratebeer.android.R;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.CommandFailureResult;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.api.command.GetUserCellarCommand;
import com.ratebeer.android.api.command.GetUserCellarCommand.CellarBeer;
import com.ratebeer.android.api.command.RemoveFromCellarCommand;
import com.ratebeer.android.gui.components.RateBeerActivity;
import com.ratebeer.android.gui.components.RateBeerFragment;
import com.ratebeer.android.gui.components.helpers.ArrayAdapter;
import com.ratebeer.android.gui.fragments.ConfirmDialogFragment.OnDialogResult;
import com.viewpagerindicator.TabPageIndicator;

@EFragment(R.layout.fragment_cellarview)
public class CellarViewFragment extends RateBeerFragment {
	
	private static final int MENU_REMOVE = 10;

	@FragmentArg
	@InstanceState
	protected int userId;
	@InstanceState
	protected ArrayList<CellarBeer> wants = null;
	@InstanceState
	protected ArrayList<CellarBeer> haves = null;

	@ViewById
	protected ViewPager pager;
	@ViewById(R.id.wantsview)
	protected ListView wantsView;
	@ViewById(R.id.havesview)
	protected ListView havesView;
	@ViewById
	protected TabPageIndicator titles;

	private ListAdapter lastUsedListView = null;

	public CellarViewFragment() {
	}

	@AfterViews
	public void init() {

		if (pager != null) {
			// Phone layout (using a pager to show the two lists): we still need to load the layout
			pager.setAdapter(new CellarPagerAdapter());
			titles.setViewPager(pager);
		}
		wantsView.setOnItemClickListener(onCellarBeerClick);
		havesView.setOnItemClickListener(onCellarBeerClick);
		registerForContextMenu(wantsView);
		registerForContextMenu(havesView);

		if (wants != null && haves != null) {
			publishCellar(wants, haves);
		} else {
			refreshCellar();
		}

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		MenuItem item = menu.add(RateBeerActivity.MENU_REFRESH, RateBeerActivity.MENU_REFRESH,
				RateBeerActivity.MENU_REFRESH, R.string.app_refresh);
		item.setIcon(R.drawable.ic_action_refresh);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case RateBeerActivity.MENU_REFRESH:
			refreshCellar();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		menu.add(Menu.NONE, MENU_REMOVE, MENU_REMOVE, R.string.cellar_remove);
		lastUsedListView = ((AdapterView<ListAdapter>)v).getAdapter();
	}
	
	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) item.getMenuInfo();
		final CellarBeer beer = (CellarBeer) lastUsedListView.getItem(acmi.position);
		new ConfirmDialogFragment(new OnDialogResult() {
			@Override
			public void onConfirmed() {
				removeBeerFromCellar(beer);
			}
		}, R.string.cellar_confirmremoval, beer.beerName).show(getFragmentManager(), "dialog");
		return super.onContextItemSelected(item);
	}

	private void refreshCellar() {
		execute(new GetUserCellarCommand(getUser(), userId));
	}

	protected void removeBeerFromCellar(CellarBeer beer) {
		execute(new RemoveFromCellarCommand(getUser(), beer.beerId));
	}

	private OnItemClickListener onCellarBeerClick = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			CellarBeer item = ((CellarAdapter) parent.getAdapter()).getItem(position);
			load(BeerViewFragment_.builder().beerId(item.beerId).beerName(item.beerName).build());
		}
	};

	@Override
	public void onTaskSuccessResult(CommandSuccessResult result) {
		if (result.getCommand().getMethod() == ApiMethod.GetUserCellar) {
			GetUserCellarCommand ucc = (GetUserCellarCommand) result.getCommand();
			publishCellar(ucc.getWants(), ucc.getHaves());
		} else if (result.getCommand().getMethod() == ApiMethod.RemoveFromCellar) {
			// NOTE: No feedback, so we assume that the removal was actually successful and request a refresh
			refreshCellar();
		}
	}

	private void publishCellar(ArrayList<CellarBeer> wants, ArrayList<CellarBeer> haves) {
		this.wants = wants;
		this.haves = haves;
		// Updated the list view adapters (which might be wrapped in a CellarPagerAdapter)
		if (wantsView.getAdapter() == null) {
			wantsView.setAdapter(new CellarAdapter(getActivity(), wants));
		} else {
			((CellarAdapter) wantsView.getAdapter()).replace(wants);
		}
		if (havesView.getAdapter() == null) {
			havesView.setAdapter(new CellarAdapter(getActivity(), haves));
		} else {
			((CellarAdapter) havesView.getAdapter()).replace(haves);
		}
	}

	@Override
	public void onTaskFailureResult(CommandFailureResult result) {
		publishException(null, result.getException());
	}

	private class CellarAdapter extends ArrayAdapter<CellarBeer> {

		public CellarAdapter(Context context, List<CellarBeer> objects) {
			super(context, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// Get the right view, using a ViewHolder
			ViewHolder holder;
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_cellaritem, null);
				holder = new ViewHolder();
				holder.beer = (TextView) convertView.findViewById(R.id.beer);
				holder.memo = (TextView) convertView.findViewById(R.id.memo);
				holder.quantity = (TextView) convertView.findViewById(R.id.quantity);
				holder.vintage = (TextView) convertView.findViewById(R.id.vintage);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			// Bind the data
			CellarBeer item = getItem(position);
			holder.beer.setText(item.beerName);
			holder.memo.setText(item.memo);
			holder.memo.setVisibility(item.memo == null || item.memo.equals("")? View.GONE : View.VISIBLE);
			holder.quantity.setText(item.quantity);
			holder.quantity.setVisibility(item.quantity == null || item.quantity.equals("")? View.GONE : View.VISIBLE);
			holder.vintage.setText(item.vintage);
			holder.vintage.setVisibility(item.vintage == null || item.vintage.equals("")? View.GONE : View.VISIBLE);

			return convertView;
		}

	}

	protected static class ViewHolder {
		TextView beer, memo, quantity, vintage;
	}

	private class CellarPagerAdapter extends PagerAdapter {

		public CellarPagerAdapter() {
			LayoutInflater inflater = getActivity().getLayoutInflater();
			wantsView = (ListView) inflater.inflate(R.layout.fragment_pagerlist, null);
			havesView = (ListView) inflater.inflate(R.layout.fragment_pagerlist, null);
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return getActivity().getString(R.string.cellar_wants).toUpperCase();
			case 1:
				return getActivity().getString(R.string.cellar_haves).toUpperCase();
			}
			return null;
		}

		@Override
		public Object instantiateItem(View container, int position) {
			switch (position) {
			case 0:
				((ViewPager) container).addView(wantsView, 0);
				return wantsView;
			case 1:
				((ViewPager) container).addView(havesView, 0);
				return havesView;
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
