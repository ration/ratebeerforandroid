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

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ratebeer.android.R;
import com.ratebeer.android.api.ApiException;
import com.ratebeer.android.api.Command;
import com.ratebeer.android.api.CommandFailureResult;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.api.command.SignOutCommand;
import com.ratebeer.android.app.RateBeerForAndroid;
import com.ratebeer.android.gui.SignIn;
import com.ratebeer.android.gui.components.tasks.RateBeerTaskCaller;
import com.ratebeer.android.gui.fragments.UserViewFragment;

public class RateBeerFragment extends Fragment implements RateBeerTaskCaller {

	protected boolean showSignInMenuItem = true;

	public RateBeerFragment() {
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		// Add user sign in/out item
		if (showSignInMenuItem && getRateBeerActivity() != null) {
			String userText = getRateBeerActivity().getUser() == null? getString(R.string.signin_signin): 
				getRateBeerActivity().getUser().getUsername();
			// Provide sign in/my profile action option
			MenuItem signin = menu.add(Menu.NONE, RateBeerActivity.MENU_SIGNIN, RateBeerActivity.MENU_SIGNIN, userText);
			signin.setIcon(R.drawable.ic_action_signin);
			signin.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
			if (getRateBeerActivity().getUser() != null) {
				// Provide sign out option (always shown in (overflow) menu)
				MenuItem signout = menu.add(Menu.NONE, RateBeerActivity.MENU_SIGNOUT, RateBeerActivity.MENU_SIGNOUT, R.string.signin_signout);
				signout.setIcon(R.drawable.ic_menu_signout);
				signout.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
			}
		}
		
		// If there is an action bar item representing MENU_REFRESH and we have tasks in progress, show custom view with an undetermined progress indicator
		if (getRateBeerActivity() != null && getRateBeerActivity().isInProgress()) {
			for (int i = 0; i < menu.size(); i++) {
				if (menu.getItem(i).getItemId() == RateBeerActivity.MENU_REFRESH) {
					View view = getRateBeerActivity().getLayoutInflater().inflate(R.layout.actionbar_progressitem, null);
					menu.getItem(i).setActionView(view);
				}
			}
		}

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case RateBeerActivity.MENU_SIGNIN:
			if (getRateBeerActivity().getUser() == null) {
				startActivity(new Intent(getRateBeerActivity(), SignIn.class));
			} else {
				getRateBeerActivity().load(new UserViewFragment(getRateBeerActivity().getUser().getUsername(), 
						getRateBeerActivity().getUser().getUserID()));
			}
			return true;
		case RateBeerActivity.MENU_SIGNOUT:
			getRateBeerActivity().execute(signOutHandler, new SignOutCommand(getRateBeerActivity().getApi()));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private RateBeerTaskCaller signOutHandler = new RateBeerTaskCaller() {
		@Override
		public void onTaskSuccessResult(CommandSuccessResult result) {
			// Successfully signed out
			if (getRateBeerActivity() == null) {
				// No longer visible
				return;
			}
			Toast.makeText(getRateBeerActivity(), R.string.signin_signoutsuccess, Toast.LENGTH_LONG).show();
			getRateBeerActivity().getSettings().saveUserSettings(null);
			getRateBeerActivity().invalidateOptionsMenu();
		}
		@Override
		public void onTaskFailureResult(CommandFailureResult result) {
			// Sign out failure
			String message = "";
			switch (result.getException().getType()) {
			case Offline:
				message = getText(R.string.error_offline).toString();
				break;
			case AuthenticationFailed:
				message = getText(R.string.error_authenticationfailed).toString();
				break;
			case CommandFailed:
				message = getText(R.string.error_commandfailed).toString();
				break;
			}
			Toast.makeText(getRateBeerActivity(), message, Toast.LENGTH_LONG).show();
		}
		@Override
		public boolean isBound() {
			return true;
		}
	};

	/**
	 * Convenience method that start the command execution in this fragment's activity
	 * @param command The command to execute
	 */
	protected void execute(Command command) {
		getRateBeerActivity().execute(this, command);
	}

	@Override
	public boolean isBound() {
		return getActivity() != null;
	}

	@Override
	public void onTaskFailureResult(CommandFailureResult result) {}

	@Override
	public void onTaskSuccessResult(CommandSuccessResult result) {}

	/**
	 * Publish an exception that occurred during the execution of a forum command
	 * @param textview The TextView to show the error on, or null if not applicable
	 * @param exception The exception that occurred
	 */
	protected void publishException(TextView textview, ApiException exception) {
		String message = "";
		switch (exception.getType()) {
		case Offline:
			message = getText(R.string.error_offline).toString();
			break;
		case AuthenticationFailed:
			message = getText(R.string.error_authenticationfailed).toString();
			break;
		case CommandFailed:
			message = getText(R.string.error_commandfailed).toString();
			break;
		}
		publishException(textview, message);
		
	}
	
	/**
	 * Publish an exception to the user as a toast and possibly to a text
	 * view as well.
	 * @param textview The TextView to show the error on, or null if not applicable
	 * @param message The (readily translated) message to show
	 */
	protected void publishException(TextView textview, String message) {
			
		// Show a toast message with the error
		Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
		
		// Show the error on the given TextView as well
		if (textview != null) {
			textview.setText(message);
		}
		
	}

    public RateBeerActivity getRateBeerActivity() {
    	return (RateBeerActivity) getActivity();
    }

    public RateBeerForAndroid getRateBeerApplication() {
    	return (RateBeerForAndroid) getActivity().getApplication();
    }

}
