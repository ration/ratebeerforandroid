package com.ratebeer.android.api.command;

import java.io.File;

import android.os.Environment;
import android.test.AndroidTestCase;

import com.ratebeer.android.api.ApiConnection;
import com.ratebeer.android.api.ApiConnection_;
import com.ratebeer.android.api.CommandFailureResult;
import com.ratebeer.android.api.CommandResult;
import com.ratebeer.android.api.UserSettings;

public class UploadPhotoCommandsTest extends AndroidTestCase {

	public void testExecute() {

		ApiConnection apiConnection = ApiConnection_.getInstance_(getContext());
		UserSettings empty = TestHelper.getUser(getContext(), false);

		// Try to upload a local photo for 'La Frivole Blonde' (beer id 208093)
		File photo = new File(Environment.getExternalStorageDirectory().toString() + "/Download/RateBeerTest208093.jpg");
		UploadBeerPhotoCommand styleCommand = new UploadBeerPhotoCommand(empty, 208093, photo);
		CommandResult result = styleCommand.execute(apiConnection);
		if (result instanceof CommandFailureResult) {
			fail("Upload failed: " + ((CommandFailureResult) result).getException().toString());
		}

	}

}
