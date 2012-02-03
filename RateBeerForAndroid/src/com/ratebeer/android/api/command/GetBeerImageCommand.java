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

import java.io.InputStream;
import java.net.UnknownHostException;

import org.apache.http.conn.HttpHostConnectException;

import android.graphics.drawable.Drawable;

import com.ratebeer.android.api.ApiException;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.Command;
import com.ratebeer.android.api.CommandFailureResult;
import com.ratebeer.android.api.CommandResult;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.api.HttpHelper;
import com.ratebeer.android.api.RateBeerApi;

public class GetBeerImageCommand extends Command {

	private final int beerId;
	private Drawable image;

	public GetBeerImageCommand(RateBeerApi api, int beerId) {
		super(api, ApiMethod.GetBeerImage);
		this.beerId = beerId;
	}

	public Drawable getImage() {
		return image;
	}

	@Override
	public CommandResult execute() {
		try {

			InputStream rawStream = HttpHelper.makeRawRBGet("http://www.ratebeer.com/beerimages/" + beerId + ".jpg");
			// Read the raw response stream as Drawable image and return this in a success result
			try {
				image = Drawable.createFromStream(rawStream, "tmp");
			} catch (OutOfMemoryError e) {
				// Image to big to load; since this very rarely happens (most RB images are tiny) we just ignore it
				image = null;
			}
			return new CommandSuccessResult(this);

		} catch (UnknownHostException e) {
			return new CommandFailureResult(this, new ApiException(ApiException.ExceptionType.Offline, e.toString()));
		} catch (HttpHostConnectException e) {
			return new CommandFailureResult(this, new ApiException(ApiException.ExceptionType.Offline, e.toString()));
		} catch (Exception e) {
			return new CommandFailureResult(this, new ApiException(ApiException.ExceptionType.CommandFailed,
					e.toString()));
		}
	}

}
