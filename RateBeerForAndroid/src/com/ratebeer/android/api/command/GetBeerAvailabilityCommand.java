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

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.HttpHelper;
import com.ratebeer.android.api.JsonCommand;
import com.ratebeer.android.api.UserSettings;
import com.ratebeer.android.api.command.SearchPlacesCommand.PlaceSearchResult;

public class GetBeerAvailabilityCommand extends JsonCommand {

	private final int beerId;
	private ArrayList<PlaceSearchResult> results;

	public GetBeerAvailabilityCommand(UserSettings api, int beerId) {
		super(api, ApiMethod.GetBeerAvailability);
		this.beerId = beerId;
	}

	public ArrayList<PlaceSearchResult> getPlaces() {
		return results;
	}

	@Override
	protected String makeRequest() throws ClientProtocolException, IOException {
		return HttpHelper.makeRBGet("http://ratebeer.com/json/where.asp?k=" + HttpHelper.RB_KEY + "&bd=" + beerId);
	}

	@Override
	protected void parse(JSONArray json) throws JSONException {

		results = new ArrayList<PlaceSearchResult>();
		for (int i = 0; i < json.length(); i++) {
			// {"PlaceID":4413,"PlaceName":"Keg Liquors","Latitude":38.312,"Longitude":-85.766,"PostalCode":"47129",
			// "Abbrev":"IN ","Country":"United States ","ServedBottle":false,"ServedTap":false}
			JSONObject result = json.getJSONObject(i);
			results.add(new PlaceSearchResult(Integer.parseInt(result.getString("PlaceID")), HttpHelper
					.cleanHtml(result.getString("PlaceName")), HttpHelper.cleanHtml(result.getString("Country"))));
		}

	}

}
