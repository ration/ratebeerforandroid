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

import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.Command;
import com.ratebeer.android.api.RateBeerApi;

public class PostRatingCommand extends Command {

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

	public PostRatingCommand(RateBeerApi api, int beerId, int ratingID, String origDate, String beerName, int aroma, int appearance, int taste, int palate, int overall, String comment) {
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

	public PostRatingCommand(RateBeerApi api, int beerId, String beerName, int aroma, int appearance, int taste, int palate, int overall, String comment) {
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

	public int getBeerId() {
		return beerId;
	}

	public int getRatingID() {
		return ratingID;
	}

	public String getOriginalPostDate() {
		return origDate;
	}

	public String getBeerName() {
		return beerName;
	}

	public int getAroma() {
		return aroma;
	}

	public int getAppearance() {
		return appearance;
	}

	public int getTaste() {
		return taste;
	}

	public int getPalate() {
		return palate;
	}

	public int getOverall() {
		return overall;
	}

	public String getComment() {
		return comment;
	}

	public float getTotal() {
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
		return (float)(aroma + appearance + taste + palate + overall) / 10F;
	}

}
