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

public class GetUserIdCommand extends HtmlCommand {
	
	private int userId;
	private String nowDrinking;
	private boolean isPremium;
	
	public GetUserIdCommand(RateBeerApi api) {
		super(api, ApiMethod.GetUserId);
	}
		
	public int getUserId() {
		return userId;
	}

	public String getDrinkingStatus() {
		return nowDrinking;
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

		// Searches for a unique text where the user ID can be found
		// Text looks something like: <div class="sitename"><a href="/user/101051/">erickok</a>
		// but can also be: <div class="sitename"><a href="http://www.ratebeer.com/user/101051/">erickok</a>
		String userIdText = "<a href=\"/user/";
		int userIdIndex = html.indexOf(userIdText);
		if (userIdIndex < 0) {
			throw new ApiException(ApiException.ExceptionType.CommandFailed,
					"The user ID begin HTML string was not found...");
		}
		// Get the end of the user URL, which is a /
		int userIdEnd = html.indexOf("/", userIdIndex + userIdText.length());
		userId = Integer.parseInt(html.substring(userIdIndex + userIdText.length(), userIdEnd));

		// Also look for the drinking status
		/*
		 * String drinkingText = "TODO"; int drinkingStart = response.indexOf(drinkingText); int drinkingEnd =
		 * response.indexOf("TODO", drinkingStart + drinkingText.length());
		 */
		nowDrinking = "";
		/*
		 * if (drinkingStart >= 0 && drinkingEnd > 0) { drinkingStatus = response.substring(drinkingStart +
		 * drinkingText.length(), drinkingEnd); int linkEnd = drinkingStatus.indexOf(">") + 1; if (linkEnd > 0 &&
		 * drinkingStatus.indexOf("<", linkEnd) > 0) { // Strip the 'a href' tag from this drinking status, leaving only
		 * the actual beer name drinkingStatus = drinkingStatus.substring(linkEnd, drinkingStatus.indexOf("<",
		 * linkEnd)); } drinkingStatus = HttpHelper.cleanHtml(drinkingStatus); }
		 */

		// And whether this user has a premium account
		int premiumStart = html.indexOf("<span class=premie>&nbsp;P&nbsp;</span>");
		isPremium = (premiumStart >= 0);
		
	}
	
}
