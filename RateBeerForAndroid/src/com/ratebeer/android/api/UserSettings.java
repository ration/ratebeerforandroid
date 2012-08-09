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
package com.ratebeer.android.api;

import java.util.Date;

public final class UserSettings {

	private final int userid;
	private final String username;
	private final String password;
	private final String drinkingStatus;
	private final boolean isPremium;
	private final Date lastDrinkingStatusUpdate;
	
	public UserSettings(int userid, String username, String password, String drinkingStatus, boolean isPremium, Date lastDrinkingStatusUpdate) {
		this.userid = userid;
		this.username = username;
		this.password = password;
		this.drinkingStatus = drinkingStatus;
		this.isPremium = isPremium;
		this.lastDrinkingStatusUpdate = lastDrinkingStatusUpdate;
	}

	public int getUserID() {
		return userid;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getDrinkingStatus() {
		return drinkingStatus;
	}
	
	public boolean isPremium() {
		return isPremium;
	}
	
	public Date getLastDrinkingStatusUpdate() {
		return lastDrinkingStatusUpdate;
	}

	public String toSinglePreference() {
		return userid + "|" + username + "|" + password + "|" + drinkingStatus + "|" + (isPremium? "1": "0") + "|" + 
				Long.toString(lastDrinkingStatusUpdate.getTime());
	}

	public static UserSettings fromSinglePreference(String storedPreference) {
		if (storedPreference == null) {
			return null;
		}
		String[] parts = storedPreference.split("\\|");
		if (parts.length < 3) {
			return null;
		}
		try {
			// Parse user settings from the |-delimited user setting, where drinkingStatus, isPremium and 
			// lastDrinkingStatusUpdate may be missing
			return new UserSettings(
					Integer.parseInt(parts[0]), 
					parts[1], 
					parts[2], 
					parts.length < 4? "": parts[3], 
					parts.length < 5? false: parts[4].equals("1"),
					parts.length < 6? new Date(): new Date(Long.parseLong(parts[5])));
		} catch (NumberFormatException e) {}
		return null;
	}

}
