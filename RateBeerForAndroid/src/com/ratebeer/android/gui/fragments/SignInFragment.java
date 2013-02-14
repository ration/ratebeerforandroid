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
package com.ratebeer.android.gui.fragments;

import java.util.Date;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;
import com.ratebeer.android.R;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.CommandFailureResult;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.api.UserSettings;
import com.ratebeer.android.api.command.GetUserPremiumStatusCommand;
import com.ratebeer.android.api.command.SignInCommand;
import com.ratebeer.android.api.command.SignOutCommand;
import com.ratebeer.android.gui.SignIn;
import com.ratebeer.android.gui.components.RateBeerActivity;
import com.ratebeer.android.gui.components.RateBeerFragment;
import com.ratebeer.android.gui.components.helpers.OnProgressChangedListener;

import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;

@EFragment(R.layout.fragment_signin)
public class SignInFragment extends RateBeerFragment {

	// Layout views
	@ViewById(R.id.username)
	protected EditText usernameEdit;
	@ViewById(R.id.password)
	protected EditText passwordEdit;
	@ViewById
	protected TextView requiressignin;
	@ViewById
	protected ProgressBar progress;
	@ViewById
	protected Button connect;

	public SignInFragment() {
		// Prevent adding of a sign in/out menu option in this screen
		showSignInMenuItem = false;
	}
	
	@AfterViews
	public void init() {
		
		// Click listeners
		connect.setOnClickListener(onConnect);
		
		// Redirect because some feature required a signed in user?
		if (getActivity().getIntent() != null && ((SignIn)getActivity()	).getExtraIsRedirect()) {
			requiressignin.setVisibility(View.VISIBLE);
		}
		
		// Monitor background command progress
		((RateBeerActivity)getActivity()).setOnProgressChangedListener(new OnProgressChangedListener() {
			@Override
			public void setProgress(boolean isBusy) {
				if (getActivity() == null) {
					// No longer attached
					return;
				}
				if (isBusy) {
					progress.setVisibility(View.VISIBLE);
					connect.setText(R.string.signin_connecting);
					connect.setEnabled(false);
				} else {
					progress.setVisibility(View.INVISIBLE);
					if (getUser() == null) {
						connect.setText(R.string.signin_signin);
					} else {
						connect.setText(R.string.signin_signout);
					}
					connect.setEnabled(true);
				}
			}
		});
		
		// Already signed in?
		if (getUser() != null) {
			usernameEdit.setText(getUser().getUsername());
			passwordEdit.setText(getUser().getPassword());
			connect.setText(R.string.signin_signout);
		}
		
	}

	private OnClickListener onConnect = new OnClickListener() {		
		@Override
		public void onClick(View v) {

			if (getUser() != null) {
				// Try to sign out
				execute(new SignOutCommand(getUser()));
				return;
			}
			
			// Try to sign in
			String username = usernameEdit.getText().toString().trim();
			String password = passwordEdit.getText().toString().trim();
			if (username != null && !username.equals("") && password != null && !password.equals("")) {
				// Try to sign in
				execute(new SignInCommand(getUser(), username, password));
			} else {
				publishException(null, getText(R.string.error_nouserorpass).toString());
			}
		}
	};

	@Override
	public void onTaskSuccessResult(CommandSuccessResult result) {
		if (result.getCommand().getMethod() == ApiMethod.SignOut) {
			// Successfully signed out
			getSettings().saveUserSettings(null);
			getActivity().finish();
		} else if (result.getCommand().getMethod() == ApiMethod.SignIn) {
			// Successfully signed in
			SignInCommand signInCommand = (SignInCommand) result.getCommand();
			// Store this user as the new signed in user
			String username = usernameEdit.getText().toString().trim();
			String password = passwordEdit.getText().toString().trim();
			getSettings().saveUserSettings(new UserSettings(signInCommand.getUserId(), username, password, "", false, 
					new Date()));
			// Try to retrieve the user's account status as well
			execute(new GetUserPremiumStatusCommand(getUser()));
		} else if (result.getCommand().getMethod() == ApiMethod.GetUserPremiumStatus) {
			// We also have a user status now; update the stored user settings
			GetUserPremiumStatusCommand getCommand = (GetUserPremiumStatusCommand) result.getCommand();
			Crouton.makeText(getActivity(), R.string.signin_signinsuccess, Style.CONFIRM).show();
			getSettings().saveUserSettings(new UserSettings(getUser().getUserID(), getUser().getUsername(), 
					getUser().getPassword(), "", getCommand.isPremium(), new Date(0)));
			getActivity().finish();
		}
	}

	@Override
	public void onTaskFailureResult(CommandFailureResult result) {
		publishException(null, result.getException());
	}
	
}
