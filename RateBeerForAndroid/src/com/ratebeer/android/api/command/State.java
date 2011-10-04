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

import java.util.HashMap;
import java.util.Map;

public class State {

	private final int id;
	private final int country;
	private final String name;
	
	private State(int id, int countryID, String name) {
		this.id = id;
		this.country = countryID;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public int getCountryID() {
		return country;
	}

	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return name;
	}

	/**
	 * List of all the states of the USA and Canada
	 */
	public static Map<Integer, Map<Integer, State>> ALL_STATES;
	
	/**
	 * Initialization of all the states (which will run when the State class is first used)
	 */
	static {
		ALL_STATES = new HashMap<Integer, Map<Integer, State>>();
		// USA
		ALL_STATES.put(213, new HashMap<Integer, State>());
		addState(ALL_STATES, "Alabama", 1, 213);
		addState(ALL_STATES, "Alaska", 2, 213);
		addState(ALL_STATES, "Arizona", 3, 213);
		addState(ALL_STATES, "Arkansas", 4, 213);
		addState(ALL_STATES, "California", 5, 213);
		addState(ALL_STATES, "Colorado", 6, 213);
		addState(ALL_STATES, "Connecticut", 7, 213);
		addState(ALL_STATES, "Delaware", 8, 213);
		addState(ALL_STATES, "Florida", 9, 213);
		addState(ALL_STATES, "Georgia", 10, 213);
		addState(ALL_STATES, "Hawaii", 11, 213);
		addState(ALL_STATES, "Idaho", 12, 213);
		addState(ALL_STATES, "Illinois", 14, 213);
		addState(ALL_STATES, "Indiana", 13, 213);
		addState(ALL_STATES, "Iowa", 15, 213);
		addState(ALL_STATES, "Kansas", 16, 213);
		addState(ALL_STATES, "Kentucky", 17, 213);
		addState(ALL_STATES, "Louisiana", 18, 213);
		addState(ALL_STATES, "Maine", 19, 213);
		addState(ALL_STATES, "Maryland", 20, 213);
		addState(ALL_STATES, "Massachusetts", 21, 213);
		addState(ALL_STATES, "Michigan", 22, 213);
		addState(ALL_STATES, "Minnesota", 23, 213);
		addState(ALL_STATES, "Mississippi", 24, 213);
		addState(ALL_STATES, "Missouri", 25, 213);
		addState(ALL_STATES, "Montana", 26, 213);
		addState(ALL_STATES, "Nebraska", 27, 213);
		addState(ALL_STATES, "Nevada", 28, 213);
		addState(ALL_STATES, "New Hampshire", 29, 213);
		addState(ALL_STATES, "New Jersey", 30, 213);
		addState(ALL_STATES, "New Mexico", 31, 213);
		addState(ALL_STATES, "New York", 32, 213);
		addState(ALL_STATES, "North Carolina", 33, 213);
		addState(ALL_STATES, "North Dakota", 34, 213);
		addState(ALL_STATES, "Ohio", 35, 213);
		addState(ALL_STATES, "Oklahoma", 36, 213);
		addState(ALL_STATES, "Oregon", 37, 213);
		addState(ALL_STATES, "Pennsylvania", 38, 213);
		addState(ALL_STATES, "Rhode Island", 39, 213);
		addState(ALL_STATES, "South Carolina", 40, 213);
		addState(ALL_STATES, "South Dakota", 41, 213);
		addState(ALL_STATES, "Tennessee", 42, 213);
		addState(ALL_STATES, "Texas", 43, 213);
		addState(ALL_STATES, "Utah", 44, 213);
		addState(ALL_STATES, "Vermont", 45, 213);
		addState(ALL_STATES, "Virginia", 46, 213);
		addState(ALL_STATES, "Washington", 47, 213);
		addState(ALL_STATES, "Washington DC", 48, 213);
		addState(ALL_STATES, "West Virginia", 49, 213);
		addState(ALL_STATES, "Wisconsin", 50, 213);
		addState(ALL_STATES, "Wyoming", 51, 213);
		// Canada
		ALL_STATES.put(39, new HashMap<Integer, State>());
		addState(ALL_STATES, "British Columbia", 53, 39);
		addState(ALL_STATES, "Manitoba", 54, 39);
		addState(ALL_STATES, "New Brunswick", 55, 39);
		addState(ALL_STATES, "Newfoundland", 56, 39);
		addState(ALL_STATES, "Northwest Territories", 58, 39);
		addState(ALL_STATES, "Nova Scotia", 57, 39);
		addState(ALL_STATES, "Nunavut", 64, 39);
		addState(ALL_STATES, "Ontario", 59, 39);
		addState(ALL_STATES, "Prince Edward Island", 60, 39);
		addState(ALL_STATES, "Quebec", 61, 39);
		addState(ALL_STATES, "Saskatchewan", 62, 39);
		addState(ALL_STATES, "Yukon", 63, 39);
	}
	
	/**
	 * Helper method to add states to the list of all available states
	 * @param statesList The list to add this state to
	 * @param name The human readable name of the country
	 * @param countryID The country ID
	 * @param code The country code (as used in URLs)
	 */
	private static void addState(Map<Integer, Map<Integer, State>> statesList, String name, int id, int countryID) {
		statesList.get(countryID).put(id, new State(id, countryID, name));
	}

	public String toSinglePreference() {
		return id + "|" + country + "|" + name;
	}

	public static State fromSinglePreference(String storedPreference) {
		if (storedPreference == null) {
			return null;
		}
		String[] parts = storedPreference.split("\\|");
		if (parts.length < 3) {
			return null;
		}
		try {
			return new State(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), parts[2]);
		} catch (NumberFormatException e) {}
		return null;
	}

}
