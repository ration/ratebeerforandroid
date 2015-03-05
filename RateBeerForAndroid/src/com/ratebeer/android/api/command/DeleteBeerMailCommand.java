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

<<<<<<< HEAD
=======
import java.net.HttpURLConnection;

>>>>>>> 9cb2b20cee7ae90e7a5ea61c0ebff4e0c86a6dd6
import com.ratebeer.android.api.ApiConnection;
import com.ratebeer.android.api.ApiException;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.EmptyResponseCommand;
import com.ratebeer.android.api.UserSettings;
import com.ratebeer.android.app.persistance.BeerMail;

public class DeleteBeerMailCommand extends EmptyResponseCommand {
	
	private final BeerMail mail;

	public DeleteBeerMailCommand(UserSettings api, BeerMail mail) {
		super(api, ApiMethod.DeleteBeerMail);
		this.mail = mail;
	}

	public BeerMail getMail() {
		return mail;
	}

	@Override
	protected void makeRequest(ApiConnection apiConnection) throws ApiException {
		ApiConnection.ensureLogin(apiConnection, getUserSettings());
<<<<<<< HEAD
		apiConnection.get("http://www.ratebeer.com/DeleteMessage.asp?MessageID="
				+ Integer.toString(mail.getMessageId()));
=======
		apiConnection.get("http://www.ratebeer.com/DeleteMessage.asp?MessageIDs="
				+ Integer.toString(mail.getMessageId()), HttpURLConnection.HTTP_MOVED_TEMP);
>>>>>>> 9cb2b20cee7ae90e7a5ea61c0ebff4e0c86a6dd6
	}

}
