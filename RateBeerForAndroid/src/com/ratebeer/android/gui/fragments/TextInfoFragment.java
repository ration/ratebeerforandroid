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

import android.widget.TextView;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.ViewById;
import com.ratebeer.android.R;
import com.ratebeer.android.gui.components.RateBeerFragment;

@EFragment(R.layout.fragment_textinfo)
public class TextInfoFragment extends RateBeerFragment {

	@FragmentArg
	protected String title;
	@FragmentArg
	protected String info;

	@ViewById(R.id.title)
	protected TextView titleText;
	@ViewById(R.id.info)
	protected TextView infoText;

	public TextInfoFragment() {
	}

	@AfterViews
	public void init() {
		titleText.setText(title);
		infoText.setText(info);
	}
	
	@Click
	protected void okClicked() {
		// Just close this fragment
		getFragmentManager().popBackStackImmediate();
	}

}
