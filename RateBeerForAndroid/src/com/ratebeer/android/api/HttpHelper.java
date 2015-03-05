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

import java.util.regex.Pattern;

import com.tecnick.htmlutils.htmlentities.HTMLEntities;

public class HttpHelper {

	public static final String UTF8 = "UTF-8";

	public static String cleanHtml(String value) {
		// Translation of HTML-encoded characters (and line breaks)
		value = value.replaceAll("\r\n", "");
		value = value.replaceAll("\n", "");
		value = value.replaceAll("\r", "");
		value = value.replaceAll("<br>", "\n");
		value = value.replaceAll("<br />", "\n");
		value = value.replaceAll("<BR>", "\n");
		value = value.replaceAll("<BR />", "\n");
		value = value.replaceAll("&quot;", "\"");
		return HTMLEntities.unhtmlentities(value);
	}

	public static String normalizeSearchQuery(String query) {
		// RateBeer crashes down badly when providing a ' (apostrophe) in a search; replace it instead by a ? (wildcard)
		query = query.replace("'", "?");
		
		// Now translate diacritics
		// (from http://stackoverflow.com/questions/1008802/converting-symbols-accent-letters-to-english-alphabet)
		// The Normalizer class is unavailable < API level 9, so use it through an interface using reflection
		try {
			if (android.os.Build.VERSION.SDK_INT >= 9) {
				QueryNormalizer normalizer = (QueryNormalizer) Class.forName("com.ratebeer.android.api.QueryNormalizerImpl").newInstance();
				// Normalize (which translates the diacritics)
				String normalized = normalizer.normalize(query);
				// And remove the marks to only leave Latin characters
				Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
				query = pattern.matcher(normalized).replaceAll("");
			}
		} catch (NoClassDefFoundError e) {
			// Not available - just continue
		} catch (Exception e) {
			// Not available - just continue
		}

		// Translate other special characters into English alphabet characters by hand
		query = query.replaceAll("æ", "ae");
		query = query.replaceAll("Æ", "AE");
		query = query.replaceAll("ß", "ss");
		query = query.replaceAll("ø", "o");
		query = query.replaceAll("Ø", "O");
<<<<<<< HEAD
		return query;
=======
		return query.trim();
>>>>>>> 9cb2b20cee7ae90e7a5ea61c0ebff4e0c86a6dd6
	}

}
