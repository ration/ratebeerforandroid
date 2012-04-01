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
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import android.os.Parcel;
import android.os.Parcelable;

import com.ratebeer.android.api.ApiException;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.HtmlCommand;
import com.ratebeer.android.api.HttpHelper;
import com.ratebeer.android.api.RateBeerApi;

public class GetEventDetailsCommand extends HtmlCommand {

	private final int eventId;
	private EventDetails details;

	public GetEventDetailsCommand(RateBeerApi api, int eventId) {
		super(api, ApiMethod.GetEventDetails);
		this.eventId = eventId;
	}

	public EventDetails getDetails() {
		return details;
	}

	@Override
	protected String makeRequest() throws ClientProtocolException, IOException, ApiException {
		RateBeerApi.ensureLogin(getUserSettings());
		return HttpHelper.makeRBGet("http://www.ratebeer.com/Events-Detail.asp?EventID=" + eventId);
	}

	@Override
	protected void parse(String html) throws JSONException, ApiException {

		// Parse the user's existing rating
		int eventStart = html.indexOf("<a href=/events.php>Beer Events and Festivals</a>");
		if (eventStart < 0) {
			throw new ApiException(ApiException.ExceptionType.CommandFailed,
					"The response HTML did not contain the unique event content string");
		}

		int nameStart = html.indexOf("<h1>", eventStart) + "<h1>".length();
		String name = HttpHelper.cleanHtml(html.substring(nameStart, html.indexOf("</h1>", nameStart)));

		int daysStart = html.indexOf("<h2>", nameStart) + "<h2>".length();
		String daysRaw = html.substring(daysStart, html.indexOf("</h2>", daysStart)).trim();
		String days, times;
		String multiday = "<font color=\"#FFFFFF\">Multiday</font><br>";
		if (daysRaw.startsWith(multiday)) {
			String daysStipped = daysRaw.substring(multiday.length());
			int timeSep1 = daysStipped.indexOf("-");
			int timeSep2 = daysStipped.indexOf("-", timeSep1 + 1);
			days = HttpHelper.cleanHtml(timeSep1 < 0 || timeSep2 < 0? daysStipped: daysStipped.substring(0, timeSep2));
			if (timeSep1 < 0 || timeSep2 < 0) {
				times = "";
			} else {
				String timesRaw = daysStipped.substring(timeSep2).trim();
				times = HttpHelper.cleanHtml(timesRaw.startsWith("-")? timesRaw.substring(1).trim(): timesRaw);
			}
		} else {
			int timeSep = daysRaw.indexOf("-");
			days = HttpHelper.cleanHtml(timeSep < 0? daysRaw.trim(): daysRaw.substring(0, timeSep).trim());
			times = HttpHelper.cleanHtml(timeSep <0? "": daysRaw.substring(timeSep + 1).trim());
		}

		int locationStart = html.indexOf("</h2><br>", daysStart) + "</h2><br>".length();
		String location = HttpHelper.cleanHtml(html.substring(locationStart, html.indexOf("<", locationStart)));

		int addressStart = html.indexOf("\">", locationStart) + "\">".length();
		String address = HttpHelper.cleanHtml(html.substring(addressStart, html.indexOf(" [ map ]", addressStart)).trim());

		String detailsText = "<strong><h3>Details</h3></strong><br>";
		int detailsStart = html.indexOf(detailsText, addressStart) + detailsText.length();
		String details = HttpHelper.cleanHtml(html.substring(detailsStart, html.indexOf("<b>Cost:", detailsStart))).trim();

		String contactText = "<h3>Contact Info</h3><br>";
		int contactStart = html.indexOf(contactText, detailsStart) + contactText.length();
		String contact = HttpHelper.cleanHtml(html.substring(contactStart, html.indexOf("</i>", contactStart))).trim();

		List<Attendee> attendees = new ArrayList<Attendee>();
		String attendeeText = "<br><a href=/user/";
		int attendeeStart = html.indexOf(attendeeText, contactStart);
		while (attendeeStart >= 0) {
			int attendeeIdStart = attendeeStart + attendeeText.length();
			int attendeeIdEnd = html.indexOf("/", attendeeIdStart);
			int attendeeId = Integer.parseInt(html.substring(attendeeIdStart, attendeeIdEnd));
			String attendeeName = html.substring(attendeeIdEnd + 2, html.indexOf("<", attendeeIdEnd));
			attendees.add(new Attendee(attendeeName, attendeeId));
			attendeeStart = html.indexOf(attendeeText, attendeeIdStart);
		}

		// Set the user's rating on the original command as result
		this.details = new EventDetails(name, days, times, location, address, null, details, contact, attendees);
		
	}

	public static class Attendee implements Parcelable {

		public final String name;
		public final int id;

		public Attendee(String name, int id) {
			this.name = name;
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
