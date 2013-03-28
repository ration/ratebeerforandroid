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
package com.ratebeer.android.gui.components;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.googlecode.androidannotations.annotations.Bean;
import com.googlecode.androidannotations.annotations.EService;
import com.googlecode.androidannotations.annotations.SystemService;
import com.j256.ormlite.dao.Dao;
import com.jakewharton.notificationcompat2.NotificationCompat2;
import com.jakewharton.notificationcompat2.NotificationCompat2.Builder;
import com.jakewharton.notificationcompat2.NotificationCompat2.InboxStyle;
import com.ratebeer.android.R;
import com.ratebeer.android.api.ApiConnection;
import com.ratebeer.android.api.CommandFailureResult;
import com.ratebeer.android.api.CommandResult;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.api.UserSettings;
import com.ratebeer.android.api.command.GetAllBeerMailsCommand;
import com.ratebeer.android.api.command.GetAllBeerMailsCommand.Mail;
import com.ratebeer.android.api.command.GetBeerMailCommand;
import com.ratebeer.android.app.ApplicationSettings;
import com.ratebeer.android.app.persistance.BeerMail;
import com.ratebeer.android.gui.Home_;
import com.ratebeer.android.gui.components.helpers.DatabaseConsumerService;
import com.ratebeer.android.gui.components.helpers.Log;

@EService
public class BeermailService extends DatabaseConsumerService {

	public static final String ACTION_VIEWBEERMAILS = "VIEW_BEERMAILS";
	public static final String ACTION_VIEWBEERMAIL = "VIEW_BEERMAIL";
	public static final String ACTION_REPLYBEERMAIL = "REPLY_BEERMAIL";
	public static final String EXTRA_MESSENGER = "MESSENGER";
	public static final String EXTRA_MAIL = "MAIL";

	private static final int NOTIFY_NEWBEERMAIL = 0;
	public static final int RESULT_SUCCESS = 0;
	public static final int RESULT_FAILURE = 1;
	public static final int RESULT_STARTED = 2;

	@Bean
	protected Log Log;
	@Bean
	protected ApplicationSettings applicationSettings;
	@Bean
	protected ApiConnection apiConnection;
	
	@SystemService
	protected NotificationManager notificationManager;
	@SystemService
	protected ConnectivityManager connectivityManager;

	public BeermailService() {
		super(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME + " BeermailService");
	}

	public BeermailService(String name) {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		if (isBackgroundDataDisabled()) {
			Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME,
					"Skip the update, since background data is disabled on a system-wide level");
			return;
		}

