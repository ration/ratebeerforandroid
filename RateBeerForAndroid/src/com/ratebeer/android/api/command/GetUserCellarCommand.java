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

import android.os.Parcel;
import android.os.Parcelable;

import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.Command;
import com.ratebeer.android.api.RateBeerApi;

public class GetUserCellarCommand extends Command {

	private final int forUserId;
	private ArrayList<CellarBeer> wants;
	private ArrayList<CellarBeer> haves;

	public GetUserCellarCommand(RateBeerApi api, int forUserId) {
		super(api, ApiMethod.GetUserCellar);
		this.forUserId = forUserId;
	}

	public int getForUserId() {
		return forUserId;
	}

	public void setWantsAndHaves(ArrayList<CellarBeer> wants, ArrayList<CellarBeer> haves) {
		this.wants = wants;
		this.haves = haves;
	}
	
	public ArrayList<CellarBeer> getWants() {
		return wants;
	}

	public ArrayList<CellarBeer> getHaves() {
		return haves;
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
