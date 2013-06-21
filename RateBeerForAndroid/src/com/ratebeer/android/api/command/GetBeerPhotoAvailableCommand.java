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

import org.json.JSONException;
import org.json.JSONObject;

import com.ratebeer.android.api.ApiConnection;
import com.ratebeer.android.api.ApiException;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.Command;
import com.ratebeer.android.api.CommandFailureResult;
import com.ratebeer.android.api.CommandResult;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.api.UserSettings;

public class GetBeerPhotoAvailableCommand extends Command {

	private final int beerId;
	private boolean photoIsAvailable;

	public GetBeerPhotoAvailableCommand(UserSettings api, int beerId) {
		super(api, ApiMethod.GetBeerPhotoAvailable);
		this.beerId = beerId;
	}

	public boolean hasPhotoAvailable() {
		return photoIsAvailable;
	}

	@Override
	public final CommandResult execute(ApiConnection apiConnection) {
		try {
			String json = makeRequest(apiConnection);
			parse(new JSONObject(json));
			return new CommandSuccessResult(this);
		} catch (JSONException e) {
			return new CommandFailureResult(this, new ApiException(ApiException.ExceptionType.CommandFailed,
					"JSON parsing error: " + e.toString()));
		} catch (ApiException e) {
			return new CommandFailureResult(this, e);
		}
	}

	protected String makeRequest(ApiConnection apiConnection) throws ApiException {
		return apiConnection.get("http://www.ratebeer.com/json/piccheck.asp?k=" + ApiConnection.RB_KEY + "&bid=" + beerId);
	}

	protected void parse(JSONObject json) throws JSONException {
		photoIsAvailable = json.getBoolean("ImageExists");
	}

}
