package com.ratebeer.android.api.command;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

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

public class UpcSearchCommand extends JsonCommand {

	private static final String BASE_URL = "http://www.ratebeer.com/json/upc.asp?k=" + ApiConnection.RB_KEY;

	private final String upcCode;
	private final ArrayList<UpcSearchResult> upcSearchResults = new ArrayList<UpcSearchResult>();

	public UpcSearchCommand(UserSettings api, String upcCode) {
		super(api, ApiMethod.UpcSearch);
		this.upcCode = upcCode;
	}

	public String getSearchedUpcCode() {
		return upcCode;
	}
	
	public ArrayList<UpcSearchResult> getUpcSearchResults() {
		return upcSearchResults;
	}

	@Override
	protected String makeRequest(ApiConnection apiConnection) throws ApiException {
		try {
			return apiConnection.get(BASE_URL + "&upc=" + URLEncoder.encode(upcCode, HttpHelper.UTF8));
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}

	@Override
	protected void parse(JSONArray json) throws JSONException {

		for (int i = 0; i < json.length(); i++) {
			// [{"BeerID":422,"BeerName":"Stone India Pale Ale (IPA)","BrewerID":76,"BrewerName":"Stone Brewing Co.","AverageRating":3.980633,"alcohol":6.9}]
			JSONObject jsonObject = json.getJSONObject(i);
			int beerId = jsonObject.getInt("BeerID");
			String beerName = HttpHelper.cleanHtml(jsonObject.getString("BeerName"));
			int brewerId = jsonObject.getInt("BrewerID");
			String brewerName = HttpHelper.cleanHtml(jsonObject.getString("BrewerName"));
			float averageRating = Float.parseFloat(jsonObject.getString("AverageRating"));
			float abv = Float.parseFloat(jsonObject.getString("alcohol"));

			UpcSearchResult result = new UpcSearchResult(beerId, beerName, brewerId, brewerName, averageRating, abv);
			upcSearchResults.add(result);
		}
		
	}

	public static class UpcSearchResult implements Parcelable {

		public final int beerId;
		public final String beerName;
		public final int brewerId;
		public final String brewerName;
		public final float averageRating;
		public final float abv;

		public UpcSearchResult(int beerId, String beerName, int brewerId, String brewerName, float averageRating,
				float abv) {
			this.beerId = beerId;
			this.beerName = beerName;
			this.brewerId = brewerId;
			this.brewerName = brewerName;
			this.averageRating = averageRating;
			this.abv = abv;
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			out.writeInt(beerId);
			out.writeString(beerName);
			out.writeInt(brewerId);
			out.writeString(brewerName);
			out.writeFloat(averageRating);
			out.writeFloat(abv);
		}

		public static final Parcelable.Creator<UpcSearchResult> CREATOR = new Parcelable.Creator<UpcSearchResult>() {
			@Override
			public UpcSearchResult createFromParcel(Parcel in) {
				return new UpcSearchResult(in);
			}

			@Override
			public UpcSearchResult[] newArray(int size) {
				return new UpcSearchResult[size];
			}
		};

		private UpcSearchResult(Parcel in) {
			beerId = in.readInt();
			beerName = in.readString();
			brewerId = in.readInt();
			brewerName = in.readString();
			averageRating = in.readFloat();
			abv = in.readFloat();
		}

	}

}
