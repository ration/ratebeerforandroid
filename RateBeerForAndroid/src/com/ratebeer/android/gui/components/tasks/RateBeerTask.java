package com.ratebeer.android.gui.components.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.ratebeer.android.api.Command;
import com.ratebeer.android.api.CommandFailureResult;
import com.ratebeer.android.api.CommandResult;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.app.RateBeerForAndroid;
import com.ratebeer.android.gui.components.RateBeerActivity;

/**
 * Executes a forum command as an asynchronous task and posts back the result to
 * either onTaskSuccessResult or onTaskFailureResult.
 * @author erickok
 */
public class RateBeerTask extends AsyncTask<Void, Void, CommandResult> {
	
	private final RateBeerActivity rateBeerActivity;
	private Command rbCommand;
	private RateBeerTaskCaller caller;
	
	public RateBeerTask(RateBeerActivity rateBeerActivity, RateBeerTaskCaller caller, Command rbCommand) {
		this.rateBeerActivity = rateBeerActivity;
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
		this.rateBeerActivity.onTaskFinished(this);
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