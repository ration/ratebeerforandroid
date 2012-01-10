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
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.ratebeer.android.R;
import com.ratebeer.android.api.CommandFailureResult;
import com.ratebeer.android.api.CommandResult;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.api.command.AddAvailabilityCommand;
import com.ratebeer.android.api.command.AddToCellarCommand;
import com.ratebeer.android.api.command.PostRatingCommand;
import com.ratebeer.android.api.command.SendBeerMailCommand;
import com.ratebeer.android.api.command.SetDrinkingStatusCommand;
import com.ratebeer.android.api.command.UploadBeerPhotoCommand;
import com.ratebeer.android.app.ApplicationSettings;
import com.ratebeer.android.app.RateBeerForAndroid;
import com.ratebeer.android.app.persistance.OfflineRating;
import com.ratebeer.android.gui.Home;
import com.ratebeer.android.gui.fragments.AddToCellarFragment.CellarType;

public class PosterService extends RateBeerService {

	public static final String ACTION_SETDRINKINGSTATUS = "com.ratebeer.android.SET_DRINKING_STATUS";
	public static final String ACTION_POSTRATING = "com.ratebeer.android.POST_RATING";
	public static final String ACTION_EDITRATING = "com.ratebeer.android.EDIT_RATING";
	public static final String ACTION_ADDAVAILABILITY = "com.ratebeer.android.ADD_AVAILABILITY";
	public static final String ACTION_ADDTOCELLAR = "com.ratebeer.android.ADD_TO_CELLAR";
	public static final String ACTION_SENDMAIL = "com.ratebeer.android.SEND_BEERMAIL";
	public static final String ACTION_UPLOADBEERPHOTO = "com.ratebeer.android.UPLOAD_BEER_PHOTO";
	public static final String URI_BEER = "http://ratebeer.com/b/%s/";
	public static final String EXTRA_MESSENGER = "MESSENGER";
	public static final String EXTRA_NEWSTATUS = "NEW_STATUS";
	public static final String EXTRA_BEERID = "BEER_ID";
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
	public static final String EXTRA_SELECTEDPLACES = "SELECTEDPLACES";
	public static final String EXTRA_EXTRAPLACENAME = "EXTRAPLACENAME";
	public static final String EXTRA_EXTRAPLACEID = "EXTRAPLACEID";
	public static final String EXTRA_ONBOTTLECAN = "ONBOTTLECAN";
	public static final String EXTRA_ONTAP = "ONTAP";
	public static final String EXTRA_CELLARTYPE = "CELLARTYPE";
	public static final String EXTRA_MEMO = "MEMO";
	public static final String EXTRA_VINTAGE = "VINTAGE";
	public static final String EXTRA_QUANTITY = "QUANTITY";
	public static final String EXTRA_SENDTO = "SENDTO";
	public static final String EXTRA_SUBJECT = "SUBJECT";
	public static final String EXTRA_BODY = "BODY";
	public static final String EXTRA_PHOTO = "PHOTO";
	public static final int NO_BEER_EXTRA = -1;
	public static final int NO_OFFLINE_EXTRA = -1;
	public static final int RESULT_SUCCESS = 0;
	public static final int RESULT_FAILURE = 1;

	private static final int NOTIFY_SETDRINKINGSTATUS = 0;
	private static final int NOTIFY_POSTINGRATING = 1;
	private static final int NOTIFY_ADDAVAILABILITY = 2;
	private static final int NOTIFY_ADDTOCELLAR = 3;
	private static final int NOTIFY_SENDMAIL = 4;
	private static final int NOTIFY_UPLOADPHOTO = 5;

	private static final int IMAGE_MAX_SIZE = 800; // Max pixels in one dimension

	private NotificationManager notificationManager = null;

	public PosterService() {
		super(RateBeerForAndroid.LOG_NAME + " PosterService");
	}

