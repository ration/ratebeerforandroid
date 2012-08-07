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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.ratebeer.android.R;
import com.ratebeer.android.gui.components.RateBeerFragment;

public class TextInfoFragment extends RateBeerFragment {

	private final String title;
	private final String info;
	
	private TextView titleText, infoText;
	private Button okButton;

	public TextInfoFragment() {
		this(null, null);
	}
	
	public TextInfoFragment(String title, String infoText) {
		this.title = title;
		this.info = infoText;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_textinfo, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		titleText = (TextView) getView().findViewById(R.id.title);
		infoText = (TextView) getView().findViewById(R.id.info);
		okButton = (Button) getView().findViewById(R.id.ok);
		okButton.setOnClickListener(onOkClick);

		if (savedInstanceState == null) {
			titleText.setText(title);
			infoText.setText(info);
		}
	}

	private OnClickListener onOkClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// Just close this fragment
			getFragmentManager().popBackStackImmediate();
		}
	};

}
