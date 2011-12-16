package com.ratebeer.android.api.command;

import java.util.ArrayList;

import android.test.AndroidTestCase;

import com.ratebeer.android.api.command.UpcSearchCommand.UpcSearchResult;

public class UpcSearchCommandTests extends AndroidTestCase {

	public void testExecute() {
		UpcSearchCommand command = new UpcSearchCommand(null, "636251770128");
		command.execute();
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
