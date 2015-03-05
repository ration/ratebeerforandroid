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

<<<<<<< HEAD
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;
=======
import java.net.HttpURLConnection;
>>>>>>> 9cb2b20cee7ae90e7a5ea61c0ebff4e0c86a6dd6

import com.ratebeer.android.api.ApiConnection;
import com.ratebeer.android.api.ApiException;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.EmptyResponseCommand;
import com.ratebeer.android.api.UserSettings;

public class AddAvailabilityCommand extends EmptyResponseCommand {

<<<<<<< HEAD
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
=======
	private final int beerId;
	private final int placeId;

	public AddAvailabilityCommand(UserSettings api, int beerId, int placeId) {
		super(api, ApiMethod.AddAvailability);
		this.beerId = beerId;
		this.placeId = placeId;
>>>>>>> 9cb2b20cee7ae90e7a5ea61c0ebff4e0c86a6dd6
	}

	@Override
	protected void makeRequest(ApiConnection apiConnection) throws ApiException {
		ApiConnection.ensureLogin(apiConnection, getUserSettings());
<<<<<<< HEAD
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
		apiConnection.post("http://www.ratebeer.com/Ratings/Beer/Avail-Save.asp", params);
=======
		apiConnection.get("http://ratebeer.com/json/where.asp?bd=" + beerId + "&pd=" + placeId + "&k="
				+ ApiConnection.RB_KEY, HttpURLConnection.HTTP_MOVED_PERM);
>>>>>>> 9cb2b20cee7ae90e7a5ea61c0ebff4e0c86a6dd6
	}

}
