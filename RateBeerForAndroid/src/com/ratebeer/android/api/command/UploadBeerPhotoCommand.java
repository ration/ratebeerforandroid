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
import java.io.IOException;

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;

import android.net.Uri;

import com.android.internalcopy.http.multipart.FilePart;
import com.android.internalcopy.http.multipart.MultipartEntity;
import com.android.internalcopy.http.multipart.Part;
import com.android.internalcopy.http.multipart.StringPart;
import com.ratebeer.android.api.ApiException;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.EmptyResponseCommand;
import com.ratebeer.android.api.HttpHelper;
import com.ratebeer.android.api.RateBeerApi;

public class UploadBeerPhotoCommand extends EmptyResponseCommand {

	private final int beerId;
	private final Uri photo;

	public UploadBeerPhotoCommand(RateBeerApi api, int beerId, Uri photo) {
		super(api, ApiMethod.UploadBeerPhoto);
		this.beerId = beerId;
		this.photo = photo;
	}

	public int getBeerId() {
		return beerId;
	}

	public Uri getPhotoUri() {
		return photo;
	}

	@Override
	protected void makeRequest() throws ClientProtocolException, IOException, ApiException {
		RateBeerApi.ensureLogin(getUserSettings());
		File upload = new File(photo.getPath());
		// TODO: Get the actual upload URL
		HttpPost post = new HttpPost("http://www.ratebeer.com/uploadphoto/");
		Part[] parts = { new StringPart("beerId", Integer.toString(beerId)), 
				new FilePart("file", upload, FilePart.DEFAULT_CONTENT_TYPE, null) };
		post.setEntity(new MultipartEntity(parts, post.getParams()));
		HttpHelper.makeRawRBPost(post, HttpStatus.SC_OK);
	}

}
