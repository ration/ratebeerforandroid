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
package com.ratebeer.android.gui.components;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.util.Log;
import android.widget.FrameLayout;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.ratebeer.android.R;
import com.ratebeer.android.api.Command;
import com.ratebeer.android.api.CommandFailureResult;
import com.ratebeer.android.api.CommandResult;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.api.RateBeerApi;
import com.ratebeer.android.api.UserSettings;
import com.ratebeer.android.app.ApplicationSettings;
import com.ratebeer.android.app.RateBeerForAndroid;
import com.ratebeer.android.app.persistance.DatabaseHelper;

public abstract class RateBeerActivity extends FragmentActivity implements OnProgressChangedListener {

	// Action bar items provided by RateBeerFragment
	public static final int MENU_REFRESH = 9801;
	public static final int MENU_SIGNIN = 9901;
	public static final int MENU_SIGNOUT = 9902;
	// Action bar items provided by RateBeerActivity
	private static final int MENU_ERRORREPORT = 9903;
	
	// Tracking of any running tasks
	private int tasksRunning = 0;
	private boolean inProgress = false; // Whether there is any progress going on (which is not exclusively a RateBeerTask)
	// Local progress reporter (showing progress in the action bar)
	private OnProgressChangedListener onProgressChangedListener = this;

	// ORM persistence layer database helper
	private volatile DatabaseHelper helper;
	private volatile boolean created = false;
	private volatile boolean destroyed = false;

	// Google Maps container management
	private MapView mapViewInstance = null;

	public void onCreate(Bundle savedInstanceState, int layoutResID) {

		// Set up database helper
		if (helper == null) {
			helper = getHelperInternal(this);
			created = true;
		}
		
		super.onCreate(savedInstanceState);
		setContentView(layoutResID);
		
		// DEBUG: Attach to ViewServer in order to use the HierarchyViewer tool
		//ViewServer.get(this).addWindow(this);

		initialize(savedInstanceState);
		
		// Handle a new intent
		if (savedInstanceState == null && getIntent() != null) {
			handleStartIntent(getIntent());
		}
		
	}

	public void onResume() {
		super.onResume();
		
		// Update the user settings indicator
		invalidateOptionsMenu();
		
		// DEBUG: Detach to ViewServer in order to use the HierarchyViewer tool
		//ViewServer.get(this).setFocusedWindow(this);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		releaseHelper(helper);
		destroyed = true;
		// DEBUG: Detach to ViewServer in order to use the HierarchyViewer tool
		//ViewServer.get(this).removeWindow(this);
	}

	/**
	 * Can be overridden to handle the initialization of the activity
	 * @param savedInstanceState
	 */
	public void initialize(Bundle savedInstanceState) {};
	
	/**
	 * Can be overridden to handle both startup and new intents
	 * @param intent The most recent intent received
	 */
	protected void handleStartIntent(Intent intent) {};

	/**
	 * The extending activity should load the fragment into the activity's interface
	 * @param fragment The fragment to show
	 */
	public abstract void load(RateBeerFragment fragment);

	/**
	 * The extending activity should load the fragment into the activity's interface
	 * @param leftFragment The fragment to show on the left side of the screen
	 * @param rightFragment The fragment to show on the right side of the screen
	 */
	public abstract void load(RateBeerFragment leftFragment, RateBeerFragment rightFragment);
	
    private RateBeerForAndroid getRateBeerApplication() {
    	return (RateBeerForAndroid) getApplication();
    }

	/**
	 * Returns the API to the ratebeer.com website
	 * @return The RateBeer API object
	 */
    public RateBeerApi getApi() {
    	return getRateBeerApplication().getApi();
    }

    /**
     * Returns the application settings
     * @return The application-wide settings object
     */
    public ApplicationSettings getSettings() {
    	return getRateBeerApplication().getSettings();
    }

    /**
     * Returns the user settings, if a user is signed in
     * @return The user settings or null if no user is signed in
     */
    public UserSettings getUser() {
    	return getSettings().getUserSettings();
    }

	public void setOnProgressChangedListener(OnProgressChangedListener onProgressChangedListener) {
		this.onProgressChangedListener = onProgressChangedListener;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		// Add option to send an error report
		MenuItem errorReport = menu.add(Menu.NONE, MENU_ERRORREPORT, MENU_ERRORREPORT, R.string.error_sendreport);
		errorReport.setIcon(R.drawable.ic_menu_errorreport);
		errorReport.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		
		// Allow activity and fragments to add items
		super.onCreateOptionsMenu(menu);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ERRORREPORT:
			ErrorLogSender.collectAndSendLog(this, getUser() == null? "<none>": getUser().getUsername());
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Called when a task (notably, but not exclusively, a {@link RateBeerTask}) starts or finishes progress. The activity's progress indicator (i.e.
	 * the action bar) will be updated
	 * @param inProgress Whether a task is in progress
	 */
	@Override
	public void setProgress(boolean inProgress) {
		this.inProgress  = inProgress;
		// Redraw the options menu so the refresh action bar item can show a undetermined progress indicator
		invalidateOptionsMenu();
	}

