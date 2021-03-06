package com.ratebeer.android.gui.components.helpers;

import java.sql.SQLException;
import java.util.Date;

import com.googlecode.androidannotations.annotations.EBean;
import com.googlecode.androidannotations.annotations.OrmLiteDao;
import com.googlecode.androidannotations.api.Scope;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.ratebeer.android.app.persistance.DatabaseHelper;

@EBean(scope = Scope.Singleton)
public class Log {

	public static final String LOG_NAME = "RateBeerForAndroid";
	
	private static final long MAX_LOG_AGE = 15 * 60 * 1000; // 15 minutes
	
	@OrmLiteDao(helper = DatabaseHelper.class, model = ErrorLogEntry.class)
	Dao<ErrorLogEntry, Integer> errorLogDao;

	protected void log(String logName, int priority, String message) {
		android.util.Log.println(priority, LOG_NAME, message);
		try {
			// Store this log message to the database
			errorLogDao.create(new ErrorLogEntry(priority, logName, message));
			// Truncate the error log
			DeleteBuilder<ErrorLogEntry, Integer> db = errorLogDao.deleteBuilder();
			db.setWhere(db.where().le(ErrorLogEntry.DATEANDTIME, new Date(new Date().getTime() - MAX_LOG_AGE)));
			errorLogDao.delete(db.prepare());
		} catch (SQLException e) {
			android.util.Log.e(LOG_NAME, "Cannot write log message to database: " + e.toString());
		}
	}
	
	public void e(String logName, String message) {
		log(logName, android.util.Log.ERROR, message);
	}
	
	public void i(String logName, String message) {
		log(logName, android.util.Log.INFO, message);
	}
	
	public void d(String logName, String message) {
		log(logName, android.util.Log.DEBUG, message);
	}

}
