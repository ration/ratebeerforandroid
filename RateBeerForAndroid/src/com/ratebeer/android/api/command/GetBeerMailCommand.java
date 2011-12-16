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

import android.os.Parcel;
import android.os.Parcelable;

import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.Command;
import com.ratebeer.android.api.RateBeerApi;

public class GetBeerMailCommand extends Command {

	private final int messageId;
	private MailDetails mail;

	public GetBeerMailCommand(RateBeerApi api, int messageID) {
		super(api, ApiMethod.GetBeerMail);
		this.messageId = messageID;
	}

	public int getMessageID() {
		return messageId;
	}

	public void setMail(MailDetails mail) {
		this.mail = mail;
	}

	public MailDetails getMail() {
		return mail;
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
