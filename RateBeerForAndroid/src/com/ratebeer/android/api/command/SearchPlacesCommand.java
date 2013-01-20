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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.HttpHelper;
import com.ratebeer.android.api.JsonCommand;
import com.ratebeer.android.api.UserSettings;

public class SearchPlacesCommand extends JsonCommand {

	private final String query;
	private ArrayList<PlaceSearchResult> results;

	public SearchPlacesCommand(UserSettings api, String query) {
		super(api, ApiMethod.SearchPlaces);
		this.query = query;
	}

	private String getNormalizedQuery() {
		return HttpHelper.normalizeSearchQuery(query);
	}

	public ArrayList<PlaceSearchResult> getSearchResults() {
		return results;
	}

	@Override
	protected String makeRequest() throws ClientProtocolException, IOException {
		try {
			return HttpHelper.makeRBGet("http://www.ratebeer.com/json/psstring.asp?k=" + HttpHelper.RB_KEY + "&s="
					+ URLEncoder.encode(getNormalizedQuery(), HttpHelper.UTF8));
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}

	@Override
	protected void parse(JSONArray json) throws JSONException {

		results = new ArrayList<PlaceSearchResult>();
		for (int i = 0; i < json.length(); i++) {
			JSONObject result = json.getJSONObject(i);
			results.add(new PlaceSearchResult(Integer.parseInt(result.getString("PlaceID")), HttpHelper
					.cleanHtml(result.getString("PlaceName")), HttpHelper.cleanHtml(result.getString("City"))));
		}

	}

	public static class PlaceSearchResult implements Parcelable {

		public final int placeId;
		public final String placeName;
		public final String city;

		public PlaceSearchResult(int placeId, String placeName, String city) {
			this.placeId = placeId;
			this.placeName = placeName;
			this.city = city;
		}

		public int describeContents() {
			return 0;
		}

		public void writeToParcel(Parcel out, int flags) {
			out.writeInt(placeId);
			out.writeString(placeName);
			out.writeString(city);
		}

		public static final Parcelable.Creator<PlaceSearchResult> CREATOR = new Parcelable.Creator<PlaceSearchResult>() {
			public PlaceSearchResult createFromParcel(Parcel in) {
				return new PlaceSearchResult(in);
			}

			public PlaceSearchResult[] newArray(int size) {
				return new PlaceSearchResult[size];
			}
		};

		private PlaceSearchResult(Parcel in) {
			placeId = in.readInt();
			placeName = in.readString();
			city = in.readString();
		}

	}

}
