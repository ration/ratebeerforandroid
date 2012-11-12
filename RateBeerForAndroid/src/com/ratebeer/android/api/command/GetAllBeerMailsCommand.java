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

import com.ratebeer.android.api.ApiException;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.HttpHelper;
import com.ratebeer.android.api.JsonCommand;
import com.ratebeer.android.api.RateBeerApi;

public class GetAllBeerMailsCommand extends JsonCommand {

	private ArrayList<Mail> mails;

	public GetAllBeerMailsCommand(RateBeerApi api) {
		super(api, ApiMethod.GetAllBeerMails);
	}

	public ArrayList<Mail> getMails() {
		return mails;
	}

	@Override
	protected String makeRequest() throws ClientProtocolException, IOException, ApiException {
		RateBeerApi.ensureLogin(getUserSettings());
		// HACK: Don't set the &max=X parameter because then we will receive incorrect MessageRead values... :(
		return HttpHelper.makeRBGet("http://www.ratebeer.com/json/msg.asp?k=" + HttpHelper.RB_KEY + "&u="
				+ Integer.toString(getUserSettings().getUserID()));
	}

	@Override
	protected void parse(JSONArray json) throws JSONException {

		// Parse the JSON response
		mails = new ArrayList<Mail>();
		for (int i = 0; i < json.length(); i++) {
			JSONObject result = json.getJSONObject(i);
			// HACK: MessageRead not set properly on old-style beer mail
			// A message is read when the MessageRead value not set to false
			boolean messageRead = !result.getString("MessageRead").equals("false");
			// A message is replied if it is not yet read and the Reply value is set to 1
			boolean hasReplied = !messageRead && result.getString("Reply").equals("1");
			mails.add(new Mail(result.getInt("MessageID"), HttpHelper.cleanHtml(result.getString("UserName")),
					messageRead, HttpHelper.cleanHtml(result.getString("Subject")), result.getInt("Source"), 
					HttpHelper.cleanHtml(result.getString("Sent")), hasReplied));
		}

	}

	public static class Mail implements Parcelable {

		public final int messageID;
		public final int senderID;
		public final String senderName;
		public final boolean messageRead;
		public final String subject;
		public final String sent;
		public final boolean replied;

		public Mail(int messageID, String senderName, boolean messageRead, String subject, int senderID, String sent,
				boolean replied) {
			this.messageID = messageID;
			this.senderName = senderName;
			this.messageRead = messageRead;
			this.subject = subject;
			this.senderID = senderID;
			this.sent = sent;
			this.replied = replied;
		}

		public int describeContents() {
			return 0;
		}

		public void writeToParcel(Parcel out, int flags) {
			out.writeInt(messageID);
			out.writeString(senderName);
			out.writeInt(messageRead ? 1 : 0);
			out.writeString(subject);
			out.writeInt(senderID);
			out.writeString(sent);
			out.writeInt(replied ? 1 : 0);
		}

		public static final Parcelable.Creator<Mail> CREATOR = new Parcelable.Creator<Mail>() {
			public Mail createFromParcel(Parcel in) {
				return new Mail(in);
			}

			public Mail[] newArray(int size) {
				return new Mail[size];
			}
		};

		private Mail(Parcel in) {
			messageID = in.readInt();
			senderName = in.readString();
			messageRead = in.readInt() == 1;
			subject = in.readString();
			senderID = in.readInt();
			sent = in.readString();
			replied = in.readInt() == 1;

		}

	}

}
