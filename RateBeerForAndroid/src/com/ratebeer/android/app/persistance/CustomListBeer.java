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

@DatabaseTable(tableName = "CustomListBeer")
public class CustomListBeer {

	@DatabaseField(generatedId = true)
	private Integer customListBeerId;
	@DatabaseField
	private Integer customListId;
	@DatabaseField
	private Integer beerId;
	@DatabaseField
	private String beerName;
	@DatabaseField
	private Float rating;
	@DatabaseField
	private Integer count;
	@DatabaseField(canBeNull = false)
	private Date timeSaved;

	public CustomListBeer() {
	}
	
	public CustomListBeer(Integer customListId, Integer beerId, String beerName, float rating, int count) {
		this.customListId = customListId;
		this.beerId = beerId;
		this.beerName = beerName;
		this.rating = rating;
		this.count = count;
		this.timeSaved = new Date();
	}

	public Integer getCustomListBeerId() {
		return customListBeerId;
	}

	public Integer getCustomListId() {
		return customListId;
	}

	public Integer getBeerId() {
		return beerId;
	}

	public String getBeerName() {
		return beerName;
	}

	public Float getRating() {
		return rating;
	}

	public Integer getCount() {
		return count;
	}

	public Date getTimeSaved() {
		return timeSaved;
	}

}
