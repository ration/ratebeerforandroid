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
import com.ratebeer.android.api.RateBeerApi;

public class SearchCommand extends Command {

	private final String query;
	private final int userId;
	private ArrayList<SearchResult> results;
	
	public static final int NO_USER = -1;

	public SearchCommand(RateBeerApi api, String query) {
		this(api, query, NO_USER);
	}

	public SearchCommand(RateBeerApi api, String query, int userId) {
		super(api, ApiMethod.Search);
		this.query = query;
		this.userId = userId;
	}

	public String getQuery() {
		return query;
	}

	public int getUserId() {
		return userId;
	}
	
	public void setSearchResults(ArrayList<SearchResult> results) {
		this.results = results;
	}

	public ArrayList<SearchResult> getSearchResults() {
		return results;
	}

	public static class SearchResult implements Parcelable {

		public final int beerId;
		public final String beerName;
		public final int overallPerc;
		public final int rateCount;
		public final boolean isRated;
		public final boolean isAlias;
		public final boolean isRetired;
		
		public SearchResult(int beerId, String beerName, int overallPerc, int rateCount, boolean isRated, boolean isAlias, boolean isRetired) {
			this.beerId = beerId;
			this.beerName = beerName;
			this.overallPerc = overallPerc;
			this.rateCount = rateCount;
			this.isRated = isRated;
			this.isAlias = isAlias;
			this.isRetired = isRetired;
		}

		public int describeContents() {
			return 0;
		}
		public void writeToParcel(Parcel out, int flags) {
			out.writeInt(beerId);
			out.writeString(beerName);
			out.writeInt(overallPerc);
			out.writeInt(rateCount);
			out.writeInt(isRated? 1: 0);
			out.writeInt(isAlias? 1: 0);
			out.writeInt(isRetired? 1: 0);
		}
		public static final Parcelable.Creator<SearchResult> CREATOR = new Parcelable.Creator<SearchResult>() {
			public SearchResult createFromParcel(Parcel in) {
				return new SearchResult(in);
			}
			public SearchResult[] newArray(int size) {
				return new SearchResult[size];
			}
		};
		private SearchResult(Parcel in) {
			beerId = in.readInt();
			beerName = in.readString();
			overallPerc = in.readInt();
			rateCount = in.readInt();
			isRated = in.readInt() == 1;
			isAlias = in.readInt() == 1;
			isRetired = in.readInt() == 1;
		}
		
	}

}
