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
	protected String makeRequest() throws ClientProtocolException, IOException {
		return HttpHelper.makeRBGet("http://www.ratebeer.com/json/msg.asp?k=" + HttpHelper.RB_KEY + "&u="
				+ Integer.toString(getUserSettings().getUserID()) + "&mid=" + Integer.toString(messageId));
	}

	@Override
	protected void parse(JSONArray json) throws JSONException {

		JSONObject result = json.getJSONObject(0);
		mail = new MailDetails(result.getInt("MessageID"), result.getString("Body"));

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
