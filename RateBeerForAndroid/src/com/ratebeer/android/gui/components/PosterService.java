/*
 * This file is part of RateBeer For Android. RateBeer for Android is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version. RateBeer for Android is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU
 * General Public License along with RateBeer for Android. If not, see <http://www.gnu.org/licenses/>.
 */
package com.ratebeer.android.gui.components;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;

import com.googlecode.androidannotations.annotations.Bean;
import com.googlecode.androidannotations.annotations.EService;
import com.googlecode.androidannotations.annotations.SystemService;
import com.ratebeer.android.R;
import com.ratebeer.android.api.ApiConnection;
import com.ratebeer.android.api.CommandFailureResult;
import com.ratebeer.android.api.CommandResult;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.api.UserSettings;
import com.ratebeer.android.api.command.AddAvailabilityCommand;
import com.ratebeer.android.api.command.AddToCellarCommand;
import com.ratebeer.android.api.command.AddUpcCodeCommand;
import com.ratebeer.android.api.command.DeleteTickCommand;
import com.ratebeer.android.api.command.ImageUrls;
import com.ratebeer.android.api.command.PostRatingCommand;
import com.ratebeer.android.api.command.PostTickCommand;
import com.ratebeer.android.api.command.SendBeerMailCommand;
import com.ratebeer.android.api.command.SendBeerReplyCommand;
import com.ratebeer.android.api.command.SetDrinkingStatusCommand;
import com.ratebeer.android.api.command.UploadBeerPhotoCommand;
import com.ratebeer.android.app.ApplicationSettings;
import com.ratebeer.android.app.persistance.OfflineRating;
import com.ratebeer.android.gui.Home_;
import com.ratebeer.android.gui.components.helpers.DatabaseConsumerService;
import com.ratebeer.android.gui.components.helpers.Log;
import com.ratebeer.android.gui.fragments.AddToCellarFragment.CellarType;

@EService
public class PosterService extends DatabaseConsumerService {

	public static final String ACTION_SETDRINKINGSTATUS = "com.ratebeer.android.SET_DRINKING_STATUS";
	public static final String ACTION_POSTRATING = "com.ratebeer.android.POST_RATING";
	public static final String ACTION_EDITRATING = "com.ratebeer.android.EDIT_RATING";
	public static final String ACTION_POSTTICK = "com.ratebeer.android.POST_TICK";
	public static final String ACTION_ADDAVAILABILITY = "com.ratebeer.android.ADD_AVAILABILITY";
	public static final String ACTION_ADDTOCELLAR = "com.ratebeer.android.ADD_TO_CELLAR";
	public static final String ACTION_SENDMAIL = "com.ratebeer.android.SEND_BEERMAIL";
	public static final String ACTION_UPLOADBEERPHOTO = "com.ratebeer.android.UPLOAD_BEER_PHOTO";
	public static final String ACTION_ADDUPCCODE = "com.ratebeer.android.ADD_UPCCODE";
	public static final String URI_BEER = "http://ratebeer.com/b/%s/";
	public static final String EXTRA_MESSENGER = "MESSENGER";
	public static final String EXTRA_NEWSTATUS = "NEW_STATUS";
	public static final String EXTRA_BEERID = "BEER_ID";
	public static final String EXTRA_USERID = "USER_ID";
	public static final String EXTRA_OFFLINEID = "OFFLINE_ID";
	public static final String EXTRA_ORIGRATINGID = "ORIGRATING_ID";
	public static final String EXTRA_ORIGRATINGDATE = "ORIGRATING_DATE";
	public static final String EXTRA_BEERNAME = "BEER_NAME";
	public static final String EXTRA_AROMA = "AROMA";
	public static final String EXTRA_APPEARANCE = "APPEARANCE";
	public static final String EXTRA_TASTE = "TASTE";
	public static final String EXTRA_PALATE = "PALATE";
	public static final String EXTRA_OVERALL = "OVERALL";
	public static final String EXTRA_COMMENT = "COMMENT";
	public static final String EXTRA_LIKED = "LIKED";
<<<<<<< HEAD
	public static final String EXTRA_SELECTEDPLACES = "SELECTEDPLACES";
	public static final String EXTRA_EXTRAPLACENAME = "EXTRAPLACENAME";
	public static final String EXTRA_EXTRAPLACEID = "EXTRAPLACEID";
	public static final String EXTRA_ONBOTTLECAN = "ONBOTTLECAN";
	public static final String EXTRA_ONTAP = "ONTAP";
=======
	public static final String EXTRA_PLACEID = "EXTRAPLACEID";
>>>>>>> 9cb2b20cee7ae90e7a5ea61c0ebff4e0c86a6dd6
	public static final String EXTRA_CELLARTYPE = "CELLARTYPE";
	public static final String EXTRA_MEMO = "MEMO";
	public static final String EXTRA_VINTAGE = "VINTAGE";
	public static final String EXTRA_QUANTITY = "QUANTITY";
	public static final String EXTRA_SENDTO = "SENDTO";
	public static final String EXTRA_SUBJECT = "SUBJECT";
	public static final String EXTRA_BODY = "BODY";
	public static final String EXTRA_REPLYTO = "REPLYTO";
	public static final String EXTRA_RECIPIENT = "RECIPIENT";
	public static final String EXTRA_PHOTO = "PHOTO";
	public static final String EXTRA_UPCCODE = "UPCCODE";
	public static final int EXTRA_TICK_DELETE = -1;
	public static final int NO_BEER_EXTRA = -1;
	public static final int NO_OFFLINE_EXTRA = -1;
	public static final int NO_REPLY_EXTRA = 0;
	public static final int RESULT_SUCCESS = 0;
	public static final int RESULT_FAILURE = 1;

