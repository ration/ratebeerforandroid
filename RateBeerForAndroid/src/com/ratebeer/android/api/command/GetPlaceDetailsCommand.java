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


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ratebeer.android.api.ApiConnection;
import com.ratebeer.android.api.ApiException;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.HttpHelper;
import com.ratebeer.android.api.JsonCommand;
import com.ratebeer.android.api.UserSettings;
import com.ratebeer.android.api.command.GetPlacesAroundCommand.Place;

public class GetPlaceDetailsCommand extends JsonCommand {

	private final int placeId;
	private Place details;

	public GetPlaceDetailsCommand(UserSettings api, int placeId) {
		super(api, ApiMethod.GetPlaceDetails);
		this.placeId = placeId;
	}

	public Place getDetails() {
		return details;
	}

	@Override
	protected String makeRequest(ApiConnection apiConnection) throws ApiException {
		return apiConnection.get("http://www.ratebeer.com/json/pss.asp?pid=" + Integer.toString(placeId) + "&k="
				+ ApiConnection.RB_KEY);
	}

	@Override
	protected void parse(JSONArray json) throws JSONException {

		JSONObject result = json.getJSONObject(0);
		String percentile = result.getString("Percentile");

		// Get the details directly form the JSON object
		details = new Place(result.getInt("PlaceID"), HttpHelper.cleanHtml(result.getString("PlaceName")),
				result.getInt("PlaceType"), HttpHelper.cleanHtml(result.getString("Address")),
				HttpHelper.cleanHtml(result.getString("City")), result.getString("StateID"),
				result.getInt("CountryID"), HttpHelper.cleanHtml(result.getString("PostalCode")),
				HttpHelper.cleanHtml(result.getString("PhoneNumber")), percentile.equals("null") ? -1
						: (int) Float.parseFloat(percentile), result.isNull("RateCount") ? 0
						: result.getInt("RateCount"), HttpHelper.cleanHtml(result.getString("PhoneAC")),
				result.getDouble("Latitude"), result.getDouble("Longitude"), -1D);

	}

}
