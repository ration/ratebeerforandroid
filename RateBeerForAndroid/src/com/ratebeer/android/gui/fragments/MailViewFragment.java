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
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.ratebeer.android.R;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.CommandFailureResult;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.api.command.DeleteBeerMailCommand;
import com.ratebeer.android.app.persistance.BeerMail;
import com.ratebeer.android.gui.components.BeermailService;
import com.ratebeer.android.gui.components.RateBeerFragment;
import com.ratebeer.android.gui.fragments.ConfirmDialogFragment.OnDialogResult;

public class MailViewFragment extends RateBeerFragment {

	private static final String STATE_MAIL = "mail";

	private static final int MENU_REPLY = 0;
	private static final int MENU_DELETE = 1;

	private TextView subjectText, bodyText;
	private Button senderText;
	private static DateFormat dateFormat = null;

	private BeerMail mail;

	public MailViewFragment() {
		this.mail = null;
	}

	/**
	 * Show the details of some mail
	 * @param mail The mail object to show the details of
	 */
	public MailViewFragment(BeerMail mail) {
		this.mail = mail;
	}

	public MailViewFragment(Bundle extras) {
		// Assume there is an extra containing the BeerMail object
		this.mail = extras.getParcelable(BeermailService.EXTRA_MAIL);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_mailview, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		subjectText = (TextView) getView().findViewById(R.id.subject);
		bodyText = (TextView) getView().findViewById(R.id.body);
		senderText = (Button) getView().findViewById(R.id.sender);
		senderText.setOnClickListener(onSenderClick);

		if (savedInstanceState != null) {
			mail = savedInstanceState.getParcelable(STATE_MAIL);
		}

		if (dateFormat == null) {
			dateFormat = android.text.format.DateFormat.getDateFormat(getActivity());
		}

		if (mail != null) {
			// Load mail details into the widgets
			subjectText.setText(mail.getSubject());
			senderText.setText(getString(R.string.app_by, mail.getSenderName()) + mail.getSent() != null && 
					dateFormat != null? "\n" + dateFormat.format(mail.getSent()): "");
			bodyText.setMovementMethod(LinkMovementMethod.getInstance());
			try {
				bodyText.setText(Html.fromHtml(mail.getBody().replace("\n", "<br />")));
			} catch (Exception e) {
				// This can happen if the mail is very long, in which case Html.fromHtml throws a RuntimeException
				// As a fallback, don't parse the body as HTML but print the plain text instead
				bodyText.setText(mail.getBody().replace("<br />", "\n"));
			}
			
			// Also set this mail to read (which should already be done on the server by now)
			mail.setIsRead(true);
		}

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		MenuItem item2 = menu.add(Menu.NONE, MENU_REPLY, MENU_REPLY, R.string.mail_reply);
		item2.setIcon(R.drawable.ic_action_reply);
		item2.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		MenuItem item3 = menu.add(Menu.NONE, MENU_DELETE, MENU_DELETE, R.string.mail_delete);
		item3.setIcon(R.drawable.ic_action_delete);
		item3.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_REPLY:
			getRateBeerActivity().load(new SendMailFragment(mail.getSenderName(), mail.getSubject(), mail.getBody()));
			break;
		case MENU_DELETE:
			// ASk to confirm the removal of this mail
			new ConfirmDialogFragment(new OnDialogResult() {
				@Override
				public void onConfirmed() {
					if (getRateBeerActivity() != null) {
						execute(new DeleteBeerMailCommand(getRateBeerActivity().getApi(), mail));
					}
				}
			}, R.string.mail_confirmdelete, mail.getSenderName()).show(getSupportFragmentManager(), "dialog");
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(STATE_MAIL, mail);
	}

	private OnClickListener onSenderClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			getRateBeerActivity().load(new UserViewFragment(mail.getSenderName(), mail.getSenderId()));
		}
	};

	@Override
	public void onTaskSuccessResult(CommandSuccessResult result) {
		if (result.getCommand().getMethod() == ApiMethod.DeleteBeerMail) {
			DeleteBeerMailCommand dbmCommand = (DeleteBeerMailCommand) result.getCommand();
			// Remove the mail from the database and close the screen
			try {
				getRateBeerActivity().getHelper().getBeerMailDao().delete(dbmCommand.getMail());
				getSupportFragmentManager().popBackStack();
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
