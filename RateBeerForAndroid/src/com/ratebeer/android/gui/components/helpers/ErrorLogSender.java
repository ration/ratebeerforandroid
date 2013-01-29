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
package com.ratebeer.android.gui.components.helpers;

import java.sql.SQLException;
import java.util.List;

import android.content.ActivityNotFoundException;
import android.content.Intent;

import com.googlecode.androidannotations.annotations.App;
import com.googlecode.androidannotations.annotations.Bean;
import com.googlecode.androidannotations.annotations.EBean;
import com.googlecode.androidannotations.annotations.OrmLiteDao;
import com.j256.ormlite.dao.Dao;
import com.ratebeer.android.R;
import com.ratebeer.android.app.RateBeerForAndroid;
import com.ratebeer.android.app.persistance.DatabaseHelper;

@EBean
public class ErrorLogSender {

	@App
	protected RateBeerForAndroid context;
	@Bean
	protected Log Log;
	@OrmLiteDao(helper = DatabaseHelper.class, model = ErrorLogEntry.class)
	protected Dao<ErrorLogEntry, Integer> errorLogDao;
	
    public void collectAndSendLog(final String user) {
    	
    	try {
			
    		// Prepare an email with error logging information
			StringBuilder body = new StringBuilder();
			body.append("Please describe your problem:\n\n\n");
			body.append("\nRateBeer username: ");
			body.append(user);
			body.append("\nRateBeer for Android version: ");
			body.append(ActivityUtil.getVersionNumber(context));
			body.append(" (r");
			body.append(ActivityUtil.getVersionCode(context));
			body.append(")");
			body.append("\n");
			body.append("");
			body.append("\n\nConnection and error log:");
			
			// Print the individual error log messages as stored in the database
			List<ErrorLogEntry> all = errorLogDao.queryBuilder().orderBy(ErrorLogEntry.ID, true).query();
			for (ErrorLogEntry errorLogEntry : all) {
				body.append("\n");
				body.append(errorLogEntry.getLogId());
				body.append(" -- ");
				body.append(errorLogEntry.getDateAndTime());
				body.append(" -- ");
				body.append(errorLogEntry.getPriority());
				body.append(" -- ");
				body.append(errorLogEntry.getMessage());
			}
			
			Intent target = new Intent(Intent.ACTION_SEND);
			target.setType("message/rfc822");
			target.putExtra(Intent.EXTRA_EMAIL, new String[] { "rb@2312.nl" });
			target.putExtra(Intent.EXTRA_SUBJECT, "RateBeer for Android error report");
			target.putExtra(Intent.EXTRA_TEXT, body.toString());
			try {
				context.startActivity(Intent.createChooser(target, context.getString(R.string.error_sendreport))
						.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
			} catch (ActivityNotFoundException e) {
				Log.i(RateBeerForAndroid.LOG_NAME, "Tried to send error log, but there is no email app installed.");
			}
			
		} catch (SQLException e) {
			Log.e(RateBeerForAndroid.LOG_NAME,
					"Cannot read the error log to build an error report to send: " + e.toString());
		}
    	
    }

}
