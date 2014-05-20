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
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.commonsware.cwac.merge.MergeAdapter;
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
import com.ratebeer.android.api.command.GetBeerMailPartCommand;
import com.ratebeer.android.api.command.GetBeerMailPartCommand.MailPart;
import com.ratebeer.android.app.persistance.BeerMail;
import com.ratebeer.android.app.persistance.DatabaseHelper;
import com.ratebeer.android.gui.components.BeermailService;
import com.ratebeer.android.gui.components.RateBeerFragment;
import com.ratebeer.android.gui.components.helpers.ArrayAdapter;
import com.ratebeer.android.gui.fragments.ConfirmDialogFragment.OnDialogResult;

@EFragment(R.layout.fragment_mailview)
@OptionsMenu({ R.menu.refresh, R.menu.mailview })
public class MailViewFragment extends RateBeerFragment {

	@FragmentArg
	@InstanceState
	protected BeerMail mail;

	@ViewById
	protected ListView parts;
	private TextView originalBody;
	private MailPartAdapter repliesAdapter;
	private DateFormat dateFormat;

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

		if (mail == null)
			return;

		dateFormat = android.text.format.DateFormat.getDateFormat(getActivity());
		parts.setAdapter(new MailViewAdapter());
		parts.setItemsCanFocus(true);

		// If the body text was already loaded earlier, then it was stored in the database and we can show it now
		if (mail.getBody() != null) {
			updateBodyText();
		}
		// Load the body text (original message) from the server, even when it was stored because this call also markes
		// the message as read
		execute(new GetBeerMailPartCommand(getUser(), mail.getMessageId(), false));
		// Always retrieve the replies from the server; these are not stored in the database
		execute(new GetBeerMailPartCommand(getUser(), mail.getMessageId(), true));

		// Also set this mail to read (which should already be done on the server by now)
		mail.setIsRead(true);
		try {
			beerMailDao.update(mail);
		} catch (SQLException e) {
			Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME,
					"Cannot write to database; wanted to save the read status of beer mail " + mail.getMessageId());
		}

	}

	@OptionsItem(R.id.menu_reply)
	protected void onReply() {
		load(SendMailFragment_.buildReplyFromExisting(mail));
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

	@OptionsItem(R.id.menu_refresh)
	protected void onRefreshScreen() {
		// See if there are new replies to this beer mail
		execute(new GetBeerMailPartCommand(getUser(), mail.getMessageId(), true));
	}

	protected OnClickListener onSenderClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			load(UserViewFragment_.builder().userId(mail.getSenderId()).userName(mail.getSenderName()).build());
		}
	};

	private void updateBodyText() {
		originalBody.setMovementMethod(LinkMovementMethod.getInstance());
		try {
			originalBody.setText(Html.fromHtml(mail.getBody().replace("\n", "<br />")));
		} catch (Exception e) {
			// This can happen if the mail is very long, in which case Html.fromHtml throws a RuntimeException
			// As a fallback, don't parse the body as HTML but print the plain text instead
			originalBody.setText(mail.getBody().replace("<br />", "\n"));
		}
	}

	private void updateReplies(List<MailPart> parts) {
		repliesAdapter.replace(parts);
	}

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
		} else if (result.getCommand().getMethod() == ApiMethod.GetBeerMailPart) {
			GetBeerMailPartCommand gbmpCommand = (GetBeerMailPartCommand) result.getCommand();
			if (gbmpCommand.fetchReplies()) {
				// We performed this command to get the replies to an original beer mail
				updateReplies(gbmpCommand.getParts());
			} else {
				// We performed this command to get the mail body text (the first message)
				try {
					// Update the database and show the received body text
					if (gbmpCommand.getParts().size() == 1)
						mail.updateBody(gbmpCommand.getParts().get(0).body);
					beerMailDao.update(mail);
					updateBodyText();
				} catch (SQLException e) {
					Log.d(com.ratebeer.android.gui.components.helpers.Log.LOG_NAME,
							"Cannot write to database; wanted to save the body text of beer mail "
									+ mail.getMessageId());
				}
			}
		}
	}

	@Override
	public void onTaskFailureResult(CommandFailureResult result) {
		publishException(null, result.getException());
	}

	private class MailViewAdapter extends MergeAdapter {

		public MailViewAdapter() {

			// Add the mail header
			TextView subjectText = (TextView) getActivity().getLayoutInflater().inflate(R.layout.list_item_mailheader,
					null);
			addView(subjectText);
			subjectText.setText(mail.getSubject());

			// Add the original, first message and show the sender text on its button
			View original = getActivity().getLayoutInflater().inflate(R.layout.list_item_mailpart, null);
			addView(original);
			Button originalSender = (Button) original.findViewById(R.id.sender);
			originalBody = (TextView) original.findViewById(R.id.body);
			String dateText = "";
			try {
				if (mail.getSent() != null) {
					dateText = "\n" + dateFormat.format(mail.getSent());
				}
			} catch (Exception e) {
				// Cannot format date; ignore and don't show instead
			}
			originalSender.setText(getString(R.string.app_by, mail.getSenderName()) + dateText);
			originalSender.setOnClickListener(onSenderClick);

			// Set the list of replies
			repliesAdapter = new MailPartAdapter(getActivity(), new ArrayList<MailPart>());
			addAdapter(repliesAdapter);
		}

	}

	private class MailPartAdapter extends ArrayAdapter<MailPart> {

		public MailPartAdapter(Context context, List<MailPart> objects) {
			super(context, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// Get the right view, using a ViewHolder
			ViewHolder holder;
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_mailpart, null);
				holder = new ViewHolder();
				holder.sender = (TextView) convertView.findViewById(R.id.sender);
				holder.body = (TextView) convertView.findViewById(R.id.body);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			// Bind the data
			final MailPart item = getItem(position);
			if (getActivity() != null) {
				String dateText = "";
				try {
					if (mail.getSent() != null) {
						dateText = "\n" + dateFormat.format(item.sent);
					}
				} catch (Exception e) {
					// Cannot format date; ignore and don't show instead
				}
				holder.sender.setText(getString(R.string.app_by, item.userName) + dateText);
				holder.body.setText(item.body);
				holder.sender.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						load(UserViewFragment_.builder().userId(item.sourceUserId).userName(item.userName)
								.build());
					}
				});
			}

			return convertView;
		}

	}

	protected static class ViewHolder {
		TextView sender, body;
	}

}
