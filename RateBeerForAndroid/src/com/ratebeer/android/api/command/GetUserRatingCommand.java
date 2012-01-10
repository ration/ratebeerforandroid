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

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import android.os.Parcel;
import android.os.Parcelable;

import com.ratebeer.android.api.ApiException;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.HtmlCommand;
import com.ratebeer.android.api.HttpHelper;
import com.ratebeer.android.api.RateBeerApi;

public class GetUserRatingCommand extends HtmlCommand {

	private final int beerId;
	private OwnBeerRating rating;

	public GetUserRatingCommand(RateBeerApi api, int beerId) {
		super(api, ApiMethod.GetUserRating);
		this.beerId = beerId;
	}

	public OwnBeerRating getRating() {
		return rating;
	}

	@Override
	protected String makeRequest() throws ClientProtocolException, IOException {
		return HttpHelper.makeRBGet("http://www.ratebeer.com/beer/rate/" + beerId + "/");
	}

	@Override
	protected void parse(String html) throws JSONException, ApiException {

		// Parse the user's existing rating
		int ratingStart = html.indexOf("name=OrigDate id=OrigDate value=\"");
		if (ratingStart < 0) {
			throw new ApiException(ApiException.ExceptionType.CommandFailed,
					"The response HTML did not contain the unique rating content string");
		}

		int origDateStart = ratingStart + "name=OrigDate id=OrigDate value=\"".length();
		String origDate = html.substring(origDateStart, html.indexOf("\"", origDateStart));
		if (origDate.length() <= 0) {
			// Not yet rated!
			rating = null;
			return; // Success, just not rated
		}
		int ratingIDStart = html.indexOf("name=RatingID id=RatingID value=\"", ratingStart)
				+ "name=RatingID id=RatingID value=\"".length();
		int ratingID = Integer.parseInt(html.substring(ratingIDStart, html.indexOf("\"", ratingIDStart)));

		int numbersStart = html.indexOf("<strong>Aroma</strong>");
		String selectedText = "SELECTED";

		int aromaStart = html.indexOf(selectedText, numbersStart) - 3;
		int aroma = Integer.parseInt(html.substring(aromaStart, html.indexOf(" ", aromaStart)).replace("=", ""));

		int appearanceStart = html.indexOf(selectedText, aromaStart + selectedText.length()) - 3;
		int appearance = Integer.parseInt(html.substring(appearanceStart, html.indexOf(" ", appearanceStart)).replace(
				"=", ""));

		int tasteStart = html.indexOf(selectedText, appearanceStart + selectedText.length()) - 3;
		int taste = Integer.parseInt(html.substring(tasteStart, html.indexOf(" ", tasteStart)).replace("=", ""));

		int palateStart = html.indexOf(selectedText, tasteStart + selectedText.length()) - 3;
		int palate = Integer.parseInt(html.substring(palateStart, html.indexOf(" ", palateStart)).replace("=", ""));

		int overallStart = html.indexOf(selectedText, palateStart + selectedText.length()) - 3;
		int overall = Integer.parseInt(html.substring(overallStart, html.indexOf(" ", overallStart)).replace("=", ""));

		String commentsText = " class=\"normBack\">";
		int commentsStart = html.indexOf(commentsText, overallStart) + commentsText.length();
		String comments = HttpHelper.cleanHtml(html.substring(commentsStart, html.indexOf("<", commentsStart)));

		// Set the user's rating on the original command as result
		rating = new OwnBeerRating(ratingID, origDate, appearance, aroma, taste, palate, overall, comments);

	}

	public static class OwnBeerRating implements Parcelable {

		public final int ratingID;
		public final String origDate;
		public final int appearance;
		public final int aroma;
		public final int taste;
		public final int palate;
		public final int overall;
		public final String comments;

		public OwnBeerRating(int ratingID, String origDate, int appearance, int aroma, int taste, int palate,
				int overall, String comments) {
			this.ratingID = ratingID;
			this.origDate = origDate;
			this.appearance = appearance;
			this.aroma = aroma;
			this.taste = taste;
			this.palate = palate;
			this.overall = overall;
			this.comments = comments;
		}

		public int describeContents() {
			return 0;
		}

		public void writeToParcel(Parcel out, int flags) {
			out.writeInt(ratingID);
			out.writeString(origDate);
			out.writeInt(appearance);
			out.writeInt(aroma);
			out.writeInt(taste);
			out.writeInt(palate);
			out.writeInt(overall);
			out.writeString(comments);
		}

		public static final Parcelable.Creator<OwnBeerRating> CREATOR = new Parcelable.Creator<OwnBeerRating>() {
			public OwnBeerRating createFromParcel(Parcel in) {
				return new OwnBeerRating(in);
			}

			public OwnBeerRating[] newArray(int size) {
				return new OwnBeerRating[size];
			}
		};

		private OwnBeerRating(Parcel in) {
			ratingID = in.readInt();
			origDate = in.readString();
			appearance = in.readInt();
			aroma = in.readInt();
			taste = in.readInt();
			palate = in.readInt();
			overall = in.readInt();
			comments = in.readString();
		}

	}

}
