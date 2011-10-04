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

public interface WheelAdapter<T> {
	
	public int getAfterNum();

	public int getBeforeNum();

	public int getSize();

	public int nextIndex();

	public int prevIndex();

	public int getSelectedIndex();

	public void setSelectedIndex(int index);
	
	public void setSelectedValue(T val);

	public T getValue(int index);

	public T getSelectedValue();

	public String getLabel(int index);

	public String getSelectedLabel();
	
}