	public PosterService(String name) {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		if (notificationManager == null) {
			notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		}

		// Proper intent received?
		if (intent == null || intent.getAction() == null) {
			Log.d(RateBeerForAndroid.LOG_NAME, "No intent action to perform");
			return;
		}

		// Proper user settings?
		RateBeerForAndroid app = (RateBeerForAndroid) getApplication();
		ApplicationSettings settings = app.getSettings();
		if (settings.getUserSettings() == null) {
			Log.d(RateBeerForAndroid.LOG_NAME, "Canceling " + intent.getAction()
					+ " intent because there are no user settings known.");
			return;
		}

		// Try to set the drinking status
		if (intent.getAction().equals(ACTION_SETDRINKINGSTATUS)) {

			// Get new status text
			String newStatus = intent.getStringExtra(EXTRA_NEWSTATUS);
			int beerId = intent.getIntExtra(EXTRA_BEERID, NO_BEER_EXTRA);
			if (newStatus == null) {
				Log.d(RateBeerForAndroid.LOG_NAME, "No new drinking status is intent; cancelling");
				return;
			}

			// Synchronously set the new drinking status
			// During the operation a notification will be shown
			Log.d(RateBeerForAndroid.LOG_NAME, "Now setting drinking status to " + newStatus);
			Intent recoverIntent;
			if (beerId == NO_BEER_EXTRA) {
				// If no specific beer was tight to this drinking status, assume it was from the home screen's free text
				// input
				recoverIntent = new Intent(this, Home.class);
			} else {
				recoverIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(URI_BEER, Integer
						.toString(beerId))));
			}
			createNotification(NOTIFY_SETDRINKINGSTATUS, getString(R.string.app_settingdrinking), getString(
					R.string.home_nowdrinking, newStatus), true, recoverIntent);
			CommandResult result = new SetDrinkingStatusCommand(app.getApi(), newStatus).execute();
			if (result instanceof CommandSuccessResult) {
				notificationManager.cancel(NOTIFY_SETDRINKINGSTATUS);
				// If requested, call back the messenger, i.e. the calling activity
				callbackMessenger(intent, RESULT_SUCCESS);
			} else {
				String e = result instanceof CommandFailureResult ? ((CommandFailureResult) result).getException()
						.toString() : "Unknown error";
				Log.d(RateBeerForAndroid.LOG_NAME, "Setting drinking status to " + newStatus + " failed: " + e);
				createNotification(NOTIFY_SETDRINKINGSTATUS, getString(R.string.app_settingdrinking),
						getString(R.string.error_commandfailed), true, recoverIntent);
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
				Log.d(RateBeerForAndroid.LOG_NAME, "Missing extras in the POSTRATING intent; cancelling.");
				return;
			}

