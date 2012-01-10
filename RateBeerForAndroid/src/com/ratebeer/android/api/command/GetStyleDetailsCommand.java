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
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import android.os.Parcel;
import android.os.Parcelable;

import com.ratebeer.android.api.ApiException;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.HtmlCommand;
import com.ratebeer.android.api.HttpHelper;
import com.ratebeer.android.api.RateBeerApi;
import com.ratebeer.android.api.command.GetTopBeersCommand.TopBeer;

public class GetStyleDetailsCommand extends HtmlCommand {

	private final int styleId;
	private StyleDetails details;

	public GetStyleDetailsCommand(RateBeerApi api, int styleId) {
		super(api, ApiMethod.GetStyleDetails);
		this.styleId = styleId;
	}

	public StyleDetails getDetails() {
		return details;
	}

	@Override
	protected String makeRequest() throws ClientProtocolException, IOException {
		return HttpHelper.makeRBGet("http://www.ratebeer.com/beerstyles/s/" + styleId + "/");
	}

	@Override
	protected void parse(String html) throws JSONException, ApiException {

		// Parse the top beers table
		int tableStart = html.indexOf("<small><b>MORE BEER STYLES</b></small><br>");
		if (tableStart < 0) {
			throw new ApiException(ApiException.ExceptionType.CommandFailed,
					"The response HTML did not contain the unique top beers of the style table begin HTML string");
		}

		int nameStart = html.indexOf("<h1>") + "<h1>".length();
		String name = HttpHelper.cleanHtml(html.substring(nameStart, html.indexOf("</h1>", nameStart)));

		int descriptionStart = html.indexOf("\">", nameStart) + "\">".length();
		String description = HttpHelper.cleanHtml(html.substring(descriptionStart,
				html.indexOf("</div>", descriptionStart)).trim());

		String servedInText = ".gif\">&nbsp;";
		List<String> servedIn = new ArrayList<String>();
		int servedInStart = html.indexOf(servedInText, descriptionStart);
		while (servedInStart >= 0) {
			servedIn.add(HttpHelper.cleanHtml(html.substring(servedInStart, html.indexOf("<br>", servedInStart))));
			servedInStart = html.indexOf(servedInText, servedInStart + 1);
		}

		String rowText = "<td class=\"beer\"><font color=\"#999999\">";
		int rowStart = html.indexOf(rowText, servedInStart) + rowText.length();
		ArrayList<TopBeer> beers = new ArrayList<TopBeer>();

		while (rowStart > 0 + rowText.length()) {

			int orderNr = Integer.parseInt(html.substring(rowStart, html.indexOf("<", rowStart)));

			int idStart1 = html.indexOf("<A HREF=\"/beer/", rowStart) + "<A HREF=\"/beer/".length();
			int idStart2 = html.indexOf("/", idStart1) + "/".length();
			int beerId = Integer.parseInt(html.substring(idStart2, html.indexOf("/", idStart2)));

			int beerStart = html.indexOf(">", idStart2) + ">".length();
			String beerName = HttpHelper.cleanHtml(html.substring(beerStart, html.indexOf("<", beerStart)));

			int scoreStart = html.indexOf("<b>", beerStart) + "<b>".length();
			float score = Float.parseFloat(html.substring(scoreStart, html.indexOf("<", scoreStart)));

			int countStart = html.indexOf("#999999\">", scoreStart) + "#999999\">".length();
			int count = Integer.parseInt(html.substring(countStart, html.indexOf("&", countStart)));

			beers.add(new TopBeer(orderNr, beerId, beerName, score, count, Integer.toString(styleId)));
			rowStart = html.indexOf(rowText, countStart) + rowText.length();
		}

		details = new StyleDetails(name, description, servedIn, beers);

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
