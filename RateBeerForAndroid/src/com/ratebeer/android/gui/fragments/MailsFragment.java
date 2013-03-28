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
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
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
import com.ratebeer.android.app.persistance.BeerMail;
import com.ratebeer.android.app.persistance.DatabaseHelper;
import com.ratebeer.android.gui.components.*;
import com.ratebeer.android.gui.components.helpers.ArrayAdapter;
import com.ratebeer.android.gui.fragments.ConfirmDialogFragment.OnDialogResult;

@EFragment(R.layout.fragment_mails)
@OptionsMenu({R.menu.refresh, R.menu.mails})
public class MailsFragment extends RateBeerFragment {

	private static final int MENU_DELETE = 10;
	private static final int MENU_REPLY = 11;

	@ViewById
	protected TextView empty;
	@ViewById
	protected ListView mails;

	@OrmLiteDao(helper = DatabaseHelper.class, model = BeerMail.class)
	Dao<BeerMail, Long> beerMailDao;
	
	public MailsFragment() {
	}

	@AfterViews
	public void init() {

		mails.setOnItemClickListener(onItemSelected);
		registerForContextMenu(mails);
		loadMails();

	}

	@OptionsItem(R.id.menu_sendmail)
	protected void onSendMail() {
		load(SendMailFragment_.builder().build());
	}


	@OptionsItem(R.id.menu_refresh)
	protected void refreshMails() {
		// Start the background service to get new mail
		Intent i = new Intent(getActivity(), BeermailService_.class);
		i.putExtra(BeermailService.EXTRA_MESSENGER, new Messenger(new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (getActivity() == null) {
					// No longer visible
					return;
				}
				// Callback from the poster service; now reload the screen
				RateBeerActivity ac = ((RateBeerActivity)getActivity());
				if (msg.arg1 == BeermailService.RESULT_SUCCESS) {
					loadMails();
					// Force the progress indicator to stop
					ac.setProgress(false);
				} else if (msg.arg1 == BeermailService.RESULT_FAILURE) {
					publishException(empty, getString(R.string.mail_couldnotretrieve));
					// Force the progress indicator to stop
					ac.setProgress(false);
				} else if (msg.arg1 == BeermailService.RESULT_STARTED) {
					// Force the progress indicator to start
					ac.setProgress(true);
				}
			}
		}));
		getActivity().startService(i);
	}

	private void loadMails() {

		try {			
			// Get mails from database
			List<BeerMail> result = beerMailDao.queryBuilder().orderBy(BeerMail.MESSAGEID_FIELD_NAME, false).query();

			// Show in list view
			if (mails.getAdapter() == null) {
				mails.setAdapter(new MailsAdapter(getActivity(), result));
			} else {
				((MailsAdapter) mails.getAdapter()).replace(result);
			}
			mails.setVisibility(result.size() == 0 ? View.GONE : View.VISIBLE);
			empty.setVisibility(result.size() == 0 ? View.VISIBLE : View.GONE);
		} catch (SQLException e) {
			// Not available!
			publishException(empty, getString(R.string.mail_notavailable));
			return;
		}

	}

	private OnItemClickListener onItemSelected = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			BeerMail item = ((MailsAdapter) mails.getAdapter()).getItem(position);
			load(MailViewFragment_.builder().mail(item).build());
		}
	};

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		menu.add(Menu.NONE, MENU_DELETE, MENU_DELETE, R.string.mail_delete);
		menu.add(Menu.NONE, MENU_REPLY, MENU_REPLY, R.string.mail_reply);
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) item.getMenuInfo();
		final BeerMail mail = (BeerMail) mails.getItemAtPosition(acmi.position);
		switch (item.getItemId()) {
		case MENU_DELETE:
			// ASk to confirm the removal of this mail
			new ConfirmDialogFragment(new OnDialogResult() {
				@Override
				public void onConfirmed() {
					execute(new DeleteBeerMailCommand(getUser(), mail));
				}
			}, R.string.mail_confirmdelete, mail.getSenderName()).show(getFragmentManager(), "dialog");
			break;
		case MENU_REPLY:
			// Start the mail reply screen
			load(SendMailFragment.buildReplyFromExisting(mail.getSenderName(), mail.getSubject(), mail.getBody()));
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
				beerMailDao.delete(dbmCommand.getMail());
				loadMails();
			} catch (SQLException e) {
				publishException(null, getString(R.string.mail_notavailable));
			}
		}
	}

	@Override
	public void onTaskFailureResult(CommandFailureResult result) {
		publishException(empty, result.getException());
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
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_mail, null);
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