	/**
	 * Returns whether this activity has tasks that are in progress
	 * @return True if some task (e.g. a RateBeerTask) is in progress
	 */
	public boolean isInProgress() {
		return inProgress;
	}
	
    /**
     * Starts the execution of a forum command
     * @param command The command to execute
     */
    public void execute(RateBeerTaskCaller caller, Command command) {
    	RateBeerTask task = new RateBeerTask(caller, command);
    	//getActivity().addTask(task);
    	tasksRunning++;
    	// Start execution
    	task.execute();
    	if (onProgressChangedListener != null) {
    		onProgressChangedListener.setProgress(true);
    	}
    }

	public void onTaskFinished(RateBeerTask task) {
		tasksRunning--;
		if (onProgressChangedListener != null) {
			onProgressChangedListener.setProgress(tasksRunning > 0);
		}
	};

	/**
	 * Executes a forum command as an asynchronous task and posts back the result to
	 * either onTaskSuccessResult or onTaskFailureResult.
	 */
	private class RateBeerTask extends AsyncTask<Void, Void, CommandResult> {
		
		private Command rbCommand;
		private RateBeerTaskCaller caller;
		
		public RateBeerTask(RateBeerTaskCaller caller, Command rbCommand) {
			this.caller = caller;
			this.rbCommand = rbCommand;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected CommandResult doInBackground(Void... params) {
			return rbCommand.execute();
		}

		@Override
		protected void onPostExecute(CommandResult result) {
			onTaskFinished(this);
			Log.d(RateBeerForAndroid.LOG_NAME, result.toString());
			if (caller.isBound()) {
				if (result instanceof CommandSuccessResult) {
					caller.onTaskSuccessResult((CommandSuccessResult) result);
				} else if (result instanceof CommandFailureResult) {
					caller.onTaskFailureResult((CommandFailureResult) result);
				}
			}
		}

	}

	public MapView requestMapViewInstance() {

		// Create the map view if if we didn't have one yet
		if (mapViewInstance == null) {
			mapViewInstance = new MapView(this, getString(R.string.app_googlemapskey));
		}
		
		// Make sure it is not attached to any view
		if (mapViewInstance.getParent() != null && mapViewInstance.getParent() instanceof FrameLayout) {
			((FrameLayout)mapViewInstance.getParent()).removeView(mapViewInstance);
		}
		
		return mapViewInstance;
		
	}

	public GeoPoint getPoint(double lat, double lon) {
		return(new GeoPoint((int)(lat*1000000.0), (int)(lon*1000000.0)));
	}

	/**
	 * Get a database helper for this action.
	 */
	public DatabaseHelper getHelper() {
		if (helper == null) {
			if (!created) {
				throw new IllegalStateException("A call has not been made to onCreate() yet so the helper is null");
			} else if (destroyed) {
				throw new IllegalStateException(
						"A call to onDestroy has already been made and the helper cannot be used after that point");
			} else {
				throw new IllegalStateException("Helper is null for some unknown reason");
			}
		} else {
			return helper;
		}
	}

	/**
	 * Get a connection source for this action.
	 */
	public ConnectionSource getConnectionSource() {
		return getHelper().getConnectionSource();
	}

	/**
	 * This is called internally by the class to populate the helper object instance. This should not be called directly
	 * by client code unless you know what you are doing. Use {@link #getHelper()} to get a helper instance. If you are
	 * managing your own helper creation, override this method to supply this activity with a helper instance.
	 * 
	 * <p>
	 * <b> NOTE: </b> If you override this method, you most likely will need to override the
	 * {@link #releaseHelper(OrmLiteSqliteOpenHelper)} method as well.
	 * </p>
	 */
	protected DatabaseHelper getHelperInternal(Context context) {
		DatabaseHelper newHelper = (DatabaseHelper) OpenHelperManager.getHelper(context, DatabaseHelper.class);
		return newHelper;
	}

	/**
	 * Release the helper instance created in {@link #getHelperInternal(Context)}. You most likely will not need to call
	 * this directly since {@link #onDestroy()} does it for you.
	 * 
	 * <p>
	 * <b> NOTE: </b> If you override this method, you most likely will need to override the
	 * {@link #getHelperInternal(Context)} method as well.
	 * </p>
	 */
	protected void releaseHelper(DatabaseHelper helper) {
		OpenHelperManager.releaseHelper();
		helper = null;
	}

}
