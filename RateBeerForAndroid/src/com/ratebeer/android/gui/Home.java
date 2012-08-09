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
package com.ratebeer.android.gui;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;

import com.ratebeer.android.app.RateBeerForAndroid;
import com.ratebeer.android.gui.components.BeermailService;
import com.ratebeer.android.gui.components.BootReceiver;
import com.ratebeer.android.gui.components.PosterService;

public class Home extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Start the background service, if necessary
		BootReceiver.startAlarm(getApplicationContext());

		// See which activity should be started
		Intent startActivity = null;
		if (RateBeerForAndroid.isTablet(getResources())) {
			startActivity = new Intent(this, HomeTablet.class);
		} else {
			startActivity = new Intent(this, HomePhone.class);
		}
		if (getIntent().getAction() != null && getIntent().getAction().equals(Intent.ACTION_SEARCH)) {
			// Home activity should start a search
			startActivity.putExtra(SearchManager.QUERY, getIntent().getStringExtra(SearchManager.QUERY));
		} else if (getIntent().getAction() != null && getIntent().getAction().equals(Intent.ACTION_VIEW)) {
			// Home activity should show some specific beer/place/event
			startActivity.setData(getIntent().getData());
			startActivity.setAction(Intent.ACTION_VIEW);
		} else if (getIntent().getAction() != null && getIntent().getAction().equals(PosterService.ACTION_EDITRATING)) {
			// Home activity should start editing a beer rating
			startActivity.replaceExtras(getIntent().getExtras());
			startActivity.setAction(PosterService.ACTION_EDITRATING);
		} else if (getIntent().getAction() != null && getIntent().getAction().equals(PosterService.ACTION_ADDUPCCODE)) {
			// Home activity should show the add UPC code screen again
			startActivity.replaceExtras(getIntent().getExtras());
			startActivity.setAction(PosterService.ACTION_ADDUPCCODE);
		} else if (getIntent() != null && getIntent().getAction() != null && getIntent().getAction().equals(BeermailService.ACTION_VIEWBEERMAILS)) {
			// Open the beermails screen
			startActivity.setAction(BeermailService.ACTION_VIEWBEERMAILS);
		} else if (getIntent() != null && getIntent().getAction() != null && getIntent().getAction().equals(BeermailService.ACTION_VIEWBEERMAIL)) {
			// Open the beermail screen to a specific mail
			startActivity.replaceExtras(getIntent().getExtras());
			startActivity.setAction(BeermailService.ACTION_VIEWBEERMAIL);
		} else if (getIntent() != null && getIntent().getAction() != null && getIntent().getAction().equals(BeermailService.ACTION_REPLYBEERMAIL)) {
			// Open the beermail reply screen to a specific mail
			startActivity.replaceExtras(getIntent().getExtras());
			startActivity.setAction(BeermailService.ACTION_REPLYBEERMAIL);
		}
		
		startActivity(startActivity);
		finish();
	}

}