	private static final int NOTIFY_SETDRINKINGSTATUS = 0;
	private static final int NOTIFY_POSTINGRATING = 1;
	private static final int NOTIFY_ADDAVAILABILITY = 2;
	private static final int NOTIFY_ADDTOCELLAR = 3;
	private static final int NOTIFY_SENDMAIL = 4;
	private static final int NOTIFY_UPLOADPHOTO = 5;
	private static final int NOTIFY_ADDUPCCODE = 6;
	private static final int NOTIFY_POSTINGTICK = 7;

	private static final int IMAGE_MAX_SIZE = 1280; // Max pixels in one dimension

	@Bean
	protected Log Log;
	@Bean
	protected ApplicationSettings applicationSettings;
	@Bean
	protected ApiConnection apiConnection;
<<<<<<< HEAD
	
=======

>>>>>>> 9cb2b20cee7ae90e7a5ea61c0ebff4e0c86a6dd6
	@SystemService
	protected NotificationManager notificationManager;

	public PosterService() {
		super(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME + " PosterService");
	}

	public PosterService(String name) {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		// Proper intent received?
		if (intent == null || intent.getAction() == null) {
			Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "No intent action to perform");
			return;
		}

		// Proper user settings?
		UserSettings user = applicationSettings.getUserSettings();
		if (user == null) {
			Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "Canceling " + intent.getAction()
					+ " intent because there are no user settings known.");
			return;
		}

		// Try to set the drinking status
		if (intent.getAction().equals(ACTION_SETDRINKINGSTATUS)) {

			// Get new status text
			String newStatus = intent.getStringExtra(EXTRA_NEWSTATUS);
			int beerId = intent.getIntExtra(EXTRA_BEERID, NO_BEER_EXTRA);
			if (newStatus == null) {
<<<<<<< HEAD
				Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "No new drinking status is intent; cancelling");
=======
				Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME,
						"No new drinking status is intent; cancelling");
>>>>>>> 9cb2b20cee7ae90e7a5ea61c0ebff4e0c86a6dd6
				return;
			}

			// Synchronously set the new drinking status
			// During the operation a notification will be shown
<<<<<<< HEAD
			Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "Now setting drinking status to " + newStatus);
=======
			Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "Now setting drinking status to "
					+ newStatus);
>>>>>>> 9cb2b20cee7ae90e7a5ea61c0ebff4e0c86a6dd6
			Intent recoverIntent;
			if (beerId == NO_BEER_EXTRA) {
				// If no specific beer was tight to this drinking status, assume it was from the home screen's free text
				// input
				recoverIntent = new Intent(this, Home_.class);
			} else {
<<<<<<< HEAD
				recoverIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(URI_BEER, Integer
						.toString(beerId))));
			}
			createNotification(NOTIFY_SETDRINKINGSTATUS, getString(R.string.app_settingdrinking), getString(
					R.string.home_nowdrinking, newStatus), true, recoverIntent, null, beerId);
=======
				recoverIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(URI_BEER,
						Integer.toString(beerId))));
			}
			createNotification(NOTIFY_SETDRINKINGSTATUS, getString(R.string.app_settingdrinking),
					getString(R.string.home_nowdrinking, newStatus), true, recoverIntent, null, beerId);
