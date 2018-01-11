package de.tum.socialcomp.android;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.StatusCallback;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

import de.tum.socialcomp.android.sensor.LocationChangeListener;
import de.tum.socialcomp.android.sensor.OnLocationChangeInterface;
import de.tum.socialcomp.android.ui.CharacterSectionFragment;
import de.tum.socialcomp.android.ui.ClassSectionFragment;
import de.tum.socialcomp.android.ui.FacultySectionFragment;
import de.tum.socialcomp.android.ui.MainSectionFragment;
import de.tum.socialcomp.android.ui.MapSectionFragment;
import de.tum.socialcomp.android.ui.SettingsSectionFragment;
import de.tum.socialcomp.android.ui.SplashSectionFragment;
import de.tum.socialcomp.android.ui.StatisticsSectionFragment;
import de.tum.socialcomp.android.ui.TeamSectionFragment;
import de.tum.socialcomp.android.webservices.util.HttpPoster;
import de.tum.socialcomp.android.webservices.util.HttpGetter;

// facebook imports

/**
 * This is class represents the main activity of the game,
 * it manages the UI but also the game logic including
 * setting up services such as the Facebook login data
 * and the Google Cloud Messaging. 
 * 
 * To maneuver through the code, it is best to start with the
 * 	void onCreate(Bundle savedInstanceState) - method.
 * 
 * 
 * 
 * @author Niklas Klügel
 *
 * @author Michael Sailer
 * @author Jonas Mayer
 * @author Paul Preißner
 */

@SuppressLint({ "NewApi", "ValidFragment" })
public class MainActivity extends FragmentActivity {

	// Google Cloud Messaging / Play Service specifics
	private static final String EXTRA_MESSAGE = "message";
	private static final String PROPERTY_GCM_REG_ID = "GCMDeviceID";
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	// For SharedPreferences and GM & FacebookData
	private static final String PROPERTY_FACEBOOK_ID = "FacebookID";
	private static final String PROPERTY_FACEBOOK_NAME = "FacebookName";
	private static final String PROPERTY_APP_VERSION = "appVersion";

	/**
	 * Tag used on log messages.
	 */
	private static final String GCM_TAG = "GCM";
	private static final String FB_TAG = "Facebook";

	/**
	 * Shared attributes
	 */
	private GoogleCloudMessaging gcm;
	private SharedPreferences prefs;
	private Context context;

	private LocationManager locationManager;
	private LocationChangeListener locationChangeListener;
	public static Menu menu;

	private static MainActivity instance = null;

	/*Added for switching between fragments (maybe keeping them constant instead of newly creating them on each switch reduces perf loss? and keeps data in memory?
	one var for each fragment*/
	public static MapSectionFragment mapsectionfrag = new MapSectionFragment();
	private static TeamSectionFragment teamsectionfrag = new TeamSectionFragment();

	private static SettingsSectionFragment settingssectionfrag = new SettingsSectionFragment();
	private static FacultySectionFragment facultysectionfrag = new FacultySectionFragment();
	private static MainSectionFragment mainsectionfrag = new MainSectionFragment();
	private static StatisticsSectionFragment statssectionfrag = new StatisticsSectionFragment();
	private static CharacterSectionFragment charactersectionfrag = new CharacterSectionFragment();
	public static ClassSectionFragment classsectionfrag = new ClassSectionFragment();

	private static android.support.v4.app.Fragment currentFragment = mapsectionfrag;

	//fbID and name of this user
	public static String facebookID;
	public static String name;

	//array of faculties (info will be grabbed from server), and facultyID of this user
	public static int facultyID = -1; //should be -1
	public static int numberOfFaculties = 5;
	public static String[] allFaculties = new String[numberOfFaculties];

	//array of capture points/flags, struct to define what one such flag is (has a name, letter/ID, flag position and zone corner positions)
	public flagStruct[] capturePoints;

	public class flagStruct {
		public String letter;
		public String name;
		public double flagX;
		public double flagY;
		public ArrayList<Double> zoneX = new ArrayList<Double>();
		public ArrayList<Double> zoneY = new ArrayList<Double>();
	}

	//struct and array of skill classes (with which the user "constructs" their class
	public static int numberOfBaseClasses = 5;
	public static BaseClass[] baseClasses;

	public class BaseClass {
		public String name;
		public String description;
		int id;
	}

