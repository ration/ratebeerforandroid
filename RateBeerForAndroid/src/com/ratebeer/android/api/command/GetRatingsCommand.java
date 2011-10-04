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
import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.Command;
import com.ratebeer.android.api.RateBeerApi;

public class GetRatingsCommand extends Command {
	
	private final int beerId;
	private ArrayList<BeerRating> ratings;
	
	public GetRatingsCommand(RateBeerApi api, int beerId) {
		super(api, ApiMethod.GetBeerRatings);
		this.beerId = beerId;
	}
	
	public int getBeerId() {
		return beerId;
	}
	
	public void setRatings(ArrayList<BeerRating> ratings) {
		this.ratings = ratings;
	}

	public ArrayList<BeerRating> getRatings() {
		return ratings;
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
		public final String userId;
		public final String userName;
		public final String city;
		public final String stateId;
		public final String state;
		public final String countryId;
		public final String country;
		public final String rateCount;

		public BeerRating(String resultNum, String ratingId, String appearance, String aroma, String flavor,
			String mouthfeel, String overall, String totalScore, String comments, Date timeEntered,
			Date timeUpdated, String userId, String userName, String city, String stateId, String state,
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
			out.writeLong(timeEntered == null? -1L: timeEntered.getTime());
			out.writeLong(timeUpdated == null? -1L: timeUpdated.getTime());
			out.writeString(userId);
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
			timeEntered = timeEnteredSeconds == -1L? null: new Date(timeEnteredSeconds);
			timeUpdated = timeUpdatedSeconds == -1L? null: new Date(timeUpdatedSeconds);
			userId = in.readString();
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
