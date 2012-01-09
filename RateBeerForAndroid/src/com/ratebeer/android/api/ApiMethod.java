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

	// @formatter:off
	
	// Administrative
	SignIn,
	SignOut,
	GetUserId,
	SetDrinkingStatus,
	
	// Users
	SearchUsers,
	GetUserImage,
	GetUserDetails,
	GetUserRatings,
	GetUserRating,
	GetUserCellar,
	PostRating, 
	
	// Cellar
	AddToCellar,
	RemoveFromCellar,
	
	// Beers
	SearchBeers,
	UpcSearch,
	GetBeerDetails,
	GetBeerImage,
	GetBeerRatings,
	GetBeerRating,
	GetTopBeers,
	UploadBeerPhoto,
	
	// Styles
	GetStyleDetails,
	
	// Places
	SearchPlaces,
	GetPlaceDetails,
	GetPlacesAround,
	CheckIn,
	GetCheckins,
	
	// Availability
	GetFavouritePlaces, 
	AddAvailability, 
	GetAvailableBeers,
	GetBeerAvailability,
	
	// Events
	GetEvents,
	GetEventDetails,
	SetEventAttendance,
	
	// Beermail
	GetAllBeerMails, 
	GetBeerMail, 
	DeleteBeerMail,
	SendBeerMail;
	
	// @formatter:on

}