>>>>>>> 9cb2b20cee7ae90e7a5ea61c0ebff4e0c86a6dd6
			CommandResult result = new SetDrinkingStatusCommand(user, newStatus).execute(apiConnection);
			if (result instanceof CommandSuccessResult) {
				notificationManager.cancel(NOTIFY_SETDRINKINGSTATUS);
				// If requested, call back the messenger, i.e. the calling activity
				callbackMessenger(intent, RESULT_SUCCESS);
			} else {
				String e = result instanceof CommandFailureResult ? ((CommandFailureResult) result).getException()
						.toString() : "Unknown error";
<<<<<<< HEAD
				Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "Setting drinking status to " + newStatus + " failed: " + e);
=======
				Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "Setting drinking status to "
						+ newStatus + " failed: " + e);
>>>>>>> 9cb2b20cee7ae90e7a5ea61c0ebff4e0c86a6dd6
				createNotification(NOTIFY_SETDRINKINGSTATUS, getString(R.string.app_settingdrinking),
						getString(R.string.error_commandfailed), true, recoverIntent, null, beerId);
				// If requested, call back the messenger, i.e. the calling activity
				callbackMessenger(intent, RESULT_FAILURE);
			}

		}

		// Try to add a new rating
		if (intent.getAction().equals(ACTION_POSTRATING)) {

			// Get rating details
			int beerId = intent.getIntExtra(EXTRA_BEERID, NO_BEER_EXTRA);
			String beerName = intent.getStringExtra(EXTRA_BEERNAME);
			int offlineId = intent.getIntExtra(EXTRA_OFFLINEID, NO_OFFLINE_EXTRA);
			int ratingId = intent.getIntExtra(EXTRA_ORIGRATINGID, -1);
			String origDate = intent.getStringExtra(EXTRA_ORIGRATINGDATE);
			int aroma = intent.getIntExtra(EXTRA_AROMA, -1);
			int appearance = intent.getIntExtra(EXTRA_APPEARANCE, -1);
			int taste = intent.getIntExtra(EXTRA_TASTE, -1);
			int palate = intent.getIntExtra(EXTRA_PALATE, -1);
			int overall = intent.getIntExtra(EXTRA_OVERALL, -1);
			String comment = intent.getStringExtra(EXTRA_COMMENT);
			if (beerId == NO_BEER_EXTRA || aroma <= 0 || appearance <= 0 || taste <= 0 || palate <= 0 || overall <= 0
					|| beerName == null || comment == null) {
<<<<<<< HEAD
				Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "Missing extras in the POSTRATING intent; cancelling.");
=======
				Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME,
						"Missing extras in the POSTRATING intent; cancelling.");
>>>>>>> 9cb2b20cee7ae90e7a5ea61c0ebff4e0c86a6dd6
				return;
			}

			// Synchronously post the new rating
			// During the operation a notification will be shown
			Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "Now posting rating for " + beerName);
			Intent recoverIntent = new Intent(getApplicationContext(), Home_.class);
			recoverIntent.replaceExtras(intent.getExtras());
			recoverIntent.setAction(ACTION_EDITRATING);
<<<<<<< HEAD
			createNotification(NOTIFY_POSTINGRATING, getString(R.string.app_postingrating), getString(
					R.string.app_rated, beerName, PostRatingCommand.calculateTotal(aroma, appearance, taste, palate,
							overall)), true, recoverIntent, null, beerId);
			CommandResult result = new PostRatingCommand(user, beerId, ratingId, origDate, beerName, aroma,
					appearance, taste, palate, overall, comment).execute(apiConnection);
=======
			createNotification(
					NOTIFY_POSTINGRATING,
					getString(R.string.app_postingrating),
					getString(R.string.app_rated, beerName,
							PostRatingCommand.calculateTotal(aroma, appearance, taste, palate, overall)), true,
					recoverIntent, null, beerId);
			CommandResult result = new PostRatingCommand(user, beerId, ratingId, origDate, beerName, aroma, appearance,
					taste, palate, overall, comment).execute(apiConnection);
>>>>>>> 9cb2b20cee7ae90e7a5ea61c0ebff4e0c86a6dd6
			if (result instanceof CommandSuccessResult) {
				notificationManager.cancel(NOTIFY_POSTINGRATING);
				// If requested, call back the messenger, i.e. the calling activity
				callbackMessenger(intent, RESULT_SUCCESS);
				// Ratings are usually stored locally as offline rating using the ORM persistence layer
				// If so, it can now be removed
				try {
					if (offlineId != NO_OFFLINE_EXTRA) {
						OfflineRating offlineRating = getHelper().getOfflineRatingDao().queryForId(offlineId);
						if (offlineRating != null) {
<<<<<<< HEAD
							Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "Deleted the offline rating for this beer as well.");
=======
							Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME,
									"Deleted the offline rating for this beer as well.");
>>>>>>> 9cb2b20cee7ae90e7a5ea61c0ebff4e0c86a6dd6
							getHelper().getOfflineRatingDao().delete(offlineRating);
						}
					}
				} catch (SQLException e) {
<<<<<<< HEAD
					Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "Offline rating not available: " + e.toString());
