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
    
    Originally part of smooth-wheel
    by Qixiang Chen
    http://code.google.com/p/smooth-wheel/
    
	Licensed under the Apache License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may obtain
	a copy of the License at
		http://www.apache.org/licenses/LICENSE-2.0
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package com.ratebeer.android.gui.wheel;

import java.util.ArrayList;
import java.util.Arrays;

public class StringWheelAdapter implements WheelAdapter<String> {

	private OnSelectionChangedListener<String> _listener;
	protected ArrayList<String> _val;
	protected int _cur;

	public StringWheelAdapter(OnSelectionChangedListener<String> listener, String[] vals) {
		this._listener = listener;
		_val = new ArrayList<String>();
		_val.addAll(Arrays.asList(vals));
	}

	public StringWheelAdapter(OnSelectionChangedListener<Integer> listener, ArrayList<String> vals) {
		_val = vals;
	}

	public int getAfterNum() {
		return this.getSize() - this._cur;
	}

	public int getBeforeNum() {
		return _cur;
	}

	public int getSelectedIndex() {
		return _cur;
	}

	public String getCurLabel() {
		return "" + this._val.get(_cur);
	}

	public String getSelectedValue() {
		return this._val.get(this.getSelectedIndex());
	}
	
	public String getLabel(int index) {
		return this._val.get(index);
	}

	public int getSize() {
		return this._val.size();
	}

	public String getValue(int index) {
		return this._val.get(index);
	}

	public int nextIndex() {
		if (this._cur < this.getSize() - 1) {
			this._cur += 1;
			this._listener.onSelectionChanged(getSelectedValue());
		}
		return this._cur;
	}

	public int prevIndex() {
		if (this._cur > 0) {
			this._cur -= 1;
			this._listener.onSelectionChanged(getSelectedValue());
		}
		return this._cur;
	}

	public void setSelectedIndex(int index) {
		this._cur = index;
		this._listener.onSelectionChanged(getSelectedValue());
	}

	@Override
	public void setSelectedValue(String val) {
		for (int i = 0; i < _val.size(); i++) {
			if (_val.get(i).equals(val)) {
				setSelectedIndex(i);
				return;
			}
		}
		setSelectedIndex(0);
	}

	@Override
	public String getSelectedLabel() {
		return getLabel(getSelectedIndex());
	}

}
