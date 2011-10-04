/*
 *	This file was originally part of Transdroid <http://www.transdroid.org>
 *	
 *	Transdroid is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *	
 *	Transdroid is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *	
 *	You should have received a copy of the GNU General Public License
 *	along with Transdroid.  If not, see <http://www.gnu.org/licenses/>.
 *	
 */
package com.ratebeer.android.gui.components;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Environment;

import com.j256.ormlite.dao.Dao;
import com.ratebeer.android.api.HttpHelper;
import com.ratebeer.android.app.persistance.OfflineRating;

public class ImportExport {

	public static final String DEFAULT_FILES_DIR = Environment.getExternalStorageDirectory().toString() + "/RateBeerForAndroid";
	public static final String DEFAULT_RATINGS_FILENAME = "/OfflineRatings.json";
	public static final File DEFAULT_RATINGS_FILE = new File(DEFAULT_FILES_DIR + DEFAULT_RATINGS_FILENAME);
	
	/**
	 * Synchronously writes the offline ratings stored in the database
	 * @param dao The database to write records to
	 * @param inputFile The file from which to read ratings
	 * @throws JSONException Thrown when the file did not contain valid JSON content
	 * @throws IOException Thrown when the file could not be read 
	 * @throws SQLException Thrown when the database could not be written to
	 */
	public static void importRatings(Dao<OfflineRating, Integer> dao, File inputFile) throws JSONException, IOException, SQLException {
		
		// Read the settings file
		String raw = HttpHelper.getResponseString(new FileInputStream(inputFile));
		JSONObject json = new JSONObject(raw);
		
		if (json.has("ratings")) {
			for (int i = 0; i < json.getJSONArray("ratings").length(); i++) {
				JSONObject item = json.getJSONArray("ratings").getJSONObject(i);
				OfflineRating rating = OfflineRating.fromFile(
						item.getInt("beerId"),
						item.has("beerName")? item.getString("beerName"): null,
						item.getInt("originalRatingId"),
						item.has("originalRatingDate")? item.getString("originalRatingDate"): null,
						item.getInt("aroma"),
						item.getInt("appearance"),
						item.getInt("taste"),
						item.getInt("palate"),
						item.getInt("overall"),
						item.has("comments")? item.getString("comments"): null,
						new Date(item.getLong("timeSaved")));
				dao.create(rating);	
			}
		}
		
	}
	
	/**
	 * Export the offline ratings from the database to a file
	 * @param list The offline ratings to export
	 * @param outputfile The file to write to
	 * @throws JSONException Thrown when the content could not be written to JSON
	 * @throws IOException Thrown when the file could not be written to
	 */
	public static void exportRatings(List<OfflineRating> list, File outputfile) throws JSONException, IOException {
		
		// Create a single JSON object with all offline ratings
		JSONObject json = new JSONObject();
		JSONArray ratings = new JSONArray();
		
		for (OfflineRating item : list) {
			JSONObject rating = new JSONObject();
			rating.put("beerId", item.getBeerId());
			rating.put("beerName", item.getBeerName());
			rating.put("originalRatingId", item.getOriginalRatingId());
			rating.put("originalRatingDate", item.getOriginalRatingDate());
			rating.put("aroma", item.getAroma());
			rating.put("appearance", item.getAppearance());
			rating.put("taste", item.getTaste());
			rating.put("palate", item.getPalate());
			rating.put("overall", item.getOverall());
			rating.put("comments", item.getComments());
			rating.put("timeSaved", item.getTimeSaved().getTime());
			ratings.put(rating);
		}
		json.put("ratings", ratings);
		
		// Serialize the JSON object to a file
		if (outputfile.exists()) {
			outputfile.delete();
		}
		outputfile.getParentFile().mkdirs();
		outputfile.createNewFile();
		FileWriter writer = new FileWriter(outputfile);
		writer.write(json.toString(2));
		writer.flush();
		writer.close();
		
	}

}
