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

import org.json.JSONException;

import android.os.Parcel;
import android.os.Parcelable;

import com.ratebeer.android.api.ApiConnection;
import com.ratebeer.android.api.ApiException;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.HtmlCommand;
import com.ratebeer.android.api.HttpHelper;
import com.ratebeer.android.api.RateBeerApi;
import com.ratebeer.android.api.UserSettings;

public class GetUserCellarCommand extends HtmlCommand {

	private final int forUserId;
	private ArrayList<CellarBeer> wants;
	private ArrayList<CellarBeer> haves;

	public GetUserCellarCommand(UserSettings api, int forUserId) {
		super(api, ApiMethod.GetUserCellar);
		this.forUserId = forUserId;
	}

	public ArrayList<CellarBeer> getWants() {
		return wants;
	}

	public ArrayList<CellarBeer> getHaves() {
		return haves;
	}

	@Override
	protected String makeRequest(ApiConnection apiConnection) throws ApiException {
		RateBeerApi.ensureLogin(apiConnection, getUserSettings());
		return apiConnection.get("http://www.ratebeer.com/user/" + forUserId + "/cellar/");
	}

	@Override
	protected void parse(String html) throws JSONException, ApiException {

		// Parse the user's cellar
		int wantsStart = html.indexOf("Wants");
		int havesStart = html.indexOf("Haves");
		if (wantsStart < 0 || havesStart < 0) {
			throw new ApiException(ApiException.ExceptionType.CommandFailed,
					"The response HTML did not contain the unique wants/haves content string");
		}

		wants = new ArrayList<CellarBeer>();
		haves = new ArrayList<CellarBeer>();
		String wantRowText = "/wishlist/have/";
		int wantRowStart = html.indexOf(wantRowText, wantsStart) + wantRowText.length();
		while (wantRowStart > 0 + wantRowText.length() && wantRowStart < havesStart) {

			int beerId = Integer.parseInt(html.substring(wantRowStart, html.indexOf("/", wantRowStart)));

			int beerStart = html.indexOf("/\">", wantRowStart + 10) + 3;
			String beerName = HttpHelper.cleanHtml(html.substring(beerStart, html.indexOf("<", beerStart)));

			String memoText = "left: 46px;\">";
			int memoStart = html.indexOf(memoText, beerStart) + memoText.length();
			String memo = HttpHelper.cleanHtml(html.substring(memoStart, html.indexOf("</span>", memoStart))).trim();

			wants.add(new CellarBeer(beerId, beerName, memo, null, null));
			wantRowStart = html.indexOf(wantRowText, beerStart) + wantRowText.length();
		}
		String haveRowText = "/wishlist/want/";
		int haveRowStart = html.indexOf(haveRowText, havesStart) + haveRowText.length();
		if (wantRowStart < haveRowStart) {
			wantRowStart = haveRowStart;
		}
		while (haveRowStart > 0 + haveRowText.length()) {

			int beerId = Integer.parseInt(html.substring(haveRowStart, html.indexOf("/", haveRowStart)));

			int beerStart = html.indexOf("/\">", haveRowStart + 10) + 3;
			String beerName = HttpHelper.cleanHtml(html.substring(beerStart, html.indexOf("<", beerStart)));

			String memoText = "left: 46px;\">";
			int memoStart = html.indexOf(memoText, beerStart) + memoText.length();
			String memo = HttpHelper.cleanHtml(html.substring(memoStart, html.indexOf("</span>", memoStart))).trim();

			String quantity = null;
			int quantityEnd = memo.indexOf("<br>");
			if (quantityEnd >= 0) {
				quantity = memo.substring(0, quantityEnd);
				memo = memo.substring(quantityEnd + "<br>".length());
			}
			
			haves.add(new CellarBeer(beerId, beerName, memo, null, quantity));
			haveRowStart = html.indexOf(haveRowText, memoStart) + haveRowText.length();
		}

	}

	public static class CellarBeer implements Parcelable {

		public final int beerId;
		public final String beerName;
		public final String memo;
		public final String vintage;
		public final String quantity;

		public CellarBeer(int beerId, String beerName, String memo, String vintage, String quantity) {
			this.beerId = beerId;
			this.beerName = beerName;
			this.memo = memo;
			this.vintage = vintage;
			this.quantity = quantity;
		}

		public int describeContents() {
			return 0;
		}

		public void writeToParcel(Parcel out, int flags) {
			out.writeInt(beerId);
			out.writeString(beerName);
			out.writeString(memo);
			out.writeString(vintage);
			out.writeString(quantity);
		}

		public static final Parcelable.Creator<CellarBeer> CREATOR = new Parcelable.Creator<CellarBeer>() {
			public CellarBeer createFromParcel(Parcel in) {
				return new CellarBeer(in);
			}

			public CellarBeer[] newArray(int size) {
				return new CellarBeer[size];
			}
		};

		private CellarBeer(Parcel in) {
			beerId = in.readInt();
			beerName = in.readString();
			memo = in.readString();
			vintage = in.readString();
			quantity = in.readString();
		}

	}

}
