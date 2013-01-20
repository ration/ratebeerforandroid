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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.InstanceState;
import com.googlecode.androidannotations.annotations.ViewById;
import com.ratebeer.android.R;
import com.ratebeer.android.app.persistance.BeerMail;
import com.ratebeer.android.gui.components.BeermailService;
import com.ratebeer.android.gui.components.PosterService;
import com.ratebeer.android.gui.components.RateBeerFragment;

@EFragment(R.layout.fragment_rate)
public class SendMailFragment extends RateBeerFragment {

	private static final String REPLY_SUBJECT_PREFIX = "Re: ";
	private static final String REPLY_BODY_PREFIX = "\n\nOriginal Message\n......................................................\n";

	@FragmentArg
	@InstanceState
	protected String sendToField = null;
	@FragmentArg
	@InstanceState
	protected String subjectField = null;
	@FragmentArg
	@InstanceState
	protected String bodyField = null;

	@ViewById(R.id.body)
	protected EditText sendtoEdit;
	@ViewById(R.id.subject)
	protected EditText subjectEdit;
	@ViewById(R.id.body)
	protected EditText bodyEdit;
	@ViewById(R.id.addrating)
	protected Button sendButton;
	@ViewById
	protected CheckBox includeoriginal;

	public SendMailFragment() {
	}

	/**
	 * Reply to an existing message
	 * @param extras A bundle that assumes a BeermailService.EXTRA_MAIL containing a BeerMail object to reply to
	 */
	public static SendMailFragment buildFromExtras(Bundle extras) {
		// Assume there is an extra containing the BeerMail object
		BeerMail replyTo = extras.getParcelable(BeermailService.EXTRA_MAIL);
		String subject = replyTo.getSubject();
		return buildConcrete(replyTo.getSenderName(), subject.startsWith(REPLY_SUBJECT_PREFIX) ? subject
				: REPLY_SUBJECT_PREFIX + subject, REPLY_BODY_PREFIX + replyTo.getBody());
	}

	/**
	 * Reply to an existing message
	 * @param sendTo The name of the beer to be rated
	 * @param subject The ID of the beer to be rated
	 */
	public static SendMailFragment buildFromExisting(String from, String subject, String originalMessage) {
		return buildConcrete(from, subject.startsWith(REPLY_SUBJECT_PREFIX) ? subject : REPLY_SUBJECT_PREFIX + subject,
				REPLY_BODY_PREFIX + originalMessage);
	}

	private static SendMailFragment buildConcrete(String sendTo, String subject, String body) {
		return SendMailFragment_.builder().sendToField(sendTo).subjectField(subject).bodyField(body).build();
	}
	
	@AfterViews
	public void init() {

		sendButton.setOnClickListener(onSendMail);

		// Fill fields from the message we are replying to
		if (sendToField != null) {
			sendtoEdit.setText(sendToField);
			sendToField = null;
		}
		if (subjectField != null) {
			subjectEdit.setText(subjectField);
			subjectField = null;
		}
		if (bodyField != null) {
			bodyEdit.setText(bodyField);
			bodyField = null;
		}
		
		// Is a reply? Then disable the 'include' checkbox
		includeoriginal.setVisibility(bodyField != null? View.VISIBLE: View.GONE);
			
	}

	private OnClickListener onSendMail = new OnClickListener() {
		@Override
		public void onClick(View v) {
			postRating();
		}
	};

	protected void postRating() {
		// Try to submit mail to the poster service
		String sendTo = sendtoEdit.getText().toString();
		String subject = subjectEdit.getText().toString();
		String body = bodyEdit.getText().toString();
		if (!sendTo.equals("") && !subject.equals("") && !body.equals("")) {

			// Include original text of message we are replying to?
			if (includeoriginal.isChecked() && bodyField != null) {
				body += bodyField;
			}
			
			// Use the poster service to send this new mail
			Intent i = new Intent(PosterService.ACTION_SENDMAIL);
			i.putExtra(PosterService.EXTRA_SENDTO, sendTo);
			i.putExtra(PosterService.EXTRA_SUBJECT, subject);
			i.putExtra(PosterService.EXTRA_BODY, body);
			getActivity().startService(i);

			// Close this fragment
			getFragmentManager().popBackStack();

		} else {
			publishException(null, getText(R.string.mail_emptyfields).toString());
		}
	}

}
