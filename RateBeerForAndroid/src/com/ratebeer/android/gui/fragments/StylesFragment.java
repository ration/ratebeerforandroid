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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;
import com.ratebeer.android.R;
import com.ratebeer.android.api.command.Style;
import com.ratebeer.android.gui.components.RateBeerFragment;
import com.ratebeer.android.gui.components.helpers.ArrayAdapter;

@EFragment(R.layout.fragment_styles)
public class StylesFragment extends RateBeerFragment {

	@ViewById(R.id.styles)
	protected ListView stylesView;

	public StylesFragment() {
	}

	@AfterViews
	public void init() {

		stylesView.setOnItemClickListener(onItemSelected);
		stylesView.setAdapter(new StyleAdapter(getActivity(), 
				new ArrayList<Style>(Style.ALL_STYLES.values()), getActivity().getLayoutInflater()));	
	}

	private OnItemClickListener onItemSelected = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Style item = ((StyleAdapter)stylesView.getAdapter()).getItem(position);
			load(StyleViewFragment_.builder().style(item).build());
		}
	};

	public static class StyleAdapter extends ArrayAdapter<Style> {

		private LayoutInflater inflater;
		
		public StyleAdapter(Context context, List<Style> objects, LayoutInflater inflater) {
			super(context, objects);
			this.inflater = inflater;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// Get the right view, using a ViewHolder
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.list_item_style, null);
				holder = new ViewHolder();
				holder.name = (TextView) convertView.findViewById(R.id.name);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			// Bind the data
			Style item = getItem(position);
			holder.name.setText(item.getName());

			return convertView;
		}

	}
	
	protected static class ViewHolder {
		TextView name;
	}

}
