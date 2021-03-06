/*
 * This file is part of RateBeer For Android. RateBeer for Android is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version. RateBeer for Android is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU
 * General Public License along with RateBeer for Android. If not, see <http://www.gnu.org/licenses/>.
 */
package com.ratebeer.android.gui.fragments;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.HeaderViewListAdapter;
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
import com.ratebeer.android.api.command.GetUserRatingsCommand;
import com.ratebeer.android.api.command.GetUserRatingsCommand.UserRating;
import com.ratebeer.android.gui.components.RateBeerFragment;
import com.ratebeer.android.gui.components.helpers.ArrayAdapter;

@EFragment(R.layout.fragment_userratings)
@OptionsMenu({R.menu.refresh, R.menu.userratings})
public class UserRatingsFragment extends RateBeerFragment {

	private static final String DECIMAL_FORMATTER = "%.1f";
	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("M/d/yyyy");

	@FragmentArg
	@InstanceState
	protected int userId;
	@InstanceState
	protected ArrayList<UserRating> ratings = null;

	@ViewById(R.id.empty)
	protected TextView emptyText;
	@ViewById(R.id.ratings)
	protected ListView ratingsView;
	private FrameLayout ratingsViewFooter;

	private boolean loading = false;
	private int pageNr = 1;
	private boolean preventLoadingOfMore = false;
	private int sortOrder = GetUserRatingsCommand.SORTBY_DATE;

	public UserRatingsFragment() {
	}

	@AfterViews
	public void init() {

		ratingsView.setOnScrollListener(onScrollListener);
		ratingsView.setOnItemClickListener(onItemSelected);
		// Manual inflating of the 'loading spinner' layout that is displayed as the list's footer view
		ratingsViewFooter = (FrameLayout) getActivity().getLayoutInflater().inflate(R.layout.endlesslist_footer, null);
		showLoading(false);
		ratingsView.addFooterView(ratingsViewFooter);

		if (ratings != null) {
			publishResults(ratings);
		} else {
			refreshRatings();
		}

	}


	@OptionsItem(R.id.menu_sortby)
	protected void onSortRatings() {
		new UserRatingsSortDialog(this).show(getActivity().getSupportFragmentManager(), null);
	}

	private OnScrollListener onScrollListener = new OnScrollListener() {
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			boolean loadMore = totalItemCount > 0 && firstVisibleItem + visibleItemCount >= totalItemCount;
			if (loadMore && !loading && !preventLoadingOfMore) {
				pageNr++;
				loadRatings();
			}
		}
	};

	public void setSortOrder(int newSortOrder) {
		sortOrder = newSortOrder;
		refreshRatings();
	}

	@OptionsItem(R.id.menu_refresh)
	protected void refreshRatings() {
		if (ratingsView.getAdapter() != null) {
			ratingsView.setAdapter(null);
			ratingsView.setVisibility(View.GONE);
			emptyText.setVisibility(View.GONE);
		}
		pageNr = 1;
		preventLoadingOfMore = false;
		loadRatings();
	}

	private void loadRatings() {
		showLoading(true);
		execute(new GetUserRatingsCommand(getUser(), userId, pageNr, sortOrder));
	}

	private OnItemClickListener onItemSelected = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			UserRating item = ((UserRatingsAdapter) ((android.widget.HeaderViewListAdapter) ratingsView.getAdapter())
					.getWrappedAdapter()).getItem(position);
			load(BeerViewFragment_.builder().beerId(item.beerId).beerName(item.beerName).build());
		}
	};

	@Override
	public void onTaskSuccessResult(CommandSuccessResult result) {
		if (result.getCommand().getMethod() == ApiMethod.GetUserRatings) {
			showLoading(false);
			publishResults(((GetUserRatingsCommand) result.getCommand()).getUserRatings());
		}
	}

	private void publishResults(ArrayList<UserRating> result) {
		if (result.size() == 0) {
			// This was the end: don't load more now
			preventLoadingOfMore = true;
			return;
		}
		if (ratingsView.getAdapter() == null
				|| (ratingsView.getAdapter() instanceof HeaderViewListAdapter && 
						((UserRatingsAdapter) ((android.widget.HeaderViewListAdapter) 
								ratingsView.getAdapter()).getWrappedAdapter()) == null)) {
			ratingsView.setAdapter(new UserRatingsAdapter(getActivity(), result));
			this.ratings = result;
		} else {
			((UserRatingsAdapter) ((android.widget.HeaderViewListAdapter) ratingsView.getAdapter()).getWrappedAdapter())
					.addAll(result);
			this.ratings.addAll(result);
		}
		ratingsView.setVisibility(result.size() == 0 ? View.GONE : View.VISIBLE);
		emptyText.setVisibility(result.size() == 0 ? View.VISIBLE : View.GONE);
	}

	private void showLoading(boolean loading) {
		this.loading = loading;
		ratingsViewFooter.setLayoutParams(new AbsListView.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
				loading ? FrameLayout.LayoutParams.WRAP_CONTENT : 0));
	}

	@Override
	public void onTaskFailureResult(CommandFailureResult result) {
		if (result.getCommand().getMethod() == ApiMethod.GetUserRatings) {
			showLoading(false);
		}
		publishException(emptyText, result.getException());
	}

	private class UserRatingsAdapter extends ArrayAdapter<UserRating> {

		public UserRatingsAdapter(Context context, List<UserRating> objects) {
			super(context, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// Get the right view, using a ViewHolder
			ViewHolder holder;
			if (convertView == null || convertView.getTag() == null) {
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_userrating, null);
				holder = new ViewHolder();
				holder.beer = (TextView) convertView.findViewById(R.id.beer);
				holder.brewer = (TextView) convertView.findViewById(R.id.brewer);
				holder.style = (TextView) convertView.findViewById(R.id.style);
				holder.score = (TextView) convertView.findViewById(R.id.score);
				holder.myrating = (TextView) convertView.findViewById(R.id.myrating);
				holder.date = (TextView) convertView.findViewById(R.id.date);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			// Bind the data
			UserRating item = getItem(position);
			holder.beer.setText(item.beerName);
			holder.brewer.setText(item.brewerName);
			holder.style.setText(item.styleName);
			holder.score.setText(getString(R.string.myratings_avg, String.format(DECIMAL_FORMATTER, item.score)));
			holder.myrating.setText(String.format(DECIMAL_FORMATTER, item.myRating));
			holder.date.setText(DATE_FORMATTER.format(item.date));

			return convertView;
		}

	}

	protected static class ViewHolder {
		TextView beer, brewer, style, score, myrating, date;
	}

}