=======
					Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "Offline rating not available: "
							+ e.toString());
>>>>>>> 9cb2b20cee7ae90e7a5ea61c0ebff4e0c86a6dd6
				}
			} else {
				String e = result instanceof CommandFailureResult ? ((CommandFailureResult) result).getException()
						.toString() : "Unknown error";
<<<<<<< HEAD
				Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "Posting of rating for " + beerName + " failed: " + e);
=======
				Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "Posting of rating for " + beerName
						+ " failed: " + e);
>>>>>>> 9cb2b20cee7ae90e7a5ea61c0ebff4e0c86a6dd6
				createNotification(NOTIFY_POSTINGRATING, getString(R.string.app_postingrating),
						getString(R.string.error_commandfailed), true, recoverIntent, null, beerId);
				// If requested, call back the messenger, i.e. the calling activity
				callbackMessenger(intent, RESULT_FAILURE);
			}

		}

		// Try to post a tick update
		if (intent.getAction().equals(ACTION_POSTTICK)) {

			// Get tick details
			int beerId = intent.getIntExtra(EXTRA_BEERID, NO_BEER_EXTRA);
			String beerName = intent.getStringExtra(EXTRA_BEERNAME);
			int userId = intent.getIntExtra(EXTRA_USERID, -1);
			int liked = intent.getIntExtra(EXTRA_LIKED, EXTRA_TICK_DELETE);
			if (beerId == NO_BEER_EXTRA || beerName == null || userId <= 0 || liked == 0 || liked > 5 || liked < -1) {
<<<<<<< HEAD
				Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "Missing extras in the POSTRATING intent; cancelling.");
=======
				Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME,
						"Missing extras in the POSTRATING intent; cancelling.");
>>>>>>> 9cb2b20cee7ae90e7a5ea61c0ebff4e0c86a6dd6
				return;
			}

			// Synchronously post the tick update
			// During the operation a notification will be shown
			Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "Now ticking " + beerName);
<<<<<<< HEAD
			Intent recoverIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(URI_BEER, Integer
					.toString(beerId))));
=======
			Intent recoverIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(URI_BEER,
					Integer.toString(beerId))));
>>>>>>> 9cb2b20cee7ae90e7a5ea61c0ebff4e0c86a6dd6
			// If liked (the actual tick) is set to -1 we delete this tick instead
			boolean del = liked == EXTRA_TICK_DELETE;
			createNotification(NOTIFY_POSTINGTICK,
					getString(del ? R.string.app_removingtick : R.string.app_postingtick),
					getString(R.string.app_forbeer, beerName), true, recoverIntent, null, beerId);
			CommandResult result;
			if (del) {
				result = new DeleteTickCommand(user, beerId, userId, beerName).execute(apiConnection);
			} else {
				result = new PostTickCommand(user, beerId, userId, beerName, liked).execute(apiConnection);
			}
			if (result instanceof CommandSuccessResult) {
				notificationManager.cancel(NOTIFY_POSTINGTICK);
				// If requested, call back the messenger, i.e. the calling activity
				callbackMessenger(intent, RESULT_SUCCESS);
			} else {
				String e = result instanceof CommandFailureResult ? ((CommandFailureResult) result).getException()
						.toString() : "Unknown error";
<<<<<<< HEAD
				Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, (del ? "Removing of tick for " : "Ticking of ") + beerName
						+ " failed: " + e);
=======
				Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, (del ? "Removing of tick for "
						: "Ticking of ") + beerName + " failed: " + e);
