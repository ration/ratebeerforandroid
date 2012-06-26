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
package com.ratebeer.android.gui.components.tasks;

import com.ratebeer.android.api.CommandFailureResult;
import com.ratebeer.android.api.CommandSuccessResult;

public interface RateBeerTaskCaller {

	/**
	 * Should return whether the executor is still bound to the executing activity, i.e. to this
	 * @return True if it still bound/connected to the task-executing activity
	 */
	public boolean isBound();
	
    /**
     * Should be implemented to respond to successfully executed commands
     * @param result The command result
     */
	public void onTaskSuccessResult(CommandSuccessResult result);

    /**
     * Should be implemented to respond to failed executed commands
     * @param result The command result
     */
	public void onTaskFailureResult(CommandFailureResult result);

}