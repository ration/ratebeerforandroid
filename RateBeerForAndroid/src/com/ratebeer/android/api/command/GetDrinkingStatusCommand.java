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
import com.ratebeer.android.api.RateBeerApi;

public class GetDrinkingStatusCommand extends HtmlCommand {
	
	private String nowDrinking;
	
	public GetDrinkingStatusCommand(RateBeerApi api) {
		super(api, ApiMethod.GetDrinkingStatus);
	}
		
	public String getDrinkingStatus() {
		return nowDrinking;
	}

	@Override
	protected String makeRequest() throws ClientProtocolException, IOException {
		return HttpHelper.makeRBGet("http://www.ratebeer.com/user/" + getUserSettings().getUserID() + "/stats/");
	}

	@Override
	protected void parse(String html) throws JSONException, ApiException {

		// Look for the drinking status
		nowDrinking = "";
		int drinkingStart = html.indexOf("<span class=\"userIsDrinking\">");
		if (drinkingStart >= 0) {
			String beerStartText = "is drinking";
			int beerStart = html.indexOf(beerStartText, drinkingStart);
			int beerEnd = html.indexOf("<", beerStart + beerStartText.length());
			if (beerStart >= 0) {
				nowDrinking = HttpHelper.cleanHtml(html.substring(beerStart + beerStartText.length(), beerEnd).trim());
			}
		}

	}
	
}
