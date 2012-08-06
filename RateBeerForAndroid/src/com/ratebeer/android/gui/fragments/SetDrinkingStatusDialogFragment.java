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

import com.ratebeer.android.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.EditText;

public class SetDrinkingStatusDialogFragment extends DialogFragment {

	private final OnDialogResult resultListener;

	public SetDrinkingStatusDialogFragment() {
		this(null);
	}
	
	public SetDrinkingStatusDialogFragment(OnDialogResult resultListener) {
		this.resultListener = resultListener;
		setRetainInstance(true);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final View layout = getActivity().getLayoutInflater().inflate(R.layout.dialog_setdrinkingstatus, null);
		final EditText newStatusInput = (EditText) layout.findViewById(R.id.newstatus);
		return new AlertDialog.Builder(getActivity())
			//.setIcon(android.R.drawable.ic_dialog_info)
			.setTitle(R.string.app_newstatus)
			.setView(layout)
			.setPositiveButton(android.R.string.ok, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					resultListener.onSetNewStatus(newStatusInput.getText().toString());
				}
			})
			.setNegativeButton(android.R.string.cancel, null)
			.create();
	}
	
	public interface OnDialogResult {
		public void onSetNewStatus(String newStatus);
	}
	
}
