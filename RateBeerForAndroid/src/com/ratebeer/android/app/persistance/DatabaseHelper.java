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
package com.ratebeer.android.app.persistance;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.ratebeer.android.gui.components.helpers.ErrorLogEntry;

/**
 * Helper to access the database to access persisting objects.
 * @author erickok
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	private static final String DATABASE_NAME = "ratebeer.db";
	private static final int DATABASE_VERSION = 6;

	private Dao<OfflineRating, Integer> ratingDao;
	private Dao<BeerMail, Integer> mailDao;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
		try {
			TableUtils.createTable(connectionSource, OfflineRating.class);
			TableUtils.createTable(connectionSource, BeerMail.class);
			TableUtils.createTable(connectionSource, ErrorLogEntry.class);
			TableUtils.createTable(connectionSource, CustomList.class);
			TableUtils.createTable(connectionSource, CustomListBeer.class);
		} catch (SQLException e) {
			Log.e(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "Could not create new table for OfflineRating", e);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int oldVersion,
			int newVersion) {
		try {
			switch (oldVersion) {
			case 1:
			case 2:
				// We don't care about those old version; just drop and recreate the tables
				TableUtils.dropTable(connectionSource, OfflineRating.class, true);
				TableUtils.createTable(connectionSource, OfflineRating.class);
			case 3:
				TableUtils.createTable(connectionSource, BeerMail.class);
			case 4:
				TableUtils.createTable(connectionSource, ErrorLogEntry.class);
			case 5:
				TableUtils.createTable(connectionSource, CustomList.class);
				TableUtils.createTable(connectionSource, CustomListBeer.class);
			/*case 3:
				UpdateBuilder<OfflineRating, Integer> upgrade = getOfflineRatingDao().updateBuilder();
				upgrade.updateColumnExpression("offlineId", "0");
				getOfflineRatingDao().update(upgrade.prepare());
				getOfflineRatingDao().executeRaw("alter table...", "");
				etc...*/
			}
			
		} catch (SQLException e) {
			Log.e(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "Could not upgrade the table for OfflineRating", e);
		}
	}

	public Dao<OfflineRating, Integer> getOfflineRatingDao() throws SQLException {
		if (ratingDao == null) {
			ratingDao = getDao(OfflineRating.class);
		}
		return ratingDao;
	}

	public Dao<BeerMail, Integer>  getBeerMailDao() throws SQLException {
		if (mailDao == null) {
			mailDao = getDao(BeerMail.class);
		}
		return mailDao;
	}
}
