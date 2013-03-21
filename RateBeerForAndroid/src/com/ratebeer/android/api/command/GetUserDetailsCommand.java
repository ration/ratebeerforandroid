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

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.os.Parcel;
import android.os.Parcelable;

import com.ratebeer.android.api.ApiConnection;
import com.ratebeer.android.api.ApiException;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.HtmlCommand;
import com.ratebeer.android.api.HttpHelper;
import com.ratebeer.android.api.UserSettings;

public class GetUserDetailsCommand extends HtmlCommand {
	
	private final int userId;
	private UserDetails details;
	
	public GetUserDetailsCommand(UserSettings api, int userId) {
		super(api, ApiMethod.GetUserDetails);
		this.userId = userId;
	}
	
	public UserDetails getDetails() {
		return details;
	}

	@Override
	protected String makeRequest(ApiConnection apiConnection) throws ApiException {
		return apiConnection.get("http://www.ratebeer.com/user/" + userId + "/");
	}

	@Override
	protected void parse(String html) throws JSONException, ApiException {

		// Parse the user's details and recent ratings
		int userStart = html.indexOf("class=\"selected\">profile</a><br>");
		if (userStart < 0) {
			throw new ApiException(ApiException.ExceptionType.CommandFailed,
					"The response HTML did not contain the unique event content string");
		}

		final String nameText = "<h1>";
		int nameStart = html.indexOf(nameText, userStart) + nameText.length();
		String name = html.substring(nameStart, html.indexOf("</h1>", nameStart)).trim();
		if (name.indexOf(" ") >= 0)
			name = name.substring(0, name.indexOf(" "));

		String locationText = "<div style=\"float: left;\">";
		int locationStart = html.indexOf(locationText, nameStart) + locationText.length();
		String location = html.substring(locationStart, html.indexOf("<br>", locationStart)).trim();

		int joinedStart = html.indexOf("class=\"GrayItalic\">", locationStart) + "class=\"GrayItalic\">".length();
		String joined = html.substring(joinedStart, html.indexOf("<", joinedStart)).trim();

		int lastSeenStart = html.indexOf("class=\"GrayItalic\">", joinedStart) + "class=\"GrayItalic\">".length();
		String lastSeen = html.substring(lastSeenStart, html.indexOf("<", lastSeenStart));

		int beerRateCountStart = html.indexOf("<b>", lastSeenStart) + "<b>".length();
		int beerRateCount = Integer.parseInt(html.substring(beerRateCountStart,
				html.indexOf("</b>", beerRateCountStart)));

		int placeRateCountStart = html.indexOf("<b>", beerRateCountStart) + "<b>".length();
		int placeRateCount = Integer.parseInt(html.substring(placeRateCountStart,
				html.indexOf("</b>", placeRateCountStart)));

		int avgScoreGivenPresent = html.indexOf("Avg Score Given: ", placeRateCountStart);
		String avgScoreGiven = null;
		if (avgScoreGivenPresent >= 0) {
			int avgScoreGivenStart = avgScoreGivenPresent + "Avg Score Given: ".length();
			avgScoreGiven = HttpHelper.cleanHtml(html.substring(avgScoreGivenStart, html.indexOf(" ", avgScoreGivenStart)));
		}

		int avgBeerRatedPresent = html.indexOf("Avg Beer Rated: ", avgScoreGivenPresent);
		String avgBeerRated = null;
		if (avgBeerRatedPresent >= 0) {
			int avgBeerRatedStart = avgBeerRatedPresent + "Avg Beer Rated: ".length();
			avgBeerRated = HttpHelper.cleanHtml(html.substring(avgBeerRatedStart, html.indexOf(" ", avgBeerRatedStart)));
		}

		String styleText = "Favorite style: <a href=\"/beerstyles/";
		int styleStart = html.indexOf(styleText, placeRateCountStart);
		int styleId = -1;
		String styleName = null;
		if (styleStart >= 0) {
			styleStart += styleText.length();
			int styleIdStart = html.indexOf("/", styleStart) + 1;
			styleId = Integer.parseInt(html.substring(styleIdStart, html.indexOf("/", styleIdStart)));
			int styleNameStart = html.indexOf("<b>", styleStart) + "<b>".length();
			styleName = HttpHelper.cleanHtml(html.substring(styleNameStart, html.indexOf("</b>", styleNameStart)));
		}

		List<RecentBeerRating> ratings = new ArrayList<RecentBeerRating>();
		String ratingText = "style=\"height: 21px;\"><A HREF=\"/beer/";
		int ratingStart = html.indexOf(ratingText, styleStart);
		while (ratingStart >= 0) {
			int beerIdStart = html.indexOf("/", ratingStart + ratingText.length()) + 1;
			int beerId = Integer.parseInt(html.substring(beerIdStart, html.indexOf("/", beerIdStart)));

			int beerNameStart = html.indexOf(">", beerIdStart) + ">".length();
			String beerName = HttpHelper.cleanHtml(html.substring(beerNameStart, html.indexOf("<", beerNameStart)));

			int beerStyleStart = html.indexOf("smallGray\">", beerNameStart) + "smallGray\">".length();
			String beerStyle = HttpHelper.cleanHtml(html.substring(beerStyleStart, html.indexOf("<", beerStyleStart)));

			int beerRatingStart = html.indexOf("bold;\">", beerStyleStart) + "bold;\">".length();
			String beerRating = html.substring(beerRatingStart, html.indexOf("<", beerRatingStart));

			int beerDateStart = html.indexOf("smallGray\">", beerRatingStart) + "smallGray\">".length();
			String beerDate = html.substring(beerDateStart, html.indexOf("<", beerDateStart));

			ratings.add(new RecentBeerRating(beerId, beerName, beerStyle, beerRating, beerDate));
			ratingStart = html.indexOf(ratingText, beerDateStart);
		}

		// Set the user's rating on the original command as result
		details = new UserDetails(name, joined, lastSeen, location, beerRateCount, placeRateCount,
				avgScoreGiven, avgBeerRated, styleName, styleId, ratings);
		
	}
	
