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
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.ratebeer.android.R;
import com.ratebeer.android.api.ApiMethod;
import com.ratebeer.android.api.CommandFailureResult;
import com.ratebeer.android.api.CommandSuccessResult;
import com.ratebeer.android.api.command.DeleteBeerMailCommand;
import com.ratebeer.android.app.persistance.BeerMail;
import com.ratebeer.android.gui.components.ArrayAdapter;
import com.ratebeer.android.gui.components.BeermailService;
import com.ratebeer.android.gui.components.RateBeerActivity;
import com.ratebeer.android.gui.components.RateBeerFragment;
import com.ratebeer.android.gui.fragments.ConfirmDialogFragment.OnDialogResult;

public class MailsFragment extends RateBeerFragment {

	private static final int MENU_SEND = 0;
	private static final int MENU_DELETE = 10;
	private static final int MENU_REPLY = 11;

	private LayoutInflater inflater;
	private TextView emptyText;
	private ListView mailsView;

	public MailsFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		this.inflater = inflater;
		return inflater.inflate(R.layout.fragment_mails, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		emptyText = (TextView) getView().findViewById(R.id.empty);
		mailsView = (ListView) getView().findViewById(R.id.mails);
		mailsView.setOnItemClickListener(onItemSelected);
		registerForContextMenu(mailsView);

		loadMails();

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		MenuItem item = menu.add(Menu.NONE, RateBeerActivity.MENU_REFRESH, RateBeerActivity.MENU_REFRESH,
				R.string.app_refresh);
		item.setIcon(R.drawable.ic_action_refresh);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		MenuItem item2 = menu.add(Menu.NONE, MENU_SEND, MENU_SEND, R.string.mail_sendmail);
		item2.setIcon(R.drawable.ic_action_add);
		item2.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case RateBeerActivity.MENU_REFRESH:
			refreshMails();
			break;
		case MENU_SEND:
			getRateBeerActivity().load(new SendMailFragment());
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void refreshMails() {
		// Start the background service to get new mail
		Intent i = new Intent(getActivity(), BeermailService.class);
		i.putExtra(BeermailService.EXTRA_MESSENGER, new Messenger(new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// Callback from the poster service; now reload the screen
				if (msg.arg1 == BeermailService.RESULT_SUCCESS) {
					loadMails();
					// Force the progress indicator to stop
					getRateBeerActivity().setProgress(false);
				} else if (msg.arg1 == BeermailService.RESULT_FAILURE) {
					publishException(emptyText, getString(R.string.mail_couldnotretrieve));
					// Force the progress indicator to stop
					getRateBeerActivity().setProgress(false);
				} else if (msg.arg1 == BeermailService.RESULT_STARTED) {
					// Force the progress indicator to start
					getRateBeerActivity().setProgress(true);
				}
			}
		}));
		getActivity().startService(i);
	}

	private void loadMails() {

		try {
			// Get mails from database
			List<BeerMail> result = getRateBeerActivity().getHelper().getBeerMailDao().queryBuilder()
					.orderBy(BeerMail.MESSAGEID_FIELD_NAME, false).query();

			// Show in list view
			if (mailsView.getAdapter() == null) {
				mailsView.setAdapter(new MailsAdapter(getActivity(), result));
			} else {
				((MailsAdapter) mailsView.getAdapter()).replace(result);
			}
			mailsView.setVisibility(result.size() == 0 ? View.GONE : View.VISIBLE);
			emptyText.setVisibility(result.size() == 0 ? View.VISIBLE : View.GONE);
		} catch (SQLException e) {
			// Not available!
			publishException(emptyText, getString(R.string.mail_notavailable));
			return;
		}

	}

	private OnItemClickListener onItemSelected = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			BeerMail item = ((MailsAdapter) mailsView.getAdapter()).getItem(position);
			getRateBeerActivity().load(new MailViewFragment(item));
		}
	};

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		menu.add(Menu.NONE, MENU_DELETE, MENU_DELETE, R.string.mail_delete);
		menu.add(Menu.NONE, MENU_REPLY, MENU_REPLY, R.string.mail_reply);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) item.getMenuInfo();
		final BeerMail mail = (BeerMail) mailsView.getItemAtPosition(acmi.position);
		switch (item.getItemId()) {
		case MENU_DELETE:
			// ASk to confirm the removal of this mail
			new ConfirmDialogFragment(new OnDialogResult() {
				@Override
				public void onConfirmed() {
					execute(new DeleteBeerMailCommand(getRateBeerActivity().getApi(), mail));
				}
			}, R.string.mail_confirmdelete, mail.getSenderName()).show(getSupportFragmentManager(), "dialog");
			break;
		case MENU_REPLY:
			// Start the mail reply screen
			getRateBeerActivity().load(new SendMailFragment(mail.getSenderName(), mail.getSubject(), mail.getBody()));
			break;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onTaskSuccessResult(CommandSuccessResult result) {
		if (result.getCommand().getMethod() == ApiMethod.DeleteBeerMail) {
			DeleteBeerMailCommand dbmCommand = (DeleteBeerMailCommand) result.getCommand();
			// Remove the mail from the database and refresh the screen
			try {
				getRateBeerActivity().getHelper().getBeerMailDao().delete(dbmCommand.getMail());
				loadMails();
			} catch (SQLException e) {
				publishException(null, getString(R.string.mail_notavailable));
			}
		}
	}

	@Override
	public void onTaskFailureResult(CommandFailureResult result) {
		publishException(emptyText, result.getException());
	}

	private static DateFormat dateFormat = null;
	private static CharSequence repliedString = null;
	private static CharSequence newString = null;
	private static CharSequence readString = null;

	private class MailsAdapter extends ArrayAdapter<BeerMail> {

		public MailsAdapter(Context context, List<BeerMail> objects) {
			super(context, objects);
			if (dateFormat == null) {
				dateFormat = android.text.format.DateFormat.getDateFormat(getActivity());
				repliedString = getString(R.string.mail_relpied);
				newString = getString(R.string.mail_new);
				readString = getString(R.string.mail_read);
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// Get the right view, using a ViewHolder
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.list_item_mail, null);
				holder = new ViewHolder();
				holder.subject = (TextView) convertView.findViewById(R.id.subject);
				holder.sender = (TextView) convertView.findViewById(R.id.sender);
				holder.status = (TextView) convertView.findViewById(R.id.status);
				holder.date = (TextView) convertView.findViewById(R.id.date);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			// Bind the data
			BeerMail item = getItem(position);
			holder.subject.setText(item.getSubject());
			// holder.subject.setTag(item);
			holder.sender.setText(item.getSenderName());
			if (item.getSent() != null) {
				holder.date.setText(dateFormat.format(item.getSent()));
			}
			if (item.isReplied()) {
				holder.status.setText(repliedString);
				holder.status.setBackgroundColor(getResources().getColor(R.color.BackgroundDark));
			} else if (item.isRead()) {
				holder.status.setText(readString);
				holder.status.setBackgroundColor(getResources().getColor(R.color.DarkGrey));
			} else {
				holder.status.setText(newString);
				holder.status.setBackgroundColor(getResources().getColor(R.color.Orange));
			}

			return convertView;
		}

	}

	protected static class ViewHolder {
		TextView subject, sender, status, date;
	}

}
