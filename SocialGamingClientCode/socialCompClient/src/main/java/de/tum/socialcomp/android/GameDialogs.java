package de.tum.socialcomp.android;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;

import org.json.JSONException;
import org.json.JSONObject;

import de.tum.socialcomp.android.webservices.util.HttpPoster;

/**
 * This class is used for convenience to create
 * the various Dialogs that are shown within the app; 
 * these are mostly triggered by external events.
 * 
 * @author Niklas Klügel
 *
 * @author Paul Preißner
 */

public class GameDialogs {

	/**
	 * generic dialog that simply displays the given message
	 *
	 * @param message
	 * @param act
	 * @return
	 */
	public static Dialog createGenericDialog(final CharSequence message, final MainActivity act) {

		AlertDialog.Builder builder = new AlertDialog.Builder(act);
		Dialog abortDialog = builder
			.setMessage(message)
				.setPositiveButton("Alrighty!", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// do nothing for now
					}
			})
			.create();

		return abortDialog;
	}
}
