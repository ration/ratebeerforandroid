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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ratebeer.android.R;
import com.ratebeer.android.gui.components.PosterService;
import com.ratebeer.android.gui.components.RateBeerFragment;

public class AddToCellarFragment extends RateBeerFragment {

	public enum CellarType {
		Want,
		Have
	}

	private static final String STATE_BEERID = "beerId";
	private static final String STATE_BEERNAME = "beerName";
	private static final String STATE_CELLARTYPE = "cellarType";

	private TextView addingwhatText, quantityLabel, vintageLabel;
	private EditText memoText, vintageText, quantityText;
	private Button addButton, cancelButton;

	protected String beerName;
	protected int beerId;
	protected CellarType cellarType;

	public AddToCellarFragment() {
		this(null, -1, null);
	}

	/**
	 * Allow adding of a beer to the cellar (a want or a have)
	 * @param beerName The beer name, or null if not known
	 * @param beerId The beer ID
	 * @param cellarType Whether to add a want or a have
	 */
	public AddToCellarFragment(String beerName, int beerId, CellarType cellarType) {
		this.beerName = beerName;
		this.beerId = beerId;
		this.cellarType = cellarType;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_addtocellar, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		addingwhatText = (TextView) getView().findViewById(R.id.addingwhat);
		memoText = (EditText) getView().findViewById(R.id.memo);
		vintageLabel = (TextView) getView().findViewById(R.id.vintage_label);
		vintageText = (EditText) getView().findViewById(R.id.vintage);
		quantityLabel = (TextView) getView().findViewById(R.id.quantity_label);
		quantityText = (EditText) getView().findViewById(R.id.quantity);
		addButton = (Button) getView().findViewById(R.id.add);
		addButton.setOnClickListener(onAddToCellar);
		cancelButton = (Button) getView().findViewById(R.id.cancel);
		cancelButton.setOnClickListener(onCancelClicked);

		// Show/hide the appropriate fields
		if (cellarType == CellarType.Want) {
			vintageLabel.setVisibility(View.GONE);
			vintageText.setVisibility(View.GONE);
			quantityLabel.setVisibility(View.GONE);
			quantityText.setVisibility(View.GONE);
		}
		
		if (savedInstanceState != null) {
			beerName = savedInstanceState.getString(STATE_BEERNAME);
			beerId = savedInstanceState.getInt(STATE_BEERID);
			cellarType = CellarType.valueOf(savedInstanceState.getString(STATE_CELLARTYPE));
		}
		addingwhatText.setText(cellarType == CellarType.Have? R.string.cellar_addingahave: R.string.cellar_addingawant);
		
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_BEERNAME, beerName);
		outState.putInt(STATE_BEERID, beerId);
		outState.putString(STATE_CELLARTYPE, cellarType.name());
	}
	
	private OnClickListener onCancelClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// Just close this fragment
			getSupportFragmentManager().popBackStack();
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
			i.putExtra(PosterService.EXTRA_MEMO, memoText.getText().toString());
			i.putExtra(PosterService.EXTRA_VINTAGE, vintageText.getText().toString());
			i.putExtra(PosterService.EXTRA_QUANTITY, quantityText.getText().toString());
			getActivity().startService(i);

			// Close this fragment
			getSupportFragmentManager().popBackStack();
			
		}
	};

}
