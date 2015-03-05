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

	public static String getBeerPhotoUrl(int beerId) {
		return "http://res.cloudinary.com/ratebeer/image/upload/w_300,c_limit,q_100,d_beer_def.png/beer_" + beerId + ".jpg";
	}

	public static String getUserPhotoUrl(String username) {
		return "http://res.cloudinary.com/ratebeer/image/upload/w_300,c_limit,q_100,d_user_def.png/user_" + username + ".jpg";
	}

<<<<<<< HEAD
=======
	public static String getBeerPhotoHighResUrl(int beerId) {
		return "http://res.cloudinary.com/ratebeer/image/upload/w_1024,c_limit,q_100,d_beer_def.png/beer_" + beerId + ".jpg";
	}

	public static String getUserPhotoHighResUrl(String username) {
		return "http://res.cloudinary.com/ratebeer/image/upload/w_1024,c_limit,q_100,d_user_def.png/user_" + username + ".jpg";
	}

>>>>>>> 9cb2b20cee7ae90e7a5ea61c0ebff4e0c86a6dd6
}
