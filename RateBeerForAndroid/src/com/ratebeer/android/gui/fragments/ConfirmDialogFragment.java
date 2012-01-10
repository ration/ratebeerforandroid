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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ConfirmDialogFragment extends DialogFragment {

	private final OnDialogResult resultListener;
	private final int dialogTitle;
	private final Object[] dialogTitleArgs;

	public ConfirmDialogFragment() {
		this(null, -1);
	}
	
	public ConfirmDialogFragment(OnDialogResult resultListener, int dialogTitle, Object... dialogTitleArgs) {
		this.resultListener = resultListener;
		this.dialogTitle = dialogTitle;
		this.dialogTitleArgs = dialogTitleArgs;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new AlertDialog.Builder(getActivity())
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(getString(dialogTitle, dialogTitleArgs))
			.setPositiveButton(android.R.string.yes, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					resultListener.onConfirmed();
				}
			})
			.setNegativeButton(android.R.string.no, null)
			.create();
	}
	
	public interface OnDialogResult {
		public void onConfirmed();
	}
	
}
