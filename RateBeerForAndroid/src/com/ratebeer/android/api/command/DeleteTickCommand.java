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

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;

import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.HttpHelper;
import com.ratebeer.android.api.JsonCommand;
import com.ratebeer.android.api.UserSettings;

public class DeleteTickCommand extends JsonCommand {

	private final int beerId;
	private final int userID;
	private final String beerName;

	public DeleteTickCommand(UserSettings api, int beerId, int userID, String beerName) {
		super(api, ApiMethod.PostTick);
		this.beerId = beerId;
		this.userID = userID;
		this.beerName = beerName;
	}

	public String getBeerName() {
		return beerName;
	}

	@Override
	protected String makeRequest() throws ClientProtocolException, IOException {
		return HttpHelper.makeRBGet("http://www.ratebeer.com/json/bt.asp?k=" + HttpHelper.RB_KEY + "&m=3&u=" + userID
				+ "&b=" + beerId);
	}

	@Override
	protected void parse(JSONArray json) throws JSONException {
		// Response should be a JSON string like [{"":"Deleted"}]
		if (json.length() <= 0 || json.getJSONObject(0).length() <= 0
				|| !json.getJSONObject(0).getString("").equals("Deleted")) {
			throw new JSONException("Response contained valid JSON data but the reply was not [{\"\":\"Deleted\"}]");
		}
	}
	
}