>>>>>>> 9cb2b20cee7ae90e7a5ea61c0ebff4e0c86a6dd6
				createNotification(NOTIFY_POSTINGRATING, getString(del ? R.string.app_removingtick
						: R.string.app_postingtick), getString(R.string.error_commandfailed), true, recoverIntent,
						null, beerId);
				// If requested, call back the messenger, i.e. the calling activity
				callbackMessenger(intent, RESULT_FAILURE);
			}

		}

		// Try to add beer availability info
		if (intent.getAction().equals(ACTION_ADDAVAILABILITY)) {

			// Get beer and selected places
			int beerId = intent.getIntExtra(EXTRA_BEERID, -1);
			String beerName = intent.getStringExtra(EXTRA_BEERNAME);
<<<<<<< HEAD
			int[] selectedPlaces = intent.getIntArrayExtra(EXTRA_SELECTEDPLACES);
			String extraPlaceName = intent.getStringExtra(EXTRA_EXTRAPLACENAME);
			int extraPlaceId = intent.getIntExtra(EXTRA_EXTRAPLACEID, -1);
			boolean isOnBottleCan = intent.getBooleanExtra(EXTRA_ONBOTTLECAN, false);
			boolean isOnTap = intent.getBooleanExtra(EXTRA_ONTAP, false);
			if (beerId <= 0 || beerName == null) {
				Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "Missing extras in the ADDAVAILABILITY intent; cancelling.");
=======
			int placeId = intent.getIntExtra(EXTRA_PLACEID, -1);
			if (beerId <= 0 || beerName == null) {
				Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME,
						"Missing extras in the ADDAVAILABILITY intent; cancelling.");
>>>>>>> 9cb2b20cee7ae90e7a5ea61c0ebff4e0c86a6dd6
				return;
			}

			// Synchronously post the availability info
			// During the operation a notification will be shown
			Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "Now adding availability for " + beerName);
<<<<<<< HEAD
			Intent recoverIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(URI_BEER, Integer
					.toString(beerId))));
			createNotification(NOTIFY_ADDAVAILABILITY, getString(R.string.app_addingavailability), getString(
					R.string.app_addingforbeer, beerName), true, recoverIntent, null, beerId);
			CommandResult result = new AddAvailabilityCommand(user, beerId, selectedPlaces, extraPlaceName,
					extraPlaceId, isOnBottleCan, isOnTap).execute(apiConnection);
=======
			Intent recoverIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(URI_BEER,
					Integer.toString(beerId))));
			createNotification(NOTIFY_ADDAVAILABILITY, getString(R.string.app_addingavailability),
					getString(R.string.app_addingforbeer, beerName), true, recoverIntent, null, beerId);
			CommandResult result = new AddAvailabilityCommand(user, beerId, placeId).execute(apiConnection);
>>>>>>> 9cb2b20cee7ae90e7a5ea61c0ebff4e0c86a6dd6
			if (result instanceof CommandSuccessResult) {
				notificationManager.cancel(NOTIFY_ADDAVAILABILITY);
			} else {
				String e = result instanceof CommandFailureResult ? ((CommandFailureResult) result).getException()
						.toString() : "Unknown error";
<<<<<<< HEAD
				Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "Adding of availability info for " + beerName + " failed: " + e);
=======
				Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "Adding of availability info for "
						+ beerName + " failed: " + e);
>>>>>>> 9cb2b20cee7ae90e7a5ea61c0ebff4e0c86a6dd6
				createNotification(NOTIFY_ADDAVAILABILITY, getString(R.string.app_addingavailability),
						getString(R.string.error_commandfailed), true, recoverIntent, null, beerId);
			}

		}

		// Try to add a beer to the cellar (a want or a have)
		if (intent.getAction().equals(ACTION_ADDTOCELLAR)) {

			// Get beer and notes
			int beerId = intent.getIntExtra(EXTRA_BEERID, -1);
			String beerName = intent.getStringExtra(EXTRA_BEERNAME);
			CellarType cellarType = CellarType.valueOf(intent.getStringExtra(EXTRA_CELLARTYPE));
			String memo = intent.getStringExtra(EXTRA_MEMO);
			String vintage = intent.getStringExtra(EXTRA_VINTAGE);
			String quantity = intent.getStringExtra(EXTRA_QUANTITY);
			if (beerId <= 0 || beerName == null) {
<<<<<<< HEAD
				Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "Missing extras in the ADDAVAILABILITY intent; cancelling.");
=======
				Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME,
						"Missing extras in the ADDAVAILABILITY intent; cancelling.");
>>>>>>> 9cb2b20cee7ae90e7a5ea61c0ebff4e0c86a6dd6
				return;
			}

			// Synchronously post the new cellar beer
			// During the operation a notification will be shown
			Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "Now adding " + beerName + " to the cellar");
<<<<<<< HEAD
			Intent recoverIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(URI_BEER, Integer
					.toString(beerId))));
			createNotification(NOTIFY_ADDTOCELLAR, getString(R.string.app_addingtocellar), getString(
					cellarType == CellarType.Have ? R.string.app_addhave : R.string.app_addwant, beerName), true,
					recoverIntent, null, beerId);
