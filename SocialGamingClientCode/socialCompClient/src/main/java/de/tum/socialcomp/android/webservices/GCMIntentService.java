package de.tum.socialcomp.android.webservices;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import de.tum.socialcomp.android.MainActivity;

/**
 * This class is used to dispatch messages that have been received
 * via the Google Cloud Messaging Service. This indend service
 * will then redirect messages back to the MainActivity
 * where the logic of most reactions is implemented.
 * 
 * @author Niklas Klügel
 *
 */
public class GCMIntentService extends IntentService {

	public GCMIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		

	        String action = intent.getAction();
	        if (action.equals("com.google.android.c2dm.intent.REGISTRATION")) {
	            handleRegistration(intent);
	        } else if (action.equals("com.google.android.c2dm.intent.RECEIVE")) {
	            handleMessage(intent);
	        }
		
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent); 
        //GCMBroadcastReceiver.completeWakefulIntent(intent);
		
	}

	private void handleMessage(Intent intent) {

		try {
			JSONObject gcmMessage = new JSONObject(intent.getStringExtra("message"));

			Log.v("Received GCM message", gcmMessage.toString());

			if(gcmMessage.has("type")){
				if(gcmMessage.getString("type").equals("game")){

					/*Idea: leave this as is, any Push messages from the server shall bear the type "game" (how-to must be somewhere in initial server code)
					* forward any game relevant message to Main activity, then in .receivedGameMessage() evaluate JSON object of message
					* (could be used for: HP loss/gain, changes to flags (owners/captures), game end messages etc)*/

					/*receivedGameMessage was removed from MainActivity along with a bunch of other methods
					* that we dont use anymore*/
					//MainActivity.getInstance().receivedGameMessage(gcmMessage);

				} else if(gcmMessage.getString("type").equals("server")){
					MainActivity.getInstance().showLogMessage("Server (GCM): "+gcmMessage.getString("subtype"));
				}

			}


		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void handleRegistration(Intent intent) {
		Log.v(this.getClass().getName(), "registered");
		
	}
	
}
