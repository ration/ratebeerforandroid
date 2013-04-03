package com.ratebeer.android.gui.fragments;

import com.ratebeer.android.R;
import com.ratebeer.android.api.command.SearchBeersCommand.BeerSearchResultComparator;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

@SuppressLint("ValidFragment")
public class BrewerBeersSortDialog extends DialogFragment {

	private BrewerViewFragment brewerViewFragment;

	public BrewerBeersSortDialog() {
		this(null);
	}

	public BrewerBeersSortDialog(BrewerViewFragment brewerViewFragment) {
		this.brewerViewFragment = brewerViewFragment;
		setRetainInstance(true);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new AlertDialog.Builder(getActivity()).setTitle(R.string.myratings_sortby)
				.setItems(R.array.brewers_sortby_names, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Get the enum value of the clicked id via the brewers_sortby_values XML resource array
						brewerViewFragment.setSortOrder(BeerSearchResultComparator.SortBy.valueOf(getResources()
								.getStringArray(R.array.brewers_sortby_values)[which]));
					}
				}).create();
	}

}