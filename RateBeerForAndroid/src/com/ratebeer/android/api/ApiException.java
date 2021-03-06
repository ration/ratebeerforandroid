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
package com.ratebeer.android.api;

public class ApiException extends Exception {

	private static final long serialVersionUID = -3698590320346609951L;
	
	final private ExceptionType type;

	public ApiException(ExceptionType type, String internal) {
		super(internal);
		this.type = type;
	}
	
	/**
	 * The type of exception that occurred
	 */
	public enum ExceptionType {
		Offline,
		ConnectionError,
		AuthenticationFailed, 
		CommandFailed;
	}

	public ExceptionType getType() {
		return type;
	}

}
