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
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.commonsware.cwac.merge.MergeAdapter;
import com.ratebeer.android.R;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.CommandFailureResult;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.api.command.GetStyleDetailsCommand;
import com.ratebeer.android.api.command.Style;
import com.ratebeer.android.api.command.GetStyleDetailsCommand.StyleDetails;
import com.ratebeer.android.api.command.GetTopBeersCommand.TopBeer;
import com.ratebeer.android.gui.components.ActivityUtil;
import com.ratebeer.android.gui.components.ArrayAdapter;
import com.ratebeer.android.gui.components.RateBeerActivity;
import com.ratebeer.android.gui.components.RateBeerFragment;

public class StyleViewFragment extends RateBeerFragment {

	private static final String DECIMAL_FORMATTER = "%.1f";
	private static final String STATE_STYLE = "style";
	private static final String STATE_DETAILS = "details";

	private LayoutInflater inflater;
	private ListView styleView;

	private TextView nameText, descriptionText;
	private StyleBeerAdapter beersAdapter;
	
	protected Style style;
	private StyleDetails details = null;
	
	public StyleViewFragment() {
		this(null);
	}

	/**
	 * Show a specific style's details and list of top beers in the style
	 * @param style The beer style to show
	 */
	public StyleViewFragment(Style style) {
		this.style = style;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		this.inflater = inflater;
		return inflater.inflate(R.layout.fragment_styleview, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		styleView = (ListView) getView().findViewById(R.id.styleview);
		if (styleView != null) {
			styleView.setAdapter(new StyleViewAdapter());
			styleView.setItemsCanFocus(true);
		} else {
			// Tablet
			ListView beersView = (ListView) getView().findViewById(R.id.beers);
			beersAdapter = new StyleBeerAdapter(getActivity(), new ArrayList<TopBeer>());
			beersView.setAdapter(beersAdapter);
			initFields(getView());
		}
		
		if (savedInstanceState != null) {
			style = savedInstanceState.getParcelable(STATE_STYLE);
			if (savedInstanceState.containsKey(STATE_DETAILS)) {
				StyleDetails savedDetails = savedInstanceState.getParcelable(STATE_DETAILS);
				publishDetails(savedDetails);
			}
		} else {
			refreshDetails();
		}
		// Already fill in the style name
		nameText.setText(style.getName());
		
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		MenuItem item = menu.add(RateBeerActivity.MENU_REFRESH, RateBeerActivity.MENU_REFRESH, RateBeerActivity.MENU_REFRESH, R.string.app_refresh);
		item.setIcon(R.drawable.ic_action_refresh);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(android.view.MenuItem item) {
		switch (item.getItemId()) {
		case RateBeerActivity.MENU_REFRESH:
			refreshDetails();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(STATE_STYLE, style);
		if (details != null) {
			outState.putParcelable(STATE_DETAILS, details);
		}
	}

	private void refreshDetails() {
		execute(new GetStyleDetailsCommand(getRateBeerActivity().getApi(), style.getId()));
	}

	private void onStyleBeerClick(String beerName, int beerId, int ratings) {
		getRateBeerActivity().load(new BeerViewFragment(beerName, beerId, ratings));
	}
	
	@Override
	public void onTaskSuccessResult(CommandSuccessResult result) {
		if (result.getCommand().getMethod() == ApiMethod.GetStyleDetails) {
			publishDetails(((GetStyleDetailsCommand) result.getCommand()).getDetails());
		}
	}

	private void publishDetails(StyleDetails details) {
		this.details = details;
		// Show details
		setDetails(details);
	}

	/**
	 * Overrides the different textual details about this style, as shown as header, as well as the list of top beers
	 * @param details The style details object, which includes the list of top beers in the style
	 */
	public void setDetails(StyleDetails details) {
		nameText.setText(details.name);
		descriptionText.setText(details.description);
		// TODO: Add servedIn
		beersAdapter.replace(details.beers);
	}

	@Override
	public void onTaskFailureResult(CommandFailureResult result) {
		publishException(null, result.getException());
	}

	private class StyleViewAdapter extends MergeAdapter {

		public StyleViewAdapter() {

			// Set the style detail fields
			View fields = getActivity().getLayoutInflater().inflate(R.layout.fragment_styledetails, null);
			addView(fields);
			initFields(fields);
			
			// Set the list of top beers
			beersAdapter = new StyleBeerAdapter(getActivity(), new ArrayList<TopBeer>());
			addAdapter(beersAdapter);
		}

	}
	
	private class StyleBeerAdapter extends ArrayAdapter<TopBeer> {

		private OnClickListener onRowClick = new OnClickListener() {
			@Override
			public void onClick(View v) {
				TopBeer beer = (TopBeer) v.findViewById(R.id.beer).getTag();
				onStyleBeerClick(beer.beerName, beer.beerId, beer.rateCount);
			}
		};

		public StyleBeerAdapter(Context context, List<TopBeer> objects) {
			super(context, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// Get the right view, using a ViewHolder
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.list_item_stylebeer, null);
				ActivityUtil.makeListItemClickable(convertView, onRowClick);
				holder = new ViewHolder();
				holder.order = (TextView) convertView.findViewById(R.id.order);
				holder.beer = (TextView) convertView.findViewById(R.id.beer);
				holder.score = (TextView) convertView.findViewById(R.id.score);
				holder.count = (TextView) convertView.findViewById(R.id.count);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			// Bind the data
			TopBeer item = getItem(position);
			holder.beer.setTag(item);
			holder.order.setText(Integer.toString(item.orderNr));
			holder.beer.setText(item.beerName);
			holder.score.setText(String.format(DECIMAL_FORMATTER, item.score));
			holder.count.setText(Integer.toString(item.rateCount) + " " + getString(R.string.details_ratings));
			
			return convertView;
		}

	}

	protected static class ViewHolder {
		TextView order, beer, score, count;
	}

	public void initFields(View fields) {
		nameText = (TextView) fields.findViewById(R.id.name);
		descriptionText = (TextView) fields.findViewById(R.id.description);
	}

}
