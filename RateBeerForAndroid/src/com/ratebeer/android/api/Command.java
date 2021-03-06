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

/**
 * Represents a command that can be executed against the RateBeer servers. The result may either be
 * a {@link CommandSuccessResult} or a {@link CommandFailureResult}. The {@link HtmlCommand}, 
 * {@link JsonCommand} and {@link EmptyResponseCommand} classes can be used as a helpers. 
 * @author erickok
 */
public abstract class Command {

	private final UserSettings userSettings;
	private final ApiMethod method;

	/**
	 * Construct a command for a certain method
	 * @param userSettings The currently signed in user settings
	 * @param method The method to perform
	 */
	protected Command(UserSettings userSettings, ApiMethod method) {
		this.userSettings = userSettings;
		this.method = method;
	}

	public UserSettings getUserSettings() {
		return userSettings;
	}

	public ApiMethod getMethod() {
		return method;
	}

	/**
	 * Execute the set up command against the forum
	 * @param apiConnection The current RateBeer API connection to execute the command against
	 * @return The command result
	 */
	public abstract CommandResult execute(ApiConnection apiConnection);

	@Override
	public String toString() {
		return method.toString();
	}

}
