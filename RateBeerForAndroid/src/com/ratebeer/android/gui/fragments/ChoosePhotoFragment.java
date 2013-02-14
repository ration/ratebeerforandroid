package com.ratebeer.android.gui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.ratebeer.android.R;

public class ChoosePhotoFragment extends SherlockDialogFragment {

	private BeerViewFragment beerViewFragment;

	public ChoosePhotoFragment() {
		this(null);
	}
	public ChoosePhotoFragment(BeerViewFragment beerViewFragment) {
		this.beerViewFragment = beerViewFragment;
		setRetainInstance(true);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new AlertDialog.Builder(getActivity())
				.setIcon(android.R.drawable.ic_dialog_info)
				.setTitle(R.string.upload_choose)
				.setItems(new String[] { getString(R.string.upload_newphoto), getString(R.string.upload_pick) },
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if (which == 0) {
									beerViewFragment.onStartPhotoSnapping();
								} else {
									beerViewFragment.onStartPhotoPicking();
								}
							}
						}).create();
	}
	
}