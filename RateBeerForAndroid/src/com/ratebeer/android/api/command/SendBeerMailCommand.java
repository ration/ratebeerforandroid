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

import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.Command;
import com.ratebeer.android.api.RateBeerApi;

public class SendBeerMailCommand extends Command {

	private final String sendTo;
	private final String subject;
	private final String body;

	public SendBeerMailCommand(RateBeerApi api, String sendTo, String subject, String body) {
		super(api, ApiMethod.SendBeerMail);
		this.sendTo = sendTo;
		this.subject = subject;
		this.body = body;
	}

	public String getSendTo() {
		return sendTo;
	}

	public String getSubject() {
		return subject;
	}

	public String getBody() {
		return body;
	}

}
