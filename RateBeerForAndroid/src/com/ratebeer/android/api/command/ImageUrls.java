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


public class ImageUrls {

	public static String getBeerPhotoUrl(int beerdId) {
		return "http://res.cloudinary.com/ratebeer/image/upload/w_300,c_limit,q_100,d_beer_def.png/beer_" + beerdId + ".jpg";
	}

	public static String getUserPhotoUrl(String username) {
		return "http://res.cloudinary.com/ratebeer/image/upload/w_300,c_limit,q_100,d_user_def.png/user_" + username + ".jpg";
	}

}
