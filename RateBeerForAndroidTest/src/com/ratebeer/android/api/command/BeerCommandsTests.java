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

import com.ratebeer.android.api.ApiConnection;
import com.ratebeer.android.api.ApiConnection_;
import com.ratebeer.android.api.UserSettings;
import com.ratebeer.android.api.command.GetBeerDetailsCommand.BeerDetails;
import com.ratebeer.android.api.command.SearchBeersCommand.BeerSearchResult;

public class BeerCommandsTests extends AndroidTestCase {

	public void testExecute() {
		
		// Search for beers with 'rooie dop' in the name
		ApiConnection apiConnection = ApiConnection_.getInstance_(getContext());
		UserSettings signInUser = TestHelper.getUser(getContext(), true);
		SearchBeersCommand searchCommand = new SearchBeersCommand(signInUser, "rooie dop", signInUser.getUserID());
		searchCommand.execute(apiConnection);
		assertNotNull(searchCommand.getSearchResults());
		assertTrue("More than one beer should have 'rooie dop' in the name.", searchCommand.getSearchResults().size() > 0);
		// Should be a beer called 'Rooie Dop The Daily Grind'
		BeerSearchResult searched = null;
		for (BeerSearchResult beer : searchCommand.getSearchResults()) {
			if (beer.beerName.equals("Rooie Dop The Daily Grind")) {
				searched = beer;
				break;
			}
		}
		assertNotNull(searched);
		
		// Get beer details
		GetBeerDetailsCommand getCommand = new GetBeerDetailsCommand(signInUser, searched.beerId);
		getCommand.execute(apiConnection);
		assertNotNull(getCommand.getDetails());
		BeerDetails rooiedop = getCommand.getDetails();
		assertEquals("Rooie Dop The Daily Grind", rooiedop.beerName);
		assertEquals("Rooie Dop", rooiedop.brewerName);
		assertEquals("Porter", rooiedop.beerStyle);
		assertTrue(rooiedop.description.startsWith("We love the smell of fresh coffee"));
		assertEquals(6.5F, rooiedop.alcohol);

		// TODO: Test other beer commands
	}

}
