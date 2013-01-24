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

import java.io.File;
import java.util.Arrays;

import org.apache.http.message.BasicNameValuePair;

import com.ratebeer.android.api.ApiConnection;
import com.ratebeer.android.api.ApiException;
import com.ratebeer.android.api.ApiException.ExceptionType;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.EmptyResponseCommand;
import com.ratebeer.android.api.RateBeerApi;
import com.ratebeer.android.api.UserSettings;

public class UploadBeerPhotoCommand extends EmptyResponseCommand {

	private final int beerId;
	private final File photo;

	public UploadBeerPhotoCommand(UserSettings api, int beerId, File photo) {
		super(api, ApiMethod.UploadBeerPhoto);
		this.beerId = beerId;
		this.photo = photo;
	}

	public int getBeerId() {
		return beerId;
	}

	public File getPhotoFile() {
		return photo;
	}

	@Override
	protected void makeRequest(ApiConnection apiConnection) throws ApiException {
		RateBeerApi.ensureLogin(apiConnection, getUserSettings());
		String result = apiConnection.postFile("http://www.ratebeer.com/ajax/m_savebeerpic.asp", 
				Arrays.asList(new BasicNameValuePair("BeerID", Integer.toString(beerId))), photo, "attach1");
		if (!result.contains("\"status\":\"success\"")) {
			throw new ApiException(ExceptionType.CommandFailed, "Uploading of photo doesn't seem to be succesfull: "
					+ result);
		}
	}

}