			// Synchronously post the new rating
			// During the operation a notification will be shown
			Log.d(RateBeerForAndroid.LOG_NAME, "Now posting rating for " + beerName);
			Intent recoverIntent = new Intent(getApplicationContext(), Home.class);
			recoverIntent.replaceExtras(intent.getExtras());
			recoverIntent.setAction(ACTION_EDITRATING);
			createNotification(NOTIFY_POSTINGRATING, getString(R.string.app_postingrating), getString(
					R.string.app_rated, beerName, PostRatingCommand.calculateTotal(aroma, appearance, taste, palate,
							overall)), true, recoverIntent);
			CommandResult result = new PostRatingCommand(app.getApi(), beerId, ratingId, origDate, beerName, aroma,
					appearance, taste, palate, overall, comment).execute();
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
							Log.d(RateBeerForAndroid.LOG_NAME, "Deleted the offline rating for this beer as well.");
							getHelper().getOfflineRatingDao().delete(offlineRating);
						}
					}
				} catch (SQLException e) {
					Log.d(RateBeerForAndroid.LOG_NAME, "Offline rating not available: " + e.toString());
				}
			} else {
				String e = result instanceof CommandFailureResult ? ((CommandFailureResult) result).getException()
						.toString() : "Unknown error";
				Log.d(RateBeerForAndroid.LOG_NAME, "Posting of rating for " + beerName + " failed: " + e);
				createNotification(NOTIFY_POSTINGRATING, getString(R.string.app_postingrating),
						getString(R.string.error_commandfailed), true, recoverIntent);
				// If requested, call back the messenger, i.e. the calling activity
				callbackMessenger(intent, RESULT_FAILURE);
			}

		}

		// Try to add beer availability info
		if (intent.getAction().equals(ACTION_ADDAVAILABILITY)) {

			// Get beer and selected places
			int beerId = intent.getIntExtra(EXTRA_BEERID, -1);
			String beerName = intent.getStringExtra(EXTRA_BEERNAME);
			int[] selectedPlaces = intent.getIntArrayExtra(EXTRA_SELECTEDPLACES);
			String extraPlaceName = intent.getStringExtra(EXTRA_EXTRAPLACENAME);
			int extraPlaceId = intent.getIntExtra(EXTRA_EXTRAPLACEID, -1);
			boolean isOnBottleCan = intent.getBooleanExtra(EXTRA_ONBOTTLECAN, false);
			boolean isOnTap = intent.getBooleanExtra(EXTRA_ONTAP, false);
			if (beerId <= 0 || beerName == null) {
				Log.d(RateBeerForAndroid.LOG_NAME, "Missing extras in the ADDAVAILABILITY intent; cancelling.");
				return;
			}

			// Synchronously post the availability info
			// During the operation a notification will be shown
			Log.d(RateBeerForAndroid.LOG_NAME, "Now adding availability for " + beerName);
			Intent recoverIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(URI_BEER, Integer
					.toString(beerId))));
			createNotification(NOTIFY_ADDAVAILABILITY, getString(R.string.app_addingavailability), getString(
					R.string.app_addingforbeer, beerName), true, recoverIntent);
			CommandResult result = new AddAvailabilityCommand(app.getApi(), beerId, selectedPlaces, extraPlaceName,
					extraPlaceId, isOnBottleCan, isOnTap).execute();
			if (result instanceof CommandSuccessResult) {
				notificationManager.cancel(NOTIFY_ADDAVAILABILITY);
			} else {
				String e = result instanceof CommandFailureResult ? ((CommandFailureResult) result).getException()
						.toString() : "Unknown error";
				Log.d(RateBeerForAndroid.LOG_NAME, "Adding of availability info for " + beerName + " failed: " + e);
				createNotification(NOTIFY_ADDAVAILABILITY, getString(R.string.app_addingavailability),
						getString(R.string.error_commandfailed), true, recoverIntent);
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
				Log.d(RateBeerForAndroid.LOG_NAME, "Missing extras in the ADDAVAILABILITY intent; cancelling.");
				return;
			}

			// Synchronously post the new cellar beer
			// During the operation a notification will be shown
			Log.d(RateBeerForAndroid.LOG_NAME, "Now adding " + beerName + " to the cellar");
			Intent recoverIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(URI_BEER, Integer
					.toString(beerId))));
			createNotification(NOTIFY_ADDTOCELLAR, getString(R.string.app_addingtocellar), getString(
					cellarType == CellarType.Have ? R.string.app_addhave : R.string.app_addwant, beerName), true,
					recoverIntent);
			CommandResult result = new AddToCellarCommand(app.getApi(), cellarType, beerId, memo, vintage, quantity)
					.execute();
			if (result instanceof CommandSuccessResult) {
				notificationManager.cancel(NOTIFY_ADDTOCELLAR);
			} else {
				String e = result instanceof CommandFailureResult ? ((CommandFailureResult) result).getException()
						.toString() : "Unknown error";
				Log.d(RateBeerForAndroid.LOG_NAME, "Adding of " + beerName + " to cellar failed: " + e);
				createNotification(NOTIFY_ADDTOCELLAR, getString(R.string.app_addingtocellar),
						getString(R.string.error_commandfailed), true, recoverIntent);
			}

		}

		// Try to send a mail
		if (intent.getAction().equals(ACTION_SENDMAIL)) {

			// Get mail details
			String sendTo = intent.getStringExtra(EXTRA_SENDTO);
			String subject = intent.getStringExtra(EXTRA_SUBJECT);
			String body = intent.getStringExtra(EXTRA_BODY);

			// Synchronously send the mail
			// During the operation a notification will be shown
			Log.d(RateBeerForAndroid.LOG_NAME, "Now sending mail to " + sendTo);
			Intent recoverIntent = new Intent(Intent.ACTION_VIEW);
			// TODO: Direct this recovery intent to the send mail screen
			createNotification(NOTIFY_SENDMAIL, getString(R.string.mail_sendingmail), getString(
					R.string.mail_sendingto, sendTo), true, recoverIntent);
			CommandResult result = new SendBeerMailCommand(app.getApi(), sendTo, subject, body).execute();
			if (result instanceof CommandSuccessResult) {
				notificationManager.cancel(NOTIFY_SENDMAIL);
			} else {
				String e = result instanceof CommandFailureResult ? ((CommandFailureResult) result).getException()
						.toString() : "Unknown error";
				Log.d(RateBeerForAndroid.LOG_NAME, "Sending of mail to " + sendTo + " failed: " + e);
				createNotification(NOTIFY_SENDMAIL, getString(R.string.mail_sendingmail),
						getString(R.string.error_commandfailed), true, recoverIntent);
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
				Log.d(RateBeerForAndroid.LOG_NAME,
						"No photo URI provided or the photo URI does not point to an existing file; cancelling");
				return;
			}

			// Synchronously upload the photo for the specified beer
			// During the operation a notification will be shown
			Log.d(RateBeerForAndroid.LOG_NAME, "Uploading photo for " + beerName);
			Intent recoverIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(URI_BEER, Integer
					.toString(beerId))));
			createNotification(NOTIFY_UPLOADPHOTO, getString(R.string.app_uploadingphoto), getString(
					R.string.app_photofor, beerName), true, recoverIntent);

			// Make sure the photo is no bigger than 250kB
			try {
				decodeFile(photo);
			} catch (IOException e1) {
				Log.d(RateBeerForAndroid.LOG_NAME, "Resizing of photo + " + photo.toString() + " for " + beerName + 
						" failed: " + e1);
				createNotification(NOTIFY_UPLOADPHOTO, getString(R.string.app_uploadingphoto),
						getString(R.string.error_commandfailed), true, recoverIntent);
				// If requested, call back the messenger, i.e. the calling activity
				callbackMessenger(intent, RESULT_FAILURE);
				return;
			}
			
			// Start actual upload of the now-resized file
			CommandResult result = new UploadBeerPhotoCommand(app.getApi(), beerId, photo).execute();
			if (result instanceof CommandSuccessResult) {
				notificationManager.cancel(NOTIFY_UPLOADPHOTO);
				// If requested, call back the messenger, i.e. the calling activity
				callbackMessenger(intent, RESULT_SUCCESS);
			} else {
				String e = result instanceof CommandFailureResult ? ((CommandFailureResult) result).getException()
						.toString() : "Unknown error";
				Log.d(RateBeerForAndroid.LOG_NAME, "Uploading photo for " + beerName + " failed: " + e);
				createNotification(NOTIFY_UPLOADPHOTO, getString(R.string.app_uploadingphoto),
						getString(R.string.error_commandfailed), true, recoverIntent);
				// If requested, call back the messenger, i.e. the calling activity
				callbackMessenger(intent, RESULT_FAILURE);
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
				Log.e(RateBeerForAndroid.LOG_NAME, "Cannot call back to activity to deliver message '" + msg.toString()
						+ "'");
			}
		}
	}

	private void createNotification(int id, String line1, String line2, boolean autoCancel, Intent contentIntent) {

		// Set up notification (right now) with two lines of text (and optionally an intent)
		Notification notification = new Notification(R.drawable.ic_stat_notification, line1, System.currentTimeMillis());
		if (autoCancel) {
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
		}
		notification.setLatestEventInfo(getApplicationContext(), line1, line2,
				PendingIntent.getActivity(this, 0, contentIntent, 0));

		// Send notification
		notificationManager.notify(id, notification);

	}

}
