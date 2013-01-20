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
package com.ratebeer.android.api.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import com.ratebeer.android.api.ApiException;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.EmptyResponseCommand;
import com.ratebeer.android.api.HttpHelper;
import com.ratebeer.android.api.RateBeerApi;
import com.ratebeer.android.api.UserSettings;

public class AddAvailabilityCommand extends EmptyResponseCommand {

	public static final int NO_EXTRA_PLACE = -1;

	private final int beerId;
	private final int[] selectedFavourites;
	private final String extraPlaceName;
	private final int extraPlaceId;
	private final boolean onBottleCan;
	private final boolean onTap;

	public AddAvailabilityCommand(UserSettings api, int beerId, int[] selectedFavourites, String extraPlaceName,
			int extraPlaceId, boolean onBottleCan, boolean onTap) {
		super(api, ApiMethod.AddAvailability);
		this.beerId = beerId;
		this.selectedFavourites = selectedFavourites;
		this.extraPlaceName = extraPlaceName;
		this.extraPlaceId = extraPlaceId;
		this.onBottleCan = onBottleCan;
		this.onTap = onTap;
	}

	@Override
	protected void makeRequest() throws ClientProtocolException, IOException, ApiException {
		RateBeerApi.ensureLogin(getUserSettings());
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>(Arrays.asList(new BasicNameValuePair(
				"UserID", Integer.toString(getUserSettings().getUserID())),
				new BasicNameValuePair("BeerID", Integer.toString(beerId)), new BasicNameValuePair("PlaceName",
						extraPlaceId == AddAvailabilityCommand.NO_EXTRA_PLACE ? "" : extraPlaceName),
				new BasicNameValuePair("PlaceName2", extraPlaceId == AddAvailabilityCommand.NO_EXTRA_PLACE ? ""
						: extraPlaceName), new BasicNameValuePair("PlaceID",
						extraPlaceId == AddAvailabilityCommand.NO_EXTRA_PLACE ? "" : Integer.toString(extraPlaceId))));
		for (int p = 0; p < selectedFavourites.length; p++) {
			params.add(new BasicNameValuePair("placeid", Integer.toString(selectedFavourites[p])));
		}
		if (onBottleCan) {
			params.add(new BasicNameValuePair("ServedBottle", "on"));
		}
		if (onTap) {
			params.add(new BasicNameValuePair("ServedTap", "on"));
		}
		HttpHelper.makeRBPost("http://www.ratebeer.com/Ratings/Beer/Avail-Save.asp", params);
	}

}
