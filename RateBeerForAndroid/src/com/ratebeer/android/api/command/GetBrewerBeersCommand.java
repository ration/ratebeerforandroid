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

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ratebeer.android.api.ApiConnection;
import com.ratebeer.android.api.ApiException;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.HttpHelper;
import com.ratebeer.android.api.JsonCommand;
import com.ratebeer.android.api.UserSettings;
import com.ratebeer.android.api.command.SearchBeersCommand.BeerSearchResult;

public class GetBrewerBeersCommand extends JsonCommand {

	private final int brewerId;
	private final int userId;
	private ArrayList<BeerSearchResult> results;

	public static final int NO_USER = -1;

	public GetBrewerBeersCommand(UserSettings api, int brewerId) {
		this(api, brewerId, NO_USER);
	}

	public GetBrewerBeersCommand(UserSettings api, int brewerId, int userId) {
		super(api, ApiMethod.GetBrewerBeers);
		this.brewerId = brewerId;
		this.userId = userId;
	}

	public ArrayList<BeerSearchResult> getBeers() {
		return results;
	}

	@Override
	protected String makeRequest(ApiConnection apiConnection) throws ApiException {
		return apiConnection.get("http://www.ratebeer.com/json/bw.asp?k=" + ApiConnection.RB_KEY + "&b=" + brewerId
				+ (userId != SearchBeersCommand.NO_USER ? "&u=" + userId : ""));
	}

	@Override
	protected void parse(JSONArray json) throws JSONException {

		// Parse the JSON response
		results = new ArrayList<BeerSearchResult>();
		for (int i = 0; i < json.length(); i++) {
			JSONObject result = json.getJSONObject(i);
			String pctl = result.getString("OverallPctl");
			results.add(new BeerSearchResult(Integer.parseInt(result.getString("BeerID")), HttpHelper.cleanHtml(result
					// TODO: This should parse as a double and be displayed as integer instead
					.getString("BeerName")), (pctl.equals("null") ? -1 : (int) Double.parseDouble(pctl)), Integer
					.parseInt(result.getString("RateCount")), result.getInt("UserHadIt") == 1, result
					.getBoolean("IsAlias"), result.getBoolean("Retired")));
		}

	}

}
