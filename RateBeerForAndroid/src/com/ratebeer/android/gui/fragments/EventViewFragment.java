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
package com.ratebeer.android.gui.fragments;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.commonsware.cwac.merge.MergeAdapter;
import com.google.android.maps.MapView;
import com.ratebeer.android.R;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.CommandFailureResult;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.api.HttpHelper;
import com.ratebeer.android.api.command.GetEventDetailsCommand;
import com.ratebeer.android.api.command.GetEventDetailsCommand.Attendee;
import com.ratebeer.android.api.command.GetEventDetailsCommand.EventDetails;
import com.ratebeer.android.api.command.SetEventAttendanceCommand;
import com.ratebeer.android.gui.components.ActivityUtil;
import com.ratebeer.android.gui.components.ArrayAdapter;
import com.ratebeer.android.gui.components.RateBeerActivity;
import com.ratebeer.android.gui.components.RateBeerFragment;

public class EventViewFragment extends RateBeerFragment {

	private static final String STATE_EVENTNAME = "eventName";
	private static final String STATE_EVENTID = "eventId";
	private static final String STATE_DETAILS = "details";
	private static final int MENU_SHARE = 0;

	private LayoutInflater inflater;
	private ListView eventView;

	private TextView nameText, detailsText, contactText, attendeeslabel;
	private Button locationText, timeText, setattendanceButton;
	private FrameLayout mapFrame;
	private AttendeeAdapter attendeeAdapter;
	
	protected String eventName;
	protected int eventId;
	private EventDetails details = null;
	
	public EventViewFragment() {
		this(null, -1);
	}

	/**
	 * Show a specific event's details, with the event name known in advance
	 * @param eventName The event name, or null if not known
	 * @param eventId The beer ID
	 */
	public EventViewFragment(String eventName, int eventId) {
		this.eventName = eventName;
		this.eventId = eventId;
	}
	
	/**
	 * Show a specific event's details, without the event name known in advance
	 * @param eventId The event ID
	 */
	public EventViewFragment(int eventId) {
		this(null, eventId);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		this.inflater = inflater;
		return inflater.inflate(R.layout.fragment_eventview, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		eventView = (ListView) getView().findViewById(R.id.eventview);
		if (eventView != null) {
			eventView.setAdapter(new EventViewAdapter());
			eventView.setItemsCanFocus(true);
		} else {
			// Tablet
			ListView attendeesView = (ListView) getView().findViewById(R.id.attendees);
			attendeeAdapter = new AttendeeAdapter(getActivity(), new ArrayList<Attendee>());
			attendeesView.setAdapter(attendeeAdapter);
			initFields(getView());
		}
		
		if (savedInstanceState != null) {
			eventName = savedInstanceState.getString(STATE_EVENTNAME);
			eventId = savedInstanceState.getInt(STATE_EVENTID);
			if (savedInstanceState.containsKey(STATE_DETAILS)) {
				EventDetails savedDetails = savedInstanceState.getParcelable(STATE_DETAILS);
				publishDetails(savedDetails);
			}
		} else {
			refreshDetails();
		}
		
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		MenuItem item = menu.add(RateBeerActivity.MENU_REFRESH, RateBeerActivity.MENU_REFRESH, RateBeerActivity.MENU_REFRESH, R.string.app_refresh);
		item.setIcon(R.drawable.ic_action_refresh);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		MenuItem item2 = menu.add(Menu.NONE, MENU_SHARE, MENU_SHARE, R.string.app_share);
		item2.setIcon(R.drawable.ic_action_share);
		item2.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case RateBeerActivity.MENU_REFRESH:
			refreshDetails();
			break;
		case MENU_SHARE:
			// Start a share intent for this event
			Intent s = new Intent(Intent.ACTION_SEND);
			s.setType("text/plain");
			s.putExtra(Intent.EXTRA_TEXT, getString(R.string.events_share, eventName, eventId));
			startActivity(Intent.createChooser(s, getString(R.string.events_shareevent)));
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_EVENTNAME, eventName);
		outState.putInt(STATE_EVENTID, eventId);
		if (details != null) {
			outState.putParcelable(STATE_DETAILS, details);
		}
	}

