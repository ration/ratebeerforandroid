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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.ratebeer.android.api.ApiConnection;
import com.ratebeer.android.api.ApiException;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.HttpHelper;
import com.ratebeer.android.api.JsonCommand;
import com.ratebeer.android.api.UserSettings;

public class GetRatingsCommand extends JsonCommand {

	public static final int NO_USER = -1;
	public static final int NO_STATE = -1;

	private final int beerId;
	private final int userId;
	private ArrayList<BeerRating> ratings;

	public GetRatingsCommand(UserSettings api, int beerId) {
		this(api, beerId, NO_USER);
	}

	public GetRatingsCommand(UserSettings api, int beerId, int userId) {
		super(api, ApiMethod.GetBeerRatings);
		this.beerId = beerId;
		this.userId = userId;
	}

	public int getForUserId() {
		return userId;
	}
	
	public ArrayList<BeerRating> getRatings() {
		return ratings;
	}

	@Override
	protected String makeRequest(ApiConnection apiConnection) throws ApiException {
		return apiConnection.get("http://www.ratebeer.com/json/gr.asp?k=" + ApiConnection.RB_KEY + "&bid=" + beerId
				+ (userId > NO_USER ? "&uid=" + Integer.toString(userId) : ""));
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
			int stateID = NO_STATE;
			if (!result.getString("StateID").equals("null")) {
				stateID = Integer.parseInt(result.getString("StateID"));
			}
			ratings.add(new BeerRating(result.getInt("RatingID"), result.getInt("Appearance"), result.getInt("Aroma"),
					result.getInt("Flavor"), result.getInt("Mouthfeel"), result.getInt("Overall"), result
							.getString("TotalScore"), HttpHelper.cleanHtml(result.getString("Comments")), timeEntered,
					timeUpdated, result.getInt("UserID"), HttpHelper.cleanHtml(result.getString("UserName")),
					HttpHelper.cleanHtml(result.getString("City")), stateID, (result.has("State") ? HttpHelper
							.cleanHtml(result.getString("State")) : ""), result.getInt("CountryID"), (result
							.has("Country") ? HttpHelper.cleanHtml(result.getString("Country")) : ""), result
							.getInt("RateCount")));
		}

	}

	public static class BeerRating implements Parcelable {

		public final int ratingId;
		public final int appearance;
		public final int aroma;
		public final int flavor;
		public final int mouthfeel;
		public final int overall;
		public final String totalScore;
		public final String comments;
		public final Date timeEntered;
		public final Date timeUpdated;
		public final int userId;
		public final String userName;
		public final String city;
		public final int stateId;
		public final String state;
		public final int countryId;
		public final String country;
		public final int rateCount;

		public BeerRating(int ratingId, int appearance, int aroma, int flavor, int mouthfeel, int overall,
				String totalScore, String comments, Date timeEntered, Date timeUpdated, int userId, String userName,
				String city, int stateId, String state, int countryId, String country, int rateCount) {
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
			out.writeInt(ratingId);
			out.writeInt(appearance);
			out.writeInt(aroma);
			out.writeInt(flavor);
			out.writeInt(mouthfeel);
			out.writeInt(overall);
			out.writeString(totalScore);
			out.writeString(comments);
			out.writeLong(timeEntered == null ? -1L : timeEntered.getTime());
			out.writeLong(timeUpdated == null ? -1L : timeUpdated.getTime());
			out.writeInt(userId);
			out.writeString(userName);
			out.writeString(city);
			out.writeInt(stateId);
			out.writeString(state);
			out.writeInt(countryId);
			out.writeString(country);
			out.writeInt(rateCount);
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
			ratingId = in.readInt();
			appearance = in.readInt();
			aroma = in.readInt();
			flavor = in.readInt();
			mouthfeel = in.readInt();
			overall = in.readInt();
			totalScore = in.readString();
			comments = in.readString();
			long timeEnteredSeconds = in.readLong();
			long timeUpdatedSeconds = in.readLong();
			timeEntered = timeEnteredSeconds == -1L ? null : new Date(timeEnteredSeconds);
			timeUpdated = timeUpdatedSeconds == -1L ? null : new Date(timeUpdatedSeconds);
			userId = in.readInt();
			userName = in.readString();
			city = in.readString();
			stateId = in.readInt();
			state = in.readString();
			countryId = in.readInt();
			country = in.readString();
			rateCount = in.readInt();
		}

	}

}
