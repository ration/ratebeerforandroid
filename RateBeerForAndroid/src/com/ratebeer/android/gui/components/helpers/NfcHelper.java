package com.ratebeer.android.gui.components.helpers;

import android.annotation.TargetApi;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.ratebeer.android.R;

public class NfcHelper {

	private SherlockFragmentActivity activity;

	public NfcHelper(SherlockFragmentActivity activity) {
		this.activity = activity;
	}

	/**
	 * If the device is new enough and supports/has enabled NFC, allow NFC message exchange through Android Beam.
	 */
	@TargetApi(14)
	public void startNfc() {
		if (android.os.Build.VERSION.SDK_INT >= 14) {
			NfcAdapter nfc = NfcAdapter.getDefaultAdapter(activity);
			if (nfc != null)
				nfc.setNdefPushMessageCallback(onCreateNfcMessage, activity);
		}

	}

	/**
	 * Marshals the request to create a message (to send to the now-nearby device) to the currently shown Fragment.
	 */
	private CreateNdefMessageCallback onCreateNfcMessage = new CreateNdefMessageCallback() {
		@TargetApi(14)
		@Override
		public NdefMessage createNdefMessage(NfcEvent event) {
			// A device is close; ask the currently visible fragment to create a message
			Fragment content = activity.getSupportFragmentManager().findFragmentById(R.id.frag_content);
			if (content != null && content instanceof NdefUriProvider)
				return new NdefMessage(new NdefRecord[] { NdefRecord.createUri(((NdefUriProvider) content)
						.createNdefUri()) });
			return null;
		}
	};

	public interface NdefUriProvider {
		public Uri createNdefUri();
	}
}
