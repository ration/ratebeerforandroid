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

import android.content.Intent;
import android.net.Uri;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;
import com.ratebeer.android.R;
import com.ratebeer.android.gui.components.RateBeerFragment;
import com.ratebeer.android.gui.components.helpers.ActivityUtil;

@EFragment(R.layout.fragment_about)
public class AboutFragment extends RateBeerFragment {
	
	@ViewById
	protected TextView version;
	
	public AboutFragment() {
	}

	@AfterViews
	protected void init() {
		version.setText(ActivityUtil.getVersionNumber(getActivity()) + " (v"
				+ ActivityUtil.getVersionCode(getActivity()) + ")");
	}
	
	@Click
	protected void rblinkClicked() {
		if (getActivity() != null)
			load(UserViewFragment_.builder().userId(101051).userName("erickok").build());
	}

	@Click
	protected void weblinkClicked() {
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://2312.nl")));
	}

	@Click
	protected void maillinkClicked() {
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:rb@2312.nl")));
	}

}