=======
			Intent recoverIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(URI_BEER,
					Integer.toString(beerId))));
			createNotification(NOTIFY_ADDTOCELLAR, getString(R.string.app_addingtocellar),
					getString(cellarType == CellarType.Have ? R.string.app_addhave : R.string.app_addwant, beerName),
					true, recoverIntent, null, beerId);
>>>>>>> 9cb2b20cee7ae90e7a5ea61c0ebff4e0c86a6dd6
			CommandResult result = new AddToCellarCommand(user, cellarType, beerId, memo, vintage, quantity)
					.execute(apiConnection);
			if (result instanceof CommandSuccessResult) {
				notificationManager.cancel(NOTIFY_ADDTOCELLAR);
			} else {
				String e = result instanceof CommandFailureResult ? ((CommandFailureResult) result).getException()
						.toString() : "Unknown error";
<<<<<<< HEAD
				Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "Adding of " + beerName + " to cellar failed: " + e);
=======
				Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "Adding of " + beerName
						+ " to cellar failed: " + e);
>>>>>>> 9cb2b20cee7ae90e7a5ea61c0ebff4e0c86a6dd6
				createNotification(NOTIFY_ADDTOCELLAR, getString(R.string.app_addingtocellar),
						getString(R.string.error_commandfailed), true, recoverIntent, null, beerId);
			}

		}

		// Try to send a mail
		if (intent.getAction().equals(ACTION_SENDMAIL)) {

			// Get mail details
			String sendTo = intent.getStringExtra(EXTRA_SENDTO);
			String subject = intent.getStringExtra(EXTRA_SUBJECT);
			String body = intent.getStringExtra(EXTRA_BODY);
			int replyTo = intent.getIntExtra(EXTRA_REPLYTO, NO_REPLY_EXTRA);
			int recipient = intent.getIntExtra(EXTRA_RECIPIENT, NO_REPLY_EXTRA);

			// Synchronously send the mail or reply
			// During the operation a notification will be shown
			Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME,
					(replyTo == NO_REPLY_EXTRA ? "Now sending mail to " : "Now sending reply to " + replyTo + " to ")
							+ sendTo);
			Intent recoverIntent = new Intent(getApplicationContext(), Home_.class);
			recoverIntent.replaceExtras(intent.getExtras());
			recoverIntent.setAction(ACTION_SENDMAIL);
			createNotification(
					NOTIFY_SENDMAIL,
					getString(R.string.mail_sendingmail),
					getString((replyTo == NO_REPLY_EXTRA ? R.string.mail_sendingto : R.string.mail_replyingto), sendTo),
					true, recoverIntent, sendTo, NO_BEER_EXTRA);
			CommandResult result;
			if (replyTo == NO_REPLY_EXTRA)
				result = new SendBeerMailCommand(user, sendTo, subject, body).execute(apiConnection);
			else
				result = new SendBeerReplyCommand(user, replyTo, recipient, body).execute(apiConnection);
			if (result instanceof CommandSuccessResult) {
				notificationManager.cancel(NOTIFY_SENDMAIL);
			} else {
				String e = result instanceof CommandFailureResult ? ((CommandFailureResult) result).getException()
						.toString() : "Unknown error";
<<<<<<< HEAD
				Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "Sending of mail to " + sendTo + " failed: " + e);
=======
				Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "Sending of mail to " + sendTo
						+ " failed: " + e);
>>>>>>> 9cb2b20cee7ae90e7a5ea61c0ebff4e0c86a6dd6
				createNotification(NOTIFY_SENDMAIL, getString(R.string.mail_sendingmail),
						getString(R.string.error_commandfailed), true, recoverIntent, sendTo, NO_BEER_EXTRA);
			}

		}

		// Upload photo of a beer
		if (intent.getAction().equals(ACTION_UPLOADBEERPHOTO)) {

			// Get photo URI and beer id and name
			File photo = (File) intent.getSerializableExtra(EXTRA_PHOTO);
			int beerId = intent.getIntExtra(EXTRA_BEERID, NO_BEER_EXTRA);
			String beerName = intent.getStringExtra(EXTRA_BEERNAME);
			if (beerName == null) {
				beerName = "beer with ID " + Integer.toString(beerId);
			}
			if (photo == null || photo.getPath() == null || !(new File(photo.getPath()).exists())) {
				Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME,
						"No photo URI provided or the photo URI does not point to an existing file; cancelling");
				return;
			}

			// Synchronously upload the photo for the specified beer
			// During the operation a notification will be shown
			Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "Uploading photo for " + beerName);
