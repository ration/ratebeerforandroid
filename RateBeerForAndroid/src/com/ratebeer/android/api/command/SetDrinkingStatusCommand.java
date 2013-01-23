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

import java.util.Arrays;

import org.apache.http.HttpStatus;
import org.apache.http.message.BasicNameValuePair;

import com.ratebeer.android.api.ApiConnection;
import com.ratebeer.android.api.ApiException;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.EmptyResponseCommand;
import com.ratebeer.android.api.RateBeerApi;
import com.ratebeer.android.api.UserSettings;

public class SetDrinkingStatusCommand extends EmptyResponseCommand {

	private final String newStatus;

	public SetDrinkingStatusCommand(UserSettings api, String newStatus) {
		super(api, ApiMethod.SetDrinkingStatus);
		this.newStatus = newStatus;
	}

	@Override
	protected void makeRequest(ApiConnection apiConnection) throws ApiException {
		RateBeerApi.ensureLogin(apiConnection, getUserSettings());
		apiConnection.post("http://www.ratebeer.com/userstatus-process.asp",
				Arrays.asList(new BasicNameValuePair("MyStatus", newStatus)),
				// Note that we get an HTTP 404 response even when the request is successful...
				HttpStatus.SC_NOT_FOUND);
	}

}
