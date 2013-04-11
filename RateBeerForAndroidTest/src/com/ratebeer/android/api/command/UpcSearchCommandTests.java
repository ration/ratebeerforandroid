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
		UpcSearchCommand command = new UpcSearchCommand(user, "652682011012");
		command.execute(apiConnection);
		ArrayList<UpcSearchResult> results = command.getUpcSearchResults();
		assertNotNull(results);
		assertEquals(1, results.size());
		UpcSearchResult result = results.get(0);
		assertEquals(15917, result.beerId);
		assertEquals("Three Floyds Dark Lord Russian Imperial Stout", result.beerName);
		assertEquals(231, result.brewerId);
		assertEquals("Three Floyds Brewing Company", result.brewerName);
		assertTrue("Average rating is ~4.32", result.averageRating >= 4.01 && result.averageRating <= 4.99);
		assertTrue("ABV is ~15%", result.abv >= 10.1 && result.abv <= 19.9);
		
	}

}
