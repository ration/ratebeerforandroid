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
package com.ratebeer.android.api;

public enum ApiMethod {

	SignIn,
	SignOut,
	GetUserId,
	GetUserImage,
	GetUserDetails,
	GetUserRatings,
	GetUserRating,
	GetUserCellar,
	Search,
	GetBeerDetails,
	GetBeerImage,
	GetBeerRatings,
	GetBeerRating,
	GetBeerAvailability,
	GetStyleDetails,
	GetPlaces,
	GetPlaceBeerAvailability,
	GetEvents,
	GetEventDetails,
	GetTopBeers,
	SetDrinkingStatus,
	PostRating, 
	GetFavouritePlaces, 
	FindPlaces, 
	AddAvailability, 
	AddToCellar,
	SetEventAttendance;

}