	public static class RecentBeerRating implements Parcelable {

		public final int id;
		public final String name;
		public final String styleName;
		public final String rating;
		public final String date;
		
		public RecentBeerRating(int id, String name, String styleName, String rating, String date) {
			this.id = id;
			this.name = name;
			this.styleName = styleName;
			this.rating = rating;
			this.date = date;
		}

		public int describeContents() {
			return 0;
		}
		public void writeToParcel(Parcel out, int flags) {
			out.writeInt(id);
			out.writeString(name);
			out.writeString(styleName);
			out.writeString(rating);
			out.writeString(date);
		}
		public static final Parcelable.Creator<RecentBeerRating> CREATOR = new Parcelable.Creator<RecentBeerRating>() {
			public RecentBeerRating createFromParcel(Parcel in) {
				return new RecentBeerRating(in);
			}
			public RecentBeerRating[] newArray(int size) {
				return new RecentBeerRating[size];
			}
		};
		private RecentBeerRating(Parcel in) {
			id = in.readInt();
			name = in.readString();
			styleName = in.readString();
			rating = in.readString();
			date = in.readString();
		}
		
	}
	
	public static class UserDetails implements Parcelable {

		public final String name;
		public final String location;
		public final String joined;
		public final String lastSeen;
		public final int beerRateCount;
		public final int placeRateCount;
		public final String avgScoreGiven;
		public final String avgBeerRated;
		public final String favStyleName;
		public final int favStyleId;
		public final List<RecentBeerRating> recentBeerRatings;
		
		public UserDetails(String name, String joined, String lastSeen, String location, int beerRateCount, 
				int placeRateCount, String avgScoreGiven, String avgBeerRated, String favStyleName, int favStyleId, List<RecentBeerRating> recentBeerRatings) {
			this.name = name;
			this.location = location;
			this.joined = joined;
			this.lastSeen = lastSeen;
			this.beerRateCount = beerRateCount;
			this.placeRateCount = placeRateCount;
			this.avgScoreGiven = avgScoreGiven;
			this.avgBeerRated = avgBeerRated;
			this.favStyleName = favStyleName;
			this.favStyleId = favStyleId;
			this.recentBeerRatings = recentBeerRatings;
		}

		public int describeContents() {
			return 0;
		}
		public void writeToParcel(Parcel out, int flags) {
			out.writeString(name);
			out.writeString(location);
			out.writeString(joined);
			out.writeString(lastSeen);
			out.writeInt(beerRateCount);
			out.writeInt(placeRateCount);
			out.writeString(avgScoreGiven);
			out.writeString(avgBeerRated);
			out.writeString(favStyleName);
			out.writeInt(favStyleId);
			out.writeTypedList(recentBeerRatings);
		}
		public static final Parcelable.Creator<UserDetails> CREATOR = new Parcelable.Creator<UserDetails>() {
			public UserDetails createFromParcel(Parcel in) {
				return new UserDetails(in);
			}
			public UserDetails[] newArray(int size) {
				return new UserDetails[size];
			}
		};
		private UserDetails(Parcel in) {
			name = in.readString();
			location = in.readString();
			joined = in.readString();
			lastSeen = in.readString();
			beerRateCount = in.readInt();
			placeRateCount = in.readInt();
			avgScoreGiven = in.readString();
			avgBeerRated = in.readString();
			favStyleName = in.readString();
			favStyleId = in.readInt();
			recentBeerRatings = new ArrayList<GetUserDetailsCommand.RecentBeerRating>();
			in.readTypedList(recentBeerRatings, RecentBeerRating.CREATOR);
		}
		
	}

}
