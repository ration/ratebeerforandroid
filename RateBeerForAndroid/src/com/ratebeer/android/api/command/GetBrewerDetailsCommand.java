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

public class GetBrewerDetailsCommand extends JsonCommand {

	private final int brewerId;
	private BrewerDetails details;

	public GetBrewerDetailsCommand(UserSettings api, int beerId) {
		super(api, ApiMethod.GetBrewerDetails);
		this.brewerId = beerId;
	}

	public BrewerDetails getDetails() {
		return details;
	}

	@Override
	protected String makeRequest() throws ClientProtocolException, IOException {
		return HttpHelper.makeRBGet("http://www.ratebeer.com/json/bi.asp?k=" + HttpHelper.RB_KEY + "&b=" + brewerId);
	}

	@Override
	protected void parse(JSONArray json) throws JSONException {

		details = null;
		if (json.length() > 0) {
			JSONObject result = json.getJSONObject(0);
			details = new BrewerDetails(result.getInt("BrewerID"),
					HttpHelper.cleanHtml(result.getString("BrewerName")), HttpHelper.cleanHtml(result
							.getString("BrewerDescription")), HttpHelper.cleanHtml(result.getString("BrewerAddress")),
					HttpHelper.cleanHtml(result.getString("BrewerCity")), result.isNull("BrewerStateID") ? -1
							: result.getInt("BrewerStateID"), result.getInt("BrewerCountryID"),
					HttpHelper.cleanHtml(result.getString("BrewerZipCode")), result.getInt("BrewerTypeID"),
					HttpHelper.cleanHtml(result.getString("BrewerWebSite")), HttpHelper.cleanHtml(result
							.getString("Facebook")), HttpHelper.cleanHtml(result.getString("Twitter")),
					HttpHelper.cleanHtml(result.getString("BrewerEmail")), HttpHelper.cleanHtml(result
							.getString("BrewerPhone")), HttpHelper.cleanHtml(result.getString("Opened")));
		}

	}

	public static class BrewerDetails implements Parcelable {

		public final int brewerId;
		public final String brewerName;
		public final String description;
		public final String address;
		public final String city;
		public final int stateID;
		public final int countryID;
		public final String zipcode;
		public final int type;
		public String website;
		public String facebook;
		public String twitter;
		public String email;
		public String phone;
		public String opened;

		public BrewerDetails(int brewerID, String brewerName, String description, String address, String city,
				int stateID, int countryID, String zipcode, int type, String website, String facebook, String twitter,
				String email, String phone, String opened) {
			this.brewerId = brewerID;
			this.brewerName = brewerName;
			this.description = description;
			this.address = address;
			this.city = city;
			this.stateID = stateID;
			this.countryID = countryID;
			this.zipcode = zipcode;
			this.type = type;
			this.website = website;
			this.facebook = facebook;
			this.twitter = twitter;
			this.email = email;
			this.phone = phone;
			this.opened = opened;
		}

		public int describeContents() {
			return 0;
		}

		public void writeToParcel(Parcel out, int flags) {
			out.writeInt(brewerId);
			out.writeString(brewerName);
			out.writeString(description);
			out.writeString(address);
			out.writeString(city);
			out.writeInt(stateID);
			out.writeInt(countryID);
			out.writeString(zipcode);
			out.writeInt(type);
			out.writeString(website);
			out.writeString(facebook);
			out.writeString(twitter);
			out.writeString(email);
			out.writeString(phone);
			out.writeString(opened);
		}

		public static final Parcelable.Creator<BrewerDetails> CREATOR = new Parcelable.Creator<BrewerDetails>() {
			public BrewerDetails createFromParcel(Parcel in) {
				return new BrewerDetails(in);
			}

			public BrewerDetails[] newArray(int size) {
				return new BrewerDetails[size];
			}
		};

		private BrewerDetails(Parcel in) {
			brewerId = in.readInt();
			brewerName = in.readString();
			description = in.readString();
			address = in.readString();
			city = in.readString();
			stateID = in.readInt();
			countryID = in.readInt();
			zipcode = in.readString();
			type = in.readInt();
			website = in.readString();
			facebook = in.readString();
			twitter = in.readString();
			email = in.readString();
			phone = in.readString();
			opened = in.readString();
		}

	}

}
