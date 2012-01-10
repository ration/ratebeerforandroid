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
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import android.os.Parcel;
import android.os.Parcelable;

import com.ratebeer.android.api.ApiException;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.HtmlCommand;
import com.ratebeer.android.api.HttpHelper;
import com.ratebeer.android.api.RateBeerApi;

public class GetTopBeersCommand extends HtmlCommand {

	private final TopListType topList;
	private final Country country;
	private ArrayList<TopBeer> beers;

	public GetTopBeersCommand(RateBeerApi api) {
		this(api, TopListType.Top50, null);
	}

	public GetTopBeersCommand(RateBeerApi api, Country country) {
		this(api, TopListType.TopByCountry, country);
	}

	private GetTopBeersCommand(RateBeerApi api, TopListType topList, Country country) {
		super(api, ApiMethod.GetTopBeers);
		this.topList = topList;
		this.country = country;
	}

	public ArrayList<TopBeer> getBeers() {
		return beers;
	}

	/**
	 * The type of top list to retrieve
	 */
	public static enum TopListType {
		Top50,
		TopByCountry
	}

	public static class TopBeer implements Parcelable {

		public final int orderNr;
		public final int beerId;
		public final String beerName;
		public final double score;
		public final int rateCount;
		public final String styleName;
		
		public TopBeer(int orderNr, int beerId, String beerName, double score, int rateCount, String styleName) {
			this.orderNr = orderNr;
			this.beerId = beerId;
			this.beerName = beerName;
			this.score = score;
			this.rateCount = rateCount;
			this.styleName = styleName;
		}

		public int describeContents() {
			return 0;
		}
		public void writeToParcel(Parcel out, int flags) {
			out.writeInt(orderNr);
			out.writeInt(beerId);
			out.writeString(beerName);
			out.writeDouble(score);
			out.writeInt(rateCount);
			out.writeString(styleName);
		}
		public static final Parcelable.Creator<TopBeer> CREATOR = new Parcelable.Creator<TopBeer>() {
			public TopBeer createFromParcel(Parcel in) {
				return new TopBeer(in);
			}
			public TopBeer[] newArray(int size) {
				return new TopBeer[size];
			}
		};
		private TopBeer(Parcel in) {
			orderNr = in.readInt();
			beerId = in.readInt();
			beerName = in.readString();
			score = in.readDouble();
			rateCount = in.readInt();
			styleName = in.readString();
		}
		
	}

	@Override
	protected String makeRequest() throws ClientProtocolException, IOException {
		switch (topList) {
		case Top50:
			// TODO: Replace with API call http://www.ratebeer.com/json/tb.asp?m=top50&k=<KEY>
			return HttpHelper.makeRBGet("http://www.ratebeer.com/beer/top-50/");
		case TopByCountry:
			// TODO: Replace with API call http://www.ratebeer.com/json/tb.asp?m=country&c=<COUNTRYID>&k=<KEY>
			return HttpHelper.makeRBGet("http://www.ratebeer.com/beer/country/"
							+ country.getCode() + "/"
							+ country.getId() + "/");
		}
		return null;
	}

	@Override
	protected void parse(String html) throws JSONException, ApiException {
		switch (topList) {
		case Top50:

			// Parse the top beers table
			int tableStart = html.indexOf("<h1>the ratebeer top 50");
			if (tableStart < 0) {
				throw new ApiException(ApiException.ExceptionType.CommandFailed,
						"The response HTML did not contain the unique top beers table begin HTML string");
			}
			String rowText = "</TR><TR";
			int rowStart = html.indexOf(rowText, tableStart) + rowText.length();
			int tableEnd = html.indexOf("</TABLE>", rowStart) + "</TABLE>".length();
			beers = new ArrayList<TopBeer>();

			while (rowStart > 0 + rowText.length() && tableEnd > rowStart) {

				int orderStart = html.indexOf("class=\"dlo\">", rowStart) + "class=\"dlo\">".length();
				int orderNr = Integer.parseInt(html.substring(orderStart, html.indexOf("&", orderStart)));

				int idStart1 = html.indexOf("<A HREF=\"/beer/", orderStart) + "<A HREF=\"/beer/".length();
				int idStart2 = html.indexOf("/", idStart1) + "/".length();
				int beerId = Integer.parseInt(html.substring(idStart2, html.indexOf("/", idStart2)));

				int beerStart = html.indexOf(">", idStart2) + ">".length();
				String beerName = HttpHelper.cleanHtml(html.substring(beerStart, html.indexOf("<", beerStart)));

				int scoreStart = html.indexOf("<b>", beerStart) + "<b>".length();
				float score = Float.parseFloat(html.substring(scoreStart, html.indexOf("<", scoreStart)));

				int countStart = html.indexOf("#999999\">", scoreStart) + "#999999\">".length();
				int count = Integer.parseInt(html.substring(countStart, html.indexOf("&", countStart)));

				int styleStart = html.indexOf("/\">", countStart) + "/\">".length();
				String style = html.substring(styleStart, html.indexOf("<", styleStart));

				beers.add(new TopBeer(orderNr, beerId, beerName, score, count, style));
				rowStart = html.indexOf(rowText, styleStart) + rowText.length();
			}

		case TopByCountry:

			// Parse the top beers table
			int table2Start = html.indexOf("<h1>the best beers of ");
			if (table2Start < 0) {
				throw new ApiException(ApiException.ExceptionType.CommandFailed,
						"The response HTML did not contain the unique top beers table begin HTML string");
			}
			String row2Text = "</TR><TR";
			int row2Start = html.indexOf(row2Text, table2Start) + row2Text.length();
			int table2End = html.indexOf("</TABLE>", row2Start) + "</TABLE>".length();
			beers = new ArrayList<TopBeer>();

			while (row2Start > 0 + row2Text.length() && table2End > row2Start) {

				int orderStart = html.indexOf("#999999\">", row2Start) + "#999999\">".length();
				int orderNr = Integer.parseInt(html.substring(orderStart, html.indexOf("<", orderStart)));

				int idStart1 = html.indexOf("<A HREF=\"/beer/", orderStart) + "<A HREF=\"/beer/".length();
				int idStart2 = html.indexOf("/", idStart1) + "/".length();
				int beerId = Integer.parseInt(html.substring(idStart2, html.indexOf("/", idStart2)));

				int beerStart = html.indexOf(">", idStart2) + ">".length();
				String beerName = HttpHelper.cleanHtml(html.substring(beerStart, html.indexOf("<", beerStart)));

				int scoreStart = html.indexOf("<b>", beerStart) + "<b>".length();
				float score = Float.parseFloat(html.substring(scoreStart, html.indexOf("<", scoreStart)));

				int countStart = html.indexOf("#999999\">", scoreStart) + "#999999\">".length();
				int count = Integer.parseInt(html.substring(countStart, html.indexOf("&", countStart)));

				int styleStart = html.indexOf("/\">", countStart) + "/\">".length();
				String style = html.substring(styleStart, html.indexOf("<", styleStart));

				beers.add(new TopBeer(orderNr, beerId, beerName, score, count, style));
				row2Start = html.indexOf(row2Text, styleStart) + row2Text.length();
			}

		}
	}

}
