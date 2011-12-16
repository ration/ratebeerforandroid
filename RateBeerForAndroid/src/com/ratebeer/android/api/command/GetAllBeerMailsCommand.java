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

import android.os.Parcel;
import android.os.Parcelable;

import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.Command;
import com.ratebeer.android.api.RateBeerApi;

public class GetAllBeerMailsCommand extends Command {

	private final int count;
	private ArrayList<Mail> mails;

	public GetAllBeerMailsCommand(RateBeerApi api, int count) {
		super(api, ApiMethod.GetAllBeerMails);
		this.count = count;
	}

	public int getCount() {
		return count;
	}

	public void setMail(ArrayList<Mail> mails) {
		this.mails = mails;
	}

	public ArrayList<Mail> getMails() {
		return mails;
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
			out.writeInt(messageRead? 1: 0);
			out.writeString(subject);
			out.writeInt(senderID);
			out.writeString(sent);
			out.writeInt(replied? 1: 0);
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
