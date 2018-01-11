package de.tum.socialcomp.android.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import de.tum.socialcomp.android.Configuration;
import de.tum.socialcomp.android.GameDialogs;
import de.tum.socialcomp.android.MainActivity;
import de.tum.socialcomp.android.R;
import de.tum.socialcomp.android.webservices.util.HttpGetter;
import de.tum.socialcomp.android.webservices.util.HttpPoster;

/**
 * This Fragment shows the Map View for the game
 * 
 * The map Google Maps.
 * 
 * Most of the code is directly based on the examples given at:
 *
 * There are two additional functions:
 * - When another user is tapped on the map the name is shown in an info window.
 * - When performed a tap on the other user's info window he/she can
 * be poked (a dialog will appear).
 *
 * @author Niklas Klügel, Fabian Kloos
 *
 * @author Michael Sailer, Jonas Mayer, Paul Preißner
 * 
 */

public class MapSectionFragment extends Fragment {

	View rootView;

	private GoogleMap map;

	static Marker _myLocation;
	Location location;
	LocationManager locationManager;
	String locationProvider = LocationManager.GPS_PROVIDER ;//NETWORK_PROVIDER;

	static boolean initialized = false;
	public static MapSectionFragment instance;

	static ArrayList<PlayerMarker> player_markers = new ArrayList<PlayerMarker>();
	static Marker[] flag_markers = new Marker[8];
	static Polygon[] flagpolys = new Polygon[8];
	private final static int markersize_player =40;
	private final static int markersize_flags =60;
	TeamPolygons teamPolygons;

	private int currentHP = 0;
	private int oldHP;
	private int maxHP = 0;
	private int level = 1;
	private int exp = 0;
	private int oldExp;
	private int expToNext;
	private String status = "";
	private static boolean dead = false;
	private boolean levelUp = false;

	Bitmap self, tombstone;
	Bitmap[] playerBitmaps = new Bitmap[5]; // used to be Bitmap teamIN, teamCH, teamMW, teamMA, teamPH;

	/*ability_cooldown[0] == true -> ability1 is not available because of cooldown
	* ability_cooldown[1] == true -> ability2 is not available because of cooldown*/
	private boolean[] abilty_cooldown = {false,false};

	// ids of the standard icons for the abilities; are set in onStart()
	private int[] abilty_icon = new int[2];

	Faculty faculty = new Faculty();

	// waiting time between updates
	long milliWait = 1000;
	Thread downThread;

	/* true when fragment switch is about to be executed
	* So the updateThread knows that he has to terminate*/
	public static boolean fragmentSwitch;

    //Zoomfactor for centralMapOnMyLocation
    private final static float ZOOM = 18f;
	public boolean zoomedout = false;


	class PlayerMarker
	{
		public Marker marker;
		public String ID;

		public PlayerMarker(Marker marker, String ID)
		{
			this.marker = marker;
			this.ID = ID;
		}
	}

	/*
	* on Start, initialize five arrays with a set number of offscreen faculty markers each,
	* plus five class wide indices
	* on update, just update positions of markers depending on what faculty a player is
	* if array markers are not sufficient, what then? cant just add markers. array of "various additional"?
	* to update frequently: new thread on Start or Resume (plus yield accordingly), in thread, call updatePlayersInVicinity method through public static reference to map fragment
	* -> thus also remove the updateFlags method from updatePlayersInVicinity, since map isnt cleared anymore in each update
	* UPDATE: only using one list now instead of five arrays*/

