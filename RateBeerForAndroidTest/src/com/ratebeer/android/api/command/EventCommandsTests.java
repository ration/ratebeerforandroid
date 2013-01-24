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

import android.test.AndroidTestCase;

import com.ratebeer.android.api.ApiConnection;
import com.ratebeer.android.api.ApiConnection_;
import com.ratebeer.android.api.UserSettings;
import com.ratebeer.android.api.command.GetEventDetailsCommand.Attendee;
import com.ratebeer.android.api.command.GetEventDetailsCommand.EventDetails;
import com.ratebeer.android.api.command.GetEventsCommand.Event;

public class EventCommandsTests extends AndroidTestCase {

	public void testExecute() {

		// Get events for Equatorial Guinea (because this always includes the yearly Orval Appreciation Day)
		final Country country = Country.ALL_COUNTRIES.get(new Integer(66));
		final String eventName = "Annual Orval Appreciation Day";

		// GetEventsCommand test
		ApiConnection apiConnection = ApiConnection_.getInstance_(getContext());
		UserSettings user = TestHelper.getUser(getContext(), true);
		// This will get all events, i.e. http://www.ratebeer.com/FestsInMyArea.asp?CountryID=66
		GetEventsCommand eventsCommand = new GetEventsCommand(user, country, null);
		eventsCommand.execute(apiConnection);
		assertTrue(eventsCommand.getEvents() != null && eventsCommand.getEvents().size() > 0);
		// Orval Appreciation Day should be present here as first hit
		Event orval = eventsCommand.getEvents().get(0);
		assertTrue("First event is the Orval Appreciation Day", orval.eventName.indexOf(eventName) >= 0);
		assertEquals("Anytown", orval.city);

		// GetEventDetailsCommand test
		GetEventDetailsCommand eventCommand = new GetEventDetailsCommand(user, orval.eventID);
		eventCommand.execute(apiConnection);
		assertNotNull(eventCommand.getDetails());
		EventDetails event = eventCommand.getDetails();
		assertTrue("Event name includes 'Orval Appreciation Day'", event.name.indexOf(eventName) >= 0);
		assertEquals("ALL DAY(s) LONG", event.location);
		assertTrue("Orval event date is always in April", event.days.indexOf("April") >= 0);
		assertTrue("Orval event date doesn't include HTML", event.days.indexOf("<") < 0 && event.days.indexOf(">") < 0);
		assertEquals("Orval event doesn't include times", "", event.times);
		assertTrue("Decription starts with Embrace it.", event.details.startsWith("Embrace it."));
		// Also make sure there is an attendee called erickok with id 101051
		assertNotNull(event.attendees);
		Attendee erickok = null;
		for (Attendee attendee : event.attendees) {
			if (attendee.name.equals("erickok")) {
				erickok = attendee;
				break;
			}
		}
		assertNotNull(erickok);
		assertEquals(101051, erickok.id);

	}

}
