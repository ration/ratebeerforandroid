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

import java.io.IOException;
import java.util.Arrays;

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import com.ratebeer.android.api.ApiException;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.EmptyResponseCommand;
import com.ratebeer.android.api.HttpHelper;
import com.ratebeer.android.api.RateBeerApi;

public class SetEventAttendanceCommand extends EmptyResponseCommand {

	private final int eventId;
	private final boolean isGoing;

	public SetEventAttendanceCommand(RateBeerApi api, int eventId, boolean isGoing) {
		super(api, ApiMethod.SetEventAttendance);
		this.eventId = eventId;
		this.isGoing = isGoing;
	}

	public boolean isGoing() {
		return isGoing;
	}

	@Override
	protected void makeRequest() throws ClientProtocolException, IOException, ApiException {
		RateBeerApi.ensureLogin(getUserSettings());
		HttpHelper.makeRBPost("http://www.ratebeer.com/eventprocess-attend.asp", Arrays.asList(
				new BasicNameValuePair("EventID", Integer.toString(eventId)),
				new BasicNameValuePair("IsGoing", isGoing ? "1" : "0")),
		// Note that we get an HTTP 500 response even when the request is successfull...
				HttpStatus.SC_INTERNAL_SERVER_ERROR);
	}

}
