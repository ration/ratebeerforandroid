package com.ratebeer.android.gui.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.ratebeer.android.R;

@SuppressLint("ValidFragment")
public class CustomListBeersSortDialog extends DialogFragment {

	private CustomListFragment customListFragment;

	public CustomListBeersSortDialog() {
		this(null);
	}

	public CustomListBeersSortDialog(CustomListFragment customListFragment) {
		this.customListFragment = customListFragment;
		setRetainInstance(true);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new AlertDialog.Builder(getActivity()).setTitle(R.string.myratings_sortby)
				.setItems(R.array.custom_sortby_names, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Get the enum value of the clicked id via the custom_sortby_values XML resource array
						customListFragment.setSortOrder(CustomListFragment.CustomListBeerComparator.SortBy
								.valueOf(getResources().getStringArray(R.array.custom_sortby_values)[which]));
					}
				}).create();
	}

}