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

import android.os.Parcel;
import android.os.Parcelable;

import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.Command;
import com.ratebeer.android.api.RateBeerApi;

public class GetUserRatingCommand extends Command {
	
	private final int beerId;
	private OwnBeerRating rating;
	
	public GetUserRatingCommand(RateBeerApi api, int beerId) {
		super(api, ApiMethod.GetUserRating);
		this.beerId = beerId;
	}
	
	public int getBeerId() {
		return beerId;
	}

	public void setRating(OwnBeerRating rating) {
		this.rating = rating;
	}

	public OwnBeerRating getRating() {
		return rating;
	}

	public static class OwnBeerRating implements Parcelable {

		public final int ratingID;
		public final String origDate;
		public final int appearance;
		public final int aroma;
		public final int taste;
		public final int palate;
		public final int overall;
		public final String comments;
		
		public OwnBeerRating(int ratingID, String origDate, int appearance, int aroma, int taste,
			int palate, int overall, String comments) {
			this.ratingID = ratingID;
			this.origDate = origDate;
			this.appearance = appearance;
			this.aroma = aroma;
			this.taste = taste;
			this.palate = palate;
			this.overall = overall;
			this.comments = comments;
		}

		public int describeContents() {
			return 0;
		}
		public void writeToParcel(Parcel out, int flags) {
			out.writeInt(ratingID);
			out.writeString(origDate);
			out.writeInt(appearance);
			out.writeInt(aroma);
			out.writeInt(taste);
			out.writeInt(palate);
			out.writeInt(overall);
			out.writeString(comments);
		}
		public static final Parcelable.Creator<OwnBeerRating> CREATOR = new Parcelable.Creator<OwnBeerRating>() {
			public OwnBeerRating createFromParcel(Parcel in) {
				return new OwnBeerRating(in);
			}
			public OwnBeerRating[] newArray(int size) {
				return new OwnBeerRating[size];
			}
		};
		private OwnBeerRating(Parcel in) {
			ratingID = in.readInt();
			origDate = in.readString();
			appearance = in.readInt();
			aroma = in.readInt();
			taste = in.readInt();
			palate = in.readInt();
			overall = in.readInt();
			comments = in.readString();
		}
		
	}

}
