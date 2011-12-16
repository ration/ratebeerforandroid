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
import java.util.Comparator;
import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.Command;
import com.ratebeer.android.api.RateBeerApi;

public class GetUserRatingsCommand extends Command {

	private final int forUserId;
	private final int pageNr;
	private final int sortOrder;
	private ArrayList<UserRating> ratings;

	public static final int SORTBY_BEER = 1;
	public static final int SORTBY_BREWER = 2;
	public static final int SORTBY_STYLE = 3;
	public static final int SORTBY_MYRATING = 4;
	public static final int SORTBY_DATE = 5;
	public static final int SORTBY_SCORE = 6;

	public GetUserRatingsCommand(RateBeerApi api, int forUserId, int pageNr, int sortOrder) {
		super(api, ApiMethod.GetUserRatings);
		this.forUserId = forUserId;
		this.pageNr = pageNr;
		this.sortOrder = sortOrder;
	}

	public int getForUserId() {
		return forUserId;
	}

	public int getPageNr() {
		return pageNr;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public void setUserRatings(ArrayList<UserRating> ratings) {
		this.ratings = ratings;
	}

	public ArrayList<UserRating> getUserRatings() {
		return ratings;
	}

	public static class UserRating implements Parcelable {

		public final int beerId;
		public final String beerName;
		public final String brewerName;
		public final String styleName;
		public final float score;
		public final float myRating;
		public final Date date;

		public UserRating(int beerId, String beerName, String brewerName, String styleName, float score,
			float myRating, Date date) {
			this.beerId = beerId;
			this.beerName = beerName;
			this.brewerName = brewerName;
			this.styleName = styleName;
			this.score = score;
			this.myRating = myRating;
			this.date = date;
		}

		public int describeContents() {
			return 0;
		}
		public void writeToParcel(Parcel out, int flags) {
			out.writeInt(beerId);
			out.writeString(beerName);
			out.writeString(brewerName);
			out.writeString(styleName);
			out.writeFloat(score);
			out.writeFloat(myRating);
			out.writeLong(date.getTime());
		}
		public static final Parcelable.Creator<UserRating> CREATOR = new Parcelable.Creator<UserRating>() {
			public UserRating createFromParcel(Parcel in) {
				return new UserRating(in);
			}
			public UserRating[] newArray(int size) {
				return new UserRating[size];
			}
		};
		private UserRating(Parcel in) {
			beerId = in.readInt();
			beerName = in.readString();
			brewerName = in.readString();
			styleName = in.readString();
			score = in.readFloat();
			myRating = in.readFloat();
			date = new Date(in.readLong());
		}

	}

	public static class UserRatingComparator implements Comparator<UserRating> {

		private int sortOrder;

		public UserRatingComparator(int sortOrder) {
			this.sortOrder = sortOrder;
		}

		@Override
		public int compare(UserRating rating1, UserRating rating2) {
			switch (sortOrder) {
			case SORTBY_BEER:
				rating1.beerName.compareTo(rating2.beerName);
			case SORTBY_BREWER:
				rating1.brewerName.compareTo(rating2.brewerName);
			case SORTBY_STYLE:
				rating1.styleName.compareTo(rating2.styleName);
			case SORTBY_SCORE:
				new Float(rating1.score).compareTo(new Float(rating2.score));
			case SORTBY_MYRATING:
				new Float(rating1.myRating).compareTo(new Float(rating2.myRating));
			}
			// Default to date sorting
			return rating1.date.compareTo(rating2.date);
		}

	}
}
