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

import com.ratebeer.android.api.ApiException;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.EmptyResponseCommand;
import com.ratebeer.android.api.HttpHelper;
import com.ratebeer.android.api.RateBeerApi;

public class SignInCommand extends EmptyResponseCommand {

	private int userId;
	private final String username;
	private final String password;

	public SignInCommand(RateBeerApi api, String username, String password) {
		super(api, ApiMethod.SignIn);
		this.username = username;
		this.password = password;
	}

	public int getUserId() {
		return userId;
	}

	@Override
	protected void makeRequest() throws ClientProtocolException, IOException, ApiException {
		userId = HttpHelper.signIn(username, password);
	}

}
