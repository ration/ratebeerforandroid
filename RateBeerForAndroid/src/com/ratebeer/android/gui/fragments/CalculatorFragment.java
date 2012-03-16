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

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ratebeer.android.R;
import com.ratebeer.android.gui.components.RateBeerFragment;

public class CalculatorFragment extends RateBeerFragment {

	protected enum MeasurementSystem {
		Metric,
		Imperial,
		US
	}
	
	protected Map<MeasurementSystem, double[]> defaults;
	{
		defaults = new HashMap<CalculatorFragment.MeasurementSystem, double[]>();
		defaults.put(MeasurementSystem.Metric, new double[] {330D, 375D, 500D, 750D});
		defaults.put(MeasurementSystem.Imperial, new double[] {12D, 15D, 20D, 40D});
		defaults.put(MeasurementSystem.US, new double[] {12D, 16D, 22D, 26D});
	}
	
	protected Map<MeasurementSystem, String> units;
	{
		units = new HashMap<CalculatorFragment.MeasurementSystem, String>();
		units.put(MeasurementSystem.Metric, "ml");
		units.put(MeasurementSystem.Imperial, "oz");
		units.put(MeasurementSystem.US, "oz");
	}

	DecimalFormat df = new DecimalFormat("#");
	private Spinner systemSpinner;
	private Button from1, from2, from3, from4, to1, to2, to3, to4, fromTo, toFrom, clear;
	private EditText fromQuantity, fromEuro, fromCent, toQuantity, toEuro, toCent;
	private TextView fromFor, toFor;
	
	public CalculatorFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_calculator, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		fromQuantity = (EditText) getView().findViewById(R.id.from_quantity);
		fromEuro = (EditText) getView().findViewById(R.id.from_euro);
		fromCent = (EditText) getView().findViewById(R.id.from_cent);
		toQuantity = (EditText) getView().findViewById(R.id.to_quantity);
		toEuro = (EditText) getView().findViewById(R.id.to_euro);
		toCent = (EditText) getView().findViewById(R.id.to_cent);
		fromTo = (Button) getView().findViewById(R.id.from_to);
		toFrom = (Button) getView().findViewById(R.id.to_from);
		fromFor = (TextView) getView().findViewById(R.id.from_for);
		toFor = (TextView) getView().findViewById(R.id.to_for);
		clear = (Button) getView().findViewById(R.id.clear);
		from1 = addButton(R.id.from1, fromQuantity, 0);
		from2 = addButton(R.id.from2, fromQuantity, 1);
		from3 = addButton(R.id.from3, fromQuantity, 2);
		from4 = addButton(R.id.from4, fromQuantity, 3);
		to1 = addButton(R.id.to1, toQuantity, 0);
		to2 = addButton(R.id.to2, toQuantity, 1);
		to3 = addButton(R.id.to3, toQuantity, 2);
		to4 = addButton(R.id.to4, toQuantity, 3);
		systemSpinner = (Spinner) getView().findViewById(R.id.system);
		systemSpinner.setOnItemSelectedListener(onSystemChanged);
		populateSystemSpinner();
		//systemSpinner.setSelection(0);
		fromTo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				calculate(fromQuantity, fromEuro, fromCent, toQuantity, toEuro, toCent);				
			}
		});
		toFrom.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				calculate(toQuantity, toEuro, toCent, fromQuantity, fromEuro, fromCent);				
			}
		});
		clear.setOnClickListener(onClearClicked);

	}

	private Button addButton(int field, final EditText quantityText, final int valueIndex) {
		Button button = (Button) getView().findViewById(field);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				quantityText.setText(df.format(defaults.get(getSelectedSystem())[valueIndex]));
			}
		});
		return button;
	}

	private void calculate(EditText quan1, EditText euro1, EditText cent1, EditText quan2, EditText euro2,
			EditText cent2) {
		try {
			Double q1 = Double.parseDouble(quan1.getText().toString());
			Double e1 = Double.parseDouble(euro1.getText().toString());
			Double c1 = Double.parseDouble(cent1.getText().toString());
			Double q2 = Double.parseDouble(quan2.getText().toString());
			Double price = ((e1 * 100) + c1) / q1 * q2;
			euro2.setText(df.format((price - price % 100) / 100));
			cent2.setText(df.format(price % 100));
		} catch (NumberFormatException e) {
			Toast.makeText(getRateBeerApplication(), R.string.calc_fillinall, Toast.LENGTH_LONG).show();
		}
	}

	protected MeasurementSystem getSelectedSystem() {
		return (MeasurementSystem) systemSpinner.getSelectedItem();
	}

	private void populateSystemSpinner() {
		MeasurementSystem[] allSystems = MeasurementSystem.values();
		android.widget.ArrayAdapter<MeasurementSystem> adapter = new android.widget.ArrayAdapter<MeasurementSystem>(
				getActivity(), android.R.layout.simple_spinner_item, allSystems);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		systemSpinner.setAdapter(adapter);
	}

	private OnItemSelectedListener onSystemChanged = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			// Update button labels
			String unit = units.get(getSelectedSystem());
			from1.setText(df.format(defaults.get(getSelectedSystem())[0]) + unit);
			from2.setText(df.format(defaults.get(getSelectedSystem())[1]) + unit);
			from3.setText(df.format(defaults.get(getSelectedSystem())[2]) + unit);
			from4.setText(df.format(defaults.get(getSelectedSystem())[3]) + unit);
			to1.setText(df.format(defaults.get(getSelectedSystem())[0]) + unit);
			to2.setText(df.format(defaults.get(getSelectedSystem())[1]) + unit);
			to3.setText(df.format(defaults.get(getSelectedSystem())[2]) + unit);
			to4.setText(df.format(defaults.get(getSelectedSystem())[3]) + unit);
			fromFor.setText(getString(R.string.calc_for, unit));
			toFor.setText(getString(R.string.calc_for, unit));
		}
		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// Not possible to de-select
		}
	};

	private OnClickListener onClearClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			fromQuantity.setText("");
			fromEuro.setText("");
			fromCent.setText("");
			toQuantity.setText("");
			toEuro.setText("");
			toCent.setText("");
		}
	};

}
