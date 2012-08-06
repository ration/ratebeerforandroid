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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

import com.ratebeer.android.R;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.CommandFailureResult;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.api.command.GetUserRatingsCommand;
import com.ratebeer.android.api.command.GetUserRatingsCommand.UserRating;
import com.ratebeer.android.gui.components.ArrayAdapter;
import com.ratebeer.android.gui.components.RateBeerActivity;
import com.ratebeer.android.gui.components.RateBeerFragment;

public class UserRatingsFragment extends RateBeerFragment {

	private static final String DECIMAL_FORMATTER = "%.1f";
	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("M/d/yyyy");
	private static final String STATE_USERID = "userId";
	private static final String STATE_PAGENR = "pageNr";
	private static final String STATE_SORTORDER = "sortOrder";
	private static final String STATE_RATINGS = "ratings";

	private static final int MENU_SORTBY = 1;

	private LayoutInflater inflater;
	private TextView emptyText;
	private ListView ratingsView;
	private FrameLayout ratingsViewFooter;

	private int userId;
	private boolean loading = false;
	private int pageNr = 1;
	private boolean preventLoadingOfMore = false;
	private int sortOrder = GetUserRatingsCommand.SORTBY_DATE;
	private ArrayList<UserRating> ratings = null;

	public UserRatingsFragment() {
		this(-1);
	}

	public UserRatingsFragment(int userId) {
		this.userId = userId;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		this.inflater = inflater;
		return inflater.inflate(R.layout.fragment_userratings, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		emptyText = (TextView) getView().findViewById(R.id.empty);
		ratingsView = (ListView) getView().findViewById(R.id.ratings);
		ratingsView.setOnScrollListener(onScrollListener);
		ratingsView.setOnItemClickListener(onItemSelected);
		ratingsViewFooter = (FrameLayout) inflater.inflate(R.layout.endlesslist_footer, null);
		showLoading(false);
		ratingsView.addFooterView(ratingsViewFooter);

		if (savedInstanceState != null) {
			pageNr = savedInstanceState.getInt(STATE_PAGENR);
			sortOrder = savedInstanceState.getInt(STATE_SORTORDER);
			if (savedInstanceState.containsKey(STATE_RATINGS)) {
				ArrayList<UserRating> savedRatings = savedInstanceState.getParcelableArrayList(STATE_RATINGS);
				publishResults(savedRatings);
			}
		} else {
			refreshRatings();
		}

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		MenuItem item = menu.add(Menu.NONE, RateBeerActivity.MENU_REFRESH, RateBeerActivity.MENU_REFRESH,
				R.string.app_refresh);
		item.setIcon(R.drawable.ic_action_refresh);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		MenuItem item2 = menu.add(Menu.NONE, MENU_SORTBY, MENU_SORTBY, R.string.myratings_sortby);
		item2.setIcon(R.drawable.ic_action_sortby);
		item2.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case RateBeerActivity.MENU_REFRESH:
			refreshRatings();
			break;
		case MENU_SORTBY:
			new UserRatingsSortDialog().show(getSupportActivity().getSupportFragmentManager(), null);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_USERID, userId);
		outState.putInt(STATE_PAGENR, pageNr);
		outState.putInt(STATE_SORTORDER, sortOrder);
		if (ratings != null) {
			outState.putParcelableArrayList(STATE_RATINGS, ratings);
		}
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

	protected void setSortOrder(int newSortOrder) {
		sortOrder = newSortOrder;
		refreshRatings();
	}

	private void refreshRatings() {
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
		// Log.d(RateBeerForAndroid.LOG_NAME, "Loading ratings for user " + userId + " page " + pageNr);
		showLoading(true);
		execute(new GetUserRatingsCommand(getRateBeerActivity().getApi(), userId, pageNr, sortOrder));
	}

	private OnItemClickListener onItemSelected = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			UserRating item = ((UserRatingsAdapter) ((android.widget.HeaderViewListAdapter) ratingsView.getAdapter())
					.getWrappedAdapter()).getItem(position);
			getRateBeerActivity().load(new BeerViewFragment(item.beerName, item.beerId));
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
				convertView = inflater.inflate(R.layout.list_item_userrating, null);
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

			if (getRateBeerActivity() == null) {
				return convertView;
			}
			
			// Bind the data
			UserRating item = getItem(position);
			if (getActivity() != null) {
				holder.beer.setText(item.beerName);
				holder.brewer.setText(item.brewerName);
				holder.style.setText(item.styleName);
				holder.score.setText(getString(R.string.myratings_avg, String.format(DECIMAL_FORMATTER, item.score)));
				holder.myrating.setText(String.format(DECIMAL_FORMATTER, item.myRating));
				holder.date.setText(DATE_FORMATTER.format(item.date));
			}

			return convertView;
		}

	}

	protected static class ViewHolder {
		TextView beer, brewer, style, score, myrating, date;
	}

	public class UserRatingsSortDialog extends DialogFragment {
		public UserRatingsSortDialog() {}
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return new AlertDialog.Builder(getActivity()).setTitle(R.string.myratings_sortby).setItems(
					R.array.myratings_sortby_names, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// The (0-based) ID of which item was selected is directly matched with the (1-based)
							// GetUserRatingsCommand's order ID, f.e. the fourth array item 'My rating' is the sort
							// order with ID 5 (which is SORTBY_DATE)
							setSortOrder(which + 1);
						}
					}).create();
		}

	}

}
