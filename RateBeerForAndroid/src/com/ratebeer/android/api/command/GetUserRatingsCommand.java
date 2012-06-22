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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import android.os.Parcel;
import android.os.Parcelable;

import com.ratebeer.android.api.ApiException;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.HtmlCommand;
import com.ratebeer.android.api.HttpHelper;
import com.ratebeer.android.api.RateBeerApi;

public class GetUserRatingsCommand extends HtmlCommand {

	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("M/d/yyyy");

	private final int forUserId;
	private final int pageNr;
	private final int sortOrder;
	private ArrayList<UserRating> ratings;

	public static final int SORTBY_BEER = 1;
	public static final int SORTBY_BREWER = 2;
	public static final int SORTBY_STYLE = 3;
	public static final int SORTBY_MYRATING = 4;
	public static final int SORTBY_DATE = 5;
	public static final int SORTBY_SCORE = 6;

	public GetUserRatingsCommand(RateBeerApi api, int forUserId, int pageNr, int sortOrder) {
		super(api, ApiMethod.GetUserRatings);
		this.forUserId = forUserId;
		this.pageNr = pageNr;
		this.sortOrder = sortOrder;
	}

	public ArrayList<UserRating> getUserRatings() {
		return ratings;
	}

	@Override
	protected String makeRequest() throws ClientProtocolException, IOException, ApiException {
		RateBeerApi.ensureLogin(getUserSettings());
		return HttpHelper.makeRBGet("http://www.ratebeer.com/user/" + forUserId + "/ratings/" + pageNr + "/"
				+ sortOrder + "/");
	}

	@Override
	protected void parse(String html) throws JSONException, ApiException {

		// Maybe no ratings?
		if (html.indexOf("<b>0</b> <span class=\"userDetails\">beer ratings</span>") >= 0) {
			ratings = new ArrayList<UserRating>();
			return;
		}
		
		// Parse the beer ratings table
		int tableStart = html.indexOf("<!-- RATINGS -->");
		if (tableStart < 0) {
			throw new ApiException(ApiException.ExceptionType.CommandFailed,
					"The response HTML did not contain the unique ratings table begin HTML string");
		}
		String rowText = "><A HREF=\"/beer/";
		int rowStart = html.indexOf(rowText, tableStart) + rowText.length();
		ratings = new ArrayList<UserRating>();

		while (rowStart > 0 + rowText.length()) {

			int idStart = html.indexOf("/", rowStart) + 1;
			int beerId = Integer.parseInt(html.substring(idStart, html.indexOf("/", idStart)));

			int beerStart = html.indexOf(">", idStart) + 1;
			String beerName = HttpHelper.cleanHtml(html.substring(beerStart, html.indexOf("<", beerStart)));

			int brewerStart = html.indexOf("/\">", beerStart) + 3;
			String brewerName = HttpHelper.cleanHtml(html.substring(brewerStart, html.indexOf("<", brewerStart)));

			int styleStart = html.indexOf("owrap>", brewerStart) + "owrap>".length();
			String styleName = HttpHelper.cleanHtml(html.substring(styleStart, html.indexOf("</", styleStart)));

			int scoreStart = html.indexOf("center>", styleStart) + "center>".length();
			float score = Float.parseFloat(html.substring(scoreStart, html.indexOf("<", scoreStart)));

			int myRatingStart = html.indexOf("center>", scoreStart) + "center>".length();
			float myRating = Float.parseFloat(html.substring(myRatingStart, html.indexOf("<", myRatingStart)));

			int dateStart = html.indexOf("align=right>", myRatingStart) + "align=right>".length();
			Date date = new Date();
			try {
				date = DATE_FORMATTER.parse(html.substring(dateStart, html.indexOf("&", dateStart)));
			} catch (ParseException e) {
			}

			ratings.add(new UserRating(beerId, beerName, brewerName, styleName, score, myRating, date));
			rowStart = html.indexOf(rowText, dateStart) + rowText.length();
		}

	}

	public static class UserRating implements Parcelable {

		public final int beerId;
		public final String beerName;
		public final String brewerName;
		public final String styleName;
		public final float score;
		public final float myRating;
		public final Date date;

		public UserRating(int beerId, String beerName, String brewerName, String styleName, float score,
				float myRating, Date date) {
			this.beerId = beerId;
			this.beerName = beerName;
			this.brewerName = brewerName;
			this.styleName = styleName;
			this.score = score;
			this.myRating = myRating;
			this.date = date;
		}

		public int describeContents() {
			return 0;
		}

		public void writeToParcel(Parcel out, int flags) {
			out.writeInt(beerId);
			out.writeString(beerName);
			out.writeString(brewerName);
			out.writeString(styleName);
			out.writeFloat(score);
			out.writeFloat(myRating);
			out.writeLong(date.getTime());
		}

		public static final Parcelable.Creator<UserRating> CREATOR = new Parcelable.Creator<UserRating>() {
			public UserRating createFromParcel(Parcel in) {
				return new UserRating(in);
			}

			public UserRating[] newArray(int size) {
				return new UserRating[size];
			}
		};

		private UserRating(Parcel in) {
			beerId = in.readInt();
			beerName = in.readString();
			brewerName = in.readString();
			styleName = in.readString();
			score = in.readFloat();
			myRating = in.readFloat();
			date = new Date(in.readLong());
		}

	}

	public static class UserRatingComparator implements Comparator<UserRating> {

		private int sortOrder;

		public UserRatingComparator(int sortOrder) {
			this.sortOrder = sortOrder;
		}

		@Override
		public int compare(UserRating rating1, UserRating rating2) {
			switch (sortOrder) {
			case SORTBY_BEER:
				rating1.beerName.compareTo(rating2.beerName);
			case SORTBY_BREWER:
				rating1.brewerName.compareTo(rating2.brewerName);
			case SORTBY_STYLE:
				rating1.styleName.compareTo(rating2.styleName);
			case SORTBY_SCORE:
				new Float(rating1.score).compareTo(new Float(rating2.score));
			case SORTBY_MYRATING:
				new Float(rating1.myRating).compareTo(new Float(rating2.myRating));
			}
			// Default to date sorting
			return rating1.date.compareTo(rating2.date);
		}

	}
}