	public static String[][] superClasses;
	public static int[] chosenClassIDs = {-1, -1};

	public int initialHP;
	public int initialMaxHP;
	public int initialLevel;
	public int initialExp;

	public class Ability {
		int classID;
		public String name;
		public String description;
		public String cooldown;
	}

	public static Ability[] abilities;

	private boolean gotDataFromServer = false;
	private boolean loginSuccesfull = false;


	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// basic setup for application context
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		switchToFragment(new SplashSectionFragment());

		MainActivity.instance = this;
		context = getApplicationContext();

		// This logs the Keyhash used by Facebook for App development
		// (convenience)
		this.logKeyhash();

		/**********************************
		 *** The important parts of the initialization start here
		 ***
		 **********************************/

		/*****
		 *
		 * initializing the UserInterface ...
		 *
		 *****/
		Log.i(this.getClass().getName(), "Init LogView...");
		this.initLogView();

		/*****
		 *
		 * initializing the sensors ...
		 *
		 *****/
		Log.i(this.getClass().getName(), "Init sensors...");
		this.initLocationServices();

		/*****
		 *
		 * initializing the webservices ...
		 *
		 * Note: the initFacebookSessionAndLoginOnCallback method will not only
		 * open a Facebook session but it will also log in to our webservice
		 * once this session is (Asynchronously) established!
		 *
		 *****/
		Log.i(this.getClass().getName(), "Init GCM & FB Login...");
		this.initGoogleCloudMessaging();
		this.initFacebookSessionAndLoginOnCallback();
	}

	@Override
	public void onStart() {
		super.onStart();
		if (!gotDataFromServer) {
			getDataFromServer();
			gotDataFromServer = true;
		}

		// set user active
		new HttpPoster().execute(new String[]{
				"users", MainActivity.facebookID, "setActive"});
		Log.i(this.getClass().getName(), "User was set Active.");
	}

	private void getDataFromServer() {
		HttpGetter request;
		//Initializing baseClasses
		//grab array of base classes from server, assign to baseClasses array for later use
		Log.i(this.getClass().getName(), "Trying to GET baseClasses...");
		request = new HttpGetter();
		request.execute(new String[]{"data", "getBaseClasses"});
		try {
			String requestResult = request.get();
			// if we just received an empty json, ignore
			if (!requestResult.isEmpty() && !requestResult.equals("{ }")) {
				JSONArray json = new JSONArray(requestResult);
				MainActivity.numberOfBaseClasses = json.length();
				MainActivity.baseClasses = new BaseClass[numberOfBaseClasses];
				JSONObject baseClassJson;
				for (int i = 0; i < numberOfBaseClasses; i++) {
					baseClassJson = json.getJSONObject(i);
					baseClasses[i] = new BaseClass();
					baseClasses[i].name = baseClassJson.getString("name");
					baseClasses[i].description = baseClassJson.getString("description");
					baseClasses[i].id = baseClassJson.getInt("id");
				}
			}
		} catch (Exception e) {
			Log.e("BaseClasses Exception: ", e.getMessage());
		}

		// Initializing abilities
		Log.i(this.getClass().getName(), "Trying to GET abilities...");
		request = new HttpGetter();
		request.execute(new String[]{"data", "getAbilities"});
		try {
			String requestResult = request.get();
			// if we just received an empty json, ignore
			if (!requestResult.isEmpty() && !requestResult.equals("{ }")) {
				JSONArray jsonAbilities = new JSONArray(requestResult);
				int numberOfAbilities = jsonAbilities.length();
				MainActivity.abilities = new Ability[numberOfAbilities];
				JSONObject jsonAbility;
				for (int i = 0; i < numberOfAbilities; i++) {
					jsonAbility = jsonAbilities.getJSONObject(i);
					MainActivity.abilities[i] = new Ability();
					MainActivity.abilities[i].classID = jsonAbility.getInt("classID");
					MainActivity.abilities[i].name = jsonAbility.getString("name");
					MainActivity.abilities[i].description = jsonAbility.getString("description");
					MainActivity.abilities[i].cooldown = jsonAbility.getString("cooldown");
				}
			}
		} catch (Exception e) {
			Log.e("Abilities Exception: ", e.getMessage());
		}

		//Initializing superClasses
		//similar to baseClasses init
		Log.i(this.getClass().getName(), "Trying to GET superClasses...");
		request = new HttpGetter();
		request.execute(new String[]{"data", "getSuperClasses"});
		try {
			String requestResult = request.get();
			// if we just received an empty json, ignore
			if (!requestResult.isEmpty() && !requestResult.equals("{ }")) {
				JSONArray json = new JSONArray(requestResult);
				JSONObject superClassJson;
				JSONArray superClassArray;
				JSONObject superClassJson2;
				superClasses = new String[numberOfBaseClasses][numberOfBaseClasses];
				for (int i = 0; i < numberOfBaseClasses; i++) {
					superClassJson = json.getJSONObject(i);
					superClassArray = superClassJson.getJSONArray("Subclasses");
					for (int j = 0; j < numberOfBaseClasses; j++) {
						superClassJson2 = superClassArray.getJSONObject(j);
						superClasses[i][j] = superClassJson2.getString("name");
					}
				}
			}
		} catch (Exception e) {
			Log.e("SuperClass Exception: ", e.getMessage());
		}

		//Initializing facultyNames
		//get array from server, save into local array for later use
		Log.i(this.getClass().getName(), "Trying to GET allFaculties...");
		request = new HttpGetter();
		request.execute(new String[]{"data", "faculties"});
		try {
			String requestResult = request.get();
			// if we just received an empty json, ignore
			if (!requestResult.isEmpty() && !requestResult.equals("{ }")) {
				JSONArray json = new JSONArray(requestResult);
				MainActivity.numberOfFaculties = json.length();
				for (int i = 0; i < numberOfFaculties; i++) {
					allFaculties[i] = json.getJSONObject(i).getString("name");
				}
			}
		} catch (Exception e) {
			Log.e("AllFacs Exception: ", e.getMessage());
		}

		//Initializing flag info
		//grab array of flag info from server, assign to flag structs in local flag array
		Log.i(this.getClass().getName(), "Trying to GET CapturePoints...");
		request = new HttpGetter();
		request.execute(new String[]{"data", "getCapturePoints"});
		try {
			String requestResult = request.get();
			// if we just received an empty json, ignore
			if (!requestResult.isEmpty() && !requestResult.equals("{ }")) {
				JSONObject json = new JSONObject(requestResult);
				int nCP = 0;
				JSONArray Zone;
				nCP = json.getInt("nCapturePoints");
				capturePoints = new flagStruct[nCP];
				JSONArray capPtsJson = json.getJSONArray("CapturePoints");
				for (int i = 0; i < nCP; i++) {
					JSONObject capPt = capPtsJson.getJSONObject(i);
					flagStruct flag = new flagStruct();

					flag.letter = capPt.getString("letter");
					flag.name = capPt.getString("name");
					flag.flagX = capPt.getJSONObject("flag").getDouble("x");
					flag.flagY = capPt.getJSONObject("flag").getDouble("y");

					Zone = capPt.getJSONArray("zone");
					for (int j = 0; j < Zone.length(); j++) {
						flag.zoneX.add(Zone.getJSONObject(j).getDouble("x"));
						flag.zoneY.add(Zone.getJSONObject(j).getDouble("y"));
					}
					capturePoints[i] = flag;
				}
			}
		} catch (Exception e) {
			Log.e("CapPoints Exception: ", e.getMessage());
		}

		if (this.loginSuccesfull) {
			getFacultyAndPerformFragmentSwitch();
		}
	}

	/*called after facebook login has been completed
	* logs the player in on the server with their unique facebookID and current position*/
	private void serverLogin() {
		Location lastLocation = locationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		double latitude;
		double longitude;
		if (lastLocation != null) {
			latitude = lastLocation.getLatitude();
			longitude = lastLocation.getLongitude();
		} else {
			latitude = Configuration.DefaultLatitude;
			longitude = Configuration.DefaultLongitude;
		}
		String firstName;
		if (MainActivity.name.indexOf(" ") > 0) {
			firstName = MainActivity.name.substring(0, MainActivity.name.indexOf(" "));
		} else {
			/*Can this really happen??*/
			firstName = "Default";
		}

		MainActivity.name = firstName;

		Log.i(this.getClass().getName(), "Logging in on Server...");
		new HttpPoster().execute(new String[]{"users", firstName,
				MainActivity.facebookID,
				"" + latitude,
				"" + longitude, "login"});

		this.loginSuccesfull = true;

		if (this.gotDataFromServer) {
			getFacultyAndPerformFragmentSwitch();
		}
	}

	/* is called from whoever finishes last of serverLogin() and getDataFromServer()
	* this should make sure that everything has been done before switching fragments
	* everything being: assigning the player a faculty if not done already, grabbing their class and ability info, hp etc
	 */
	private void getFacultyAndPerformFragmentSwitch() {
		HttpGetter request;

		/*getAllData from Server*/
		Log.i(this.getClass().getName(), "Trying to getAllData");
		try {
			request = new HttpGetter();
			request.execute(new String[]{"users", MainActivity.facebookID, "getAllData"});
			String requestResult = request.get();
			if (!requestResult.isEmpty() && !requestResult.equals("{ }")) {
				JSONObject jsonStats = new JSONObject(requestResult);
				// get status
				JSONObject jsonStatus = jsonStats.getJSONObject("Status");
				initialMaxHP = jsonStatus.getInt("maxhp");
				initialHP = jsonStatus.getInt("hp");
				initialLevel = jsonStatus.getInt("level");
				initialExp = jsonStatus.getInt("ExP");

				JSONObject jsonClass = jsonStats.getJSONObject("Class");
				chosenClassIDs[0] = jsonClass.getInt("class1");
				chosenClassIDs[1] = jsonClass.getInt("class2");
			}
		} catch (Exception e) {
			Log.e("getAllData Exception: ", e.getMessage());
		}

		//here, check whether the player has a legit faculty locally assigned,
		//if not, grab their faculty as known to the server
		if (MainActivity.facultyID == -1) {
			Log.i(this.getClass().getName(), "Trying GET getFaculty...");
			request = new HttpGetter();
			request.execute(new String[]{"users", this.getFacebookID(context), "getFaculty"});
			try {
				String requestResult = request.get();
				// if we just received an empty json, ignore
				if (!requestResult.isEmpty() && !requestResult.equals("{ }")) {
					JSONObject json = new JSONObject(requestResult);
					MainActivity.facultyID = json.getInt("id");
				}
			} catch (Exception e) {
				Log.e("Faculty Exception: ", e.getMessage());
			}
			Log.i(this.getClass().getName(), "faculty = " + MainActivity.facultyID);
		}

		//if no faculty info for players is found on server either, open the faculty fragment so the player can set one
		Log.i(this.getClass().getName(), "Switching fragment...");
		if (MainActivity.facultyID <= -1) {
			getActionBar().setTitle("Select a faculty");
			switchToFragment(facultysectionfrag);
		} else {
			// if the player hasnt chosen a class -> class screen
			if (chosenClassIDs[0] == -1 && chosenClassIDs[1] == -1) {
				getActionBar().setTitle("Choose your Class");
				switchToFragment(classsectionfrag);
			} else { // else -> map screen
				getActionBar().setTitle("Live Map");
				switchToFragment(mapsectionfrag);
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		checkPlayServices();

		/*upThread = new Thread(runnable);
		upThread.start();*/
	}

	@Override
	public void onPause() {
		super.onPause();

		// wait until thread has terminated
		/*if(upThread != null) {
			try {
				upThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}*/
	}

	@Override
	public void onStop() {
		super.onStop();

		// perform "logout" -> set status on inActive
		new HttpPoster().execute(new String[]{
				"users", MainActivity.facebookID, "setInactive"});
		Log.i(this.getClass().getName(), "User was set INactive.");
	}

	/**
	 * This is simply used to show the key hash that is used by
	 * Facebook to identify the developer's devices.
	 * Otherwise the application *WILL NOT* log in since the
	 * Facebook application is not verified to run on this device.
	 * <p/>
	 * This is a convenience function; the keyhash can also be acquired
	 * as shown in the Facebook introductory tutorial:
	 * https://developers.facebook.com/docs/android/getting-started/
	 */
	private void logKeyhash() {
		// log the key hash so it can be pasted to the facebook developer
		// console
		try {

			PackageInfo info = getPackageManager().getPackageInfo(
					"de.tum.socialcomp.android", PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				Log.d("KEYHASH",
						Base64.encodeToString(md.digest(), Base64.DEFAULT));
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

	}

	public static MainActivity getInstance() {
		return instance;
	}

	// this is used to show a timestamp
	private long startTime = 0L;

	public void initLogView() {
		startTime = System.currentTimeMillis();

	}

	/**
	 * Shows a simple log message on the MainSectionFragment.
	 * This is used to show that the application logged in.
	 *
	 * @param logMessage
	 */
	public void showLogMessage(final String logMessage) {
		MainActivity.instance.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				TextView welcome = (TextView) MainActivity.instance
						.findViewById(R.id.welcome);
				if (welcome != null) { //added in this check because with pagination disabled, this method would make the app crash since "welcome" is part of MainSectionFragment, which is not created without pagination
					welcome.setText(welcome.getText()
							+ "\n"
							+ (System.currentTimeMillis() - MainActivity.instance.startTime)
							/ 1000 + ": " + logMessage);
				}
			}
		});
	}


	/**
	 * Initializes the location based services; when the location has changed
	 * a heartbeat signal is sent to the webservice updating the user's location
	 * in the database.
	 */
	private void initLocationServices() {

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationChangeListener = new LocationChangeListener();

		// When the location has changed, then send an update to the webservice
		locationChangeListener
				.setOnLoctationChangedListener(new OnLocationChangeInterface() {
					@Override
					public void locationChanged(Location loc) {
						String facebookID;
						facebookID = MainActivity.this
								.getFacebookID(getBaseContext());

						if (!facebookID.isEmpty()) {
							new HttpPoster().execute(new String[]{
									"users", facebookID,
									loc.getLatitude() + "",
									loc.getLongitude() + "", "updatePosition"});
						}

					}
				});

		// Use both, gps and network, on the cost of battery drain. But this way
		// we are likely to get some localization information in most cases.
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			//requestLocationUpdates (String provider, long minTime, float minDistance, LocationListener listener)
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 1,
					Configuration.MinimumDistanceForLocationUpdates,
					locationChangeListener);
		} else {
			Log.e(this.getClass().getName(), "Pos: GPS not available!");
		}

		if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			locationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 1,
					Configuration.MinimumDistanceForLocationUpdates,
					locationChangeListener);
		} else {
			Log.e(this.getClass().getName(), "Pos: Net. Provider not available!");
		}

	}

	/**
	 * This requests a new device ID for Google Cloud Messaging and stores
	 * it in the SharedPreferences, otherwise, if already requested it takes
	 * the stored one. Using this device ID the webservice can send Push
	 * Messages to this device.
	 * <p/>
	 * This *REQUIRES* the Google Play Services APK to be installed.
	 * <p/>
	 * The code for this initialization procedure is directly taken from:
	 * http://developer.android.com/reference/com/google/android/gms/gcm/GoogleCloudMessaging.html
	 */
	private void initGoogleCloudMessaging() {
		// Check device for Play Services APK. If check succeeds, proceed with
		// GCM registration.
		if (checkPlayServices()) {
			gcm = GoogleCloudMessaging.getInstance(this);
			String regId = getGoogleCloudMessagingRegistrationID(context);

			if (regId.isEmpty()) {
				registerGoogleCloudMessagingInBackground();
			}
		} else {
			Log.e(GCM_TAG, "No valid Google Play Services APK found.");
		}
	}

	/**
	 * This method logs into Facebook. When the application is attempting to
	 * log in for the first time, it will open a dialog whether the user accepts
	 * the application accessing Facebook user data.
	 * <p/>
	 * Once a a Facebook session has been established the application will log
	 * into our webservice and send:
	 * - the facebook authentication token (this will be used by the webservice to access the users facebook data)
	 * - the Google Cloud Messaging ID (this is used to contact the device from the webservice)
	 * - the user's location
	 * <p/>
	 * Since establishing the Facebook session is an asynchronous task (because of the involved network communication),
	 * a callback object is used to trigger the processing once this is operation is successful.
	 * Here the log in to our webservice is finally performed.
	 * <p/>
	 * The Facebook session login is directly based on the examples:
	 * https://developers.facebook.com/docs/getting-started/facebook-sdk-for-android-using-android-studio/3.0/
	 */
	private void initFacebookSessionAndLoginOnCallback() {
		Log.i(this.getClass().getName(), "Trying to log in to Facebook...");

		// start Facebook Login		

		final StatusCallback callback = new Session.StatusCallback() {

			// callback when session changes state
			@Override
			public void call(Session session, SessionState state,
							 Exception exception) {

				Log.e("FACEBOOK", " session exception => " + exception
						+ " session state " + state);

				if (session.isOpened()) {

					/**
					 * This gives an example of how to access the facebook graph directly from the Facebook
					 * Android SDK; in this case we simply request the user's facebook name and display it
					 * as log message on the MainActivityFragment.
					 */
					Request.executeMeRequestAsync(session,
							new Request.GraphUserCallback() {
								// callback after Graph API response with user
								// object
								@Override
								public void onCompleted(GraphUser user,
														Response response) {
									if (user != null) {
										// check whether we have already saved
										// the facebook credentials
										if (getFacebookID(context).isEmpty()) {
											storeFacebookIDAndName(
													user.getId(),
													user.getName());
										}

										/*Initialize facbookID and name*/
										MainActivity.facebookID = MainActivity.getInstance().getFacebookID(context);
										MainActivity.name = MainActivity.getInstance().getFacebookName(context);

										/*now the facebbok login has been completed and we have access to
										* the facebbokID and name*/
										serverLogin();
									}
								}

							});
				}
			}
		};

		final Session.OpenRequest request = new Session.OpenRequest(this);

		// request special permissions for the app, in our case we want to get
		// the user's friends (who are also using the application)

		request.setPermissions(Arrays.asList("user_friends"));

		request.setCallback(callback);

		Session session = Session.getActiveSession();

		if (session == null || session.isClosed()) {
			session = new Session(this);
		}

		if (!session.isOpened()) {
			session.openForRead(request);
		}

		Session.setActiveSession(session);

	}

	/**
	 * ***********************************************
	 * The following methods are simply helper methods
	 * necessary to store/retrieve preferences or
	 * to support the registration of the Google Cloud Messaging
	 * services or to alter the UI.
	 * *************************************************
	 * ************************************************
	 */

	//basically deprecated now
	void showDialog(final Dialog dia) {
		dia.show();
	}

	private SharedPreferences getSharedPreferences(Context context) {
		// This sample app persists the registration ID in shared preferences,
		return getSharedPreferences(MainActivity.class.getSimpleName(),
				Context.MODE_PRIVATE);
	}

	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	private void registerGoogleCloudMessagingInBackground() {
		Log.i("GCM", "Registering..");
		new AsyncTask() {
			@Override
			protected String doInBackground(Object... params) {
				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(context);
					}
					String regId = gcm.register(Configuration.GoogleCloudMessagingSenderID);
					msg = ">>Device registered, registration ID=" + regId;
					storeGoogleCloudMessagingDeviceID(context, regId);
				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
					// If there is an error, don't just keep trying to register.
					// Require the user to click a button again, or perform
					// exponential back-off.
				}
				return msg;
			}

		}.execute(null, null, null);
	}

	private void storeGoogleCloudMessagingDeviceID(Context context, String regId) {
		final SharedPreferences prefs = getSharedPreferences(context);
		int appVersion = getAppVersion(context);
		Log.i(GCM_TAG, "Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_GCM_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}

	private String getGoogleCloudMessagingRegistrationID(Context context) {
		final SharedPreferences prefs = getSharedPreferences(context);
		String registrationId = prefs.getString(PROPERTY_GCM_REG_ID, "");
		if (registrationId.isEmpty()) {
			Log.i(GCM_TAG, "Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
				Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			Log.i(GCM_TAG, "App version changed.");
			return "";
		}
		return registrationId;
	}

	private void storeFacebookIDAndName(String facebookID, String name) {
		final SharedPreferences prefs = getSharedPreferences(context);
		int appVersion = getAppVersion(context);

		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_FACEBOOK_ID, facebookID);
		editor.putString(PROPERTY_FACEBOOK_NAME, name);
		editor.commit();
	}

	public String getFacebookID(Context context) {
		String ret = "";

		final SharedPreferences prefs = getSharedPreferences(context);
		String facebookID = prefs.getString(PROPERTY_FACEBOOK_ID, "");
		if (facebookID.isEmpty()) {
			Log.i(FB_TAG, "Facebook ID not found.");
			return "";
		} else {
			ret = facebookID;
		}

		return ret;
	}

	public String getFacebookName(Context context) {
		String ret = "";

		final SharedPreferences prefs = getSharedPreferences(context);
		String facebookName = prefs.getString(PROPERTY_FACEBOOK_NAME, "");
		if (facebookName.isEmpty()) {
			Log.i(FB_TAG, "Facebook ID not found.");
			return "";
		} else {
			ret = facebookName;
		}

		return ret;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.misc, menu);
		MainActivity.menu = menu;

		return true;
	}

	/**
	 * manages taps/clicks in dropdown menu/drawer
	 * determines which option/item was tapped on, then switches to the corresponding fragment
	 *
	 * @param item specifies which dropdown option was tapped on
	 * @return whether tap was successful (and fragment switch initiated)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		/*dont allow someone to switch out of the faculty fragment manually.
		* The fragment is switched automatically once they have selected a faculty.*/
		if (currentFragment != facultysectionfrag && item != null) {
			Log.i("Selected Item: ", item.toString());
			int id = item.getItemId();
			switch (id) {
				case R.id.action_map:
					//switch to Map fragment
					getActionBar().setTitle("Live Map");
					currentFragment = mapsectionfrag;
					break;
				case R.id.action_team:
					//switch to Team fragment
					currentFragment = teamsectionfrag;
					getActionBar().setTitle("Team Scoreboard");
					break;
				case R.id.action_settings:
					currentFragment = settingssectionfrag;
					getActionBar().setTitle("Credits");
					break;
				case R.id.action_stats:
					//switch to Stats fragment
					currentFragment = statssectionfrag;
					getActionBar().setTitle("Statistics");
					break;
				case R.id.action_character:
					//switch to Character fragment
					currentFragment = charactersectionfrag;
					getActionBar().setTitle("My Character");
					break;
				case R.id.action_zoom:
					mapsectionfrag.zoomOut();
					MenuItem zoom = menu.findItem(R.id.action_zoom);
					if (mapsectionfrag.zoomedout)
						zoom.setIcon(R.drawable.ic_zoom_in);
					else
						zoom.setIcon(R.drawable.ic_zoom_out);
					break;
				case R.id.action_attack:
					mapsectionfrag.attack();
					break;
				case R.id.ability1:
					mapsectionfrag.callAbility(1);
					break;
				case R.id.ability2:
					mapsectionfrag.callAbility(2);
					break;
				default:
					break;
			}
			getSupportFragmentManager().beginTransaction().replace(R.id.MainFrameLayout, currentFragment).commit();
			return true;
		}
		return false;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (Session.getActiveSession() != null && resultCode == RESULT_OK) {
			Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
		} else {
			Log.e(FB_TAG, "Failed to open session!");
		}
	}

	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.i(GCM_TAG, "This device is not supported.");
				finish();
			}
			return false;
		}
		return true;
	}

	public void setActionBarTitle(String title) {
		getActionBar().setTitle(title);
	}

	public void switchToFragment(android.support.v4.app.Fragment frag) {
		currentFragment = frag;
		//switches to the specified fragment
		getSupportFragmentManager().beginTransaction().replace(R.id.MainFrameLayout, frag).commit();
	}


// upThread not used because of LocationChangeListener

	/*upload-Thread similar to MapSection downThread
	* to upload player position to server at set frequency
	* */
	Thread upThread;
	long milliWaitUP = 1500;

	Runnable runnable = new Runnable() {
		public void run() {
			while (true) {
				if(loginSuccesfull) {
					Location lastLocation = null;
					if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
						lastLocation = locationManager
								.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
					} else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
						lastLocation = locationManager
								.getLastKnownLocation(LocationManager.GPS_PROVIDER);
					}

					if (lastLocation != null) {
						// if the phone has never been located the last location
						// can be 0, use a default location
						new HttpPoster().execute(new String[]{"users",
								facebookID,
								"" + lastLocation.getLatitude(),
								"" + lastLocation.getLongitude(), "updatePosition"});
					} else {
						new HttpPoster().execute(new String[]{"users",
								facebookID,
								"" + Configuration.DefaultLatitude,
								"" + Configuration.DefaultLongitude, "updatePosition"});
					}
					Log.i(this.getClass().getName(), "Sent position data to GameServer");
				}

				synchronized (this) {
					try {
						wait(milliWaitUP);
					} catch (Exception e) {
					}
				}
			}
		}
	};

}