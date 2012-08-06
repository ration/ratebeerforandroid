package com.ratebeer.android.gui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.ratebeer.android.R;

public class UserRatingsSortDialog extends DialogFragment {

	private UserRatingsFragment userRatingsFragment;

	public UserRatingsSortDialog() {
		this(null);
	}

	public UserRatingsSortDialog(UserRatingsFragment userRatingsFragment) {
		this.userRatingsFragment = userRatingsFragment;
		setRetainInstance(true);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new AlertDialog.Builder(getActivity()).setTitle(R.string.myratings_sortby)
				.setItems(R.array.myratings_sortby_names, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// The (0-based) ID of which item was selected is directly matched with the (1-based)
						// GetUserRatingsCommand's order ID, f.e. the fourth array item 'My rating' is the sort
						// order with ID 5 (which is SORTBY_DATE)
						userRatingsFragment.setSortOrder(which + 1);
					}
				}).create();
	}

}