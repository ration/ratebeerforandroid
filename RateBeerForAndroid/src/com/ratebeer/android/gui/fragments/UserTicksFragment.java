/*
 * This file is part of RateBeer For Android. RateBeer for Android is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version. RateBeer for Android is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU
 * General Public License along with RateBeer for Android. If not, see <http://www.gnu.org/licenses/>.
 */
package com.ratebeer.android.gui.fragments;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.InstanceState;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.ViewById;
import com.ratebeer.android.R;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.CommandFailureResult;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.api.command.GetBeerDetailsCommand;
import com.ratebeer.android.api.command.GetBeerDetailsCommand.BeerDetails;
import com.ratebeer.android.api.command.GetUserTicksCommand;
import com.ratebeer.android.api.command.GetUserTicksCommand.UserTick;
import com.ratebeer.android.gui.components.RateBeerFragment;
import com.ratebeer.android.gui.components.helpers.ArrayAdapter;

@EFragment(R.layout.fragment_userticks)
@OptionsMenu(R.menu.refresh)
public class UserTicksFragment extends RateBeerFragment {

	@InstanceState
	protected ArrayList<UserTick> ticks = null;

	@ViewById(R.id.empty)
	protected TextView emptyText;
	@ViewById(R.id.ticks)
	protected ListView ticksView;
	private DateFormat displayDateFormat;

	public UserTicksFragment() {
	}

	@AfterViews
	public void init() {

		displayDateFormat = android.text.format.DateFormat.getDateFormat(getActivity());
		ticksView.setOnItemClickListener(onItemSelected);

		if (ticks != null) {
			publishResults(ticks);
		} else {
			refreshTicks();
		}

	}

	@OptionsItem(R.id.menu_refresh)
	protected void refreshTicks() {
		execute(new GetUserTicksCommand(getUser()));
	}

	private OnItemClickListener onItemSelected = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			UserTick item = ((UserTicksAdapter) ticksView.getAdapter()).getItem(position);
			load(BeerViewFragment_.builder().beerId(item.beerdId).beerName(item.beerName).build());
		}
	};

	@Override
	public void onTaskSuccessResult(CommandSuccessResult result) {
		if (result.getCommand().getMethod() == ApiMethod.GetUserTicks) {
			publishResults(((GetUserTicksCommand) result.getCommand()).getUserTicks());
		} else if (result.getCommand().getMethod() == ApiMethod.GetBeerDetails) {
			publishTickNames((GetBeerDetailsCommand) result.getCommand());
		}
	}

	private void publishResults(ArrayList<UserTick> result) {
		this.ticks = result;
		if (ticks == null) {
			ticksView.setVisibility(View.GONE);
			emptyText.setVisibility(View.GONE);
			return;
		}
		if (ticksView.getAdapter() == null) {
			ticksView.setAdapter(new UserTicksAdapter(getActivity(), result));
		} else {
			((UserTicksAdapter) ticksView.getAdapter()).replace(result);
		}
		ticksView.setVisibility(result.size() == 0 ? View.GONE : View.VISIBLE);
		emptyText.setVisibility(result.size() == 0 ? View.VISIBLE : View.GONE);
	}

	private void publishTickNames(GetBeerDetailsCommand command) {
		if (ticksView.getAdapter() == null) {
			// Huh? This should really never happen... but let's be defensive about it and silently ignore it
			return;
		}
		((UserTicksAdapter) ticksView.getAdapter()).updateNames(command.getDetails());
	}

	@Override
	public void onTaskFailureResult(CommandFailureResult result) {
		publishException(emptyText, result.getException());
	}

	private class UserTicksAdapter extends ArrayAdapter<UserTick> {

		public UserTicksAdapter(Context context, List<UserTick> objects) {
			super(context, objects);
		}

		public void updateNames(BeerDetails details) {
			// Find the beer for which we received the names and refresh the adapter views
			for (int i = 0; i < getCount(); i++) {
				UserTick tick = getItem(i);
				if (tick.beerdId == details.beerId) {
					tick.beerName = details.beerName;
					tick.brewerName = details.brewerName;
					tick.isLoadingNames = false;
					notifyDataSetChanged();
					break;
				}
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// Get the right view, using a ViewHolder
			ViewHolder holder;
			if (convertView == null || convertView.getTag() == null) {
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_tick, null);
				holder = new ViewHolder();
				holder.loading = (ProgressBar) convertView.findViewById(R.id.loading);
				holder.beer = (TextView) convertView.findViewById(R.id.beer);
				holder.brewer = (TextView) convertView.findViewById(R.id.brewer);
				holder.liked = (TextView) convertView.findViewById(R.id.liked);
				holder.date = (TextView) convertView.findViewById(R.id.date);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			// Bind the data
			UserTick item = getItem(position);
			if (item.beerName != null && item.brewerName != null) {
				holder.loading.setVisibility(View.GONE);
				holder.beer.setVisibility(View.VISIBLE);
				holder.beer.setText(item.beerName);
				holder.brewer.setVisibility(View.VISIBLE);
				holder.brewer.setText(item.brewerName);
				holder.liked.setVisibility(View.VISIBLE);
				holder.liked.setText(Integer.toString(item.liked));
				holder.date.setVisibility(View.VISIBLE);
				holder.date.setText(displayDateFormat.format(item.timeEntered));
			} else {
				holder.loading.setVisibility(View.VISIBLE);
				holder.beer.setVisibility(View.GONE);
				holder.brewer.setVisibility(View.GONE);
				holder.liked.setVisibility(View.GONE);
				holder.date.setVisibility(View.GONE);
				if (!item.isLoadingNames) {
					execute(new GetBeerDetailsCommand(getUser(), item.beerdId));
				}
			}

			return convertView;
		}

	}

	protected static class ViewHolder {
		ProgressBar loading;
		TextView beer, brewer, liked, date;
	}

}
