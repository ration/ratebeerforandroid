/*
 *	This file was originally part of Transdroid <http://www.transdroid.org>
 *	
 *	Transdroid is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *	
 *	Transdroid is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *	
 *	You should have received a copy of the GNU General Public License
 *	along with Transdroid.  If not, see <http://www.gnu.org/licenses/>.
 *	
 */
package com.ratebeer.android.gui.components.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import com.ratebeer.android.R;
import com.ratebeer.android.app.RateBeerForAndroid;

import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;

public class ErrorLogSender {

    public static final String LOG_COLLECTOR_PACKAGE_NAME = "com.xtralogic.android.logcollector";//$NON-NLS-1$
    public static final String ACTION_SEND_LOG = "com.xtralogic.logcollector.intent.action.SEND_LOG";//$NON-NLS-1$
    public static final String EXTRA_SEND_INTENT_ACTION = "com.xtralogic.logcollector.intent.extra.SEND_INTENT_ACTION";//$NON-NLS-1$
    public static final String EXTRA_DATA = "com.xtralogic.logcollector.intent.extra.DATA";//$NON-NLS-1$
    public static final String EXTRA_ADDITIONAL_INFO = "com.xtralogic.logcollector.intent.extra.ADDITIONAL_INFO";//$NON-NLS-1$
    public static final String EXTRA_SHOW_UI = "com.xtralogic.logcollector.intent.extra.SHOW_UI";//$NON-NLS-1$
    public static final String EXTRA_FILTER_SPECS = "com.xtralogic.logcollector.intent.extra.FILTER_SPECS";//$NON-NLS-1$
    public static final String EXTRA_FORMAT = "com.xtralogic.logcollector.intent.extra.FORMAT";//$NON-NLS-1$
    public static final String EXTRA_BUFFER = "com.xtralogic.logcollector.intent.extra.BUFFER";//$NON-NLS-1$

    public static void collectAndSendLog(final Activity activity, final String user){
        final Intent intent = new Intent(ACTION_SEND_LOG);
        final boolean isInstalled = ActivityUtil.isIntentAvailable(activity, intent);
        
        if (!isInstalled){
            new AlertDialog.Builder(activity)
            .setTitle("RateBeer for Android")
            .setIcon(android.R.drawable.ic_dialog_info)
            .setMessage(R.string.error_lc_install)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int whichButton){
                    Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pname:" + LOG_COLLECTOR_PACKAGE_NAME));
                    marketIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (ActivityUtil.isIntentAvailable(activity, marketIntent)) {
                    	activity.startActivity(marketIntent);
                    } else {
    					Crouton.makeText(activity, R.string.app_nomarket, Style.ALERT).show();
                    }
                }
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
        }
        else
        {
            new AlertDialog.Builder(activity)
            .setTitle("RateBeer for Android")
            .setIcon(android.R.drawable.ic_dialog_info)
            .setMessage(R.string.error_lc_run)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int whichButton){
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(EXTRA_SEND_INTENT_ACTION, Intent.ACTION_SENDTO);
                    intent.putExtra(EXTRA_DATA, Uri.parse("mailto:rb@2312.nl"));
                    intent.putExtra(EXTRA_ADDITIONAL_INFO, "My problem:\n\n\nRateBeer for Android version " + ActivityUtil.getVersionNumber(activity) + "\nUser: " + user + "\n");
                    intent.putExtra(Intent.EXTRA_SUBJECT, "RateBeer for Android error report");
                    intent.putExtra(EXTRA_FORMAT, "time");
                    
                    String[] filterSpecs = new String[] { 
                    		"AndroidRuntime:E", 
                    		RateBeerForAndroid.LOG_NAME + ":*", 
                    		"*:S" }; // "ActivityManager:*"
                    intent.putExtra(EXTRA_FILTER_SPECS, filterSpecs);
                    
                    activity.startActivity(intent);
                }
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
        }
    }

}
