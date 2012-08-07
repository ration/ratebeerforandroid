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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ratebeer.android.R;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.CommandFailureResult;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.api.UserSettings;
import com.ratebeer.android.api.command.GetUserStatusCommand;
import com.ratebeer.android.api.command.SignInCommand;
import com.ratebeer.android.api.command.SignOutCommand;
import com.ratebeer.android.gui.SignIn;
import com.ratebeer.android.gui.components.OnProgressChangedListener;
import com.ratebeer.android.gui.components.RateBeerFragment;

public class SignInFragment extends RateBeerFragment {

	private EditText usernameEdit, passwordEdit;
	private TextView requiressignin;
	private ProgressBar progress;
	private Button connect;

	public SignInFragment() {
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_signin, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// Prevent adding of a sign in/out menu option in this screen
		showSignInMenuItem = false;
		
		usernameEdit = (EditText) getView().findViewById(R.id.username);
		passwordEdit = (EditText) getView().findViewById(R.id.password);
		progress = (ProgressBar) getView().findViewById(R.id.progress);
		requiressignin = (TextView) getView().findViewById(R.id.requiressignin);
		
		// Click listeners
		connect = (Button) getView().findViewById(R.id.connect);
		connect.setOnClickListener(onConnect);
		
		// Redirect because some feature required a signed in user?
		if (getActivity().getIntent() != null && getActivity().getIntent().getBooleanExtra(SignIn.EXTRA_REDIRECT, false)) {
			requiressignin.setVisibility(View.VISIBLE);
		}
		
		// Monitor background command progress
		getRateBeerActivity().setOnProgressChangedListener(new OnProgressChangedListener() {
			@Override
			public void setProgress(boolean isBusy) {
				if (getRateBeerActivity() == null) {
					// No longer attached
					return;
				}
				if (isBusy) {
					progress.setVisibility(View.VISIBLE);
					connect.setText(R.string.signin_connecting);
					connect.setEnabled(false);
				} else {
					progress.setVisibility(View.INVISIBLE);
					if (getRateBeerActivity().getUser() == null) {
						connect.setText(R.string.signin_signin);
					} else {
						connect.setText(R.string.signin_signout);
					}
					connect.setEnabled(true);
				}
			}
		});
		
		if (savedInstanceState == null) {
			// Already signed in?
			if (getRateBeerActivity().getUser() != null) {
				usernameEdit.setText(getRateBeerActivity().getUser().getUsername());
				passwordEdit.setText(getRateBeerActivity().getUser().getPassword());
				connect.setText(R.string.signin_signout);
			}
		}
		
	}

	private OnClickListener onConnect = new OnClickListener() {		
		@Override
		public void onClick(View v) {

			if (getRateBeerActivity().getUser() != null) {
				// Try to sign out
				execute(new SignOutCommand(getRateBeerActivity().getApi()));
				return;
			}
			
			// Try to sign in
			String username = usernameEdit.getText().toString().trim();
			String password = passwordEdit.getText().toString().trim();
			if (username != null && !username.equals("") && password != null && !password.equals("")) {
				// Try to sign in
				execute(new SignInCommand(getRateBeerActivity().getApi(), username, password));
			} else {
				publishException(null, getText(R.string.error_nouserorpass).toString());
			}
		}
	};

	@Override
	public void onTaskSuccessResult(CommandSuccessResult result) {
		if (result.getCommand().getMethod() == ApiMethod.SignOut) {
			// Successfully signed out
			getRateBeerActivity().getSettings().saveUserSettings(null);
			getActivity().finish();
		} else if (result.getCommand().getMethod() == ApiMethod.SignIn) {
			// Successfully signed in
			SignInCommand signInCommand = (SignInCommand) result.getCommand();
			// Store this user as the new signed in user
			String username = usernameEdit.getText().toString().trim();
			String password = passwordEdit.getText().toString().trim();
			getRateBeerActivity().getSettings().saveUserSettings(new UserSettings(signInCommand.getUserId(), username, 
					password, "", false));
			// Try to retrieve the user status as well
			execute(new GetUserStatusCommand(getRateBeerActivity().getApi()));
		} else if (result.getCommand().getMethod() == ApiMethod.GetUserStatus) {
			// We also have a user status now; update the stored user settings
			GetUserStatusCommand getCommand = (GetUserStatusCommand) result.getCommand();
			Toast.makeText(getRateBeerActivity(), R.string.signin_signinsuccess, Toast.LENGTH_LONG).show();
			UserSettings ex = getRateBeerActivity().getSettings().getUserSettings();
			getRateBeerActivity().getSettings().saveUserSettings(new UserSettings(ex.getUserID(), ex.getUsername(), 
					ex.getPassword(), getCommand.getDrinkingStatus(), getCommand.isPremium()));
			getActivity().finish();
		}
	}

	@Override
	public void onTaskFailureResult(CommandFailureResult result) {
		publishException(null, result.getException());
	}
	
}
