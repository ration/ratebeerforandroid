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
package com.ratebeer.android.gui.fragments;

import java.sql.SQLException;
import java.text.DateFormat;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.InstanceState;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.OrmLiteDao;
import com.googlecode.androidannotations.annotations.ViewById;
import com.j256.ormlite.dao.Dao;
import com.ratebeer.android.R;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.CommandFailureResult;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.api.command.DeleteBeerMailCommand;
import com.ratebeer.android.app.RateBeerForAndroid;
import com.ratebeer.android.app.persistance.BeerMail;
import com.ratebeer.android.app.persistance.DatabaseHelper;
import com.ratebeer.android.gui.components.BeermailService;
import com.ratebeer.android.gui.components.RateBeerFragment;
import com.ratebeer.android.gui.fragments.ConfirmDialogFragment.OnDialogResult;

@EFragment(R.layout.fragment_mailview)
@OptionsMenu(R.menu.mailview)
public class MailViewFragment extends RateBeerFragment {

	private static DateFormat dateFormat = null;

	@FragmentArg
	@InstanceState
	protected BeerMail mail;

	@ViewById
	protected TextView subject, body;
	protected Button sender;

	@OrmLiteDao(helper = DatabaseHelper.class, model = BeerMail.class)
	Dao<BeerMail, Long> beerMailDao;
	
	public MailViewFragment() {
	}

	public static MailViewFragment buildFromExtras(Bundle extras) {
		// Assume there is an extra containing the BeerMail object
		return MailViewFragment_.builder().mail((BeerMail) extras.getParcelable(BeermailService.EXTRA_MAIL)).build();
	}

	@AfterViews
	public void init() {

		sender.setOnClickListener(onSenderClick);

		if (dateFormat == null) {
			dateFormat = android.text.format.DateFormat.getDateFormat(getActivity());
		}

		if (mail != null) {
			// Load mail details into the widgets
			subject.setText(mail.getSubject());
			String dateText = "";
			try {
				if (mail.getSent() != null) {
					dateText = "\n" + dateFormat.format(mail.getSent());
				}
			} catch (Exception e) {
				// Cannot format date; ignore and don't show instead
			}
			sender.setText(getString(R.string.app_by, mail.getSenderName()) + dateText);
			body.setMovementMethod(LinkMovementMethod.getInstance());
			try {
				body.setText(Html.fromHtml(mail.getBody().replace("\n", "<br />")));
			} catch (Exception e) {
				// This can happen if the mail is very long, in which case Html.fromHtml throws a RuntimeException
				// As a fallback, don't parse the body as HTML but print the plain text instead
				body.setText(mail.getBody().replace("<br />", "\n"));
			}
			
			// Also set this mail to read (which should already be done on the server by now)
			mail.setIsRead(true);
			try {
				beerMailDao.update(mail);
			} catch (SQLException e) {
				Log.d(RateBeerForAndroid.LOG_NAME,
						"Cannot write to database; wanted to save the read status of beer mail " + mail.getMessageId());
			}
		}

	}

	@OptionsItem(R.id.menu_reply)
	protected void onReply() {
		load(SendMailFragment_.buildFromExisting(mail.getSenderName(), mail.getSubject(), mail.getBody()));
	}

	@OptionsItem(R.id.menu_delete)
	protected void onDelete() {
		// ASk to confirm the removal of this mail
		new ConfirmDialogFragment(new OnDialogResult() {
			@Override
			public void onConfirmed() {
				if (getActivity() != null) {
					execute(new DeleteBeerMailCommand(getUser(), mail));
				}
			}
		}, R.string.mail_confirmdelete, mail.getSenderName()).show(getFragmentManager(), "dialog");
	}
	
	private OnClickListener onSenderClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			load(UserViewFragment_.builder().userId(mail.getSenderId()).userName(mail.getSenderName()).build());
		}
	};

	@Override
	public void onTaskSuccessResult(CommandSuccessResult result) {
		if (result.getCommand().getMethod() == ApiMethod.DeleteBeerMail) {
			DeleteBeerMailCommand dbmCommand = (DeleteBeerMailCommand) result.getCommand();
			// Remove the mail from the database and close the screen
			try {
				beerMailDao.delete(dbmCommand.getMail());
				getFragmentManager().popBackStack();
			} catch (SQLException e) {
				publishException(null, getString(R.string.mail_notavailable));
			}
		}
	}

	@Override
	public void onTaskFailureResult(CommandFailureResult result) {
		publishException(null, result.getException());
	}

}
