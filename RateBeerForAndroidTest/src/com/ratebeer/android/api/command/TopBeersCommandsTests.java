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
import com.ratebeer.android.api.command.GetStyleDetailsCommand.StyleDetails;
import com.ratebeer.android.api.command.GetTopBeersCommand.TopBeer;

public class TopBeersCommandsTests extends AndroidTestCase {

	public void testExecute() {

		// Get top beers for the Abt/Quadrupel style, where Westvleteren 12 is always on top
		final Style quad = Style.ALL_STYLES.get(80);
		
		// GetStyleDetailsCommand test
		ApiConnection apiConnection = ApiConnection_.getInstance_(getContext());
		UserSettings user = TestHelper.getUser(getContext(), false);
		GetStyleDetailsCommand styleCommand = new GetStyleDetailsCommand(user, quad.getId());
		styleCommand.execute(apiConnection);
		assertNotNull(styleCommand.getDetails());
		StyleDetails style = styleCommand.getDetails();
		assertEquals("Abt/Quadrupel", style.name);
		assertTrue("Style description doesn't include HTML", style.name.indexOf("<") < 0 && style.name.indexOf(">") < 0);
		// Not displayed in the app yet, but the glasses are also parsed
		assertEquals(1, style.servedIn.size());
		assertEquals("Trappist glass", style.servedIn.get(0));
		// Check that Westvleteren 12 is on top here
		assertNotNull(style.beers);
		assertEquals(25, style.beers.size()); // Not logged in, so we should see 25 (only premium users see 50)
		TopBeer westvleteren = style.beers.get(0);
		assertNotNull(westvleteren);
		assertEquals("Westvleteren 12 (XII)", westvleteren.beerName);
		assertEquals(4934, westvleteren.beerId);
		assertEquals(1, westvleteren.orderNr);
		assertTrue("Westvleteren score is between 4 and 5", westvleteren.score > 4D && westvleteren.score < 5D);

	}

}
