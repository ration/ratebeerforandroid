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
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.Command;
import com.ratebeer.android.api.RateBeerApi;
import com.ratebeer.android.api.command.GetTopBeersCommand.TopBeer;

public class GetStyleDetailsCommand extends Command {
	
	private final int styleId;
	private StyleDetails details;
	
	public GetStyleDetailsCommand(RateBeerApi api, int styleId) {
		super(api, ApiMethod.GetStyleDetails);
		this.styleId = styleId;
	}
	
	public int getStyleId() {
		return styleId;
	}

	public void setDetails(StyleDetails details) {
		this.details = details;
	}

	public StyleDetails getDetails() {
		return details;
	}

	public static class StyleDetails implements Parcelable {

		public final String name;
		public final String description;
		public final List<String> servedIn;
		public final List<TopBeer> beers;
		
		public StyleDetails(String name, String description, List<String> servedIn, List<TopBeer> beers) {
			this.name = name;
			this.description = description;
			this.servedIn = servedIn;
			this.beers = beers;
		}

		public int describeContents() {
			return 0;
		}
		public void writeToParcel(Parcel out, int flags) {
			out.writeString(name);
			out.writeString(description);
			out.writeStringList(servedIn);
			out.writeTypedList(beers);
		}
		public static final Parcelable.Creator<StyleDetails> CREATOR = new Parcelable.Creator<StyleDetails>() {
			public StyleDetails createFromParcel(Parcel in) {
				return new StyleDetails(in);
			}
			public StyleDetails[] newArray(int size) {
				return new StyleDetails[size];
			}
		};
		private StyleDetails(Parcel in) {
			name = in.readString();
			description = in.readString();
			servedIn = new ArrayList<String>();
			in.readStringList(servedIn);
			beers = new ArrayList<TopBeer>();
			in.readTypedList(beers, TopBeer.CREATOR);
		}
		
	}

}
