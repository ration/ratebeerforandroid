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

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Specific command type that needs to parse the sevrer response as JSON array
 * @author erickok
 */
public abstract class JsonCommand extends Command {

	protected JsonCommand(UserSettings api, ApiMethod method) {
		super(api, method);
	}

	@Override
	public final CommandResult execute(ApiConnection apiConnection) {
		try {
			String json = makeRequest(apiConnection);
			parse(new JSONArray(json));
			return new CommandSuccessResult(this);
		} catch (JSONException e) {
			return new CommandFailureResult(this, new ApiException(ApiException.ExceptionType.CommandFailed,
					"JSON parsing error: " + e.toString()));
		} catch (ApiException e) {
			return new CommandFailureResult(this, e);
		}
	}

	protected abstract String makeRequest(ApiConnection apiConnection) throws ApiException;

	protected abstract void parse(JSONArray json) throws JSONException;

}
