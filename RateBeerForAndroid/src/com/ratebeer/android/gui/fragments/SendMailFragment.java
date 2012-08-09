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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.ratebeer.android.R;
import com.ratebeer.android.app.persistance.BeerMail;
import com.ratebeer.android.gui.components.BeermailService;
import com.ratebeer.android.gui.components.PosterService;
import com.ratebeer.android.gui.components.RateBeerFragment;

public class SendMailFragment extends RateBeerFragment {

	private static final String STATE_BODY = "body";
	private static final String REPLY_SUBJECT_PREFIX = "Re: ";
	private static final String REPLY_BODY_PREFIX = "\n\nOriginal Message\n......................................................\n";

	private EditText sendtoEdit, subjectEdit, bodyEdit;
	private Button sendButton;
	private CheckBox includeoriginalBox;

	private String sendToField = null;
	private String subjectField = null;
	private String bodyField = null;

	public SendMailFragment() {
	}

	/**
	 * Reply to an existing message
	 * @param extras A bundle that assumes a BeermailService.EXTRA_MAIL containing a BeerMail object to reply to
	 */
	public SendMailFragment(Bundle extras) {
		// Assume there is an extra containing the BeerMail object
		BeerMail replyTo = extras.getParcelable(BeermailService.EXTRA_MAIL);
		String subject = replyTo.getSubject();
		this.sendToField = replyTo.getSenderName();
		this.subjectField = subject .startsWith(REPLY_SUBJECT_PREFIX)? subject: REPLY_SUBJECT_PREFIX + subject;
		this.bodyField = REPLY_BODY_PREFIX + replyTo.getBody();
	}

	/**
	 * Reply to an existing message
	 * @param sendTo The name of the beer to be rated
	 * @param subject The ID of the beer to be rated
	 */
	public SendMailFragment(String from, String subject, String originalMessage) {
		this.sendToField = from;
		this.subjectField = subject.startsWith(REPLY_SUBJECT_PREFIX)? subject: REPLY_SUBJECT_PREFIX + subject;
		this.bodyField = REPLY_BODY_PREFIX + originalMessage;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_sendmail, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// Initialize fields
		sendtoEdit = (EditText) getView().findViewById(R.id.sendto);
		subjectEdit = (EditText) getView().findViewById(R.id.subject);
		bodyEdit = (EditText) getView().findViewById(R.id.body);
		sendButton = (Button) getView().findViewById(R.id.send);
		includeoriginalBox = (CheckBox) getView().findViewById(R.id.includeoriginal);
		sendButton.setOnClickListener(onSendMail);

		// Load state (i.e. on orientation changes)
		if (savedInstanceState != null) {
			bodyField = savedInstanceState.getString(STATE_BODY);
		}

		// Fill fields from the message we are replying to
		if (savedInstanceState == null && sendToField != null) {
			sendtoEdit.setText(sendToField);
			subjectEdit.setText(subjectField);
			sendToField = null;
			subjectField = null;
		}
		
		// Is a reply? Then disable the 'include' checkbox
		includeoriginalBox.setVisibility(bodyField != null? View.VISIBLE: View.GONE);
			
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_BODY, bodyField);
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
			if (includeoriginalBox.isChecked() && bodyField != null) {
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
