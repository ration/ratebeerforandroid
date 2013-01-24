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

import com.ratebeer.android.api.ApiConnection;
import com.ratebeer.android.api.ApiException;
import com.ratebeer.android.api.ApiException.ExceptionType;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.HtmlCommand;
import com.ratebeer.android.api.UserSettings;

public class GetAliasedBeerCommand extends HtmlCommand {
	
	private final int beerId;
	private int aliasedBeerId = -1;
	
	public GetAliasedBeerCommand(UserSettings api, int beerId) {
		super(api, ApiMethod.GetAliasedBeer);
		this.beerId = beerId;
	}
	
	public int getAliasedBeerId() {
		return aliasedBeerId;
	}

	@Override
	protected String makeRequest(ApiConnection apiConnection) throws ApiException {
		// Just open the beer page for beerId
		return apiConnection.get("http://www.ratebeer.com/beer/alias/" + beerId + "/");
	}

	@Override
	protected void parse(String html) throws JSONException, ApiException {

		// Parse the user's details and recent ratings
		int aliasStart = html.indexOf("Proceed to the aliased beer");
		if (aliasStart < 0) {
			throw new ApiException(ApiException.ExceptionType.CommandFailed,
					"The response HTML did not contain the unique event content string");
		}

		// Looks something like:
		// ...Proceed to the aliased beer...<br><br><A HREF="/beer/christoffel-wijszer/155141/" class=...
		try {
			
			final String linkText = "<A HREF=\"/beer/";
			int linkStart = html.indexOf(linkText, aliasStart) + linkText.length();
			int idStart = html.indexOf("/", linkStart) + 1;
			String id = html.substring(idStart, html.indexOf("/", idStart));
			// Set the found beer alias ID on the original command as result
			aliasedBeerId = Integer.parseInt(id);
			
		} catch (Exception e) {
			throw new ApiException(ExceptionType.CommandFailed, "Couldn't parse beer alias page; maybe RateBeer's HTML output changed again?");
		}

	}

}
