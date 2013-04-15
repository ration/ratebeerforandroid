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
import android.widget.EditText;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.InstanceState;
import com.googlecode.androidannotations.annotations.ViewById;
import com.ratebeer.android.R;
import com.ratebeer.android.app.persistance.BeerMail;
import com.ratebeer.android.gui.components.BeermailService;
import com.ratebeer.android.gui.components.PosterService;
import com.ratebeer.android.gui.components.RateBeerFragment;

@EFragment(R.layout.fragment_sendmail)
public class SendMailFragment extends RateBeerFragment {

	@FragmentArg
	@InstanceState
	protected String sendToField = null;
	@FragmentArg
	@InstanceState
	protected String subjectField = null;
	@FragmentArg
	@InstanceState
	protected String bodyField = null;
	@FragmentArg
	@InstanceState
	protected int replyToField = PosterService.NO_REPLY_EXTRA;
	@FragmentArg
	@InstanceState
	protected int recipientField = PosterService.NO_REPLY_EXTRA;

	@ViewById(R.id.sendtolabel)
	protected TextView sendtoLabel;
	@ViewById(R.id.sendto)
	protected EditText sendtoEdit;
	@ViewById(R.id.subjectlabel)
	protected TextView subjectLabel;
	@ViewById(R.id.subject)
	protected EditText subjectEdit;
	@ViewById(R.id.body)
	protected EditText bodyEdit;

	public SendMailFragment() {
	}

	/**
	 * Reply to an existing message
	 * @param extras A bundle that assumes a BeermailService.EXTRA_MAIL containing a BeerMail object to reply to
	 */
	public static SendMailFragment buildReplyFromExtras(Bundle extras) {
		// Assume there is an extra containing the BeerMail object
		return buildReplyFromExisting((BeerMail) extras.getParcelable(BeermailService.EXTRA_MAIL));
	}

	/**
	 * Recover from the failure of sending a beermail
	 * @param extras A bundle that assumes extras with PosterService.EXTRA_SENDTO, PosterService.EXTRA_SUBJECT,
	 *            PosterService.EXTRA_BODY and PosterService.EXTRA_REPLYTO
	 */
	public static SendMailFragment buildFromFailedSend(Bundle extras) {
		// Assume the extras contain all required fields
		return buildConcrete(extras.getString(PosterService.EXTRA_SENDTO),
				extras.getString(PosterService.EXTRA_SUBJECT), extras.getString(PosterService.EXTRA_BODY),
				extras.getInt(PosterService.EXTRA_REPLYTO), extras.getInt(PosterService.EXTRA_RECIPIENT));
	}

	/**
	 * Reply to an existing message
	 * @param sendTo The name of the beer to be rated
	 * @param subject The ID of the beer to be rated
	 */
	public static SendMailFragment buildReplyFromExisting(BeerMail replyTo) {
		return buildConcrete(replyTo.getSenderName(), replyTo.getSubject(), null, replyTo.getMessageId(),
				replyTo.getSenderId());
	}

	private static SendMailFragment buildConcrete(String sendTo, String subject, String body, int replyTo, int recipient) {
		return SendMailFragment_.builder().sendToField(sendTo).subjectField(subject).bodyField(body)
				.recipientField(recipient).replyToField(replyTo).build();
	}

	@AfterViews
	public void init() {

		// Fill fields from the message we are replying to
		// (The fields are then set to null to prevent them from overriding new text entry on an orientation change)
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
		
		// If this is a reply then do disable the 'send to' view, as that cannot actually be changed
		if (replyToField != PosterService.NO_REPLY_EXTRA && recipientField != PosterService.NO_REPLY_EXTRA) {
			sendtoLabel.setVisibility(View.GONE);
			sendtoEdit.setVisibility(View.GONE);
			subjectLabel.setVisibility(View.GONE);
			subjectEdit.setVisibility(View.GONE);
		}

	}

	@Click(R.id.send)
	protected void postRating() {
		// Try to submit mail to the poster service
		String sendTo = sendtoEdit.getText().toString();
		String subject = subjectEdit.getText().toString();
		String body = bodyEdit.getText().toString();
		if (!sendTo.equals("") && !subject.equals("") && !body.equals("")) {

			// Use the poster service to send this new mail
			Intent i = new Intent(PosterService.ACTION_SENDMAIL);
			i.putExtra(PosterService.EXTRA_SENDTO, sendTo);
			i.putExtra(PosterService.EXTRA_SUBJECT, subject);
			i.putExtra(PosterService.EXTRA_BODY, body);
			i.putExtra(PosterService.EXTRA_REPLYTO, replyToField);
			i.putExtra(PosterService.EXTRA_RECIPIENT, recipientField);
			getActivity().startService(i);

			// Close this fragment
			getFragmentManager().popBackStack();

		} else {
			publishException(null, getText(R.string.mail_emptyfields).toString());
		}
	}

}
