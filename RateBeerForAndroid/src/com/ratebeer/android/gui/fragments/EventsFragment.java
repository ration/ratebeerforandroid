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
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.InstanceState;
import com.googlecode.androidannotations.annotations.ViewById;
import com.ratebeer.android.R;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.CommandFailureResult;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.api.command.Country;
import com.ratebeer.android.api.command.GetEventsCommand;
import com.ratebeer.android.api.command.GetEventsCommand.Event;
import com.ratebeer.android.api.command.State;
import com.ratebeer.android.gui.components.RateBeerActivity;
import com.ratebeer.android.gui.components.RateBeerFragment;
import com.ratebeer.android.gui.components.helpers.ArrayAdapter;

@EFragment(R.layout.fragment_events)
public class EventsFragment extends RateBeerFragment {

	private static final DateFormat localFormat = DateFormat.getDateInstance();
	
	protected Country country = null;
	protected State state = null;
	@InstanceState
	protected ArrayList<Event> events = null;

	@ViewById(R.id.empty)
	protected TextView emptyText;
	@ViewById(R.id.events)
	protected ListView eventsView;
	@ViewById(R.id.country)
	protected Spinner countrySpinner;
	@ViewById(R.id.state)
	protected Spinner stateSpinner;

	public EventsFragment() {
	}

	@AfterViews
	public void init() {

		countrySpinner.setOnItemSelectedListener(onCountrySelected);
		stateSpinner.setOnItemSelectedListener(onStateSelected);
		eventsView.setOnItemClickListener(onItemSelected);
		
		populateCountrySpinner();
		populateStateSpinner();
		if (events != null) {
			publishResults(events);
		} else {
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
		Arrays.sort(allCountries);
		android.widget.ArrayAdapter<Country> adapter = new android.widget.ArrayAdapter<Country>(getActivity(), android.R.layout.simple_spinner_item, allCountries);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		countrySpinner.setAdapter(adapter);
		// Select the last used country, if known
		if (getSettings().getLastUsedCountry() != null) {
			for (int j = 0; j < allCountries.length; j++) {
				if (allCountries[j].getId() == getSettings().getLastUsedCountry().getId()) {
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
		Arrays.sort(allStates);
		android.widget.ArrayAdapter<State> adapter = new android.widget.ArrayAdapter<State>(getActivity(), android.R.layout.simple_spinner_item, allStates);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		stateSpinner.setAdapter(adapter);
		// Select the last used state, if known
		state = null;
		if (getSettings().getLastUsedState() != null) {
			for (int j = 0; j < allStates.length; j++) {
				if (allStates[j].getId() == getSettings().getLastUsedState().getId()) {
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
			getSettings().saveLastUsedCountry(country);
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
			getSettings().saveLastUsedState(state);
			refreshEvents();
		}
		@Override
		public void onNothingSelected(AdapterView<?> arg0) {}
	};

	private void refreshEvents() {
		if (country != null) {
			execute(new GetEventsCommand(getUser(), country, state));
		}
	}

	private OnItemClickListener onItemSelected = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Event item = ((EventAdapter)eventsView.getAdapter()).getItem(position);
			load(EventViewFragment_.builder().eventId(item.eventID).eventName(item.eventName).build());
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
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_event, null);
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
