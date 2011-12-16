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
package fr.marvinlabs.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.CheckBox;

/**
 * CheckBox that does not react to any user event in order to let the container handle them.
 */
public class InertCheckBox extends CheckBox {

	// Provide the same constructors as the superclass
	public InertCheckBox(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	// Provide the same constructors as the superclass
	public InertCheckBox(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	// Provide the same constructors as the superclass
	public InertCheckBox(Context context) {
		super(context);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// Make the checkbox not respond to any user event
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Make the checkbox not respond to any user event
		return false;
	}

	@Override
	public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
		// Make the checkbox not respond to any user event
		return false;
	}

	@Override
	public boolean onKeyPreIme(int keyCode, KeyEvent event) {
		// Make the checkbox not respond to any user event
		return false;
	}

	@Override
	public boolean onKeyShortcut(int keyCode, KeyEvent event) {
		// Make the checkbox not respond to any user event
		return false;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// Make the checkbox not respond to any user event
		return false;
	}

	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		// Make the checkbox not respond to any user event
		return false;
	}
}
