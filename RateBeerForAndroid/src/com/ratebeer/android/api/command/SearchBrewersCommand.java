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

public class SearchBrewersCommand extends JsonCommand {

	private final String query;
	private ArrayList<BrewerSearchResult> results;

	public SearchBrewersCommand(UserSettings api, String query) {
		super(api, ApiMethod.SearchBrewers);
		this.query = query;
	}

	private String getNormalizedQuery() {
		return HttpHelper.normalizeSearchQuery(query);
	}

	public ArrayList<BrewerSearchResult> getSearchResults() {
		return results;
	}

	@Override
	protected String makeRequest() throws ClientProtocolException, IOException {
		try {
			return HttpHelper.makeRBGet("http://www.ratebeer.com/json/bss.asp?k=" + HttpHelper.RB_KEY + "&bn="
					+ URLEncoder.encode(getNormalizedQuery(), HttpHelper.UTF8));
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}

	@Override
	protected void parse(JSONArray json) throws JSONException {
		results = new ArrayList<BrewerSearchResult>();
		for (int i = 0; i < json.length(); i++) {
			JSONObject result = json.getJSONObject(i);
			results.add(new BrewerSearchResult(Integer.parseInt(result.getString("BrewerID")), HttpHelper
					.cleanHtml(result.getString("BrewerName")), HttpHelper.cleanHtml(result.getString("BrewerCity")),
					HttpHelper.cleanHtml(result.getString("Abbrev")), HttpHelper.cleanHtml(result.getString("Country"))));
		}

	}

	public static class BrewerSearchResult implements Parcelable {

		public final int brewerId;
		public final String brewerName;
		public final String city;
		public final String abbrev;
		public final String country;

		public BrewerSearchResult(int brewerId, String brewerName, String city, String abbrev, String country) {
			this.brewerId = brewerId;
			this.brewerName = brewerName;
			this.city = city;
			this.abbrev = abbrev;
			this.country = country;
		}

		public int describeContents() {
			return 0;
		}

		public void writeToParcel(Parcel out, int flags) {
			out.writeInt(brewerId);
			out.writeString(brewerName);
			out.writeString(city);
			out.writeString(abbrev);
			out.writeString(country);
		}

		public static final Parcelable.Creator<BrewerSearchResult> CREATOR = new Parcelable.Creator<BrewerSearchResult>() {
			public BrewerSearchResult createFromParcel(Parcel in) {
				return new BrewerSearchResult(in);
			}

			public BrewerSearchResult[] newArray(int size) {
				return new BrewerSearchResult[size];
			}
		};

		private BrewerSearchResult(Parcel in) {
			brewerId = in.readInt();
			brewerName = in.readString();
			city = in.readString();
			abbrev = in.readString();
			country = in.readString();
		}

	}

}
