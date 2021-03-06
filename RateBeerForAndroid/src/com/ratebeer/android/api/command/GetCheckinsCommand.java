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

public class GetCheckinsCommand extends JsonCommand {

	private final int placeID;
	private ArrayList<CheckedInUser> checkins;

	public static final int SORTBY_BEER = 1;
	public static final int SORTBY_BREWER = 2;
	public static final int SORTBY_STYLE = 3;
	public static final int SORTBY_MYRATING = 4;
	public static final int SORTBY_DATE = 5;
	public static final int SORTBY_SCORE = 6;

	public GetCheckinsCommand(UserSettings api, int placeID) {
		super(api, ApiMethod.GetCheckins);
		this.placeID = placeID;
	}

	public ArrayList<CheckedInUser> getCheckins() {
		return checkins;
	}

	@Override
	protected String makeRequest(ApiConnection apiConnection) throws ApiException {
		return apiConnection.get("http://www.ratebeer.com/json/ci.asp?t=View&p=" + Integer.toString(placeID) + "&k="
				+ ApiConnection.RB_KEY);
	}

	@Override
	protected void parse(JSONArray json) throws JSONException {

		checkins = new ArrayList<CheckedInUser>();
		for (int i = 0; i < json.length(); i++) {
			JSONObject result = json.getJSONObject(i);
			checkins.add(new CheckedInUser(result.getInt("UserID"), HttpHelper.cleanHtml(result.getString("Username"))));
		}

	}

	public static class CheckedInUser implements Parcelable {

		public final int userID;
		public final String userName;

		public CheckedInUser(int userID, String userName) {
			this.userID = userID;
			this.userName = userName;
		}

		public int describeContents() {
			return 0;
		}

		public void writeToParcel(Parcel out, int flags) {
			out.writeInt(userID);
			out.writeString(userName);
		}

		public static final Parcelable.Creator<CheckedInUser> CREATOR = new Parcelable.Creator<CheckedInUser>() {
			public CheckedInUser createFromParcel(Parcel in) {
				return new CheckedInUser(in);
			}

			public CheckedInUser[] newArray(int size) {
				return new CheckedInUser[size];
			}
		};

		private CheckedInUser(Parcel in) {
			userID = in.readInt();
			userName = in.readString();
		}

	}

}