	Runnable runnable = new Runnable() {
		public void run() {
			int i = 0;
			// run as long there is no fragment switch
			while(!fragmentSwitch) {
				//grab players around this user and current flag status/info from server
				final JSONArray jsonUsers = updatePlayersInVicinity();
				final JSONArray jsonFlags = updateFlags();
				final JSONObject atkMeshes = updateAttackMeshes();

				if(jsonUsers == null || jsonFlags == null){
					continue;
				}

				//if we're "allowed" to update (frequency/refreshrate wise) and are not zoomed out,
				// draw currently most recent player and flag info on the UI
				// otherwise just draw updated flag info
				if(i==0 && !zoomedout) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if(atkMeshes != null)
								teamPolygons.set(atkMeshes);
							teamPolygons.update();
							updatePlayerMarker();
							updateDraw(jsonUsers);
							updateDrawFlags(jsonFlags);
							centerMapOnMyLocation();
						}
					});
				}
				else{
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							updateDraw(jsonUsers);
							updateDrawFlags(jsonFlags);
						}
					});
				}

				/*get character hp values and level from Server*/
				Log.i(this.getClass().getName(), "Trying to get HP from Server");
				try {
					HttpGetter request = new HttpGetter();
					request.execute(new String[]{"users", MainActivity.facebookID, "getAllData"});
					String requestResult = request.get();
					if (!requestResult.isEmpty() && !requestResult.equals("{ }")) {
						JSONObject jsonStats = new JSONObject(requestResult);
						// get status
						final JSONObject jsonStatus = jsonStats.getJSONObject("Status");
						maxHP     = jsonStatus.getInt("maxhp");
						currentHP = jsonStatus.getInt("hp");
						exp       = jsonStatus.getInt("ExP");
						expToNext = jsonStatus.getInt("ExPToNext");
						// temporary variable to compare new and old level to see if there has been a levelUp
						int lev = jsonStatus.getInt("level");
						levelUp = lev > level;
						level = lev;

						// Returns the DEAD status so we know for sure when a player died.
						status = jsonStatus.getString("status");

						final JSONObject jsonClass = jsonStats.getJSONObject("Class");
						int cooldown1 = jsonClass.getInt("cooldown1");
						int cooldown2 = jsonClass.getInt("cooldown2");

						abilty_cooldown[0] = cooldown1 > 0 ? true : false;
						abilty_cooldown[1] = cooldown2 > 0 ? true : false;

						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								updateStatus();
								// this sets the abilities visible if the level is high enough
								setAbilitiesVisible(level);
							}
						});
					}
				}catch(Exception e){
					Log.e("getAllData Exception: ", e.getMessage());
				}

				//technically supposed to wait the rate specified in settings (settings not used in the current version)
				synchronized (this) {
					try {
						wait(milliWait);
					} catch (Exception e) {
					}
				}
				i++;
				i=i%((int)milliWait/1000*2);
			}
		}
	};

	/**
	 *
	 * @param level
	 */
	private void setAbilitiesVisible(int level){
		// Set abilities visible if available
		if(level >= 4) {
			MainActivity.menu.getItem(2).setVisible(true);
			// change icon in case of cooldown
			setAbilityIcon(1);
		}
		if(level >= 7) {
			MainActivity.menu.getItem(3).setVisible(true);
			setAbilityIcon(2);
		}
	}

	/**This method sets the abiltyIcon of the abilty specified by abilityNumber (1 or 2).
	 * If the abilty has a cooldown, we set the default icon.
	 * If the abilty is available, we set the standard icon for the abilty.
	 *
	 *
 	 * @param abilityNumber
	 */
	private void setAbilityIcon(int abilityNumber){
		if(this.abilty_cooldown[abilityNumber -1]){
			MainActivity.menu.getItem(abilityNumber+1).setIcon(R.drawable.ability_default);
		}else{
			MainActivity.menu.getItem(abilityNumber + 1).setIcon(abilty_icon[abilityNumber-1]);
		}
	}

	//init functions
	//Map is created and map data applied
	private void createMapView(){
		try{
			SupportMapFragment mapFrag = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

			map = mapFrag.getMap();
			teamPolygons = new TeamPolygons(map);
			UiSettings settings = map.getUiSettings();
			settings.setCompassEnabled(false);
			settings.setRotateGesturesEnabled(false);
			settings.setZoomGesturesEnabled(true);
			//settings.setAllGesturesEnabled(false);

			map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
													   @Override
													   public boolean onMyLocationButtonClick() {
														   centerMapOnMyLocation();
														   updatePlayerMarker();
														   return true;
													   }
												   }
			);
			// disable default centerMap-Button
			settings.setMyLocationButtonEnabled(false);

			// enable Scrolling
			map.getUiSettings().setScrollGesturesEnabled(true);

			// remove default-marker
			map.setMyLocationEnabled(false);

		} catch (NullPointerException exception) {
			Log.e("mapApp", exception.toString());
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.fragment_section_map,
				container, false);
		MainActivity.menu.clear();

		MainActivity.getInstance().getMenuInflater().inflate(R.menu.main, MainActivity.menu);
		instance = this;

		// set abilities invisible as default
		MainActivity.menu.getItem(2).setVisible(false);
		MainActivity.menu.getItem(3).setVisible(false);

		return rootView;
	}

	/**
	 *
	 * @param abilityNumber
	 */
	public void callAbility(int abilityNumber)
	{
		if(dead) {
			Toast.makeText(MainActivity.getInstance().getApplicationContext(), "You are dead and cannot cast any abilities.\n"
							+ this.currentHP + "/" + this.maxHP,
					Toast.LENGTH_SHORT).show();
			return;
		}

		if(abilityNumber != 1 && abilityNumber != 2){
			Log.v(this.getClass().getName(), "Invalid abilityNumber: " + abilityNumber + " (Should be 1 or 2)");
			return;
		}

		// tells the server to fire the specified ability
		HttpGetter request = new HttpGetter();
		request.execute(new String[]{"users", MainActivity.facebookID, "" + abilityNumber, "callAbility"});
		try {
			String requestResult = request.get();
			// if we just received an empty json, ignore
			if (!requestResult.isEmpty() && !requestResult.equals("{ }")) {
				JSONObject jsonCooldown = new JSONObject(requestResult);
				int cooldown = -1;
				boolean fired = jsonCooldown.getBoolean("fired");
				if(abilityNumber == 1) {
					cooldown = jsonCooldown.getInt("cooldown1");
				}else if (abilityNumber == 2){
					cooldown = jsonCooldown.getInt("cooldown2");
				}
				if(fired) {
					// pop-up_message with name and cooldown of the called ability
					Toast.makeText(MainActivity.getInstance().getApplicationContext(), "Called ability " +
							MainActivity.abilities[MainActivity.chosenClassIDs[abilityNumber - 1]].name + "\n" +
							"Cooldown: " + cooldown + " seconds", Toast.LENGTH_SHORT).show();
					//TODO: maybe replace this toast with one specifying the buff that is now active
				}else{
					Toast.makeText(MainActivity.getInstance().getApplicationContext(), "Cannot call ability " +
							MainActivity.abilities[MainActivity.chosenClassIDs[abilityNumber - 1]].name + " yet\n" +
							"Need to wait for cooldown: " + cooldown + " seconds left", Toast.LENGTH_SHORT).show();
				}
			}
		} catch (Exception e) {
			Log.e("CallAbility Exception: ", e.getMessage());
		}

		Log.i(this.getClass().getName(), "Called Ability: " +
				MainActivity.abilities[MainActivity.chosenClassIDs[abilityNumber - 1]].name);

	}

	//signal the server that this user is right now in position for attacking
	public void attack() {
		getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                centerMapOnMyLocation();
				new HttpPoster().execute(new String[]{"users", MainActivity.facebookID, "attack"});
            }
        });

		AttackWaitThread = new Thread(atkRunnable);
		AttackWaitThread.start();
	}

	/** simple thread to disable the atk button for 10 seconds upon pressing
	 * */
	Thread AttackWaitThread;
	public static boolean AttackCoolingDown = false;

	Runnable atkRunnable = new Runnable() {
		public void run() {
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					MainActivity.menu.getItem(1).setIcon(R.drawable.ability_default);
					MainActivity.menu.getItem(1).setEnabled(false);
					AttackCoolingDown = true;
				}
			});

			synchronized (this) {
				try {
					wait(10000); //wait 10 seconds
				} catch (Exception e) {
				}
			}

			if(!MapSectionFragment.fragmentSwitch) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						MainActivity.menu.getItem(1).setIcon(R.drawable.ic_attack);
						MainActivity.menu.getItem(1).setEnabled(true);

					}
				});
			}
			MapSectionFragment.AttackCoolingDown = false;
		}
	};

	//set the bitmaps for player markers (most notably determine which is your faculty, thus the only friendly markers/bitmaps to be used)
	public void initBitmaps() {

		/*FacultyID definition in server code:
		* 0 Informatik
		* 1 Chemie
		* 2 Maschinenbau
		* 3 Physik
		* 4 Mathematik
		* */

		playerBitmaps[0] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.in_enemy), markersize_player, markersize_player, true);
		playerBitmaps[1] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ch_enemy), markersize_player, markersize_player, true);
		playerBitmaps[2] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.mw_enemy), markersize_player, markersize_player, true);
		playerBitmaps[3] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ph_enemy), markersize_player, markersize_player, true);
		playerBitmaps[4] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ma_enemy), markersize_player, markersize_player, true);


		switch (MainActivity.facultyID) {
			case 0:  self = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.in_player), markersize_player, markersize_player, true);
				playerBitmaps[0] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.in_team), markersize_player, markersize_player, true);
				break;
			case 1:  self = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ch_player), markersize_player, markersize_player, true);
				playerBitmaps[1] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ch_team),  markersize_player, markersize_player, true);
				break;
			case 2:  self = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.mw_player), markersize_player, markersize_player, true);
				playerBitmaps[2] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.mw_team), markersize_player, markersize_player, true);
				break;
			case 3:  self = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ph_player), markersize_player, markersize_player, true);
				playerBitmaps[3] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ph_team), markersize_player, markersize_player, true);
				break;
			case 4:  self = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ma_player), markersize_player, markersize_player, true);
				playerBitmaps[4] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ma_team), markersize_player, markersize_player, true);
				break;
			default: self = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.feggit), markersize_player*2, markersize_player*2, true);
				break;
		}

		tombstone = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.tombstone), markersize_player*2, markersize_player*2, true);
	}

	//set the marker for this user
	private void initPlayerMarker() {
		updateLocation();
		try {
			// set default values for lat/lng (MI HS1)
			double latitude  = Configuration.DefaultLatitude;
			double longitude = Configuration.DefaultLongitude;
			// if we have location data use that instead
			if (location != null) {
				latitude  = location.getLatitude();
				longitude = location.getLongitude();
			}
			// if necessary remove old marker
			if(_myLocation!=null) {
				_myLocation.remove();
			}
			// initialize new marker
			_myLocation = map.addMarker(new MarkerOptions()
					.position(new LatLng(latitude, longitude))
					.title("You")
					.snippet("are here")
					.icon(BitmapDescriptorFactory.fromBitmap((!dead ? self : tombstone)))
					.anchor(0.5f, 0.5f));
		} catch (Exception e) {
			System.err.println("init player - set loc marker failed: " + e.getMessage());
		}
	}

	//initializes flag bitmaps, flag markers and polygons as grabbed from the server during app startup
	public void initFlags() {
		/*what we do is:
		* 	define bitmaps for flags a to h
		* 	for each flag in the list we received from the server, determine which bitmap is needed (see letter)
		*	add the flag marker
		*	for each value in the coordinate lists, add a node to the polygonOptions, then add the polygon*/
		Bitmap[] flagMarkerBitmaps = new Bitmap[8];
		flagMarkerBitmaps[0] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.point_a), markersize_flags, markersize_flags, true);
		flagMarkerBitmaps[1] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.point_b), markersize_flags, markersize_flags, true);
		flagMarkerBitmaps[2] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.point_c), markersize_flags, markersize_flags, true);
		flagMarkerBitmaps[3] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.point_d), markersize_flags, markersize_flags, true);
		flagMarkerBitmaps[4] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.point_e), markersize_flags, markersize_flags, true);
		flagMarkerBitmaps[5] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.point_f), markersize_flags, markersize_flags, true);
		flagMarkerBitmaps[6] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.point_g), markersize_flags, markersize_flags, true);
		flagMarkerBitmaps[7] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.point_h), markersize_flags, markersize_flags, true);

		MainActivity mainAct = MainActivity.getInstance();
		for(int i =0; i<8;i++) {
			MainActivity.flagStruct flag = mainAct.capturePoints[i];

			flag_markers[i]=map.addMarker(new MarkerOptions()
					.position(new LatLng(flag.flagX, flag.flagY))
					.title(flag.name)
					.snippet("Free")
					.icon(BitmapDescriptorFactory.fromBitmap(flagMarkerBitmaps[i]))
					.anchor(0.5f, 0.5f));

			PolygonOptions polOps = new PolygonOptions();
			int j = 0;
			for(double coor : flag.zoneX) {
				polOps.add(new LatLng(flag.zoneX.get(j), flag.zoneY.get(j)));
				j++;
			}
			flagpolys[i]= map.addPolygon(polOps
					.strokeColor(R.color.TUM_blue_trans)
					.fillColor(R.color.TUM_blue_light_trans));
		}

		for(Polygon polygon : flagpolys) {
			if(polygon !=null) {
				polygon.setFillColor(getResources().getColor(R.color.TUM_blue_light_trans));
				polygon.setStrokeColor(getResources().getColor(R.color.TUM_blue_trans));
				polygon.setStrokeWidth(2);
			}
		}
	}
	//end init functions

	//update functions

	private JSONObject updateAttackMeshes() {

		/*Wir bekommen eine Liste von Meshes,
			die mit einer Facebook-ID identifiziert werden und von den Facebook-IDs der Beteiligten Nutzer identifiziert werden.
		Idee wäre:
			wir iteriren über die empfangene liste,
			suchen anhand der identifier das Polygon in unserer gespeicherten Liste.
				Wenn gefunden,
					nehmen wir das Objekt aus der Liste
					und stecken das Polygonobjekt in eine neue temporäre Liste und sind fertig.
				Wenn nicht gefunden,
					legen wir ein neues Polygon an,
						zeichnen es auf der Karte
						und stecken es ebenfalls in die Liste.
			(->Alle in der alten Liste verbleibenden Polygone existieren auf Serverseite nicht mehr und werden deshalb von der Map gelöscht.)
			Am Ende wird die alte mit der neuen Liste überschrieben.*/

		/*This JSON has the following structure:
	 * Package:= {"hp":<value>,"allMeshes":<allmeshes>,"energy":<value>}
	 * 		allmeshes:= [{"facultyID":<facultyID>, "meshes": <facultymeshes>}, {"facultyID":<facultyID>, "meshes": <facultymeshes>}, ...]
	 *			facultymeshes:= [<mesh1>,<mesh2>, ... , <meshn>]
	 *				meshn:= [<point1>,<point2>, ..., <pointn>]
	 *					pointn:= ["facebookID":<faceboookID>,"x":<longitude>,"y":<latitude>]*/

 		JSONObject jsonPackage = new JSONObject();
		ArrayList<Polygon> polys = new ArrayList<Polygon>();

		String facebookID = MainActivity.getInstance()
				.getFacebookID(getActivity());

		HttpGetter request = new HttpGetter();
		request.execute(new String[]{"users", facebookID, "pullUpdate"});

		Log.v(this.getClass().getName(), "trying to get current attack meshes");
		try {
			String requestResult = request.get();

			// if we just received an empty json, ignore
			if (!requestResult.isEmpty()
					&& !requestResult.equals("{ }")) {
				jsonPackage = new JSONObject(requestResult);
			}
		} catch (Exception e) { // various Exceptions can be
			// thrown in the process, for
			// brevity we do a 'catch all'
			Log.e("AtkMeshes Exception: ", e.getMessage());
		}
		return jsonPackage;


		/*if(jsonPackage != null) {
			 teamPolygons.set(jsonPackage);
		}*/
	}

	/**
	 * This method handles what happens when the player loses or gains health or is marked as DEAD
	 * (by the server; status is there to make sure DEATH is caught even when HP = 0 update was missed)
	 */
	public void updateStatus(){
		//TODO: we might want to rearrange the if checks, after all it seems we're checking for !"DEAD" in 3 out of 4 checks
		if (!dead && (currentHP == 0 || status.equals("DEAD"))) {
			dead = true;
			String message = "You were killed by the enemy!\nYou will slowly recover your health points.\n" +
					"When you reach 20 HP, you will be revived.";
			GameDialogs.createGenericDialog(message, MainActivity.getInstance()).show();
			//set player icon to some "dead" symbol
			_myLocation.setIcon(BitmapDescriptorFactory.fromBitmap(tombstone));
		} else if(dead && (currentHP >= 20 && !status.equals("DEAD"))) {
			dead = false;
			Toast.makeText(MainActivity.getInstance().getApplicationContext(), "You have been revived!\n"
							+ this.currentHP + "/" + this.maxHP,
					Toast.LENGTH_SHORT).show();
			_myLocation.setIcon(BitmapDescriptorFactory.fromBitmap(self));
		} else if (this.currentHP < this.oldHP && !status.equals("DEAD")) {
			//put Toast with new HP
			Toast.makeText(MainActivity.getInstance().getApplicationContext(), "You are being ATTACKED!",
					Toast.LENGTH_SHORT).show();
		} else if (this.currentHP > this.oldHP && !levelUp && !status.equals("DEAD")){ // levelUp technically doesnt mean being healed
			Toast.makeText(MainActivity.getInstance().getApplicationContext(), "You are being HEALED!",
					Toast.LENGTH_SHORT).show();
		}

		if(levelUp){
			Toast.makeText(MainActivity.getInstance().getApplicationContext(), "LevelUp! New Level: " + this.level,
					Toast.LENGTH_SHORT).show();
		}else if(this.oldExp < this.exp){
			Toast.makeText(MainActivity.getInstance().getApplicationContext(), "Gained " + (this.exp - this.oldExp) + " EP\n" +
							"You need " + (this.expToNext) + " more for a levelUP",
					Toast.LENGTH_SHORT).show();
		}

		this.oldHP  = this.currentHP;
		this.oldExp = this.exp;

		((TextView) rootView.findViewById(R.id.HPtext)).setText("HP: " + currentHP + "/" + this.maxHP);
		((ProgressBar)rootView.findViewById(R.id.HPprogress)).setMax(maxHP);
		((ProgressBar)rootView.findViewById(R.id.HPprogress)).setProgress(currentHP);
	}

	//zoom in/out to "detail view"/overview
	public void zoomOut()
	{
		zoomedout=!zoomedout;
		if(zoomedout)
		{
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(48.264655, 11.671601), 15.5f));
			for(PlayerMarker m : player_markers)
			{
				m.marker.setVisible(false);
			}
			for(Polygon p : flagpolys)
			{
				if(p!=null)
				p.setVisible(false);
			}
		}
		else
		{
			centerMapOnMyLocation();
			for(PlayerMarker m : player_markers)
			{
				m.marker.setVisible(true);
			}
			for(Polygon p : flagpolys)
			{
				if(p!=null)
				p.setVisible(true);
			}
		}

	}

	private void centerMapOnMyLocation() {
		if (location != null) {
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),
					location.getLongitude()), ZOOM));
		} else{
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(48.262436, 11.669094), ZOOM));
		}
	}

	//grab new location from sensors/device
	private void updateLocation(){
		if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ){
			location = locationManager.getLastKnownLocation(locationProvider);
		}
	}

	/*updates own marker*/
	private void updatePlayerMarker() {
		updateLocation();
		if(location != null){
			_myLocation.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
		}else{
			initPlayerMarker();
		}
	}

	public Marker getPlayerMarker(String ID)
	{
		for(PlayerMarker marker: player_markers)
		{
			if(marker.ID.equals(ID));
				return marker.marker;
		}
		return null;
	}

	public static MapSectionFragment getInstance()
	{
		return instance;
	}

	/**
	 * Draws other players given by JSONArray jsonUsers
	 *
	 * @param jsonUsers
	 * */
	private void updateDraw(JSONArray jsonUsers){
		try {
			for (int i = 0; i < jsonUsers.length(); i++) {
				JSONObject jsonUser = jsonUsers.getJSONObject(i);
				JSONObject position = jsonUser.getJSONObject("position");

				String facebookID = jsonUser.getString("facebookID");
				String userName = jsonUser.getString("name");
				Double longitude = position.getDouble("longitude");
				Double latitude = position.getDouble("latitude");
				String status = jsonUser.getString("status");
				Integer facID = jsonUser.getInt("facultyID");
				String className = jsonUser.getString("classname");
				boolean invisible = jsonUser.getBoolean("isInvisible");

				Log.v(this.getClass().getName(), "Doing player " + userName);

				boolean found = false;
				//find Playermarker;
				for (PlayerMarker m : player_markers) {
					//Facebook-ID would probably be better
					if (m.ID.equals(facebookID)) {
						Log.v(this.getClass().getName(),
								"found " + userName);
						found = true;
						if(status.equals("ACTIVE")) {
							m.marker.setPosition(new LatLng(longitude, latitude));
							m.marker.setVisible(true);
						} else {
							m.marker.setVisible(false);
						}
						break;
					}
				}
				if (!found) {
					Bitmap bmp = null;
					if (!status.equals("ACTIVE")) {
						continue;
					}
					if (facID >= 0 && facID < 5)
						bmp = playerBitmaps[facID];
					else
						bmp = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.feggit), markersize_player*2, markersize_player*2, true);
					if(userName.equals("Michael")) {
						bmp = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.saileru), markersize_player * 2, markersize_player * 2, true);
						userName = "Senpai Saileru!";
						className = "King Kung Tofu";
					}
					//Add new marker to map for every user nearby
					if (bmp != null) {
						Marker player = map.addMarker(new MarkerOptions()
								.position(new LatLng(longitude, latitude))
								.title(userName)
								.snippet(className)
								.icon(BitmapDescriptorFactory.fromBitmap(bmp))
								.alpha(invisible?.5f:1f)
								.anchor(0.5f, 0.5f));

						player_markers.add(new PlayerMarker(player, facebookID));
						Log.v(this.getClass().getName(), "added " + userName);
					} else {
						Log.v(this.getClass().getName(),
								"bmp for " + userName + " not found. fac: " + facID);
					}
				}
				/*String fbID = jsonUser.getString("facebookID");*/
			}
			//Log.v(this.getClass().getName(),"added overlays");
		}catch(Exception e){
			Log.e("Draw Exception: ", e.getMessage());
		}
	}

	/*returns a JSONArray that contains all players in vicinity*/
	private JSONArray updatePlayersInVicinity() {
		/*update all other players' positions, i.e. do JSON getPlayersInVicinity*/

		String facebookID = MainActivity.getInstance()
				.getFacebookID(getActivity());

		HttpGetter request = new HttpGetter();
		request.execute(new String[]{"users", facebookID, "getPlayersInVicinity"});

		Log.v(this.getClass().getName(), "trying to get players in Vicinity");
		try {
			String requestResult = request.get();

			// if we just received an empty json, ignore
			if (!requestResult.isEmpty()
					&& !requestResult.equals("{ }")) {
				JSONArray jsonUsers = new JSONArray(requestResult);
				return jsonUsers;
			}
		} catch (Exception e) { // various Exceptions can be
			// thrown in the process, for
			// brevity we do a 'catch all'
			Log.e("Map Exception: ", e.getMessage());
		}

		/*if getter was not successfull*/
		return null;
	}

	/**
	 * Draws flags given by JSONArray jsonFlags
	 *
	 * @param jsonFlags
	 * */
	private void updateDrawFlags(JSONArray jsonFlags){
		try {
			for (int i = 0; i < 8; i++) {
				JSONObject jsonFlag = jsonFlags.getJSONObject(i);
				updateFlag(i, jsonFlag.getInt("dominatingFaculty"), jsonFlag.getBoolean("captured"), jsonFlag.getInt("progress"));
			}
		}catch(Exception e){
			Log.e("DrawFlags Exception: ", e.getMessage());
		}
	}

	// end update functions
	/*returns a JSONArray that contains the currents status of all flags*/
	public JSONArray updateFlags() {
		HttpGetter request = new HttpGetter();
		request.execute(new String[]{"game", "getCaptureStats"});

		try {
			String requestResult = request.get();

			// if we just received an empty json, ignore
			if (!requestResult.isEmpty()
					&& !requestResult.equals("{ }")) {
				JSONArray jsonFlags = new JSONArray(requestResult);
				return jsonFlags;
			}
		}
		catch(Exception e)
		{
			Log.e("UpdateFlags Exception: ", e.getMessage());
		}
		return null;
	}

	/**
	 * sets new dominating faculty and current capture status/percentage of said faculty for a specified flag
	 *
	 * @param flagID
	 * @param facID
	 * @param domination
	 * @param percentage
	 */
	public void updateFlag(int flagID, int facID, boolean domination, int percentage)
	{
		//gets the flag identifier and an array of percentages of the facultys currently holding de fleg

		String newOwner = "None";
		int fillColor, outlineColor;
		if(facID==MainActivity.facultyID) {
			fillColor = getResources().getColor(R.color.TUM_blue_light_trans_trans);
			outlineColor = getResources().getColor(R.color.TUM_blue_trans);
		}
		else {
			fillColor = getResources().getColor(faculty.getFacColor_trans(facID));
			outlineColor = getResources().getColor(faculty.getFacColor(facID));
		}
		newOwner = faculty.getName(facID);
		int neutral = getResources().getColor(R.color.neutral_white);
		String desc;
		if(domination) {
			desc = newOwner+" dominating!";

		}
		else
		{
			desc = newOwner + ": " + percentage + "%";
		}
		flag_markers[flagID].setSnippet(desc);

		if(domination)
		{
			flagpolys[flagID].setFillColor(fillColor);
			flagpolys[flagID].setStrokeColor(outlineColor);
		}

		else
		{
			double perc = percentage/100.0;

			int red =(int)Math.floor(perc*Color.red(fillColor)+(1.0-perc)*Color.red(neutral));
			int green = (int)Math.floor(perc*Color.green(fillColor)+(1.0-perc)*Color.green(neutral));
			int blue = (int)Math.floor(perc*Color.blue(fillColor)+(1.0-perc)*Color.blue(neutral));
			flagpolys[flagID].setFillColor(Color.argb(0x7f, red, green, blue));

			neutral = getResources().getColor(R.color.TUM_blue_light_trans);

			red =(int)Math.floor(perc*Color.red(outlineColor)+(1.0-perc)*Color.red(neutral));
			green = (int)Math.floor(perc*Color.green(outlineColor)+(1.0-perc)*Color.green(neutral));
			blue = (int)Math.floor(perc*Color.blue(outlineColor)+(1.0-perc)*Color.blue(neutral));
			flagpolys[flagID].setStrokeColor(Color.argb(0xff,red,green,blue));
		}
	}

	@Override
	public void onPause() {
		fragmentSwitch = true;

		// wait until thread has terminated
		if(downThread != null) {
			try {
				downThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		MainActivity.getInstance().initialLevel = this.level;
		MainActivity.getInstance().initialHP    = this.currentHP;
		MainActivity.getInstance().initialExp   = this.exp;

        super.onPause();
	}

	@Override
	public void onResume() {
		teamPolygons = new TeamPolygons(map);
		locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
		map.clear();
		initBitmaps();
		player_markers = new ArrayList<PlayerMarker>();
		teamPolygons = new TeamPolygons(map);
		initialized = true;
		initPlayerMarker();
		initFlags();

		fragmentSwitch = false;
		downThread = new Thread(runnable);
		downThread.start();

		if(AttackCoolingDown) {
			MainActivity.menu.getItem(1).setIcon(R.drawable.ability_default);
			MainActivity.menu.getItem(1).setEnabled(false);
		} else {
			MainActivity.menu.getItem(1).setIcon(R.drawable.ic_attack);
			MainActivity.menu.getItem(1).setEnabled(true);
		}

		// this is to avoid a "You are being healed"-Toast from updateHP in the beginning
		this.oldHP = MainActivity.getInstance().initialHP;
		// This is to avoid a "LevelUp"-Toast from updateHP in the beginning
		this.level = MainActivity.getInstance().initialLevel;
		// This is to avoid a "Gained Exp"-Toast from updateHP in the beginning
		this.oldExp= MainActivity.getInstance().initialExp;

		centerMapOnMyLocation();
		super.onResume();
	}

    @Override
	public void onStart() {
		super.onStart();
		//create the Map
		createMapView();
		initBitmaps();

		// set ability icons ids
		switch(MainActivity.chosenClassIDs[0]){
			case 0: this.abilty_icon[0] = R.drawable.ability_assault;
				break;
			case 1: this.abilty_icon[0] = R.drawable.ability_conquerer;
				break;
			case 2: this.abilty_icon[0] = R.drawable.ability_medic;
				break;
			case 3: this.abilty_icon[0] = R.drawable.ability_spy;
				break;
			case 4: this.abilty_icon[0] = R.drawable.ability_saboteur;
				break;
			default: this.abilty_icon[0] = R.drawable.ability_default;
				break;
		}
		switch(MainActivity.chosenClassIDs[1]){
			case 0: this.abilty_icon[1] =R.drawable.ability_assault;
				break;
			case 1: this.abilty_icon[1] = R.drawable.ability_conquerer;
				break;
			case 2: this.abilty_icon[1] = R.drawable.ability_medic;
				break;
			case 3: this.abilty_icon[1] = R.drawable.ability_spy;
				break;
			case 4: this.abilty_icon[1] = R.drawable.ability_saboteur;
				break;
			default: this.abilty_icon[1] = R.drawable.ability_default;
				break;
		}
	}

	public void testPolyMarker(MarkerOptions mop) {
		map.addMarker(mop);
	}
}