	private void refreshDetails() {
		execute(new GetEventDetailsCommand(getRateBeerActivity().getApi(), eventId));
	}

	private void onAttendeeClick(int userId, String username) {
		getRateBeerActivity().load(new UserViewFragment(username, userId));
	}
	
	@Override
	public void onTaskSuccessResult(CommandSuccessResult result) {
		if (result.getCommand().getMethod() == ApiMethod.GetEventDetails) {
			publishDetails(((GetEventDetailsCommand) result.getCommand()).getDetails());
		} else if (result.getCommand().getMethod() == ApiMethod.SetEventAttendance) {
			boolean isGoing = ((SetEventAttendanceCommand)result.getCommand()).isGoing();
			Toast.makeText(getActivity(), isGoing? R.string.events_nowattending: R.string.events_nownotattending, 
					Toast.LENGTH_SHORT).show();
			refreshDetails();
		}
	}

	private void publishDetails(EventDetails details) {
		this.details = details;
		// Update the event name and title
		this.eventName = details.name;
		// Show details
		setDetails(details);
	}

	/**
	 * Overrides the different textual details about this event, as shown as header, as well as the list of attendees
	 * @param details The event details object, which includes the list of attendees
	 */
	public void setDetails(EventDetails details) {
		nameText.setText(details.name);
		timeText.setText(details.days + "\n" + details.times);
		timeText.setVisibility(View.VISIBLE);
		locationText.setText(details.location + "\n" + (details.city != null? details.city + "\n": "") + details.address);
		locationText.setVisibility(View.VISIBLE);
		detailsText.setText(details.details);
		contactText.setText(details.contact);
		attendeeAdapter.replace(details.attendees);
		attendeeslabel.setVisibility(View.VISIBLE);
		
		if (details.isAttending(getRateBeerActivity().getUser().getUserID())) {
			setattendanceButton.setText(R.string.events_removeattendance);
		} else {
			setattendanceButton.setText(R.string.events_addattendance);
		}
		
		// Get the activity-wide MapView to show on this fragment and center on this event's location
		MapView mapView = getRateBeerActivity().requestMapViewInstance();
		try {
			if (Geocoder.isPresent()) {
				// Use Geocoder to look up the coordinates
				try {
					List<Address> point = new Geocoder(getActivity()).getFromLocationName(details.address + (details.city != null? " " + details.city: ""), 1);
					if (point.size() <= 0) {
						// Cannot find address: hide the map
						mapFrame.setVisibility(View.GONE);
					} else {
						// Found a location! Center the map here
						mapView.getController().setCenter(getRateBeerActivity().getPoint(point.get(0).getLatitude(), point.get(0).getLongitude()));
						mapView.getController().setZoom(15);
						mapFrame.setVisibility(View.VISIBLE);
					}
				} catch (IOException e) {
					// Canot connect to geocoder server: hide the map
					mapFrame.setVisibility(View.GONE);
				}
			}
		} catch (NoSuchMethodError e) {
			// Geocoder is not available at all: hide the map
			mapFrame.setVisibility(View.GONE);
		}
		mapFrame.addView(mapView);
		
	}

