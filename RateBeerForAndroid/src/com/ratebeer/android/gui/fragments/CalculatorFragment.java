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

import com.ratebeer.android.R;
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

import com.ratebeer.android.gui.components.RateBeerFragment;

public class CalculatorFragment extends RateBeerFragment {

	protected enum MeasurementSystem {
		Metric,
		Imperial,
		USSmall,
		USBig
	}
	
	protected Map<MeasurementSystem, double[]> defaults;
	{
		defaults = new HashMap<CalculatorFragment.MeasurementSystem, double[]>();
		defaults.put(MeasurementSystem.Metric, new double[] {330D, 375D, 500D, 750D});
		defaults.put(MeasurementSystem.Imperial, new double[] {12D, 15D, 20D, 40D});
		defaults.put(MeasurementSystem.USSmall, new double[] {12D, 14D, 16D, 22D});
		defaults.put(MeasurementSystem.USBig, new double[] {22D, 48D, 64D, 72D});
	}

	protected Map<MeasurementSystem, String> units;
	{
		units = new HashMap<CalculatorFragment.MeasurementSystem, String>();
		units.put(MeasurementSystem.Metric, "ml");
		units.put(MeasurementSystem.Imperial, "oz");
		units.put(MeasurementSystem.USSmall, "oz");
		units.put(MeasurementSystem.USBig, "oz");
	}

	protected Map<MeasurementSystem, String> names;
	{
		names = new HashMap<CalculatorFragment.MeasurementSystem, String>();
		names.put(MeasurementSystem.Metric, "Metric");
		names.put(MeasurementSystem.Imperial, "Imperial");
		names.put(MeasurementSystem.USSmall, "US Small");
		names.put(MeasurementSystem.USBig, "US Big");
	}
	protected Map<MeasurementSystem, Map<MeasurementSystem, Double>> convertFactors;
	{
		convertFactors = new HashMap<MeasurementSystem, Map<MeasurementSystem,Double>>();
		HashMap<MeasurementSystem, Double> cfMetric = new HashMap<MeasurementSystem, Double>();
		cfMetric.put(MeasurementSystem.Metric, 1D);
		cfMetric.put(MeasurementSystem.Imperial, 0.0351950797279D);
		cfMetric.put(MeasurementSystem.USSmall, 0.0338140225589D);
		cfMetric.put(MeasurementSystem.USBig, 0.0338140225589D);
		convertFactors.put(MeasurementSystem.Metric, cfMetric);
		HashMap<MeasurementSystem, Double> cfImperial = new HashMap<MeasurementSystem, Double>();
		cfImperial.put(MeasurementSystem.Metric, 28.4130625D);
		cfImperial.put(MeasurementSystem.Imperial, 1D);
		cfImperial.put(MeasurementSystem.USSmall, 0.960759936343D);
		cfImperial.put(MeasurementSystem.USBig, 0.960759936343D);
		convertFactors.put(MeasurementSystem.Imperial, cfImperial);
		HashMap<MeasurementSystem, Double> cfUS = new HashMap<MeasurementSystem, Double>();
		cfUS.put(MeasurementSystem.Metric, 29.5735296875D);
		cfUS.put(MeasurementSystem.Imperial, 1.04084273519D);
		cfUS.put(MeasurementSystem.USSmall, 1D);
		cfUS.put(MeasurementSystem.USBig, 1D);
		convertFactors.put(MeasurementSystem.USSmall, cfUS);
		convertFactors.put(MeasurementSystem.USBig, cfUS);
		
	}

	DecimalFormat df = new DecimalFormat("#");
	private Spinner fromSystemSpinner, toSystemSpinner;
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
		fromSystemSpinner = (Spinner) getView().findViewById(R.id.from_system);
		fromSystemSpinner.setOnItemSelectedListener(onSystemChanged);
		toSystemSpinner = (Spinner) getView().findViewById(R.id.to_system);
		toSystemSpinner.setOnItemSelectedListener(onSystemChanged);
		from1 = addButton(R.id.from1, fromQuantity, fromSystemSpinner, 0);
		from2 = addButton(R.id.from2, fromQuantity, fromSystemSpinner, 1);
		from3 = addButton(R.id.from3, fromQuantity, fromSystemSpinner, 2);
		from4 = addButton(R.id.from4, fromQuantity, fromSystemSpinner, 3);
		to1 = addButton(R.id.to1, toQuantity, toSystemSpinner, 0);
		to2 = addButton(R.id.to2, toQuantity, toSystemSpinner, 1);
		to3 = addButton(R.id.to3, toQuantity, toSystemSpinner, 2);
		to4 = addButton(R.id.to4, toQuantity, toSystemSpinner, 3);
		populateSystemSpinners();
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

	private Button addButton(int field, final EditText quantityText, final Spinner system, final int valueIndex) {
		Button button = (Button) getView().findViewById(field);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				quantityText.setText(df.format(defaults.get((MeasurementSystem)system.getSelectedItem())[valueIndex]));
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
			double price = ((e1 * 100) + c1) / (q1 * 
					convertFactors.get(getFromSelectedSystem()).get(getToSelectedSystem())) * q2;
			euro2.setText(df.format((price - price % 100) / 100));
			cent2.setText(df.format(price % 100));
		} catch (NumberFormatException e) {
			Toast.makeText(getRateBeerApplication(), R.string.calc_fillinall, Toast.LENGTH_LONG).show();
		}
	}

	private void populateSystemSpinners() {
		MeasurementSystem[] allSystems = MeasurementSystem.values();
		android.widget.ArrayAdapter<MeasurementSystem> fromAdapter = new android.widget.ArrayAdapter<MeasurementSystem>(
				getActivity(), android.R.layout.simple_spinner_item, allSystems);
		android.widget.ArrayAdapter<MeasurementSystem> toAdapter = new android.widget.ArrayAdapter<MeasurementSystem>(
				getActivity(), android.R.layout.simple_spinner_item, allSystems);
		fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		fromSystemSpinner.setAdapter(fromAdapter);
		toSystemSpinner.setAdapter(toAdapter);
	}

	private OnItemSelectedListener onSystemChanged = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			// Update button labels
			String fromUnit = units.get(getFromSelectedSystem());
			String toUnit = units.get(getToSelectedSystem());
			from1.setText(df.format(defaults.get(getFromSelectedSystem())[0]) + fromUnit);
			from2.setText(df.format(defaults.get(getFromSelectedSystem())[1]) + fromUnit);
			from3.setText(df.format(defaults.get(getFromSelectedSystem())[2]) + fromUnit);
			from4.setText(df.format(defaults.get(getFromSelectedSystem())[3]) + fromUnit);
			to1.setText(df.format(defaults.get(getToSelectedSystem())[0]) + toUnit);
			to2.setText(df.format(defaults.get(getToSelectedSystem())[1]) + toUnit);
			to3.setText(df.format(defaults.get(getToSelectedSystem())[2]) + toUnit);
			to4.setText(df.format(defaults.get(getToSelectedSystem())[3]) + toUnit);
			fromFor.setText(getString(R.string.calc_for, fromUnit));
			toFor.setText(getString(R.string.calc_for, toUnit));
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

	protected MeasurementSystem getFromSelectedSystem() {
		return (MeasurementSystem) fromSystemSpinner.getSelectedItem();
	}

	protected MeasurementSystem getToSelectedSystem() {
		return (MeasurementSystem) toSystemSpinner.getSelectedItem();
	}

}
