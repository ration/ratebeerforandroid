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

public abstract class Command {

	final private CommandService api;
	final private ApiMethod method;

	/**
	 * Construct a command for a certain method
	 * @param panel The panel to execute the command against
	 * @param method The method to perform
	 */
	protected Command(CommandService api, ApiMethod method) {
		this.api = api;
		this.method = method;
	}
	
	public CommandService getApi() {
		return api;
	}

	public ApiMethod getMethod() {
		return method;
	}

	/**
	 * Execute the set up command against the forum
	 * @return The command result
	 */
	public CommandResult execute() {
		return api.execute(this);
	}

	@Override
	public String toString() {
		return method.toString();
	}
	
}
