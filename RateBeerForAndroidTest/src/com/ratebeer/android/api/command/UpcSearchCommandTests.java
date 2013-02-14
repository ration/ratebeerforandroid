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

import android.test.AndroidTestCase;

import com.ratebeer.android.api.ApiConnection;
import com.ratebeer.android.api.ApiConnection_;
import com.ratebeer.android.api.UserSettings;
import com.ratebeer.android.api.command.UpcSearchCommand.UpcSearchResult;

public class UpcSearchCommandTests extends AndroidTestCase {

	public void testExecute() {
		
		// UpcSearchCommand test
		ApiConnection apiConnection = ApiConnection_.getInstance_(getContext());
		UserSettings user = TestHelper.getUser(getContext(), false);
		UpcSearchCommand command = new UpcSearchCommand(user, "636251770128");
		command.execute(apiConnection);
		ArrayList<UpcSearchResult> results = command.getUpcSearchResults();
		assertNotNull(results);
		assertEquals(1, results.size());
		UpcSearchResult result = results.get(0);
		assertEquals(422, result.beerId);
		assertEquals("Stone India Pale Ale (IPA)", result.beerName);
		assertEquals(76, result.brewerId);
		assertEquals("Stone Brewing Co.", result.brewerName);
		assertTrue(result.averageRating > 2.98 && result.averageRating < 4.98);
		assertTrue(result.abv > 6.4 && result.abv < 7.4);
		
	}

}