		// Proper user settings?
		UserSettings user = applicationSettings.getUserSettings();
		if (user == null) {
			Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "Canceling BeerMail check intent because there are no user settings known.");
			return;
		}

		callbackMessenger(intent, RESULT_STARTED);

		// Look for (new) beermail
		SimpleDateFormat sentDateFormat = new SimpleDateFormat("M/d/yyyy h:m:s a", Locale.US);
		GetAllBeerMailsCommand allMails = new GetAllBeerMailsCommand(user);
		CommandResult result = allMails.execute(apiConnection);
		if (result instanceof CommandSuccessResult) {

			Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "Received " + allMails.getMails().size() + " mail headers.");

			final int MAX_CONTENTLENGTH = 50;
			int unreadMails = 0;
			BeerMail firstUnread = null;
			List<String> mailBy = new ArrayList<String>();
			List<String> mailLines = new ArrayList<String>();
			try {

				Dao<BeerMail, Integer> dao = getHelper().getBeerMailDao();
				for (Mail mail : allMails.getMails()) {

					// Get the existing mail or retrieve new details
					BeerMail beerMail = dao.queryForId(mail.messageID);
					if (beerMail == null || beerMail.getBody().equals(getString(R.string.mail_couldnotretrieve))) {

						// Get the body too
						String body;
						GetBeerMailCommand gbmCommand = new GetBeerMailCommand(user, mail.messageID);
						CommandResult gbmResult = gbmCommand.execute(apiConnection);
						if (gbmResult instanceof CommandSuccessResult) {
							body = gbmCommand.getMail().body;
						} else {
							// TODO: Allow later retrieval of the mail body, e.g. in the mail details screen
							body = getString(R.string.mail_couldnotretrieve);
						}

						if (beerMail == null) {
							// Create a beer mail object to save to the database
							Date sent = null;
							try {
								// Parse mail date
								sent = sentDateFormat.parse(mail.sent);
							} catch (ParseException e) {
								// Cannot parse date; ignore and don't show instead
							}
							// Add to the database
							beerMail = new BeerMail(mail.messageID, mail.senderID, mail.senderName, mail.messageRead,
									mail.replied, sent, mail.subject, body);
							dao.create(beerMail);
						} else {
							// Update existing beermail object and pretend it isn't read yet
							beerMail.updateBody(body);
							beerMail.setIsRead(false);
							dao.update(beerMail);
						}
					}

					// Count unread mails
					if (!beerMail.isRead()) {
						unreadMails++;
						mailBy.add(beerMail.getSenderName());
						mailLines.add(beerMail.getSomeContent(MAX_CONTENTLENGTH ));
						if (firstUnread == null) {
							firstUnread = beerMail;
						}
					}

				}

				// Look for deleted or updated mails
				if (allMails.getMails().size() > 0) {

					// Consider all mails within the time period of our last update
					Mail oldestInUpdate = allMails.getMails().get(allMails.getMails().size() - 1);
					List<BeerMail> existing = dao.queryBuilder().orderBy(BeerMail.MESSAGEID_FIELD_NAME, false).where()
							.gt(BeerMail.MESSAGEID_FIELD_NAME, oldestInUpdate.messageID).query();

					// See if these still existed in the last update
					for (BeerMail check : existing) {
						Mail present = null;
						for (Mail mail : allMails.getMails()) {
							if (mail.messageID == check.getMessageId()) {
								present = mail;
								break;
							}
						}
						if (present == null) {
							// Not available any longer: it was removed
							dao.delete(check);
						} else {
							// Update this entry
							if (!check.isRead()) // Never make a message 'unread'
								check.setIsRead(present.messageRead);
							check.setIsReplied(present.replied);
							dao.update(check);
						}
					}

				}

			} catch (SQLException e) {
				Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "Error saving beermail to database: " + e);
				// If requested, call back the messenger, i.e. the calling activity
				callbackMessenger(intent, RESULT_FAILURE);
				return;
			}

			// Create a notification about the new mails
			Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "User has " + unreadMails + " unread mails.");
			if (!intent.hasExtra(EXTRA_MESSENGER) && unreadMails > 0) {

				// Prepare senders text
				String newMailsText = getString(R.string.app_newmails, unreadMails);
				String mailByText = "";
				final int MAX_SENDERS = 3;
				for (int i = 0; i < mailBy.size() && i < MAX_SENDERS; i++) {
					mailByText += mailBy.get(i) + ", ";
				}
				mailByText = mailByText.substring(0, mailByText.length() - 2);
				if (mailBy.size() > MAX_SENDERS) {
					mailByText += getString(R.string.app_andothers);
				}
				
				// Set notification action target
				Intent contentIntent = new Intent(this, Home_.class);
				contentIntent.setAction(ACTION_VIEWBEERMAILS);

				// Retrieve user of some sender to show with the notification
				Bitmap avatar = null;
				try {
					avatar = BitmapFactory.decodeStream(apiConnection.getRaw("http://www.ratebeer.com/UserPics/"
							+ firstUnread.getSenderName() + ".jpg"));
				} catch (Exception e) {
					// Could not load? Just don't show an image 
				}
				
				// Build old style notification
				Builder builder = new NotificationCompat2.Builder(this).setSmallIcon(R.drawable.ic_stat_notification)
						.setTicker(getString(R.string.app_newmail)).setContentTitle(newMailsText)
						.setContentText(mailByText)
						.setContentIntent(PendingIntent.getActivity(this, 0, contentIntent, 0));
				if (avatar != null) {
					builder.setLargeIcon(avatar);
				}
				
				// Enrich notification with Jelly Bean inbox style
				InboxStyle inbox = new NotificationCompat2.InboxStyle(builder).setBigContentTitle(newMailsText);
				for (int i = 0; i < mailBy.size() && i < MAX_SENDERS; i++) {
					inbox.addLine(mailBy.get(i) + ": " + mailLines.get(i));
				}
				if (unreadMails - 3 > 0) {
					inbox.setSummaryText(getString(R.string.app_moremail, Integer.toString(unreadMails - 3)));
				}
				if (unreadMails == 1) {
					Intent replyIntent = new Intent(this, Home_.class);
					replyIntent.setAction(ACTION_REPLYBEERMAIL);
					replyIntent.putExtra(BeermailService.EXTRA_MAIL, firstUnread);
					builder.addAction(R.drawable.ic_stat_reply, getString(R.string.mail_reply),
							PendingIntent.getActivity(this, 0, replyIntent, 0));
					Intent viewIntent = new Intent(this, Home_.class);
					viewIntent.setAction(ACTION_VIEWBEERMAIL);
					viewIntent.putExtra(BeermailService.EXTRA_MAIL, firstUnread);
					builder.addAction(R.drawable.ic_stat_viewmail, getString(R.string.mail_view),
							PendingIntent.getActivity(this, 0, viewIntent, 0));
				}

				// Create notification, apply settings and release
				Notification notification = inbox.build();
				notification.flags |= Notification.FLAG_AUTO_CANCEL;
				if (applicationSettings.getVibrateOnNotification()) {
					notification.defaults = Notification.DEFAULT_VIBRATE;
				}
				notification.ledARGB = 0xff003366;
				notification.ledOnMS = 300;
				notification.ledOffMS = 1000;
				notification.flags |= Notification.FLAG_SHOW_LIGHTS;
				notificationManager.notify(NOTIFY_NEWBEERMAIL, notification);
				
			}

			// If requested, call back the messenger, i.e. the calling activity
			callbackMessenger(intent, RESULT_SUCCESS);

		} else {
			String e = result instanceof CommandFailureResult ? ((CommandFailureResult) result).getException()
					.toString() : "Unknown error";
			Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "Error retrieving new beer mails: " + e);
			// If requested, call back the messenger, i.e. the calling activity
			callbackMessenger(intent, RESULT_FAILURE);
		}

	}

	@SuppressWarnings("deprecation")
	private boolean isBackgroundDataDisabled() {
		if (android.os.Build.VERSION.SDK_INT >= 14) {
			// Note: getBackgroundDataSetting will always return true on API level 14 and up
			NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
			return ni == null || !ni.isAvailable() || !ni.isConnected();
		} else {
			return !connectivityManager.getBackgroundDataSetting();
		}
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
				Log.e(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME, "Cannot call back to activity to deliver message '" + msg.toString()
						+ "'");
			}
		}
	}

}
