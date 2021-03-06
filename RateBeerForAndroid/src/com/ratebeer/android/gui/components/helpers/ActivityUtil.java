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

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;

import com.ratebeer.android.R;

import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;

public class ActivityUtil {

	/**
	 * Get current version number
	 * @return A string with the application version number
	 */
	public static String getVersionNumber(Context context) {
		String version = "?";
		try {
			PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			version = pi.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			android.util.Log.e(Log.LOG_NAME, "Package name not found to retrieve version number", e);
		}
		return version;
	}

	/**
	 * Get current version code
	 * @return A string with the application version code
	 */
	public static int getVersionCode(Context context) {
		int version = -1;
		try {
			PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			version = pi.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			android.util.Log.e(Log.LOG_NAME, "Package name not found to retrieve version number", e);
		}
		return version;
	}

	/**
     * Indicates whether the specified action can be used as an intent. This
     * method queries the package manager for installed packages that can
     * respond to an intent with the specified action. If no suitable package is
     * found, this method returns false.
     * @param context The application's environment.
     * @param intent The Intent to check for availability.
     * @return True if an Intent with the specified action can be sent and responded to, false otherwise.
     */
    public static boolean isIntentAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    /**
     * Builds a (reusable) dialog that asks to install some application from the Android market
     * @param messageResourceID The message to show to the user
     * @param marketUri The application's URI on the Android Market
     * @return The dialog to show
     */
	public static Dialog buildInstallDialog(final Activity activity, int messageResourceID, final Uri marketUri) {
		return buildInstallDialog(activity, messageResourceID, marketUri, false);
	}
    
    /**
     * Builds a (reusable) dialog that asks to install some application from the Android market
     * @param messageResourceID The message to show to the user
     * @param marketUri The application's URI on the Android Market
     * @param alternativeNegativeButtonHandler The click handler for the negative dialog button
     * @return The dialog to show
     */
	public static Dialog buildInstallDialog(final Activity activity, int messageResourceID, final Uri marketUri, 
			final boolean closeAfterInstallFailure) {
		AlertDialog.Builder fbuilder = new AlertDialog.Builder(activity);
		fbuilder.setMessage(messageResourceID);
		fbuilder.setCancelable(true);
		fbuilder.setPositiveButton(R.string.app_install, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent install = new Intent(Intent.ACTION_VIEW, marketUri);
				if (ActivityUtil.isIntentAvailable(activity, install)) {
					activity.startActivity(install);
				} else {
					Crouton.makeText(activity, R.string.app_nomarket, Style.ALERT).show();
					if (closeAfterInstallFailure) {
						activity.finish();
					}
				}
				dialog.dismiss();
			}
		});
		fbuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				if (closeAfterInstallFailure) {
					activity.finish();
				}
			}
		});
		return fbuilder.create();
	}
	
	public static void makeListItemClickable(View listItem, OnClickListener onRowClick) {
		listItem.setClickable(true);
		listItem.setFocusable(true);
		// setBackgroundResource seems to reset padding...
		// http://stackoverflow.com/questions/5890379/android-setbackgroundresource-discards-my-xml-layout-attributes
		// so manually save and restore them
		int padLeft = listItem.getPaddingLeft();
		int padRight = listItem.getPaddingRight();
		int padTop = listItem.getPaddingTop();
		int padBottom = listItem.getPaddingBottom();
		listItem.setBackgroundResource(android.R.drawable.menuitem_background);
		listItem.setPadding(padLeft, padTop, padRight, padBottom);
		listItem.setOnClickListener(onRowClick);
	}
    
}
