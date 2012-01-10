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
import com.ratebeer.android.api.RateBeerApi;

public class GetPlacesAroundCommand extends JsonCommand {

	private final int radius;
	private final double latitude;
	private final double longitude;
	private ArrayList<Place> places;

	public static final int SORTBY_BEER = 1;
	public static final int SORTBY_BREWER = 2;
	public static final int SORTBY_STYLE = 3;
	public static final int SORTBY_MYRATING = 4;
	public static final int SORTBY_DATE = 5;
	public static final int SORTBY_SCORE = 6;

	public GetPlacesAroundCommand(RateBeerApi api, int radius, double latitude, double longitude) {
		super(api, ApiMethod.GetPlacesAround);
		this.radius = radius;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public ArrayList<Place> getPlaces() {
		return places;
	}

	@Override
	protected String makeRequest() throws ClientProtocolException, IOException {
		return HttpHelper.makeRBGet("http://ratebeer.com/json/beerme.asp?k=" + HttpHelper.RB_KEY + "&mi=" + radius
				+ "&la=" + latitude + "&lo=" + longitude);
	}

	@Override
	protected void parse(JSONArray json) throws JSONException {

		places = new ArrayList<Place>();
		for (int i = 0; i < json.length(); i++) {
			JSONObject result = json.getJSONObject(i);
			String avgRating = result.getString("AvgRating");
			places.add(new Place(result.getInt("PlaceID"), HttpHelper.cleanHtml(result.getString("PlaceName")), result
					.getInt("PlaceType"), HttpHelper.cleanHtml(result.getString("Address")), HttpHelper
					.cleanHtml(result.getString("City")), result.getString("StateID"), result.getInt("CountryID"),
					HttpHelper.cleanHtml(result.getString("PostalCode")), HttpHelper.cleanHtml(result
							.getString("PhoneNumber")), avgRating.equals("null") ? -1 : (int) Float
							.parseFloat(avgRating), HttpHelper.cleanHtml(result.getString("PhoneAC")), result
							.getDouble("Latitude"), result.getDouble("Longitude"), result.getDouble("Distance")));
		}

	}

	public static class Place implements Parcelable {

		public final int placeID;
		public final String placeName;
		public final int placeType;
		public final String address;
		public final String city;
		public final String stateID;
		public final int countryID;
		public final String postalCode;
		public final String phoneNumber;
		public final int avgRating;
		public final String phoneAc;
		public final double latitude;
		public final double longitude;
		public final double distance;

		public Place(int placeID, String placeName, int placeType, String address, String city, String stateID,
				int countryID, String postalCode, String phoneNumber, int avgRating, String phoneAc, double latitude,
				double longitude, double distance) {
			this.placeID = placeID;
			this.placeName = placeName;
			this.placeType = placeType;
			this.address = address;
			this.city = city;
			this.stateID = stateID;
			this.countryID = countryID;
			this.postalCode = postalCode;
			this.phoneNumber = phoneNumber;
			this.phoneAc = phoneAc;
			this.avgRating = avgRating;
			this.latitude = latitude;
			this.longitude = longitude;
			this.distance = distance;
		}

		public int describeContents() {
			return 0;
		}

		public void writeToParcel(Parcel out, int flags) {
			out.writeInt(placeID);
			out.writeString(placeName);
			out.writeInt(placeType);
			out.writeString(address);
			out.writeString(city);
			out.writeString(stateID);
			out.writeInt(countryID);
			out.writeString(postalCode);
			out.writeString(phoneNumber);
			out.writeInt(avgRating);
			out.writeString(phoneAc);
			out.writeDouble(latitude);
			out.writeDouble(longitude);
			out.writeDouble(distance);
		}

		public static final Parcelable.Creator<Place> CREATOR = new Parcelable.Creator<Place>() {
			public Place createFromParcel(Parcel in) {
				return new Place(in);
			}

			public Place[] newArray(int size) {
				return new Place[size];
			}
		};

		private Place(Parcel in) {
			placeID = in.readInt();
			placeName = in.readString();
			placeType = in.readInt();
			address = in.readString();
			city = in.readString();
			stateID = in.readString();
			countryID = in.readInt();
			postalCode = in.readString();
			phoneNumber = in.readString();
			avgRating = in.readInt();
			phoneAc = in.readString();
			latitude = in.readDouble();
			longitude = in.readDouble();
			distance = in.readDouble();
		}

	}

}
