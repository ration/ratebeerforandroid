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

import java.util.concurrent.Executor;

import android.os.AsyncTask;

public class SinglePoolTaskExecutor implements TaskExecutor {

	private Executor executor;

	public SinglePoolTaskExecutor() {
		try {
			executor = (Executor) AsyncTask.class.getDeclaredField("THREAD_POOL_EXECUTOR").get(AsyncTask.class);
		} catch (SecurityException e) {
		} catch (NoSuchFieldException e) {
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		}
	}
	
	@Override
	public void execute(RateBeerTask task) {
		task.executeOnExecutor(executor);
	}

}