<<<<<<< HEAD
			Intent recoverIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(URI_BEER, Integer
					.toString(beerId))));
			createNotification(NOTIFY_UPLOADPHOTO, getString(R.string.app_uploadingphoto), getString(
					R.string.app_photofor, beerName), true, recoverIntent, null, beerId);
=======
			Intent recoverIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(URI_BEER,
					Integer.toString(beerId))));
			createNotification(NOTIFY_UPLOADPHOTO, getString(R.string.app_uploadingphoto),
					getString(R.string.app_photofor, beerName), true, recoverIntent, null, beerId);
>>>>>>> 9cb2b20cee7ae90e7a5ea61c0ebff4e0c86a6dd6

			// Make sure the photo is no bigger than 50kB
			try {
				decodeFile(photo);
			} catch (IOException e1) {
<<<<<<< HEAD
				Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "Resizing of photo + " + photo.toString() + " for " + beerName + 
						" failed: " + e1);
=======
				Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME,
						"Resizing of photo + " + photo.toString() + " for " + beerName + " failed: " + e1);
>>>>>>> 9cb2b20cee7ae90e7a5ea61c0ebff4e0c86a6dd6
				createNotification(NOTIFY_UPLOADPHOTO, getString(R.string.app_uploadingphoto),
						getString(R.string.error_commandfailed), true, recoverIntent, null, beerId);
				// If requested, call back the messenger, i.e. the calling activity
				callbackMessenger(intent, RESULT_FAILURE);
				return;
			}
<<<<<<< HEAD
			
=======

>>>>>>> 9cb2b20cee7ae90e7a5ea61c0ebff4e0c86a6dd6
			// Start actual upload of the now-resized file
			CommandResult result = new UploadBeerPhotoCommand(user, beerId, photo).execute(apiConnection);
			if (result instanceof CommandSuccessResult) {
				notificationManager.cancel(NOTIFY_UPLOADPHOTO);
				// If requested, call back the messenger, i.e. the calling activity
				callbackMessenger(intent, RESULT_SUCCESS);
			} else {
				String e = result instanceof CommandFailureResult ? ((CommandFailureResult) result).getException()
						.toString() : "Unknown error";
<<<<<<< HEAD
				Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "Uploading photo for " + beerName + " failed: " + e);
=======
				Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "Uploading photo for " + beerName
						+ " failed: " + e);
>>>>>>> 9cb2b20cee7ae90e7a5ea61c0ebff4e0c86a6dd6
				createNotification(NOTIFY_UPLOADPHOTO, getString(R.string.app_uploadingphoto),
						getString(R.string.error_commandfailed), true, recoverIntent, null, beerId);
				// If requested, call back the messenger, i.e. the calling activity
				callbackMessenger(intent, RESULT_FAILURE);
			}

		}

		// Try to add a UPC code to some selected beer
		if (intent.getAction().equals(ACTION_ADDUPCCODE)) {

			// Get beer and upc code
			int beerId = intent.getIntExtra(EXTRA_BEERID, -1);
			String beerName = intent.getStringExtra(EXTRA_BEERNAME);
			String upcCode = intent.getStringExtra(EXTRA_UPCCODE);
			if (beerId <= 0 || beerName == null || upcCode == null || upcCode.equals("")) {
<<<<<<< HEAD
				Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "Missing extras in the ADD_UPCCODE intent; cancelling.");
=======
				Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME,
						"Missing extras in the ADD_UPCCODE intent; cancelling.");
>>>>>>> 9cb2b20cee7ae90e7a5ea61c0ebff4e0c86a6dd6
				return;
			}

			// Synchronously call the add UPC code method
			// During the operation a notification will be shown
<<<<<<< HEAD
			Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "Adding barcode " + upcCode + " to " + beerName);
			Intent recoverIntent = new Intent(getApplicationContext(), Home_.class);
			recoverIntent.replaceExtras(intent.getExtras());
			recoverIntent.setAction(ACTION_ADDUPCCODE);
			createNotification(NOTIFY_ADDUPCCODE, getString(R.string.app_addingupccode), getString(
					R.string.app_addingcodefor, beerName), true, recoverIntent, null, beerId);
=======
			Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "Adding barcode " + upcCode + " to "
					+ beerName);
			Intent recoverIntent = new Intent(getApplicationContext(), Home_.class);
			recoverIntent.replaceExtras(intent.getExtras());
			recoverIntent.setAction(ACTION_ADDUPCCODE);
			createNotification(NOTIFY_ADDUPCCODE, getString(R.string.app_addingupccode),
					getString(R.string.app_addingcodefor, beerName), true, recoverIntent, null, beerId);
