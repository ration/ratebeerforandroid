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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

import com.ratebeer.android.R;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.CommandFailureResult;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.api.command.Country;
import com.ratebeer.android.api.command.GetEventsCommand;
import com.ratebeer.android.api.command.State;
import com.ratebeer.android.api.command.GetEventsCommand.Event;
import com.ratebeer.android.gui.components.ArrayAdapter;
import com.ratebeer.android.gui.components.RateBeerActivity;
import com.ratebeer.android.gui.components.RateBeerFragment;

public class EventsFragment extends RateBeerFragment {

	private static final String STATE_COUNTRY = "country";
	private static final String STATE_STATE = "state";
	private static final String STATE_EVENTS = "events";
	private static final DateFormat localFormat = DateFormat.getDateInstance();

	private LayoutInflater inflater;
	private TextView emptyText;
	private ListView eventsView;
	private Spinner countrySpinner;
	private Spinner stateSpinner;

	private Country country = null;
	private State state = null;
	private ArrayList<Event> events = null;

	public EventsFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		this.inflater = inflater;
		return inflater.inflate(R.layout.fragment_events, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		emptyText = (TextView) getView().findViewById(R.id.empty);
		eventsView = (ListView) getView().findViewById(R.id.events);
		countrySpinner = (Spinner) getView().findViewById(R.id.country);
		countrySpinner.setOnItemSelectedListener(onCountrySelected);
		stateSpinner = (Spinner) getView().findViewById(R.id.state);
		stateSpinner.setOnItemSelectedListener(onStateSelected);
		//stateLabel = (TextView) getView().findViewById(R.id.statelabel);
		eventsView.setOnItemClickListener(onItemSelected);
		
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(STATE_COUNTRY)) {
				country = Country.ALL_COUNTRIES.get(savedInstanceState.getInt(STATE_COUNTRY));
			}
			if (savedInstanceState.containsKey(STATE_STATE)) {
				state = State.ALL_STATES.get(country.getId()).get(savedInstanceState.getInt(STATE_STATE));
			}
			populateCountrySpinner();
			populateStateSpinner();
			if (savedInstanceState.containsKey(STATE_EVENTS)) {
				ArrayList<Event> savedEvents = savedInstanceState.getParcelableArrayList(STATE_EVENTS);
				publishResults(savedEvents);
			}
		} else {
			populateCountrySpinner();
			populateStateSpinner();
			refreshEvents();
		}
		
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		MenuItem item = menu.add(RateBeerActivity.MENU_REFRESH, RateBeerActivity.MENU_REFRESH, RateBeerActivity.MENU_REFRESH, R.string.app_refresh);
		item.setIcon(R.drawable.ic_action_refresh);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case RateBeerActivity.MENU_REFRESH:
			refreshEvents();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void populateCountrySpinner() {
		// Get an array with all the country names
		Country[] allCountries = Country.ALL_COUNTRIES.values().toArray(new Country[Country.ALL_COUNTRIES.size()]);
		android.widget.ArrayAdapter<Country> adapter = new android.widget.ArrayAdapter<Country>(getActivity(), android.R.layout.simple_spinner_item, allCountries);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		countrySpinner.setAdapter(adapter);
		// Select the last used country, if known
		if (getRateBeerActivity().getSettings().getLastUsedCountry() != null) {
			for (int j = 0; j < allCountries.length; j++) {
				if (allCountries[j].getId() == getRateBeerActivity().getSettings().getLastUsedCountry().getId()) {
					countrySpinner.setSelection(j);
					break;
				}
			}
		}
	}

	private void populateStateSpinner() {
		State[] allStates;
		if (country == null || State.ALL_STATES.get(country.getId()) == null) {
			// No states
			allStates = new State[] {};
		} else {
			// Get states for the selected country
			allStates = State.ALL_STATES.get(country.getId()).values().toArray(new State[State.ALL_STATES.get(country.getId()).size()]);
		}
		android.widget.ArrayAdapter<State> adapter = new android.widget.ArrayAdapter<State>(getActivity(), android.R.layout.simple_spinner_item, allStates);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		stateSpinner.setAdapter(adapter);
		// Select the last used state, if known
		state = null;
		if (getRateBeerActivity().getSettings().getLastUsedState() != null) {
			for (int j = 0; j < allStates.length; j++) {
				if (allStates[j].getId() == getRateBeerActivity().getSettings().getLastUsedState().getId()) {
					stateSpinner.setSelection(j);
					break;
				}
			}
		}
	}

	private OnItemSelectedListener onCountrySelected = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			// Some country was selected; load the new top beers list
			country = (Country) countrySpinner.getSelectedItem();
			getRateBeerActivity().getSettings().saveLastUsedCountry(country);
			populateStateSpinner();
			if (country == null || State.ALL_STATES.get(country.getId()) == null) {
				refreshEvents();
			}
		}
		@Override
		public void onNothingSelected(AdapterView<?> arg0) {}
	};

	private OnItemSelectedListener onStateSelected = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			// Some country was selected; load the new top beers list
			state = (State) stateSpinner.getSelectedItem();
			getRateBeerActivity().getSettings().saveLastUsedState(state);
			refreshEvents();
		}
		@Override
		public void onNothingSelected(AdapterView<?> arg0) {}
	};

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (country != null) {
			outState.putInt(STATE_COUNTRY, (int)countrySpinner.getSelectedItemId());
		}
		if (events != null) {
			outState.putParcelableArrayList(STATE_EVENTS, events);
		}
	}
	
	private void refreshEvents() {
		if (country != null) {
			execute(new GetEventsCommand(getRateBeerActivity().getApi(), country, state));
		}
	}

	private OnItemClickListener onItemSelected = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Event item = ((EventAdapter)eventsView.getAdapter()).getItem(position);
			getRateBeerActivity().load(new EventViewFragment(item.eventName, item.eventID));
		}
	};
	
	@Override
	public void onTaskSuccessResult(CommandSuccessResult result) {
		if (result.getCommand().getMethod() == ApiMethod.GetEvents) {
			publishResults(((GetEventsCommand) result.getCommand()).getEvents());
		}
	}

	private void publishResults(ArrayList<Event> result) {
		this.events = result;
		//Collections.sort(result, new UserRatingComparator(sortOrder));
		if (eventsView.getAdapter() == null) {
			eventsView.setAdapter(new EventAdapter(getActivity(), result));
		} else {
			((EventAdapter) eventsView.getAdapter()).replace(result);
		}
		eventsView.setVisibility(result.size() == 0 ? View.GONE : View.VISIBLE);
		emptyText.setVisibility(result.size() == 0 ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onTaskFailureResult(CommandFailureResult result) {
		publishException(emptyText, result.getException());
	}

	private class EventAdapter extends ArrayAdapter<Event> {

		public EventAdapter(Context context, List<Event> objects) {
			super(context, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// Get the right view, using a ViewHolder
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.list_item_event, null);
				holder = new ViewHolder();
				holder.eventName = (TextView) convertView.findViewById(R.id.eventName);
				holder.city = (TextView) convertView.findViewById(R.id.city);
				holder.date = (TextView) convertView.findViewById(R.id.date);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			// Bind the data
			Event item = getItem(position);
			holder.eventName.setText(item.eventName);
			holder.city.setText(item.city);
			holder.date.setText(localFormat.format(item.date));

			return convertView;
		}

	}
	
	protected static class ViewHolder {
		TextView eventName, city, date;
	}

}
