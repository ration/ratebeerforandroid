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
package com.ratebeer.android.app.persistance;

import java.util.Date;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "OfflineRating")
public class OfflineRating {

	@DatabaseField(generatedId = true)
	private Integer offlineId;
	@DatabaseField
	private Integer beerId;
	@DatabaseField
	private String beerName;
	@DatabaseField
	private Integer originalRatingId;
	@DatabaseField
	private String originalRatingDate;
	@DatabaseField
	private Integer appearance;
	@DatabaseField
	private Integer aroma;
	@DatabaseField
	private Integer taste;
	@DatabaseField
	private Integer palate;
	@DatabaseField
	private Integer overall;
	@DatabaseField
	private String comments;
	@DatabaseField(canBeNull = false)
	private Date timeSaved;

	public OfflineRating() {
		this.timeSaved = new Date();
	}

	public OfflineRating(Integer beerId, String beerName) {
		this();
		this.beerId = beerId;
		this.beerName = beerName;
	}

	private OfflineRating(int beerId, String beerName, int originalRatingId, String originalRatingDate, int aroma,
			int appearance, int taste, int palate, int overall, String comments, Date timeSaved) {
		this();
		this.beerId = beerId;
		this.beerName = beerName;
		this.originalRatingId = originalRatingId;
		this.originalRatingDate = originalRatingDate;
		this.appearance = appearance;
		this.aroma = aroma;
		this.taste = taste;
		this.palate = palate;
		this.overall = overall;
		this.comments = comments;
		this.timeSaved = new Date();
	}

	/**
	 * Update used when a rating is edited (e.g. the comments have changed or
	 * number are adjusted)
	 */
	public void update(Integer beerId, String beerName, Integer originalRatingId, String originalRatingDate,
			Integer appearance, Integer aroma, Integer taste, Integer palate, Integer overall, String comments) {
		this.beerId = beerId;
		this.beerName = beerName;
		this.originalRatingId = originalRatingId;
		this.originalRatingDate = originalRatingDate;
		this.appearance = appearance;
		this.aroma = aroma;
		this.taste = taste;
		this.palate = palate;
		this.overall = overall;
		this.comments = comments;
		this.timeSaved = new Date();
	}

	/**
	 * Updated used when the beer ID was searched for an existing offline-only
	 * rating
	 */
	public void update(int beerId, String beerName) {
		this.beerId = beerId;
		this.beerName = beerName;
		this.timeSaved = new Date();
	}

	public Integer getOfflineId() {
		return offlineId;
	}

	public Integer getBeerId() {
		return beerId;
	}

	public String getBeerName() {
		return beerName;
	}

	public Integer getOriginalRatingId() {
		return originalRatingId;
	}

	public String getOriginalRatingDate() {
		return originalRatingDate;
	}

	public Integer getAppearance() {
		return appearance;
	}

	public Integer getAroma() {
		return aroma;
	}

	public Integer getTaste() {
		return taste;
	}

	public Integer getPalate() {
		return palate;
	}

	public Integer getOverall() {
		return overall;
	}

	public String getComments() {
		return comments;
	}

	public Date getTimeSaved() {
		return timeSaved;
	}

	/**
	 * Instantiate an offline rating object with all values known but the ID, which will be auto-generated. Used when 
	 * importing ratings from a text file. It can then be stored to the database.
	 * @return The instantiated offline rating object with all fields filled in
	 */
	public static OfflineRating fromFile(int beerId, String beerName, int originalRatingId, String originalRatingDate,
			int aroma, int appearance, int taste, int palate, int overall, String comments, Date timeSaved) {
		return new OfflineRating(beerId, beerName, originalRatingId, originalRatingDate, aroma, appearance, taste,
				palate, overall, comments, timeSaved);
	}

}
