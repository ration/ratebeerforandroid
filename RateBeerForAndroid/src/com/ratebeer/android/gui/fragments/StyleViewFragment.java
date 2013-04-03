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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.commonsware.cwac.merge.MergeAdapter;
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
import com.ratebeer.android.api.command.GetStyleDetailsCommand;
import com.ratebeer.android.api.command.GetStyleDetailsCommand.StyleDetails;
import com.ratebeer.android.api.command.GetTopBeersCommand.TopBeer;
import com.ratebeer.android.api.command.Style;
import com.ratebeer.android.gui.components.RateBeerFragment;
import com.ratebeer.android.gui.components.helpers.ActivityUtil;
import com.ratebeer.android.gui.components.helpers.ArrayAdapter;

@EFragment(R.layout.fragment_styleview)
@OptionsMenu(R.menu.refresh)
public class StyleViewFragment extends RateBeerFragment {

	private static final String DECIMAL_FORMATTER = "%.1f";

	@FragmentArg
	@InstanceState
	protected Style style;
	@InstanceState
	protected StyleDetails details = null;
	
	@ViewById(R.id.styleview)
	protected ListView styleView;
	@ViewById(R.id.name)
	protected TextView nameText;
	@ViewById(R.id.description)
	protected TextView descriptionText;
	@ViewById(R.id.beers)
	protected ListView beersView;
	
	private StyleBeerAdapter beersAdapter;
	
	public StyleViewFragment() {
	}

	@AfterViews
	public void init() {

		if (styleView != null) {
			styleView.setAdapter(new StyleViewAdapter());
			styleView.setItemsCanFocus(true);
		} else {
			// Tablet
			beersAdapter = new StyleBeerAdapter(getActivity(), new ArrayList<TopBeer>());
			beersView.setAdapter(beersAdapter);
		}
		
		if (details != null) {
			publishDetails(details);
		} else {
			refreshDetails();
		}
		
	}

	@OptionsItem(R.id.menu_refresh)
	protected void refreshDetails() {
		execute(new GetStyleDetailsCommand(getUser(), style.getId()));
	}

	private void onStyleBeerClick(String beerName, int beerId, int ratings) {
		load(BeerViewFragment_.builder().beerName(beerName).beerId(beerId).ratingsCount(ratings).build());
	}
	
	@Override
	public void onTaskSuccessResult(CommandSuccessResult result) {
		if (result.getCommand().getMethod() == ApiMethod.GetStyleDetails) {
			publishDetails(((GetStyleDetailsCommand) result.getCommand()).getDetails());
		}
	}

	private void publishDetails(StyleDetails details) {
		this.details = details;
		if (details == null) {
			return;
		}
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
			nameText = (TextView) fields.findViewById(R.id.name);
			descriptionText = (TextView) fields.findViewById(R.id.description);
			
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
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_stylebeer, null);
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

}
