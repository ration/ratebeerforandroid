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

import java.net.UnknownHostException;
import java.util.Arrays;

import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.message.BasicNameValuePair;

import com.ratebeer.android.api.ApiException;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.Command;
import com.ratebeer.android.api.CommandFailureResult;
import com.ratebeer.android.api.CommandResult;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.api.HttpHelper;
import com.ratebeer.android.api.RateBeerApi;

public class PostRatingCommand extends Command {

	private static final String POST_SUCCESS = "<h1>availability</h1>";
	
	private final int beerId;
	private final int ratingID;
	private final String origDate;
	private final String beerName;
	private final int aroma;
	private final int appearance;
	private final int taste;
	private final int palate;
	private final int overall;
	private final String comment;

	public PostRatingCommand(RateBeerApi api, int beerId, int ratingID, String origDate, String beerName, int aroma,
			int appearance, int taste, int palate, int overall, String comment) {
		super(api, ApiMethod.PostRating);
		this.beerId = beerId;
		this.ratingID = ratingID;
		this.origDate = origDate;
		this.beerName = beerName;
		this.aroma = aroma;
		this.appearance = appearance;
		this.taste = taste;
		this.palate = palate;
		this.overall = overall;
		this.comment = comment;
	}

	public PostRatingCommand(RateBeerApi api, int beerId, String beerName, int aroma, int appearance, int taste,
			int palate, int overall, String comment) {
		super(api, ApiMethod.PostRating);
		this.beerId = beerId;
		this.ratingID = -1;
		this.origDate = null;
		this.beerName = beerName;
		this.aroma = aroma;
		this.appearance = appearance;
		this.taste = taste;
		this.palate = palate;
		this.overall = overall;
		this.comment = comment;
	}

	public String getBeerName() {
		return beerName;
	}

	private float getTotal() {
		return calculateTotal(aroma, appearance, taste, palate, overall);
	}

	/**
	 * Static method to calculate the total score of a new rating
	 * @param aroma
	 * @param appearance
	 * @param taste
	 * @param palate
	 * @param overall
	 * @return The total score, which is a float [0,5 .. 5]
	 */
	public static float calculateTotal(int aroma, int appearance, int taste, int palate, int overall) {
		return (float) (aroma + appearance + taste + palate + overall) / 10F;
	}

	@Override
	public CommandResult execute() {
		try {

			RateBeerApi.ensureLogin(getUserSettings());
			// NOTE: Maybe use the API call to http://www.ratebeer.com/m/m_saverating.asp, but it should be checked
			// whether this allows updating of a rating too
			String result = HttpHelper.makeRBPost(ratingID <= 0 ? "http://www.ratebeer.com/saverating.asp"
					: "http://www.ratebeer.com/updaterating.asp", Arrays.asList(new BasicNameValuePair("BeerID",
					Integer.toString(beerId)),
					new BasicNameValuePair("RatingID", ratingID <= 0 ? "" : Integer.toString(ratingID)),
					new BasicNameValuePair("OrigDate", origDate == null ? "" : origDate), new BasicNameValuePair(
							"aroma", Integer.toString(aroma)),
					new BasicNameValuePair("appearance", Integer.toString(appearance)), new BasicNameValuePair(
							"flavor", Integer.toString(taste)),
					new BasicNameValuePair("palate", Integer.toString(palate)), new BasicNameValuePair("overall",
							Integer.toString(overall)),
					new BasicNameValuePair("totalscore", Float.toString(getTotal())), new BasicNameValuePair(
							"Comments", comment)));
			if (result.indexOf(POST_SUCCESS) >= 0) {
				return new CommandSuccessResult(this);
			} else {
				return new CommandFailureResult(this, new ApiException(ApiException.ExceptionType.CommandFailed, 
						"The rating was posted, but the returned HTML did not contain the unique success string."));
			}

		} catch (UnknownHostException e) {
			return new CommandFailureResult(this, new ApiException(ApiException.ExceptionType.Offline, e.toString()));
		} catch (HttpHostConnectException e) {
			return new CommandFailureResult(this, new ApiException(ApiException.ExceptionType.Offline, e.toString()));
		} catch (Exception e) {
			return new CommandFailureResult(this, new ApiException(ApiException.ExceptionType.CommandFailed,
					e.toString()));
		}
	}

}
