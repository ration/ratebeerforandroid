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
package com.ratebeer.android.api;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import com.ratebeer.android.api.command.SignInCommand;

public class RateBeerApi {

	private RateBeerApi() {
	}
	
	public static void ensureLogin(UserSettings userSettings) throws ClientProtocolException, IOException, ApiException {
		// Make sure we are logged in
		if (!HttpHelper.isSignedIn()) {
			new SignInCommand(userSettings, userSettings.getUsername(), userSettings.getPassword()).execute();
			if (!HttpHelper.isSignedIn()) {
				throw new ApiException(ApiException.ExceptionType.AuthenticationFailed,
						"Tried to sign in but no (login) cookies were returned by the server");
			}
		}

	}

}