	private OnClickListener onLocationClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// Try to start Google Maps at the location
			Intent intent = new Intent(Intent.ACTION_VIEW);
			try {
				intent.setData(Uri.parse("geo:0,0?q=" + URLEncoder.encode(details.location + " " + details.address + 
						(details.city != null? " " + details.city: ""), HttpHelper.UTF8)));
			} catch (UnsupportedEncodingException e) {
			}
			startActivity(intent);
		}
	};
	
	private OnClickListener onTimeClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// Try to establish the time of the event
			long eventStart = -1;
			long eventEnd = -1;
			try {
				if (details.days != null && details.days.length() > 0) {
					String[] days = details.days.split("-");
					String startDay = "", endDay = "";
					if (days.length > 0)
						startDay = days[0].trim();
						eventStart = Date.parse(days[0].trim());
					if (days.length > 1) {
						endDay = days[1].trim();
						eventEnd = Date.parse(days[1].trim());
					} else if (days.length > 0) {
						endDay = days[0].trim();
						eventEnd = Date.parse(days[0].trim());
					}
					if (details.times != null && details.times.length() > 0) {
						String[] times = details.times.replace(".", ":").split("-");
						if (times.length > 0)
							eventStart = Date.parse(startDay + " " + times[0].trim());
						if (times.length > 1)
							eventEnd = Date.parse(endDay + " " + times[1].trim());
					}
				}
			} catch (Exception e) {
				// Ignore the failing of the date/time parsing
			}
			// Try to start the calendar application
			Intent intent = new Intent(Intent.ACTION_EDIT);  
			intent.setType("vnd.android.cursor.item/event");
			intent.putExtra("title", details.name);
			intent.putExtra("description", details.details);
			if (eventStart >= 0)
				intent.putExtra("beginTime", eventStart);
			if (eventEnd >= 0)
				intent.putExtra("endTime", eventEnd);
			startActivity(intent);
		}
	};

	private OnClickListener onSetAttendanceClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// Set (flip) the attendance to this event
			execute(new SetEventAttendanceCommand(getRateBeerActivity().getApi(), eventId, 
					!details.isAttending(getRateBeerActivity().getUser().getUserID())));
		}
	};
	
	@Override
	public void onTaskFailureResult(CommandFailureResult result) {
		publishException(null, result.getException());
	}

	private class EventViewAdapter extends MergeAdapter {

		public EventViewAdapter() {

			// Set the event detail fields
			View fields = getActivity().getLayoutInflater().inflate(R.layout.fragment_eventdetails, null);
			addView(fields);
			initFields(fields);
			
			// Set the list of attendees
			attendeeAdapter = new AttendeeAdapter(getActivity(), new ArrayList<GetEventDetailsCommand.Attendee>());
			addAdapter(attendeeAdapter);
		}

	}
	
	private class AttendeeAdapter extends ArrayAdapter<Attendee> {

		private OnClickListener onRowClick = new OnClickListener() {
			@Override
			public void onClick(View v) {
				onAttendeeClick((Integer)v.findViewById(R.id.user).getTag(), ((TextView)v.findViewById(R.id.user)).getText().toString());
			}
		};

		public AttendeeAdapter(Context context, List<Attendee> objects) {
			super(context, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// Get the right view, using a ViewHolder
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.list_item_attendee, null);
				ActivityUtil.makeListItemClickable(convertView, onRowClick);
				holder = new ViewHolder();
				holder.name = (TextView) convertView.findViewById(R.id.user);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			// Bind the data
			Attendee item = getItem(position);
			holder.name.setTag(item.id);
			holder.name.setText(item.name);
			
			return convertView;
		}

	}

	protected static class ViewHolder {
		TextView name;
	}

	public void initFields(View fields) {
		nameText = (TextView) fields.findViewById(R.id.name);
		timeText = (Button) fields.findViewById(R.id.time);
		locationText = (Button) fields.findViewById(R.id.location);
		detailsText = (TextView) fields.findViewById(R.id.details);
		contactText = (TextView) fields.findViewById(R.id.contact);
		mapFrame = (FrameLayout) fields.findViewById(R.id.map);
		attendeeslabel = (TextView) fields.findViewById(R.id.attendeeslabel);
		setattendanceButton = (Button) fields.findViewById(R.id.setattendance);
		locationText.setOnClickListener(onLocationClicked );
		timeText.setOnClickListener(onTimeClicked);
		setattendanceButton.setOnClickListener(onSetAttendanceClick);
	}

}
