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
import android.os.Parcelable;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ratebeer.android.R;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.CommandFailureResult;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.api.command.GetUserCellarCommand;
import com.ratebeer.android.api.command.GetUserCellarCommand.CellarBeer;
import com.ratebeer.android.api.command.RemoveFromCellarCommand;
import com.ratebeer.android.gui.components.ArrayAdapter;
import com.ratebeer.android.gui.components.RateBeerActivity;
import com.ratebeer.android.gui.components.RateBeerFragment;
import com.ratebeer.android.gui.fragments.ConfirmDialogFragment.OnDialogResult;
import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitleProvider;

public class CellarViewFragment extends RateBeerFragment {

	private static final String STATE_USERID = "userId";
	private static final String STATE_WANTS = "wants";
	private static final String STATE_HAVES = "haves";
	private static final int MENU_REMOVE = 10;

	private LayoutInflater inflater;
	private ViewPager pager;
	private ListView wantsView;
	private ListView havesView;

	protected int userId;
	private ArrayList<CellarBeer> wants = new ArrayList<GetUserCellarCommand.CellarBeer>();
	private ArrayList<CellarBeer> haves = new ArrayList<GetUserCellarCommand.CellarBeer>();
	private ListAdapter lastUsedListView = null;

	public CellarViewFragment() {
		this(-1);
	}

	/**
	 * Show a user's cellar (wants and haves)
	 * @param userId The user ID
	 */
	public CellarViewFragment(int userId) {
		this.userId = userId;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		this.inflater = inflater;
		return inflater.inflate(R.layout.fragment_cellarview, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		pager = (ViewPager) getView().findViewById(R.id.pager);
		if (pager != null) {
			// Phone layout (using a pager to show the two lists)
			CellarPagerAdapter cellarPagerAdapter = new CellarPagerAdapter();
			pager.setAdapter(cellarPagerAdapter);
			wantsView = cellarPagerAdapter.getWantView();
			havesView = cellarPagerAdapter.getHavesView();
			TabPageIndicator titles = (TabPageIndicator) getView().findViewById(R.id.titles);
			titles.setViewPager(pager);
		} else {
			// Tablet layout (showing both lists at the same time)
			wantsView = (ListView) getView().findViewById(R.id.wantsview);
			havesView = (ListView) getView().findViewById(R.id.havesview);
		}
		wantsView.setOnItemClickListener(onCellarBeerClick);
		havesView.setOnItemClickListener(onCellarBeerClick);
		registerForContextMenu(wantsView);
		registerForContextMenu(havesView);

		if (savedInstanceState != null) {
			userId = savedInstanceState.getInt(STATE_USERID);
			if (savedInstanceState.containsKey(STATE_WANTS) && savedInstanceState.containsKey(STATE_HAVES)) {
				wants = savedInstanceState.getParcelableArrayList(STATE_WANTS);
				haves = savedInstanceState.getParcelableArrayList(STATE_HAVES);
			}
		} else {
			refreshCellar();
		}
		// Publish the current wants and haves view, even when it is not loaded yet (and thus still empty)
		publishCellar(wants, haves);

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
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) item.getMenuInfo();
		final CellarBeer beer = (CellarBeer) lastUsedListView.getItem(acmi.position);
		new ConfirmDialogFragment(new OnDialogResult() {
			@Override
			public void onConfirmed() {
				removeBeerFromCellar(beer);
			}
		}, R.string.cellar_confirmremoval, beer.beerName).show(getSupportFragmentManager(), "dialog");
		return super.onContextItemSelected(item);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_USERID, userId);
		if (wants != null) {
			outState.putParcelableArrayList(STATE_WANTS, wants);
		}
		if (haves != null) {
			outState.putParcelableArrayList(STATE_HAVES, haves);
		}
	}

	private void refreshCellar() {
		execute(new GetUserCellarCommand(getRateBeerActivity().getApi(), userId));
	}

	protected void removeBeerFromCellar(CellarBeer beer) {
		execute(new RemoveFromCellarCommand(getRateBeerActivity().getApi(), beer.beerId));
	}

	private OnItemClickListener onCellarBeerClick = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			CellarBeer item = ((CellarAdapter) parent.getAdapter()).getItem(position);
			getRateBeerActivity().load(new BeerViewFragment(item.beerName, item.beerId));
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
		// Collections.sort(result, new PlacesComparator(sortOrder));
		// Updated the list view adapters, which might be wrapped in a CellarPagerAdapter
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
				convertView = inflater.inflate(R.layout.list_item_cellaritem, null);
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

	private class CellarPagerAdapter extends PagerAdapter implements TitleProvider {

		private ListView pagerWantsView;
		private ListView pagerHavesView;

		public CellarPagerAdapter() {
			LayoutInflater inflater = getActivity().getLayoutInflater();
			pagerWantsView = (ListView) inflater.inflate(R.layout.fragment_pagerlist, null);
			pagerHavesView = (ListView) inflater.inflate(R.layout.fragment_pagerlist, null);
			//pagerWantsView = (ListView) wantLayout.findViewById(R.id.list);
			//pagerHavesView = (ListView) haveLayout.findViewById(R.id.list);
		}

		public ListView getHavesView() {
			return pagerHavesView;
		}

		public ListView getWantView() {
			return pagerWantsView;
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public String getTitle(int position) {
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
				((ViewPager) container).addView(pagerWantsView, 0);
				return pagerWantsView;
			case 1:
				((ViewPager) container).addView(pagerHavesView, 0);
				return pagerHavesView;
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

		@Override
		public void finishUpdate(View container) {}

		@Override
		public Parcelable saveState() { return null; }

		@Override
		public void startUpdate(View container) {
		}

		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {}

	}

}
