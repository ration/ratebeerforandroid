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
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.Command;
import com.ratebeer.android.api.RateBeerApi;

public class GetUserDetailsCommand extends Command {
	
	private final int userId;
	private UserDetails details;
	
	public GetUserDetailsCommand(RateBeerApi api, int userId) {
		super(api, ApiMethod.GetUserDetails);
		this.userId = userId;
	}
	
	public int getUserId() {
		return userId;
	}

	public void setDetails(UserDetails details) {
		this.details = details;
	}

	public UserDetails getDetails() {
		return details;
	}

	public static class RecentBeerRating implements Parcelable {

		public final int id;
		public final String name;
		public final String styleName;
		public final String rating;
		public final String date;
		
		public RecentBeerRating(int id, String name, String styleName, String rating, String date) {
			this.id = id;
			this.name = name;
			this.styleName = styleName;
			this.rating = rating;
			this.date = date;
		}

		public int describeContents() {
			return 0;
		}
		public void writeToParcel(Parcel out, int flags) {
			out.writeInt(id);
			out.writeString(name);
			out.writeString(styleName);
			out.writeString(rating);
			out.writeString(date);
		}
		public static final Parcelable.Creator<RecentBeerRating> CREATOR = new Parcelable.Creator<RecentBeerRating>() {
			public RecentBeerRating createFromParcel(Parcel in) {
				return new RecentBeerRating(in);
			}
			public RecentBeerRating[] newArray(int size) {
				return new RecentBeerRating[size];
			}
		};
		private RecentBeerRating(Parcel in) {
			id = in.readInt();
			name = in.readString();
			styleName = in.readString();
			rating = in.readString();
			date = in.readString();
		}
		
	}
	
	public static class UserDetails implements Parcelable {

		public final String name;
		public final String location;
		public final String joined;
		public final String lastSeen;
		public final int beerRateCount;
		public final int placeRateCount;
		public final String avgScoreGiven;
		public final String avgBeerRated;
		public final String favStyleName;
		public final int favStyleId;
		public final List<RecentBeerRating> recentBeerRatings;
		
		public UserDetails(String name, String joined, String lastSeen, String location, int beerRateCount, 
				int placeRateCount, String avgScoreGiven, String avgBeerRated, String favStyleName, int favStyleId, List<RecentBeerRating> recentBeerRatings) {
			this.name = name;
			this.location = location;
			this.joined = joined;
			this.lastSeen = lastSeen;
			this.beerRateCount = beerRateCount;
			this.placeRateCount = placeRateCount;
			this.avgScoreGiven = avgScoreGiven;
			this.avgBeerRated = avgBeerRated;
			this.favStyleName = favStyleName;
			this.favStyleId = favStyleId;
			this.recentBeerRatings = recentBeerRatings;
		}

		public int describeContents() {
			return 0;
		}
		public void writeToParcel(Parcel out, int flags) {
			out.writeString(name);
			out.writeString(location);
			out.writeString(joined);
			out.writeString(lastSeen);
			out.writeInt(beerRateCount);
			out.writeInt(placeRateCount);
			out.writeString(avgScoreGiven);
			out.writeString(avgBeerRated);
			out.writeString(favStyleName);
			out.writeInt(favStyleId);
			out.writeTypedList(recentBeerRatings);
		}
		public static final Parcelable.Creator<UserDetails> CREATOR = new Parcelable.Creator<UserDetails>() {
			public UserDetails createFromParcel(Parcel in) {
				return new UserDetails(in);
			}
			public UserDetails[] newArray(int size) {
				return new UserDetails[size];
			}
		};
		private UserDetails(Parcel in) {
			name = in.readString();
			location = in.readString();
			joined = in.readString();
			lastSeen = in.readString();
			beerRateCount = in.readInt();
			placeRateCount = in.readInt();
			avgScoreGiven = in.readString();
			avgBeerRated = in.readString();
			favStyleName = in.readString();
			favStyleId = in.readInt();
			recentBeerRatings = new ArrayList<GetUserDetailsCommand.RecentBeerRating>();
			in.readTypedList(recentBeerRatings, RecentBeerRating.CREATOR);
		}
		
	}

}
