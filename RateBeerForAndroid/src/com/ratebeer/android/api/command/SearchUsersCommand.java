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
import java.util.Arrays;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;

import android.os.Parcel;
import android.os.Parcelable;

import com.ratebeer.android.api.ApiConnection;
import com.ratebeer.android.api.ApiException;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.HtmlCommand;
import com.ratebeer.android.api.HttpHelper;
import com.ratebeer.android.api.UserSettings;

public class SearchUsersCommand extends HtmlCommand {

	private final String query;
	private ArrayList<UserSearchResult> results;

	public SearchUsersCommand(UserSettings api, String query) {
		super(api, ApiMethod.SearchUsers);
		this.query = query;
	}

	public String getQuery() {
		return query;
	}

	public String getNormalizedQuery() {
		return HttpHelper.normalizeSearchQuery(query);
	}

	public ArrayList<UserSearchResult> getSearchResults() {
		return results;
	}

	@Override
	protected String makeRequest(ApiConnection apiConnection) throws ApiException {
		return apiConnection.post("http://www.ratebeer.com/usersearch.php",
				Arrays.asList(new BasicNameValuePair("UserName", getNormalizedQuery())));
	}

	@Override
	protected void parse(String html) throws JSONException, ApiException {
	
		// Results?
		int foundStart = html.indexOf("No users found");
		if (foundStart >= 0) {
			// Return an empty list as result
			results = new ArrayList<UserSearchResult>();
			return; // Success, just empty
		}
	
		// We are either send to a search results page or directly to the only found user's page
		int tableStart = html.indexOf("Found more than one user");
		if (tableStart < 0) {
			
			// Probably send directly to the single found user: parse that instead
			String idText = "<span class=\"userIsDrinking\">";
			int idStart = html.indexOf(idText);
			if (idStart < 0) {
				throw new ApiException(ApiException.ExceptionType.CommandFailed,
						"The response HTML did not contain the unique identifiable HTML string");
			}
			
			// Indeed a user page was found: return just the user name, id and number of ratings
			idStart += idText.length();
			String userName = html.substring(idStart, html.indexOf("<", idStart));
	
			int ratingsStart1 = html.indexOf("Last seen", idStart);
			int ratingsStart2 = html.indexOf("<b>", ratingsStart1) + "<b>".length();
			int ratings = Integer.parseInt(html.substring(ratingsStart2, html.indexOf("</b> beer", ratingsStart2)));
	
			int userIdStart = html.indexOf("/user/") + "/user/".length();
			int userId = Integer.parseInt(html.substring(userIdStart, html.indexOf("/", userIdStart)));
			
			// Make a list of just this user
			results = new ArrayList<UserSearchResult>();
			results.add(new UserSearchResult(userId, userName, ratings));
			return; // Success
		
		}

		// We are given a table of users and their number of ratings
		String rowText = "<TD class=\"beer\"><A HREF=\"/user/";
		int rowStart = html.indexOf(rowText, tableStart) + rowText.length();
		results = new ArrayList<UserSearchResult>();

		while (rowStart > 0 + rowText.length()) {

			int userId = Integer.parseInt(html.substring(rowStart, html.indexOf("/", rowStart)));

			int nameStart = html.indexOf(">", rowStart) + 1;
			String userName = HttpHelper.cleanHtml(html.substring(nameStart, html.indexOf("<", nameStart)));

			int ratingsStart = html.indexOf("<B>", nameStart) + "<B>".length();
			String ratingsRaw = html.substring(ratingsStart, html.indexOf("</B>", ratingsStart));
			int ratings = 0;
			if (ratingsRaw.length() > 0) {
				ratings = Integer.parseInt(html.substring(ratingsStart, html.indexOf("</B>", ratingsStart)));
			}

			results.add(new UserSearchResult(userId, userName, ratings));
			rowStart = html.indexOf(rowText, ratingsStart) + rowText.length();
		}

	}

	public static class UserSearchResult implements Parcelable {

		public final int userId;
		public final String userName;
		public final int ratings;
		
		public UserSearchResult(int userId, String userName, int ratings) {
			this.userId = userId;
			this.userName = userName;
			this.ratings = ratings;
		}

		public int describeContents() {
			return 0;
		}
		public void writeToParcel(Parcel out, int flags) {
			out.writeInt(userId);
			out.writeString(userName);
			out.writeInt(ratings);
		}
		public static final Parcelable.Creator<UserSearchResult> CREATOR = new Parcelable.Creator<UserSearchResult>() {
			public UserSearchResult createFromParcel(Parcel in) {
				return new UserSearchResult(in);
			}
			public UserSearchResult[] newArray(int size) {
				return new UserSearchResult[size];
			}
		};
		private UserSearchResult(Parcel in) {
			userId = in.readInt();
			userName = in.readString();
			ratings = in.readInt();
		}
		
	}

}
