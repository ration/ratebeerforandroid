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

import android.os.Parcel;
import android.os.Parcelable;

import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.Command;
import com.ratebeer.android.api.HttpHelper;
import com.ratebeer.android.api.RateBeerApi;

public class SearchUsersCommand extends Command {

	private final String query;
	private ArrayList<UserSearchResult> results;

	public SearchUsersCommand(RateBeerApi api, String query) {
		super(api, ApiMethod.SearchUsers);
		this.query = query;
	}

	public String getQuery() {
		return query;
	}

	public String getNormalizedQuery() {
		return HttpHelper.normalizeSearchQuery(query);
	}

	public void setSearchResults(ArrayList<UserSearchResult> results) {
		this.results = results;
	}

	public ArrayList<UserSearchResult> getSearchResults() {
		return results;
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
