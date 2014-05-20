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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.security.MessageDigest;

import com.android.internalcopy.http.multipart.FilePart;
import com.android.internalcopy.http.multipart.MultipartEntity;
import com.android.internalcopy.http.multipart.Part;
import com.android.internalcopy.http.multipart.StringPart;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.DiscCacheUtil;
import com.nostra13.universalimageloader.core.assist.MemoryCacheUtil;
import com.ratebeer.android.api.ApiConnection;
import com.ratebeer.android.api.ApiException;
import com.ratebeer.android.api.ApiException.ExceptionType;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.EmptyResponseCommand;
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
		try {

			// Produce SHA-1 string of signature fields and secret key
			// See http://cloudinary.com/documentation/upload_images#request_authentication
			String time = Long.toString(new Date().getTime());
			String signature = "format=jpg&public_id=beer_" + beerId + "&timestamp=" + time;
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			byte[] digest = md.digest((signature + "LfEYVhBV9Uyoo4KfF7fEWrTGVlU").getBytes());
			
			// Post the photo and parameters
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://api.cloudinary.com/v1_1/ratebeer/image/upload");
			HttpParams params = post.getParams();
			HttpClientParams.setRedirecting(params, false);
			post.setEntity(new MultipartEntity(new Part[] {
					new FilePart("file", photo, FilePart.DEFAULT_CONTENT_TYPE, FilePart.DEFAULT_CHARSET), 
					new StringPart("api_key", "595279775179754"),
					new StringPart("timestamp", time),
					new StringPart("public_id", "beer_" + beerId),
					new StringPart("signature", byteArrayToHexString(digest)),
					new StringPart("format", "jpg")
			}, params));
			HttpResponse response = client.execute(post);
			
			// Parse result as JSON stream
			InputStream instream = response.getEntity().getContent();
			String result = ApiConnection.readStream(instream);
			JSONObject json = new JSONObject(result);
			if (json.has("error"))
				throw new ApiException(ExceptionType.CommandFailed, "Photo upload completed but unsuccesful: "
						+ json.getJSONObject("error").getString("message"));
			
			// No error; now clear our internal cache so we can view the photo ourself
			MemoryCacheUtil.removeFromCache(ImageUrls.getBeerPhotoUrl(beerId), ImageLoader.getInstance().getMemoryCache());
			DiscCacheUtil.removeFromCache(ImageUrls.getBeerPhotoUrl(beerId), ImageLoader.getInstance().getDiscCache());
			
		} catch (JSONException e) {
			throw new ApiException(ExceptionType.CommandFailed, "Photo upload completed but unsuccesful: "
					+ e.toString());
		} catch (NoSuchAlgorithmException e) {
			throw new ApiException(ExceptionType.CommandFailed, "Photo upload unsuccesful: " + e.toString());
		} catch (FileNotFoundException e) {
			throw new ApiException(ExceptionType.CommandFailed, "Photo upload unsuccesful: " + e.toString());
		} catch (ClientProtocolException e) {
			throw new ApiException(ExceptionType.CommandFailed, "Photo upload unsuccesful: " + e.toString());
		} catch (IOException e) {
			throw new ApiException(ExceptionType.CommandFailed, "Photo upload unsuccesful: " + e.toString());
		}
	}

	public static String byteArrayToHexString(byte[] b) {
		String result = "";
		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}

}
