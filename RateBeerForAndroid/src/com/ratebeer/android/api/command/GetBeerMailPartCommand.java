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

public class GetBeerMailPartCommand extends JsonCommand {

	private final int messageId;
	private final boolean fetchReplies;
	private List<MailPart> parts;

	public GetBeerMailPartCommand(UserSettings api, int messageID, boolean fetchReplies) {
		super(api, ApiMethod.GetBeerMailPart);
		this.messageId = messageID;
		this.fetchReplies = fetchReplies;
	}

	public boolean fetchReplies() {
		return fetchReplies;
	}

	public List<MailPart> getParts() {
		return parts;
	}

	@Override
	protected String makeRequest(ApiConnection apiConnection) throws ApiException {
		ApiConnection.ensureLogin(apiConnection, getUserSettings());
		return apiConnection.get("http://www.ratebeer.com/json/message-view.asp?k=" + ApiConnection.RB_KEY
				+ "&u=" + Integer.toString(getUserSettings().getUserID()) + "&mid=" + Integer.toString(messageId)
				+ "&r=" + (fetchReplies ? "1" : "0"));
	}

	@Override
	protected void parse(JSONArray json) throws JSONException {

		parts = new ArrayList<GetBeerMailPartCommand.MailPart>();
		for (int i = 0; i < json.length(); i++) {
			JSONObject reply = json.getJSONObject(i);
			parts.add(new MailPart(messageId, reply.getString("Body"), reply.getString("Sent"),
					(reply.has("UserName") ? reply.getString("UserName") : null), reply.getInt("Source"), reply
							.getInt("Destination")));
		}

	}

	public static class MailPart implements Parcelable {

		public final int messageID;
		public final String body;
		public final String sent;
		public final String userName;
		public final int sourceUserId;
		public final int destinationUserId;

		public MailPart(int messageID, String body, String sent, String userName, int sourceUserId,
				int destinationUserId) {
			this.messageID = messageID;
			this.body = body;
			this.sent = sent;
			this.userName = userName;
			this.sourceUserId = sourceUserId;
			this.destinationUserId = destinationUserId;
		}

		public int describeContents() {
			return 0;
		}

		public void writeToParcel(Parcel out, int flags) {
			out.writeInt(messageID);
			out.writeString(body);
			out.writeString(sent);
			out.writeString(userName);
			out.writeInt(sourceUserId);
			out.writeInt(destinationUserId);
		}

		public static final Parcelable.Creator<MailPart> CREATOR = new Parcelable.Creator<MailPart>() {
			public MailPart createFromParcel(Parcel in) {
				return new MailPart(in);
			}

			public MailPart[] newArray(int size) {
				return new MailPart[size];
			}
		};

		private MailPart(Parcel in) {
			messageID = in.readInt();
			body = in.readString();
			sent = in.readString();
			userName = in.readString();
			sourceUserId = in.readInt();
			destinationUserId = in.readInt();
		}

	}

}
