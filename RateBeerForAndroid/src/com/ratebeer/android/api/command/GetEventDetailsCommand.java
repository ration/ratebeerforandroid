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

import android.os.Parcel;
import android.os.Parcelable;

import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.Command;
import com.ratebeer.android.api.RateBeerApi;

public class GetEventDetailsCommand extends Command {
	
	private final int eventId;
	private EventDetails details;
	
	public GetEventDetailsCommand(RateBeerApi api, int eventId) {
		super(api, ApiMethod.GetEventDetails);
		this.eventId = eventId;
	}
	
	public int getEventId() {
		return eventId;
	}

	public void setDetails(EventDetails details) {
		this.details = details;
	}

	public EventDetails getDetails() {
		return details;
	}

	public static class Attendee implements Parcelable {

		public final String name;
		public final int id;
		
		public Attendee(String name, int id) {
			this.name= name;
			this.id = id;
		}

		public int describeContents() {
			return 0;
		}
		public void writeToParcel(Parcel out, int flags) {
			out.writeString(name);
			out.writeInt(id);
		}
		public static final Parcelable.Creator<Attendee> CREATOR = new Parcelable.Creator<Attendee>() {
			public Attendee createFromParcel(Parcel in) {
				return new Attendee(in);
			}
			public Attendee[] newArray(int size) {
				return new Attendee[size];
			}
		};
		private Attendee(Parcel in) {
			name = in.readString();
			id = in.readInt();
		}
		
	}
	
	public static class EventDetails implements Parcelable {

		public final String name;
		public final String days;
		public final String times;
		public final String location;
		public final String address;
		public final String city;
		public final String details;
		public final String contact;
		public final List<Attendee> attendees;
		
		public EventDetails(String name, String days, String times, String location, String address, String city, 
				String details, String contact, List<Attendee> attendees) {
			this.name = name;
			this.days = days;
			this.times = times;
			this.location = location;
			this.address = address;
			this.city = city;
			this.details = details;
			this.contact = contact;
			this.attendees = attendees;
		}

		public int describeContents() {
			return 0;
		}
		public void writeToParcel(Parcel out, int flags) {
			out.writeString(name);
			out.writeString(days);
			out.writeString(times);
			out.writeString(location);
			out.writeString(address);
			out.writeString(city);
			out.writeString(details);
			out.writeString(contact);
			out.writeTypedList(attendees);
		}
		public static final Parcelable.Creator<EventDetails> CREATOR = new Parcelable.Creator<EventDetails>() {
			public EventDetails createFromParcel(Parcel in) {
				return new EventDetails(in);
			}
			public EventDetails[] newArray(int size) {
				return new EventDetails[size];
			}
		};
		private EventDetails(Parcel in) {
			name = in.readString();
			days = in.readString();
			times = in.readString();
			location = in.readString();
			address = in.readString();
			city = in.readString();
			details = in.readString();
			contact = in.readString();
			attendees = new ArrayList<GetEventDetailsCommand.Attendee>();
			in.readTypedList(attendees, Attendee.CREATOR);
		}

		/**
		 * Return whether some user is listed as attendee to this event
		 * @param userID The ID of the user that we want to know its attendance of
		 * @return True if the user ID is found in the list of attendents, false otherwise
		 */
		public boolean isAttending(int userID) {
			for (Attendee attendee : attendees) {
				if (attendee.id == userID) {
					return true;
				}
			}
			return false;
		}
		
	}

}