>>>>>>> 9cb2b20cee7ae90e7a5ea61c0ebff4e0c86a6dd6
			CommandResult result = new AddUpcCodeCommand(user, beerId, upcCode).execute(apiConnection);
			if (result instanceof CommandSuccessResult) {
				notificationManager.cancel(NOTIFY_ADDUPCCODE);
			} else {
				String e = result instanceof CommandFailureResult ? ((CommandFailureResult) result).getException()
						.toString() : "Unknown error";
<<<<<<< HEAD
				Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "Adding of barcode " + upcCode + " to " + beerName + " failed: " + e);
=======
				Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "Adding of barcode " + upcCode + " to "
						+ beerName + " failed: " + e);
>>>>>>> 9cb2b20cee7ae90e7a5ea61c0ebff4e0c86a6dd6
				createNotification(NOTIFY_ADDUPCCODE, getString(R.string.app_addingupccode),
						getString(R.string.error_commandfailed), true, recoverIntent, null, beerId);
			}

		}

	}

	private void decodeFile(File f) throws IOException {
		// See http://stackoverflow.com/a/3549021/243165
		// Decode image size
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;

		FileInputStream fis = new FileInputStream(f);
		BitmapFactory.decodeStream(fis, null, o);
		fis.close();

		int scale = 1;
		if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
			scale = (int) Math.pow(
					2,
					(int) Math.round(Math.log(IMAGE_MAX_SIZE / (double) Math.max(o.outHeight, o.outWidth))
							/ Math.log(0.5)));
		}

		// Decode with inSampleSize
		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = scale;
		fis = new FileInputStream(f);
		Bitmap b = BitmapFactory.decodeStream(fis, null, o2);
		fis.close();

		// Write bitmap to the desired output file
		// See http://stackoverflow.com/a/673014/243165
		FileOutputStream out = new FileOutputStream(f);
		b.compress(Bitmap.CompressFormat.JPEG, 80, out);
		out.close();
	}

	private void callbackMessenger(Intent intent, int result) {
		if (intent.hasExtra(EXTRA_MESSENGER)) {
			// Prepare a message
			Messenger callback = intent.getParcelableExtra(EXTRA_MESSENGER);
			Message msg = Message.obtain();
			msg.arg1 = result;
			try {
				// Send it back to the messenger, i.e. the activity
				callback.send(msg);
			} catch (RemoteException e) {
<<<<<<< HEAD
				Log.e(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "Cannot call back to activity to deliver message '" + msg.toString()
						+ "'");
=======
				Log.e(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME,
						"Cannot call back to activity to deliver message '" + msg.toString() + "'");
>>>>>>> 9cb2b20cee7ae90e7a5ea61c0ebff4e0c86a6dd6
			}
		}
	}

<<<<<<< HEAD
	private void createNotification(int id, String line1, String line2, boolean autoCancel, Intent contentIntent, String username, int beerId) {
=======
	private void createNotification(int id, String line1, String line2, boolean autoCancel, Intent contentIntent,
			String username, int beerId) {
>>>>>>> 9cb2b20cee7ae90e7a5ea61c0ebff4e0c86a6dd6

		// User/beer image to show?
		Bitmap avatar = null;
		try {
			if (username != null)
				avatar = BitmapFactory.decodeStream(apiConnection.getRaw(ImageUrls.getUserPhotoUrl(username)));
			if (beerId > 0)
				avatar = BitmapFactory.decodeStream(apiConnection.getRaw(ImageUrls.getBeerPhotoUrl(beerId)));
		} catch (Exception e) {
<<<<<<< HEAD
			// Could not load? Just don't show an image 
=======
			// Could not load? Just don't show an image
>>>>>>> 9cb2b20cee7ae90e7a5ea61c0ebff4e0c86a6dd6
		}

		// Set up notification with user/beer image and two lines of text (and optionally an intent)
		Builder builder = new NotificationCompat.Builder(getApplicationContext());
		builder.setSmallIcon(R.drawable.ic_stat_notification);
		if (avatar != null)
			builder.setLargeIcon(avatar);
		builder.setTicker(line1);
		builder.setContentTitle(line1);
		builder.setContentText(line2);
		builder.setAutoCancel(autoCancel);
		builder.setContentIntent(PendingIntent.getActivity(this, 0, contentIntent, 0));

		// Send notification
		Notification notification = builder.build();
		notificationManager.notify(id, notification);

	}

}
