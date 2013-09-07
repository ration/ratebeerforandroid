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
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.ratebeer.android.api.ApiConnection;
import com.ratebeer.android.api.ApiException;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.JsonCommand;
import com.ratebeer.android.api.UserSettings;

public class GetUserTicksCommand extends JsonCommand {

	private ArrayList<UserTick> userTicks;

	public GetUserTicksCommand(UserSettings api) {
		super(api, ApiMethod.GetUserTicks);
	}

	public ArrayList<UserTick> getUserTicks() {
		return userTicks;
	}

	@Override
	protected String makeRequest(ApiConnection apiConnection) throws ApiException {
		// NOTE: Contrary to the documentation it is NOT possible to send any user ID (using &u=<id>) and get that 
		/// user's ticks. Instead the signed in user is (and should be) used.
		ApiConnection.ensureLogin(apiConnection, getUserSettings());
		return apiConnection.get("http://www.ratebeer.com/json/bt.asp?m=1&k=" + ApiConnection.RB_KEY);
	}

	@Override
	protected void parse(JSONArray json) throws JSONException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yyyy h:mm:ss a", Locale.US);
		userTicks = new ArrayList<UserTick>();
		for (int i = 0; i < json.length(); i++) {
			JSONObject result = json.getJSONObject(i);
			String entered = result.getString("TimeEntered");
			Date timeEntered = null;
			try {
				timeEntered = dateFormat.parse(entered);
			} catch (ParseException e) {
			}
			userTicks.add(new UserTick(result.getInt("BeerID"), result.getInt("Liked"), timeEntered));
		}

	}

	public static class UserTick implements Parcelable {

		public final int beerdId;
		public final int liked;
		public final Date timeEntered;

		public UserTick(int beerId, int liked, Date timeEntered) {
			this.beerdId = beerId;
			this.liked = liked;
			this.timeEntered = timeEntered;
		}

		public int describeContents() {
			return 0;
		}

		public void writeToParcel(Parcel out, int flags) {
			out.writeInt(beerdId);
			out.writeInt(liked);
			out.writeLong(timeEntered == null ? -1L : timeEntered.getTime());
		}

		public static final Parcelable.Creator<UserTick> CREATOR = new Parcelable.Creator<UserTick>() {
			public UserTick createFromParcel(Parcel in) {
				return new UserTick(in);
			}

			public UserTick[] newArray(int size) {
				return new UserTick[size];
			}
		};

		private UserTick(Parcel in) {
			beerdId = in.readInt();
			liked = in.readInt();
			long timeEnteredSeconds = in.readLong();
			timeEntered = timeEnteredSeconds == -1L ? null : new Date(timeEnteredSeconds);
		}

	}

}
