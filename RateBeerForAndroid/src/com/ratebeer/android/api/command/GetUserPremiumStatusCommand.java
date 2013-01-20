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
import org.json.JSONException;

import com.ratebeer.android.api.ApiException;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.HtmlCommand;
import com.ratebeer.android.api.HttpHelper;
import com.ratebeer.android.api.UserSettings;

public class GetUserPremiumStatusCommand extends HtmlCommand {
	
	private boolean isPremium;
	
	public GetUserPremiumStatusCommand(UserSettings api) {
		super(api, ApiMethod.GetUserPremiumStatus);
	}

	public boolean isPremium() {
		return isPremium;
	}

	@Override
	protected String makeRequest() throws ClientProtocolException, IOException {
		return HttpHelper.makeRBGet("http://www.ratebeer.com/inbox");
	}

	@Override
	protected void parse(String html) throws JSONException, ApiException {

		// Whether this user has a premium account
		int premiumStart = html.indexOf("<span class=premie>&nbsp;P&nbsp;</span>");
		isPremium = (premiumStart >= 0);

	}
	
}
