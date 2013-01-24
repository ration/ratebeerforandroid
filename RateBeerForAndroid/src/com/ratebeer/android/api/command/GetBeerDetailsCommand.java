package com.ratebeer.android.api.command;
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


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.ratebeer.android.api.ApiConnection;
import com.ratebeer.android.api.ApiException;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.HttpHelper;
import com.ratebeer.android.api.JsonCommand;
import com.ratebeer.android.api.UserSettings;

public class GetBeerDetailsCommand extends JsonCommand {
	
	public final static float NO_SCORE_YET = -1F;
	
	private final int beerId;
	private BeerDetails details;
	
	public GetBeerDetailsCommand(UserSettings api, int beerId) {
		super(api, ApiMethod.GetBeerDetails);
		this.beerId = beerId;
	}
	
	public BeerDetails getDetails() {
		return details;
	}

	@Override
	protected String makeRequest(ApiConnection apiConnection) throws ApiException {
		return apiConnection.get("http://www.ratebeer.com/json/bff.asp?k=" + ApiConnection.RB_KEY + "&bd=" + beerId);
	}

	@Override
	protected void parse(JSONArray json) throws JSONException {
		
		details = null;
		if (json.length() > 0) {
			JSONObject result = json.getJSONObject(0);
			String overall = result.getString("OverallPctl");
			String style = result.getString("StylePctl");
			details = new BeerDetails(result.getInt("BeerID"), HttpHelper.cleanHtml(result.getString("BeerName")),
					result.getInt("BrewerID"), HttpHelper.cleanHtml(result.getString("BrewerName")),
					HttpHelper.cleanHtml(result.getString("BeerStyleName")), (float) result.getDouble("Alcohol"),
					overall.equals("null") ? GetBeerDetailsCommand.NO_SCORE_YET : Float.parseFloat(overall),
					style.equals("null") ? GetBeerDetailsCommand.NO_SCORE_YET : Float.parseFloat(style),
					HttpHelper.cleanHtml(result.getString("Description")));
		}

	}
	
	public static class BeerDetails implements Parcelable {

		public final int beerId;
		public final String beerName;
		public final int brewerId;
		public final String brewerName;
		public final String beerStyle;
		public final float alcohol;
		public final float overallPerc;
		public final float stylePerc;
		public final String description;
		
		public BeerDetails(int beerId, String beerName, int brewerId, String brewerName, String beerStyle, float alcohol, float overallPerc, float stylePerc, String description) {
			this.beerId = beerId;
			this.beerName = beerName;
			this.brewerId = brewerId;
			this.brewerName = brewerName;
			this.beerStyle = beerStyle;
			this.alcohol = alcohol;
			this.overallPerc = overallPerc;
			this.stylePerc = stylePerc;
			this.description = description;
		}

		public int describeContents() {
			return 0;
		}
		public void writeToParcel(Parcel out, int flags) {
			out.writeInt(beerId);
			out.writeString(beerName);
			out.writeInt(brewerId);
			out.writeString(brewerName);
			out.writeString(beerStyle);
			out.writeFloat(alcohol);
			out.writeFloat(overallPerc);
			out.writeFloat(stylePerc);
			out.writeString(description);
		}
		public static final Parcelable.Creator<BeerDetails> CREATOR = new Parcelable.Creator<BeerDetails>() {
			public BeerDetails createFromParcel(Parcel in) {
				return new BeerDetails(in);
			}
			public BeerDetails[] newArray(int size) {
				return new BeerDetails[size];
			}
		};
		private BeerDetails(Parcel in) {
			beerId = in.readInt();
			beerName = in.readString();
			brewerId = in.readInt();
			brewerName = in.readString();
			beerStyle = in.readString();
			alcohol = in.readFloat();
			overallPerc = in.readFloat();
			stylePerc = in.readFloat();
			description = in.readString();
		}
		
	}

}
