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

import android.view.View;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.googlecode.androidannotations.annotations.Bean;
import com.googlecode.androidannotations.annotations.EBean;
import com.ratebeer.android.R;
import com.ratebeer.android.api.ApiException;
import com.ratebeer.android.api.Command;
import com.ratebeer.android.api.CommandFailureResult;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.api.UserSettings;
import com.ratebeer.android.api.command.SignOutCommand;
import com.ratebeer.android.app.ApplicationSettings;
import com.ratebeer.android.gui.SignIn_;
import com.ratebeer.android.gui.components.helpers.Log;
import com.ratebeer.android.gui.components.helpers.RateBeerTaskCaller;
import com.ratebeer.android.gui.fragments.UserViewFragment_;

import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;

@EBean
public abstract class RateBeerFragment extends SherlockFragment implements RateBeerTaskCaller {

	// Action bar items provided by RateBeerFragment
	public static final int MENU_SIGNIN = 9801;
	public static final int MENU_SIGNOUT = 9802;
	
	protected boolean showSignInMenuItem = true;

	@Bean
	protected Log Log;
	@Bean
	protected ApplicationSettings applicationSettings = null;
	
	public RateBeerFragment() {
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		// Provide sign in/my profile action option
		// Note: text and action is not set until onPrepareOptionsMenu
		MenuItem signin = menu.add(Menu.NONE, MENU_SIGNIN, MENU_SIGNIN, "");
		signin.setIcon(R.drawable.ic_action_signin);
		signin.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		// Provide sign out option (always shown in (overflow) menu)
		// Note: action is not shown until onPrepareOptionsMenu
		MenuItem signout = menu.add(Menu.NONE, MENU_SIGNOUT, MENU_SIGNOUT, R.string.signin_signout);
		signout.setIcon(R.drawable.ic_menu_signout);
		signout.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		// Add user sign in/out item
		MenuItem signIn = menu.findItem(MENU_SIGNIN);
		MenuItem signOut = menu.findItem(MENU_SIGNOUT);
		if (showSignInMenuItem && getActivity() != null) {
			String userText = getUser() == null? getString(R.string.signin_signin): getUser().getUsername();
			signIn.setVisible(true);
			signIn.setTitle(userText);
			signOut.setVisible(getUser() != null);
		} else {
			signIn.setVisible(false);
			signOut.setVisible(false);
		}
		
		// If there is an action bar item representing MENU_REFRESH and we have tasks in progress, show custom view with an undetermined progress indicator
		if (getActivity() != null && getRateBeerActivity().isInProgress()) {
			for (int i = 0; i < menu.size(); i++) {
				if (menu.getItem(i).getItemId() == R.id.menu_refresh) {
					View view = getRateBeerActivity().getLayoutInflater().inflate(R.layout.actionbar_progressitem, null);
					menu.getItem(i).setActionView(view);
				}
			}
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_SIGNIN:
			if (getUser() == null) {
				SignIn_.intent(getActivity()).start();
			} else {
				load(UserViewFragment_.builder().userId(getUser().getUserID()).userName(getUser().getUsername()).build());
			}
			return true;
		case MENU_SIGNOUT:
			getRateBeerActivity().execute(signOutHandler, new SignOutCommand(getUser()));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private RateBeerTaskCaller signOutHandler = new RateBeerTaskCaller() {
		
		@Override
		public void onTaskSuccessResult(CommandSuccessResult result) {
			if (getActivity() == null) {
				// No longer visible
				return;
			}
			// Successfully signed out
			Crouton.makeText(getActivity(), R.string.signin_signoutsuccess, Style.CONFIRM).show();
			applicationSettings.saveUserSettings(null);
			getActivity().supportInvalidateOptionsMenu();
		}
		
		@Override
		public void onTaskFailureResult(CommandFailureResult result) {
			if (getActivity() == null) {
				// No longer visible
				return;
			}
			// Sign out failure
			publishException(null, result.getException());
		}
		@Override
		public boolean isBound() {
			return true;
		}
		
	};

	/**
	 * Convenience method that calls load(fragment, true) on the attached RateBeerActivity
	 * @param fragment The fragment to show
	 */
	public void load(RateBeerFragment fragment) {
		getRateBeerActivity().load(fragment, true);
	}

	/**
	 * Convenience method that calls load(fragment, true) on the attached RateBeerActivity
	 * @param fragment The fragment to show
	 * @param addToBackStack Whether to also add this fragment to the backstack
	 */
	public void load(RateBeerFragment fragment, boolean addToBackStack) {
		getRateBeerActivity().load(fragment, addToBackStack);
	}

	/**
	 * Convenience method that start the command execution in this fragment's activity
	 * @param command The command to execute
	 */
	protected void execute(Command command) {
		if (getRateBeerActivity() != null)
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
		case ConnectionError:
			message = getText(R.string.error_connectionfailure).toString();
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
		Crouton.makeText(getActivity(), message, Style.INFO).show();
		
		// Show the error on the given TextView as well
		if (textview != null) {
			textview.setText(message);
		}
		
	}

	protected ApplicationSettings getSettings() {
		return applicationSettings;
	}
	
	protected UserSettings getUser() {
		if (applicationSettings == null)
			return null;
		return applicationSettings.getUserSettings();
	}

	private RateBeerActivity getRateBeerActivity() {
		return (RateBeerActivity) getActivity();
	}
	
}
