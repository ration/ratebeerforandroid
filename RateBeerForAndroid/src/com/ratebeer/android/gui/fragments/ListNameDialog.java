package com.ratebeer.android.gui.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.EditText;

import com.ratebeer.android.R;

@SuppressLint("ValidFragment")
public class ListNameDialog extends DialogFragment {

	private CustomListsFragment customListsFragment;

	public ListNameDialog() {
		this(null);
	}

	public ListNameDialog(CustomListsFragment customListsFragment) {
		this.customListsFragment = customListsFragment;
		setRetainInstance(true);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final EditText nameText = new EditText(getActivity());
		return new AlertDialog.Builder(getActivity()).setTitle(R.string.custom_listname).setView(nameText)
				.setNegativeButton(android.R.string.cancel, null)
				.setPositiveButton(android.R.string.ok, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						customListsFragment.createList(nameText.getText().toString());
					}
				}).create();
	}

}