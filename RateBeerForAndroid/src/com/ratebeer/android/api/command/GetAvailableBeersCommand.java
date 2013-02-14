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

public class GetAvailableBeersCommand extends JsonCommand {

	private final int placeId;
	private ArrayList<AvailableBeer> results;

	public GetAvailableBeersCommand(UserSettings api, int placeId) {
		super(api, ApiMethod.GetAvailableBeers);
		this.placeId = placeId;
	}

	public ArrayList<AvailableBeer> getAvailableBeers() {
		return results;
	}

	@Override
	protected String makeRequest(ApiConnection apiConnection) throws ApiException {
		return apiConnection.get("http://www.ratebeer.com/json/beershere.asp?k=" + ApiConnection.RB_KEY + "&pid=" + placeId);
	}

	@Override
	protected void parse(JSONArray json) throws JSONException {

		// Parse the JSON response
		SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yyyy h:mm:ss a");
		results = new ArrayList<AvailableBeer>();
		for (int i = 0; i < json.length(); i++) {
			JSONObject result = json.getJSONObject(i);
			String pctl = result.getString("AverageRating");
			String entered = result.getString("TimeEntered");
			Date timeEntered = null;
			try {
				timeEntered = dateFormat.parse(entered);
			} catch (ParseException e) {
			}
			results.add(new AvailableBeer(Integer.parseInt(result.getString("BeerID")), HttpHelper.cleanHtml(result
			// TODO: This should parse as a double and be displayed as integer instead
					.getString("BeerName")), (pctl.equals("null") ? -1 : (int) Double.parseDouble(pctl)), timeEntered));
		}

	}

	public static class AvailableBeer implements Parcelable {

		public final int beerId;
		public final String beerName;
		public final int averageRating;
		public final Date timeEntered;

		public AvailableBeer(int beerId, String beerName, int averageRating, Date timeEntered) {
			this.beerId = beerId;
			this.beerName = beerName;
			this.averageRating = averageRating;
			this.timeEntered = timeEntered;
		}

		public int describeContents() {
			return 0;
		}

		public void writeToParcel(Parcel out, int flags) {
			out.writeInt(beerId);
			out.writeString(beerName);
			out.writeInt(averageRating);
			out.writeLong(timeEntered == null? -1: timeEntered.getTime());
		}

		public static final Parcelable.Creator<AvailableBeer> CREATOR = new Parcelable.Creator<AvailableBeer>() {
			public AvailableBeer createFromParcel(Parcel in) {
				return new AvailableBeer(in);
			}

			public AvailableBeer[] newArray(int size) {
				return new AvailableBeer[size];
			}
		};

		private AvailableBeer(Parcel in) {
			beerId = in.readInt();
			beerName = in.readString();
			averageRating = in.readInt();
			long timeEnteredInt = in.readLong();
			timeEntered = timeEnteredInt == -1? null: new Date(timeEnteredInt);
		}

	}

}
