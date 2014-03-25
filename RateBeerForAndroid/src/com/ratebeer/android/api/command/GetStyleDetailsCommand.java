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
import com.ratebeer.android.api.command.GetTopBeersCommand.TopBeer;

public class GetStyleDetailsCommand extends HtmlCommand {

	private final int styleId;
	private StyleDetails details;

	public GetStyleDetailsCommand(UserSettings api, int styleId) {
		super(api, ApiMethod.GetStyleDetails);
		this.styleId = styleId;
	}

	public StyleDetails getDetails() {
		return details;
	}

	@Override
	protected String makeRequest(ApiConnection apiConnection) throws ApiException {
		return apiConnection.get("http://www.ratebeer.com/ajax/top-beer-by-style.asp?style=" + styleId + "&sort=-1&order=0&min=10&max=9999&retired=0&new=0&mine=0&");
	}

	@Override
	protected void parse(String html) throws JSONException, ApiException {

		// Parse the top beers table
		int tableStart = html.indexOf("<tr class=\"tbl head\">");
		if (tableStart < 0) {
			throw new ApiException(ApiException.ExceptionType.CommandFailed,
					"The response HTML did not contain the unique top beers of the style table begin HTML string");
		}

		String rowText = "<TR><TD class=\"tbl row\">";
		int rowStart = html.indexOf(rowText, tableStart) + rowText.length();
		ArrayList<TopBeer> beers = new ArrayList<TopBeer>();

		while (rowStart > 0 + rowText.length()) {

			int orderNr = Integer.parseInt(html.substring(rowStart, html.indexOf("<", rowStart)));

			int idStart1 = html.indexOf("<A HREF=\"/beer/", rowStart) + "<A HREF=\"/beer/".length();
			int idStart2 = html.indexOf("/", idStart1) + "/".length();
			int beerId = Integer.parseInt(html.substring(idStart2, html.indexOf("/", idStart2)));

			int beerStart = html.indexOf(">", idStart2) + ">".length();
			String beerName = HttpHelper.cleanHtml(html.substring(beerStart, html.indexOf("<", beerStart)));

			int countStart = html.indexOf("tbl count\">", beerStart) + "tbl count\">".length();
			int count = Integer.parseInt(html.substring(countStart, html.indexOf("<", countStart)));

			int scoreStart = html.indexOf("tbl score\"", countStart) + "tbl score\"".length();
			int scoreStart2 = html.indexOf("\">", scoreStart) + "\">".length();
			float score = Float.parseFloat(html.substring(scoreStart2, html.indexOf("<", scoreStart2)));

			beers.add(new TopBeer(orderNr, beerId, beerName, score, count, Integer.toString(styleId)));
			rowStart = html.indexOf(rowText, countStart) + rowText.length();
		}

		details = new StyleDetails(null, null, null, beers);

	}

	public static class StyleDetails implements Parcelable {

		public final String name;
		public final String description;
		public final List<String> servedIn;
		public final List<TopBeer> beers;

		public StyleDetails(String name, String description, List<String> servedIn, List<TopBeer> beers) {
			this.name = name;
			this.description = description;
			this.servedIn = servedIn;
			this.beers = beers;
		}

		public int describeContents() {
			return 0;
		}

		public void writeToParcel(Parcel out, int flags) {
			out.writeString(name);
			out.writeString(description);
			out.writeStringList(servedIn);
			out.writeTypedList(beers);
		}

		public static final Parcelable.Creator<StyleDetails> CREATOR = new Parcelable.Creator<StyleDetails>() {
			public StyleDetails createFromParcel(Parcel in) {
				return new StyleDetails(in);
			}

			public StyleDetails[] newArray(int size) {
				return new StyleDetails[size];
			}
		};

		private StyleDetails(Parcel in) {
			name = in.readString();
			description = in.readString();
			servedIn = new ArrayList<String>();
			in.readStringList(servedIn);
			beers = new ArrayList<TopBeer>();
			in.readTypedList(beers, TopBeer.CREATOR);
		}

	}

}
