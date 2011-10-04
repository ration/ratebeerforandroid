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

import android.os.Parcel;
import android.os.Parcelable;

import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.Command;
import com.ratebeer.android.api.RateBeerApi;

public class GetFavouritePlacesCommand extends Command {

	private final int beerId;
	private ArrayList<PlaceForAvailability> places;
	
	public GetFavouritePlacesCommand(RateBeerApi api, int beerId) {
		super(api, ApiMethod.GetFavouritePlaces);
		this.beerId = beerId;
	}

	public int getBeerId() {
		return beerId;
	}

	public void setPlaces(ArrayList<PlaceForAvailability> places) {
		this.places = places;
	}

	public ArrayList<PlaceForAvailability> getPlaces() {
		return places;
	}

	public static class PlaceForAvailability implements Parcelable {

		public final int placeId;
		public final String placeName;
		public final String city;
		
		public PlaceForAvailability(int placeID, String placeName, String city) {
			this.placeId = placeID;
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
		public static final Parcelable.Creator<PlaceForAvailability> CREATOR = new Parcelable.Creator<PlaceForAvailability>() {
			public PlaceForAvailability createFromParcel(Parcel in) {
				return new PlaceForAvailability(in);
			}
			public PlaceForAvailability[] newArray(int size) {
				return new PlaceForAvailability[size];
			}
		};
		private PlaceForAvailability(Parcel in) {
			placeId = in.readInt();
			placeName = in.readString();;
			city = in.readString();
		}
		
	}

}
