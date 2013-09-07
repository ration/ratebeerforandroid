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

import java.net.HttpURLConnection;
import java.util.Arrays;

import org.apache.http.message.BasicNameValuePair;

import com.ratebeer.android.api.ApiConnection;
import com.ratebeer.android.api.ApiException;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.EmptyResponseCommand;
import com.ratebeer.android.api.UserSettings;

public class SendBeerReplyCommand extends EmptyResponseCommand {

	private final int replyTo;
	private final int recipient;
	private final String body;

	public SendBeerReplyCommand(UserSettings api, int replyTo, int recipient, String body) {
		super(api, ApiMethod.SendBeerMail);
		this.replyTo = replyTo;
		this.recipient = recipient;
		this.body = body;
	}

	@Override
	protected void makeRequest(ApiConnection apiConnection) throws ApiException {
		ApiConnection.ensureLogin(apiConnection, getUserSettings());
		apiConnection.post("http://www.ratebeer.com/SaveReply.asp",
				Arrays.asList(new BasicNameValuePair("MessID", Integer.toString(replyTo)),
						new BasicNameValuePair("Referrer", "/showmessage_new.asp?messageID=" + replyTo),
						new BasicNameValuePair("UserID", Integer.toString(recipient)),
						new BasicNameValuePair("Body", body)), HttpURLConnection.HTTP_MOVED_TEMP);
	}

}
