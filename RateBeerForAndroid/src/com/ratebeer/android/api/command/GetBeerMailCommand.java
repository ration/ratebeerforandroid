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

import android.os.Parcel;
import android.os.Parcelable;

import com.ratebeer.android.api.ApiException;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.HttpHelper;
import com.ratebeer.android.api.JsonCommand;
import com.ratebeer.android.api.RateBeerApi;

public class GetBeerMailCommand extends JsonCommand {

	private final int messageId;
	private MailDetails mail;

	public GetBeerMailCommand(RateBeerApi api, int messageID) {
		super(api, ApiMethod.GetBeerMail);
		this.messageId = messageID;
	}

	public MailDetails getMail() {
		return mail;
	}

	@Override
	protected String makeRequest() throws ClientProtocolException, IOException, ApiException {
		RateBeerApi.ensureLogin(getUserSettings());
		String raw = HttpHelper.makeRBGet("http://www.ratebeer.com/json/message-view.asp?k=" + HttpHelper.RB_KEY + "&u="
				+ Integer.toString(getUserSettings().getUserID()) + "&mid=" + Integer.toString(messageId));
		// HACK: RateBeer returns invalid JSON; its two objects with mail data, something like:
		// For now try to identify this (where the two objects end and begin at ][) and fix the JSON
		int hackPoint = raw.indexOf("][");
		if (hackPoint >= 0)
			raw = raw.replace("][", ",");
		return raw;
	}

	@Override
	protected void parse(JSONArray json) throws JSONException {

		StringBuilder mailText = new StringBuilder();
		// HACK: The first mail is given twice, so start at index 1 to skip the first mail
		// TODO: Returns this as a list of messages to it can be displayed as properly threaded messages
		for (int i = 1; i < json.length(); i++) {
			mailText.append(json.getJSONObject(i).getString("Body"));
			mailText.append("\n\n");
		}
		mail = new MailDetails(messageId, mailText.toString());

	}

	public static class MailDetails implements Parcelable {

		public final int messageID;
		public final String body;

		public MailDetails(int messageID, String body) {
			this.messageID = messageID;
			this.body = body;
		}

		public int describeContents() {
			return 0;
		}

		public void writeToParcel(Parcel out, int flags) {
			out.writeInt(messageID);
			out.writeString(body);
		}

		public static final Parcelable.Creator<MailDetails> CREATOR = new Parcelable.Creator<MailDetails>() {
			public MailDetails createFromParcel(Parcel in) {
				return new MailDetails(in);
			}

			public MailDetails[] newArray(int size) {
				return new MailDetails[size];
			}
		};

		private MailDetails(Parcel in) {
			messageID = in.readInt();
			body = in.readString();
		}

	}

}
