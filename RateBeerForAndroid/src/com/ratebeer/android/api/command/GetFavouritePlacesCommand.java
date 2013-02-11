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

import java.util.ArrayList;

import org.json.JSONException;

import com.ratebeer.android.api.ApiConnection;
import com.ratebeer.android.api.ApiException;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.HtmlCommand;
import com.ratebeer.android.api.HttpHelper;
import com.ratebeer.android.api.UserSettings;
import com.ratebeer.android.api.command.SearchPlacesCommand.PlaceSearchResult;

public class GetFavouritePlacesCommand extends HtmlCommand {

	private final int beerId;
	private ArrayList<PlaceSearchResult> places;
	
	public GetFavouritePlacesCommand(UserSettings api, int beerId) {
		super(api, ApiMethod.GetFavouritePlaces);
		this.beerId = beerId;
	}

	public ArrayList<PlaceSearchResult> getPlaces() {
		return places;
	}

	@Override
	protected String makeRequest(ApiConnection apiConnection) throws ApiException {
		ApiConnection.ensureLogin(apiConnection, getUserSettings());
		return apiConnection.get("http://www.ratebeer.com/beer/availability-add/" + beerId + "/");
	}

	@Override
	protected void parse(String html) throws JSONException, ApiException {

		// Parse the favourite places table
		int tableStart = html.indexOf("<div id=\"likely\"");
		if (tableStart < 0) {
			throw new ApiException(ApiException.ExceptionType.CommandFailed,
					"The response HTML did not contain the unique favourites table begin HTML string");
		}
		String rowText = "<INPUT name=\"placeid\" type=checkbox value=";
		int rowStart = html.indexOf(rowText, tableStart) + rowText.length();
		places = new ArrayList<PlaceSearchResult>();

		while (rowStart > 0 + rowText.length()) {

			int placeId = Integer.parseInt(html.substring(rowStart, html.indexOf(" ", rowStart)));

			int placeNameStart = html.indexOf(".click()\">", rowStart) + ".click()\">".length();
			String placeName = HttpHelper.cleanHtml(html.substring(placeNameStart, html.indexOf("<", placeNameStart)));

			int cityStart = html.indexOf("><em>in ", placeNameStart) + "><em>in ".length();
			String city = HttpHelper.cleanHtml(html.substring(cityStart, html.indexOf("<", cityStart)));

			places.add(new PlaceSearchResult(placeId, placeName, city));
			rowStart = html.indexOf(rowText, cityStart) + rowText.length();
		}
	
	}
	
}
