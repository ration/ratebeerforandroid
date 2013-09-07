package com.ratebeer.android.api.command;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

public class GetFeedCommand extends JsonCommand {

	final public static String MODE_FRIENDS = "0";
	final public static String MODE_GLOBAL = "1";
	final public static String MODE_LOCAL = "2";
	// The different feed item types that are available (with an example LinkText)
	final public static int ITEMTYPE_BEERADDED = 1; // added a new Style Name: <a href="/beer/beer-name/beerid/">Beer
													// Name</a><span class=uaa> (5.0%)
	final public static int ITEMTYPE_BEERRATING = 7; // rated <a href="/beer/beer-name/beerid/userid/">Beer Name</a>
	final public static int ITEMTYPE_PLACERATING = 8; // reviewed <a href="/p/goto/placeid/">Place Name</a>
	final public static int ITEMTYPE_ISDRINKING = 12; // Beer Name
	final public static int ITEMTYPE_EVENTATTENDANCE = 17; // is attending <a href="/event/20713/">Event Name</a>
															// (1/1/2014 in City)
	final public static int ITEMTYPE_AWARD = 18; // Award Name
	final public static int ITEMTYPE_PLACECHECKIN = 20; // checked in at <a href="/p/place-name/placeid/">Place Name,
														// City</a>
	final public static int ITEMTYPE_REACHEDRATINGS = 21; // reached # Style Name ratings!
	final public static int ITEMTYPE_BREWERYADDED = 22; // added a new brewery: <a
														// href="/brewers/brewer-name/brewerid/">Brewer Name in City</a>

	private final int mode;
	private List<FeedItem> feedItems;

	public GetFeedCommand(UserSettings api, int mode) {
		super(api, ApiMethod.GetFeed);
		this.mode = mode;
	}

	public List<FeedItem> getFeed() {
		return feedItems;
	}

	@Override
	protected String makeRequest(ApiConnection apiConnection) throws ApiException {
		return apiConnection.get("http://www.ratebeer.com/json/feed.asp?k=" + ApiConnection.RB_KEY + "&m=" + mode);
	}

	@Override
	protected void parse(JSONArray json) throws JSONException {

		feedItems = new ArrayList<FeedItem>();
		SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yyyy h:mm:ss a", Locale.US);
		if (json.length() > 0) {
			for (int i = 0; i < json.length(); i++) {
				JSONObject item = json.getJSONObject(i);
				Date timeEntered = null;
				String timeEnteredText = item.getString("TimeEntered");
				try {
					timeEntered = dateFormat.parse(timeEnteredText);
				} catch (ParseException e) {
				}
				feedItems.add(new FeedItem(item.getInt("ActivityID"), item.getString("Username"),
						item.getInt("UserID"), item.getInt("Type"), item.getInt("LinkID"), item.getString("LinkText"),
						item.getInt("ActivityNumber"), timeEntered, item.getInt("NumComments")));
			}
		}

	}

	public static class FeedItem implements Parcelable {

		public final int activityId;
		public final String userName;
		public final int userID;
		public final int type;
		public final int linkId;
		public final String linkText;
		public final int activityNumber;
		public final Date timeEntered;
		public final int numComments;

		public FeedItem(int activityId, String userName, int userID, int type, int linkId, String linkText,
				int activityNumber, Date timeEntered, int numComments) {
			this.activityId = activityId;
			this.userName = userName;
			this.userID = userID;
			this.type = type;
			this.linkId = linkId;
			this.linkText = linkText;
			this.activityNumber = activityNumber;
			this.timeEntered = timeEntered;
			this.numComments = numComments;
		}

		public int describeContents() {
			return 0;
		}

		public void writeToParcel(Parcel out, int flags) {
			out.writeInt(activityId);
			out.writeString(userName);
			out.writeInt(userID);
			out.writeInt(type);
			out.writeInt(linkId);
			out.writeString(linkText);
			out.writeInt(activityNumber);
			out.writeLong(timeEntered.getTime());
			out.writeInt(numComments);
		}

		public static final Parcelable.Creator<FeedItem> CREATOR = new Parcelable.Creator<FeedItem>() {
			public FeedItem createFromParcel(Parcel in) {
				return new FeedItem(in);
			}

			public FeedItem[] newArray(int size) {
				return new FeedItem[size];
			}
		};

		private FeedItem(Parcel in) {
			activityId = in.readInt();
			userName = in.readString();
			userID = in.readInt();
			type = in.readInt();
			linkId = in.readInt();
			linkText = in.readString();
			activityNumber = in.readInt();
			timeEntered = new Date(in.readLong());
			numComments = in.readInt();
		}

	}

}
