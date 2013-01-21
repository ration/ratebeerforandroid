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
package com.ratebeer.android.gui.components.helpers;

/**
 * Listener to any task progress, notably by the activity executing {@link RateBeerActivity$RateBeerTask}s.
 */
public interface OnProgressChangedListener {

	/**
	 * Called when a task (notably, but not exclusively, a {@link RateBeerActivity$RateBeerTask}) starts or finishes progress. The activity's 
	 * progress indicator (i.e. the action bar) will be updated.
	 * @param inProgress Whether a task is in progress
	 */
	public void setProgress(boolean inProgress);
	
}
