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

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.Bean;
import com.googlecode.androidannotations.annotations.EBean;
import com.googlecode.androidannotations.annotations.UiThread;
import com.ratebeer.android.R;
import com.ratebeer.android.api.ApiConnection;
import com.ratebeer.android.api.Command;
import com.ratebeer.android.api.CommandFailureResult;
import com.ratebeer.android.api.CommandResult;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.app.ApplicationSettings;
import com.ratebeer.android.gui.components.helpers.Log;
import com.ratebeer.android.gui.components.helpers.OnProgressChangedListener;
import com.ratebeer.android.gui.components.helpers.RateBeerTaskCaller;
import com.ratebeer.android.gui.fragments.DashboardFragment;

@EBean
public abstract class RateBeerActivity extends SherlockFragmentActivity implements OnProgressChangedListener {

	// Tracking of any running tasks
	private int tasksRunning = 0;
	private boolean inProgress = false; // Whether there is any progress going on (which is not exclusively a RateBeerTask)
	// Local progress reporter (showing progress in the action bar)
	private OnProgressChangedListener onProgressChangedListener = this;

	@Bean
	protected Log Log;
	@Bean
	protected ApplicationSettings applicationSettings;
	@Bean
	protected ApiConnection apiConnection;
	
	public RateBeerActivity() {
	}
	
	@AfterViews
	protected void prepareActionBar() {
		
		// Set up action bar
		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_USE_LOGO,
					ActionBar.DISPLAY_SHOW_TITLE);
			getSupportFragmentManager().addOnBackStackChangedListener(onBackStackChanged);
		}
		
		// DEBUG: Attach to ViewServer in order to use the HierarchyViewer tool
		//ViewServer.get(this).addWindow(this);

	}

	@Override
	public void onResume() {
		super.onResume();
		
		// Update the user settings indicator
		supportInvalidateOptionsMenu();

		// Restore 'up' button state
		Fragment now = getSupportFragmentManager().findFragmentById(R.id.frag_content);
		if (getSupportActionBar() != null)
			getSupportActionBar().setDisplayHomeAsUpEnabled(now != null && !(now instanceof DashboardFragment));
		
		// DEBUG: Detach to ViewServer in order to use the HierarchyViewer tool
		//ViewServer.get(this).setFocusedWindow(this);
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		// DEBUG: Detach to ViewServer in order to use the HierarchyViewer tool
		//ViewServer.get(this).removeWindow(this);
	}

	/**
	 * The extending activity should load the fragment into the activity's interface
	 * @param fragment The fragment to show
	 * @param addToBackStack Whether to also add this fragment to the backstack 
	 */
	public abstract void load(RateBeerFragment fragment, boolean addToBackStack);

	public void setOnProgressChangedListener(OnProgressChangedListener onProgressChangedListener) {
		this.onProgressChangedListener = onProgressChangedListener;
	}

	private OnBackStackChangedListener onBackStackChanged = new OnBackStackChangedListener() {
		@Override
		public void onBackStackChanged() {
			Fragment now = getSupportFragmentManager().findFragmentById(R.id.frag_content);
			getSupportActionBar().setDisplayHomeAsUpEnabled(now != null && !(now instanceof DashboardFragment));
		}
	};

	/**
	 * Called when a task (notably, but not exclusively, a {@link RateBeerTask}) starts or finishes progress. The 
	 * activity's progress indicator (normally the action bar) will be updated
	 * @param inProgress Whether a task is in progress
	 */
	@Override
	public void setProgress(boolean inProgress) {
		this.inProgress = inProgress;
		// Redraw the options menu so the refresh action bar item can show a undetermined progress indicator
		supportInvalidateOptionsMenu();
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
    	tasksRunning++;
    	if (onProgressChangedListener != null) {
    		onProgressChangedListener.setProgress(true);
    	}
    	// Start execution in background
    	executeTask(caller, command);
    }
    
    @Background
    protected void executeTask(RateBeerTaskCaller caller, Command command) {
    	onTaskFinished(caller, command.execute(apiConnection));
    }

    @UiThread
	public void onTaskFinished(RateBeerTaskCaller caller, CommandResult result) {
    	// Update progress state
		tasksRunning--;
		if (onProgressChangedListener != null) {
			onProgressChangedListener.setProgress(tasksRunning > 0);
		}
		// Distribute result
		Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, result.toString());
		if (caller.isBound()) {
			if (result instanceof CommandSuccessResult) {
				caller.onTaskSuccessResult((CommandSuccessResult) result);
			} else if (result instanceof CommandFailureResult) {
				caller.onTaskFailureResult((CommandFailureResult) result);
			}
		}
	};
}
