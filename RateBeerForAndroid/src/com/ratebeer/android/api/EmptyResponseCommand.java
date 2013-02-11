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
 * Specific command type that does not have response data to parse, but can return success (or failure) directly.
 * @author erickok
 */
public abstract class EmptyResponseCommand extends Command {

	protected EmptyResponseCommand(UserSettings api, ApiMethod method) {
		super(api, method);
	}

	@Override
	public final CommandResult execute(ApiConnection apiConnection) {
		try {
			makeRequest(apiConnection);
			return new CommandSuccessResult(this);
		} catch (ApiException e) {
			return new CommandFailureResult(this, e);
		}
	}

	protected abstract void makeRequest(ApiConnection apiConnection) throws ApiException;

}
