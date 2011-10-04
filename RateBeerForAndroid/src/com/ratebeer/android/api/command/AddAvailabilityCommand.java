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

public class AddAvailabilityCommand extends Command {

	public static final int NO_EXTRA_PLACE = -1;
	
	private final int beerId;
	private final int[] selectedFavourites;
	private final String extraPlaceName;
	private final int extraPlaceId;
	private final boolean onBottleCan;
	private final boolean onTap;

	public AddAvailabilityCommand(RateBeerApi api, int beerId, int[] selectedFavourites, String extraPlaceName, int extraPlaceId, boolean onBottleCan, boolean onTap) {
		super(api, ApiMethod.AddAvailability);
		this.beerId = beerId;
		this.selectedFavourites = selectedFavourites;
		this.extraPlaceName = extraPlaceName;
		this.extraPlaceId = extraPlaceId;
		this.onBottleCan = onBottleCan;
		this.onTap = onTap;
	}

	public int getBeerId() {
		return beerId;
	}

	public int[] getSelectedFavourites() {
		return selectedFavourites;
	}

	public String getExtraPlaceName() {
		return extraPlaceName;
	}

	public int getExtraPlaceId() {
		return extraPlaceId;
	}

	public boolean isOnBottleCan() {
		return onBottleCan;
	}

	public boolean isOnTap() {
		return onTap;
	}

}
