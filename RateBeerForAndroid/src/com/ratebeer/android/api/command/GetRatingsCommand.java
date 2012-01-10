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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.HttpHelper;
import com.ratebeer.android.api.JsonCommand;
import com.ratebeer.android.api.RateBeerApi;

public class GetRatingsCommand extends JsonCommand {

	private final int beerId;
	private ArrayList<BeerRating> ratings;

	public GetRatingsCommand(RateBeerApi api, int beerId) {
		super(api, ApiMethod.GetBeerRatings);
		this.beerId = beerId;
	}

	public ArrayList<BeerRating> getRatings() {
		return ratings;
	}

	@Override
	protected String makeRequest() throws ClientProtocolException, IOException {
		return HttpHelper.makeRBGet("http://www.ratebeer.com/json/gr.asp?k=" + HttpHelper.RB_KEY + "&bid=" + beerId);
	}

	@Override
	protected void parse(JSONArray json) throws JSONException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yyyy h:mm:ss a");
		ratings = new ArrayList<BeerRating>();
		for (int i = 0; i < json.length(); i++) {
			JSONObject result = json.getJSONObject(i);
			String entered = result.getString("TimeEntered");
			Date timeEntered = null;
			try {
				timeEntered = dateFormat.parse(entered);
			} catch (ParseException e) {
			}
			String updated = result.getString("TimeUpdated");
			Date timeUpdated = null;
			try {
				timeUpdated = dateFormat.parse(updated);
			} catch (ParseException e) {
			}
			ratings.add(new BeerRating(result.getString("resultNum"), result.getString("RatingID"), result
					.getString("Appearance"), result.getString("Aroma"), result.getString("Flavor"), result
					.getString("Mouthfeel"), result.getString("Overall"), result.getString("TotalScore"), HttpHelper
					.cleanHtml(result.getString("Comments")), timeEntered, timeUpdated, Integer.parseInt(result
					.getString("UserID")), HttpHelper.cleanHtml(result.getString("UserName")), HttpHelper
					.cleanHtml(result.getString("City")), result.getString("StateID"), HttpHelper.cleanHtml(result
					.getString("State")), result.getString("CountryID"), HttpHelper.cleanHtml(result
					.getString("Country")), result.getString("RateCount")));
		}

	}

	public static class BeerRating implements Parcelable {

		public final String resultNum;
		public final String ratingId;
		public final String appearance;
		public final String aroma;
		public final String flavor;
		public final String mouthfeel;
		public final String overall;
		public final String totalScore;
		public final String comments;
		public final Date timeEntered;
		public final Date timeUpdated;
		public final int userId;
		public final String userName;
		public final String city;
		public final String stateId;
		public final String state;
		public final String countryId;
		public final String country;
		public final String rateCount;

		public BeerRating(String resultNum, String ratingId, String appearance, String aroma, String flavor,
				String mouthfeel, String overall, String totalScore, String comments, Date timeEntered,
				Date timeUpdated, int userId, String userName, String city, String stateId, String state,
				String countryId, String country, String rateCount) {
			this.resultNum = resultNum;
			this.ratingId = ratingId;
			this.appearance = appearance;
			this.aroma = aroma;
			this.flavor = flavor;
			this.mouthfeel = mouthfeel;
			this.overall = overall;
			this.totalScore = totalScore;
			this.comments = comments;
			this.timeEntered = timeEntered;
			this.timeUpdated = timeUpdated;
			this.userId = userId;
			this.userName = userName;
			this.city = city;
			this.stateId = stateId;
			this.state = state;
			this.countryId = countryId;
			this.country = country;
			this.rateCount = rateCount;
		}

		public int describeContents() {
			return 0;
		}

		public void writeToParcel(Parcel out, int flags) {
			out.writeString(resultNum);
			out.writeString(ratingId);
			out.writeString(appearance);
			out.writeString(aroma);
			out.writeString(flavor);
			out.writeString(mouthfeel);
			out.writeString(overall);
			out.writeString(totalScore);
			out.writeString(comments);
			out.writeLong(timeEntered == null ? -1L : timeEntered.getTime());
			out.writeLong(timeUpdated == null ? -1L : timeUpdated.getTime());
			out.writeInt(userId);
			out.writeString(userName);
			out.writeString(city);
			out.writeString(stateId);
			out.writeString(state);
			out.writeString(countryId);
			out.writeString(country);
			out.writeString(rateCount);
		}

		public static final Parcelable.Creator<BeerRating> CREATOR = new Parcelable.Creator<BeerRating>() {
			public BeerRating createFromParcel(Parcel in) {
				return new BeerRating(in);
			}

			public BeerRating[] newArray(int size) {
				return new BeerRating[size];
			}
		};

		private BeerRating(Parcel in) {
			resultNum = in.readString();
			ratingId = in.readString();
			appearance = in.readString();
			aroma = in.readString();
			flavor = in.readString();
			mouthfeel = in.readString();
			overall = in.readString();
			totalScore = in.readString();
			comments = in.readString();
			long timeEnteredSeconds = in.readLong();
			long timeUpdatedSeconds = in.readLong();
			timeEntered = timeEnteredSeconds == -1L ? null : new Date(timeEnteredSeconds);
			timeUpdated = timeUpdatedSeconds == -1L ? null : new Date(timeUpdatedSeconds);
			userId = in.readInt();
			userName = in.readString();
			city = in.readString();
			stateId = in.readString();
			state = in.readString();
			countryId = in.readString();
			country = in.readString();
			rateCount = in.readString();
		}

	}

}
