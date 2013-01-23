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

import android.test.AndroidTestCase;

import com.ratebeer.android.api.UserSettings;
import com.ratebeer.android.api.command.GetUserDetailsCommand.UserDetails;
import com.ratebeer.android.api.command.SearchUsersCommand.UserSearchResult;

public class UserCommandsTests extends AndroidTestCase {

	public void testExecute() {
		
		// Search for users with android in the name
		UserSettings signInUser = TestHelper.getUser(getContext(), true);
		SearchUsersCommand usersCommand = new SearchUsersCommand(signInUser, "android");
		usersCommand.execute(null);
		assertNotNull(usersCommand.getSearchResults());
		assertTrue("More than one user has the string android in the name", usersCommand.getSearchResults().size() > 0);
		// Should be a user named rbandroid
		UserSearchResult searched = null;
		for (UserSearchResult user : usersCommand.getSearchResults()) {
			if (user.userName.equals("rbandroid")) {
				searched = user;
				break;
			}
		}
		assertNotNull(searched);
		
		// Get user details for rbandroid
		GetUserDetailsCommand rbandroidCommand = new GetUserDetailsCommand(signInUser, 156822);
		rbandroidCommand.execute(null);
		assertNotNull(rbandroidCommand.getDetails());
		UserDetails rbandroid = rbandroidCommand.getDetails();
		assertEquals("rbandroid", rbandroid.name);
		assertEquals("Feb 10, 2012", rbandroid.joined);
		assertTrue("Last seen date is not empty and does not contain HTML", !rbandroid.lastSeen.equals("") && 
				rbandroid.lastSeen.indexOf("<") < 0 && rbandroid.lastSeen.indexOf(">") < 0);
		assertEquals(0, rbandroid.beerRateCount);
		assertEquals(0, rbandroid.placeRateCount);
		assertEquals(45, rbandroid.favStyleId); // Saison
		assertNotNull(rbandroid.recentBeerRatings);
		assertTrue("Empty recent ratings list", rbandroid.recentBeerRatings.size() == 0);
		
		// TODO: Test with a user that DOES have ratings

	}

}
