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

public class GetTopBeersCommand extends JsonCommand {

	private final TopListType topList;
	private final Country country;
	private ArrayList<TopBeer> beers;

	public GetTopBeersCommand(UserSettings api) {
		this(api, TopListType.Top50, null);
	}

	public GetTopBeersCommand(UserSettings api, Country country) {
		this(api, TopListType.TopByCountry, country);
	}

	private GetTopBeersCommand(UserSettings api, TopListType topList, Country country) {
		super(api, ApiMethod.GetTopBeers);
		this.topList = topList;
		this.country = country;
	}

	public ArrayList<TopBeer> getBeers() {
		return beers;
	}

	/**
	 * The type of top list to retrieve
	 */
	public static enum TopListType {
		Top50, TopByCountry
	}

	public static class TopBeer implements Parcelable {

		public final int orderNr;
		public final int beerId;
		public final String beerName;
		public final double score;
		public final int rateCount;
		public final String styleName;

		public TopBeer(int orderNr, int beerId, String beerName, double score, int rateCount, String styleName) {
			this.orderNr = orderNr;
			this.beerId = beerId;
			this.beerName = beerName;
			this.score = score;
			this.rateCount = rateCount;
			this.styleName = styleName;
		}

		public int describeContents() {
			return 0;
		}

		public void writeToParcel(Parcel out, int flags) {
			out.writeInt(orderNr);
			out.writeInt(beerId);
			out.writeString(beerName);
			out.writeDouble(score);
			out.writeInt(rateCount);
			out.writeString(styleName);
		}

		public static final Parcelable.Creator<TopBeer> CREATOR = new Parcelable.Creator<TopBeer>() {
			public TopBeer createFromParcel(Parcel in) {
				return new TopBeer(in);
			}

			public TopBeer[] newArray(int size) {
				return new TopBeer[size];
			}
		};

		private TopBeer(Parcel in) {
			orderNr = in.readInt();
			beerId = in.readInt();
			beerName = in.readString();
			score = in.readDouble();
			rateCount = in.readInt();
			styleName = in.readString();
		}

	}

	@Override
	protected String makeRequest() throws ClientProtocolException, IOException {
		switch (topList) {
		case Top50:
			return HttpHelper.makeRBGet("http://www.ratebeer.com/json/tb.asp?m=top50&k=" + HttpHelper.RB_KEY);
		case TopByCountry:
			return HttpHelper.makeRBGet("http://www.ratebeer.com/json/tb.asp?m=country&c=" + country.getId() + "&k="
					+ HttpHelper.RB_KEY);
		}
		return null;
	}

	@Override
	protected void parse(JSONArray json) throws JSONException {
		beers = new ArrayList<TopBeer>();
		for (int i = 0; i < json.length(); i++) {
			JSONObject result = json.getJSONObject(i);
			double score = Double.parseDouble(result.getString("AverageRating"));
			String beerStyle = result.has("BeerStyleName") ? HttpHelper.cleanHtml(result.getString("BeerStyleName"))
					: "";
			beers.add(new TopBeer(i + 1, result.getInt("BeerID"), HttpHelper.cleanHtml(result.getString("BeerName")),
					score, result.getInt("RateCount"), beerStyle));
		}
	}

}
