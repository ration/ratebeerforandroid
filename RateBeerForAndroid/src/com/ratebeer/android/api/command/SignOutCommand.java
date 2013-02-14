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

import com.ratebeer.android.api.ApiConnection;
import com.ratebeer.android.api.ApiException;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.EmptyResponseCommand;
import com.ratebeer.android.api.UserSettings;

public class SignOutCommand extends EmptyResponseCommand {
	
	public SignOutCommand(UserSettings api) {
		super(api, ApiMethod.SignOut);
	}

	@Override
	protected void makeRequest(ApiConnection apiConnection) throws ApiException {
		apiConnection.signOut();
		if (!apiConnection.isSignedIn()) {
			return; // Success
		}
		throw new ApiException(ApiException.ExceptionType.CommandFailed,
				"Tried to log out but we still have session cookies");
	}

}
