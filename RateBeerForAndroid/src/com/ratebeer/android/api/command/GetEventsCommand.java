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
import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.Command;
import com.ratebeer.android.api.RateBeerApi;

public class GetEventsCommand extends Command {

	private final Country country;
	private final State state;
	private ArrayList<Event> events;
	
	public GetEventsCommand(RateBeerApi api, Country country, State state) {
		super(api, ApiMethod.GetEvents);
		this.country = country;
		this.state = state;
	}

	public Country getCountry() {
		return country;
	}
	
	public State getState() {
		return state;
	}

	public void setEvents(ArrayList<Event> results) {
		this.events = results;
	}

	public ArrayList<Event> getEvents() {
		return events;
	}

	public static class Event implements Parcelable {

		public final int eventID;
		public final String eventName;
		public final String city;
		public final Date date;
		
		public Event(int eventID, String eventName, String city, Date date) {
			this.eventID = eventID;
			this.eventName = eventName;
			this.city = city;
			this.date = date;
		}
		
		public int describeContents() {
			return 0;
		}
		public void writeToParcel(Parcel out, int flags) {
			out.writeInt(eventID);
			out.writeString(eventName);
			out.writeString(city);
			out.writeLong(date.getTime());
		}
		public static final Parcelable.Creator<Event> CREATOR = new Parcelable.Creator<Event>() {
			public Event createFromParcel(Parcel in) {
				return new Event(in);
			}
			public Event[] newArray(int size) {
				return new Event[size];
			}
		};
		private Event(Parcel in) {
			eventID = in.readInt();
			eventName = in.readString();;
			city = in.readString();
			date = new Date(in.readLong());
		}
		
	}

}
