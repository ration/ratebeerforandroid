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
package com.ratebeer.android.app.persistance;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "BeerMail")
public class BeerMail implements Parcelable {

	public static final String MESSAGEID_FIELD_NAME = "messageId";
	
	@DatabaseField(id = true, columnName = MESSAGEID_FIELD_NAME)
	private Integer messageId;
	@DatabaseField
	private Integer senderId;
	@DatabaseField
	private String senderName;
	@DatabaseField
	private boolean read;
	@DatabaseField
	private boolean replied;
	@DatabaseField
	private Date sent;
	@DatabaseField
	private String subject;
	@DatabaseField
	private String body;
	
	public BeerMail() {
	}
	
	public BeerMail(Integer messageId, Integer senderId, String senderName, boolean read, boolean replied, Date sent,
			String subject, String body) {
		this.messageId = messageId;
		this.senderId = senderId;
		this.senderName = senderName;
		this.read = read;
		this.replied = replied;
		this.sent = sent;
		this.subject = subject;
		this.body = body;
	}

	public Integer getMessageId() {
		return messageId;
	}

	public Integer getSenderId() {
		return senderId;
	}

	public String getSenderName() {
		return senderName;
	}

	public boolean isRead() {
		return read;
	}

	public boolean isReplied() {
		return replied;
	}

	public Date getSent() {
		return sent;
	}

	public String getSubject() {
		return subject;
	}

	public String getBody() {
		return body;
	}

	/**
	 * Returns a content snippet of the subject, with restricted length
	 * @return A string with some of the mail's textual content
	 */
	public String getSomeContent(int maxLength) {
		if (subject.length() > maxLength) {
			return subject.substring(0, maxLength - 1);
		}
		return subject;
	}
	
	public void setIsRead(boolean messageRead) {
		this.read = messageRead;
	}

	public void setIsReplied(boolean messageRelied) {
		this.replied = messageRelied;
	}

	public void updateBody(String body) {
		this.body = body;
	}

	public int describeContents() {
		return 0;
	}
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(messageId);
		out.writeInt(senderId);
		out.writeString(senderName);
		out.writeInt(read? 1: 0);
		out.writeInt(replied? 1: 0);
		out.writeLong(sent == null? -1: sent.getTime());
		out.writeString(subject);
		out.writeString(body);
	}
	public static final Parcelable.Creator<BeerMail> CREATOR = new Parcelable.Creator<BeerMail>() {
		public BeerMail createFromParcel(Parcel in) {
			return new BeerMail(in);
		}
		public BeerMail[] newArray(int size) {
			return new BeerMail[size];
		}
	};
	private BeerMail(Parcel in) {
		messageId = in.readInt();
		senderId = in.readInt();
		senderName = in.readString();
		read = in.readInt() == 1;
		replied = in.readInt() == 1;
		long sentLong = in.readLong();
		sent = sentLong == -1? null: new Date(sentLong);
		subject = in.readString();
		body = in.readString();
	}

}
