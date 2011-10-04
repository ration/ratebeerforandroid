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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.ratebeer.android.R;
import com.ratebeer.android.api.command.Style;
import com.ratebeer.android.gui.components.ArrayAdapter;
import com.ratebeer.android.gui.components.RateBeerFragment;

public class StylesFragment extends RateBeerFragment {

	private ListView stylesView;
	private LayoutInflater inflater;

	public StylesFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		this.inflater = inflater;
		return inflater.inflate(R.layout.fragment_styles, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		stylesView = (ListView) getView().findViewById(R.id.styles);
		stylesView.setOnItemClickListener(onItemSelected);

		populateStyles();
		
	}

	private OnItemClickListener onItemSelected = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Style item = ((StyleAdapter)stylesView.getAdapter()).getItem(position);
			getRateBeerActivity().load(new StyleViewFragment(item));
		}
	};
	
	private void populateStyles() {
		stylesView.setAdapter(new StyleAdapter(getActivity(), 
				new ArrayList<Style>(Style.ALL_STYLES.values()), inflater));
	}

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
