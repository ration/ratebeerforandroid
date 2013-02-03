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

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.InstanceState;
import com.googlecode.androidannotations.annotations.ViewById;
import com.ratebeer.android.R;
import com.ratebeer.android.gui.components.PosterService;
import com.ratebeer.android.gui.components.RateBeerFragment;

@EFragment(R.layout.fragment_addtocellar)
public class AddToCellarFragment extends RateBeerFragment {

	public enum CellarType {
		Want,
		Have
	}

	@FragmentArg
	@InstanceState
	protected String beerName;
	@FragmentArg
	@InstanceState
	protected int beerId;
	@FragmentArg
	@InstanceState
	protected CellarType cellarType;

	@ViewById
	protected TextView addingwhat;
	@ViewById
	protected TextView quantityLabel;
	@ViewById
	protected TextView vintageLabel;
	@ViewById
	protected EditText memo;
	@ViewById
	protected EditText vintage;
	@ViewById
	protected EditText quantity;
	@ViewById(R.id.add)
	protected Button addButton;
	@ViewById(R.id.cancel)
	protected Button cancelButton;

	public AddToCellarFragment() {
	}

	@AfterViews
	public void init() {

		addButton.setOnClickListener(onAddToCellar);
		cancelButton.setOnClickListener(onCancelClicked);

		// Show/hide the appropriate fields
		if (cellarType == CellarType.Want) {
			vintageLabel.setVisibility(View.GONE);
			vintage.setVisibility(View.GONE);
			quantityLabel.setVisibility(View.GONE);
			quantity.setVisibility(View.GONE);
		}
		
		addingwhat.setText(cellarType == CellarType.Have? R.string.cellar_addingahave: R.string.cellar_addingawant);
		
	}

	private OnClickListener onCancelClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// Just close this fragment
			getFragmentManager().popBackStack();
		}
	};
	
	private OnClickListener onAddToCellar = new OnClickListener() {
		@Override
		public void onClick(View v) {

			// Use the poster service to add the beer to the cellar
			Intent i = new Intent(PosterService.ACTION_ADDTOCELLAR);
			i.putExtra(PosterService.EXTRA_BEERID, beerId);
			i.putExtra(PosterService.EXTRA_BEERNAME, beerName);
			i.putExtra(PosterService.EXTRA_CELLARTYPE, cellarType.name());
			i.putExtra(PosterService.EXTRA_MEMO, memo.getText().toString());
			i.putExtra(PosterService.EXTRA_VINTAGE, vintage.getText().toString());
			i.putExtra(PosterService.EXTRA_QUANTITY, quantity.getText().toString());
			getActivity().startService(i);

			// Close this fragment
			getFragmentManager().popBackStack();
			
		}
	};

}
