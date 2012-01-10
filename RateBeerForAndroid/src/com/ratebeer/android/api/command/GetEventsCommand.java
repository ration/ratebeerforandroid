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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import android.os.Parcel;
import android.os.Parcelable;

import com.ratebeer.android.api.ApiException;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.HtmlCommand;
import com.ratebeer.android.api.HttpHelper;
import com.ratebeer.android.api.RateBeerApi;

public class GetEventsCommand extends HtmlCommand {

	private final Country country;
	private final State state;
	private ArrayList<Event> events;

	public GetEventsCommand(RateBeerApi api, Country country, State state) {
		super(api, ApiMethod.GetEvents);
		this.country = country;
		this.state = state;
	}

	public ArrayList<Event> getEvents() {
		return events;
	}

	@Override
	protected String makeRequest() throws ClientProtocolException, IOException {
		return HttpHelper.makeRBGet("http://www.ratebeer.com/FestsInMyArea.asp?CountryID=" + country.getId()
				+ (state == null ? "" : "&StateID=" + state.getId()));
	}

	@Override
	protected void parse(String html) throws JSONException, ApiException {

		// Parse the events table
		int tableStart = html.indexOf("REGIONAL EVENTS");
		if (tableStart < 0) {
			throw new ApiException(ApiException.ExceptionType.CommandFailed,
					"The response HTML did not contain the unique top beers table begin HTML string");
		}
		String rowText = "Events-Detail.asp?EventID=";
		int rowPresent = html.indexOf(rowText, tableStart);
		if (rowPresent < 0) {
			events = new ArrayList<GetEventsCommand.Event>();
			return; // Success, just empty
		}
		int rowStart = rowPresent + rowText.length();
		events = new ArrayList<Event>();
		SimpleDateFormat americanDate = new SimpleDateFormat("M/d/y");

		while (rowStart > 0 + rowText.length()) {

			int eventId = Integer.parseInt(html.substring(rowStart, html.indexOf("\"", rowStart)));

			int nameStart = html.indexOf(">", rowStart) + ">".length();
			String event = HttpHelper.cleanHtml(html.substring(nameStart, html.indexOf("<", nameStart)));

			int cityStart = html.indexOf("#999999>&nbsp;", nameStart) + "#999999>&nbsp;".length();
			String city = html.substring(cityStart, html.indexOf("&", cityStart));

			int dateStart = html.indexOf("#999999>&nbsp;", cityStart) + "#999999>&nbsp;".length();
			String dateString = html.substring(dateStart, html.indexOf("&", dateStart));
			Date date = null;
			try {
				date = americanDate.parse(dateString);
			} catch (ParseException e) {
			}

			events.add(new Event(eventId, event, city, date));
			rowStart = html.indexOf(rowText, dateStart) + rowText.length();
		}
		

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
			eventName = in.readString();
			;
			city = in.readString();
			date = new Date(in.readLong());
		}

	}

}
