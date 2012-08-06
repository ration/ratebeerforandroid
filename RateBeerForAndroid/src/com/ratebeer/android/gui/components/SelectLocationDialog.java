/*
 *	This file was originally part of Transdroid <http://www.transdroid.org>
 *	
 *	Transdroid is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *	
 *	Transdroid is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *	
 *	You should have received a copy of the GNU General Public License
 *	along with Transdroid.  If not, see <http://www.gnu.org/licenses/>.
 *	
 */
package com.ratebeer.android.gui.components;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.ratebeer.android.R;

public class SelectLocationDialog extends DialogFragment {

	private final OnLocationSelectedListener placesFragment;

	public SelectLocationDialog() {
		this(null);
	}
	
	public SelectLocationDialog(OnLocationSelectedListener placesFragment) {
		this.placesFragment = placesFragment;
		setRetainInstance(true);
		setStyle(STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.dialog_selectlocation, container, false);
		((Button)view.findViewById(R.id.find)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Get the query text and ask the places fragment to start a search
				if (placesFragment != null)
					placesFragment.onStartLocationSearch(((EditText)view.findViewById(R.id.query)).getText().toString());
				dismiss();
			}
		});
		((Button)view.findViewById(R.id.currentlocation)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Ask the places fragment to use the current location
				if (placesFragment != null)
					placesFragment.onUseCurrentLocation();
				dismiss();
			}
		});
		return view;
	}
	
	public interface OnLocationSelectedListener {
		public void onStartLocationSearch(String query);
		public void onUseCurrentLocation();
	}
	
}
