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
import com.ratebeer.android.gui.fragments.AddToCellarFragment.CellarType;

public class AddToCellarCommand extends Command {
	
	private final int beerId;
	private final CellarType cellarType;
	private final String memo;
	private final String vintage;
	private final String quantity;

	public AddToCellarCommand(RateBeerApi api, CellarType cellarType, int beerId, String memo, String vintage, String quantity) {
		super(api, ApiMethod.AddToCellar);
		this.beerId = beerId;
		this.cellarType = cellarType;
		this.memo = memo;
		this.vintage = vintage;
		this.quantity = quantity;
	}

	public int getBeerId() {
		return beerId;
	}

	public CellarType getCellarType() {
		return cellarType;
	}

	public String getMemo() {
		return memo;
	}

	public String getVintage() {
		return vintage;
	}

	public String getQuantity() {
		return quantity;
	}

	/**
	 * Returns whether this beer to add to the cellar is wanted by the user (instead of a have)
	 * @return True if the cellar type is Want, false otherwise
	 */
	public boolean isWant() {
		return cellarType == CellarType.Want;
	}

}
