package handler;

import main.AttackMesh;
import main.Buff;
import main.CapturePoint;
import main.Faculty;
import main.FailureJsonObject;
import main.Main;
import main.Util;
import main.User;

import java.io.IOException;
import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.restfb.json.JsonArray;
import com.restfb.json.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/*
 * Authors: Jean Paul Vieira, Benedict Drechsler, Julian Frattini
 * The UserHandler has to deal with all calls concerning one single user,
 * except updating his position
 */

public class UserHandler implements HttpHandler {

	public static final String httpContext = "/users";

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String[] url = exchange.getRequestURI().toString().split("/");

		switch (url[url.length - 1]) {
		case "login":
			login(exchange, url[2], url[3], url[4], url[5]);
			break;
		case "enterFacultyIfNone":
			enterFacultyIfNone(exchange, url[2], url[3]);
			break;
		case "getFaculty":
			getFaculty(exchange, url[2]);
			break;
		case "getPlayerStats":
			getPlayerStats(exchange, url[2]);
			break;
		case "setClasses":
			setClasses(exchange, url[2], url[3], url[4]);
			break;
		case "getAllData":
			getAllData(exchange, url[2]);
			break;
		case "updatePosition":
			updatePosition(exchange, url[2], url[3], url[4]);
			break;
		case "getPlayersInVicinity":
			getPlayersInVicinity(exchange, url[2]);
			break;

		case "attack":
			modeAttack(exchange, url[2]);
			break;
		case "passive":
			modePassive(exchange, url[2]);
			break;
		case "pullUpdate":
			try {
				pullUpdate(exchange, url[2]);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case "setActive":
			setActive(exchange, url[2]);
			break;
		case "setInactive":
			setInactive(exchange, url[2]);
			break;
			

		case "callAbility": 
			callAbility(exchange, url[2], url[3]); 
			break;

		case "poke":
			poke(exchange, url[2]);
			break;
		default:
			Util.badRequest(exchange);
		}

		exchange.close();
	}

	// This method is used to add a user to the game. A Faculty does not have to
	// be chosen at this time
	public void login(HttpExchange exchange, String name, String facebookID, String longitude, String latitude) throws IOException {

		System.out.println("'" + name + "' logging in with ID '" + facebookID + "'");

		if (name == null || name.isEmpty() || facebookID == null || facebookID.isEmpty() || longitude == null || longitude.isEmpty()
				|| latitude == null || latitude.isEmpty()) {
			FailureJsonObject res = new FailureJsonObject(100, "Illegal login attempt. One or more parameters are missing.");
			System.out.println(res.toString());
			exchange.sendResponseHeaders(400, res.getLength());
			exchange.getResponseBody().write(res.getBytes());
			return;
		}

		// Trying to find the user in the game
		User user = Main.game.findUser(facebookID);

		if (user == null) {// In case the users ID could not be found, he does  not exist
			// A new user is created and gets added to the game
			user = new User(name, facebookID, Main.game.faculties.get(-1));
			Main.game.addUser(user);
		}
		// here we have a user, either old or new, doesn't matter
		user.position.setLocation(Integer.parseInt(longitude), Integer.parseInt(latitude));

		// set the user status to active
		user.setUserStatus("ACTIVE");

		// Checking, whether a user is now inside a capture-zone
		checkConquest(user);
	}

	// Method to assign a Faculty to an user
	public void enterFacultyIfNone(HttpExchange exchange, String facebookID, String facultyID) throws IOException {
		User user = Main.game.findUser(facebookID);

		if (user == null) { // The user does not exist and the call ends with a
							// failure
			FailureJsonObject fail = new FailureJsonObject(100, "FacebookId not found, please register first.");
			exchange.sendResponseHeaders(400, fail.getLength());
			exchange.getResponseBody().write(fail.getBytes());
			System.err.println("User [fbID " + facebookID + "] could not be found");
			return;
		}

		// The user seems to exist and the call can proceed
		if (user.faculty.id == -1) {// The faculty is only set if the user has not already chosen one
			int facID = Integer.parseInt(facultyID);
			user.setFaculty(Main.game.faculties.get(facID));
			Main.game.faculties.get(-1).removeUser(user);
			Main.game.faculties.get(facID).addUser(user);
			System.out.println("User [fbID " + facebookID + "] was assigned to Faculty " + facultyID);
		} else
			System.err.println("User [fbID " + facebookID
					+ "] could not be assigned to a new faculty, since he is already a member of a faculty");
	}

	// Method to receive the current faculty of a given user
	public void getFaculty(HttpExchange exchange, String facebookID) throws IOException {
		// Trying to find the user in the game
		User user = Main.game.findUser(facebookID);

		if (user == null) { // The user does not exist and the call ends with a
							// failure
			FailureJsonObject fail = new FailureJsonObject(100, "FacebookId not found, please register first.");
			exchange.sendResponseHeaders(400, fail.getLength());
			exchange.getResponseBody().write(fail.getBytes());
			return;
		}

		JSONObject json = new JSONObject();

		// the method simply returns the id of the faculty, since this is the
		// only relevant thing for the client
		json.put("id", user.faculty.id);
		System.out.println("User " + user.name + " was found in faculty " + user.faculty.id);

		exchange.sendResponseHeaders(200, json.toString().length());
		exchange.getResponseBody().write(json.toString().getBytes());
	}

	public void getPlayerStats(HttpExchange exchange, String facebookID) throws IOException {
		User user = Main.game.findUser(facebookID);

		if (user == null) {// The user does not exist and the call ends with a
							// failure
			FailureJsonObject fail = new FailureJsonObject(100, "FacebookId not found, please register first.");
			exchange.sendResponseHeaders(400, fail.getLength());
			exchange.getResponseBody().write(fail.getBytes());
			return;
		}

		JSONObject json = new JSONObject();

		// The time a player has spent in the game is a vital part of the
		// statistics
		JSONObject PlayTime = new JSONObject();
		PlayTime.put("Hours", user.stats.hours);
		PlayTime.put("Minutes", user.stats.minutes);
		PlayTime.put("Seconds", user.stats.seconds);
		json.put("Playtime", PlayTime);

		// the other parts of the statistics are fairly standard racords
		json.put("score", user.stats.score);
		json.put("kills", user.stats.kills);
		json.put("deaths", user.stats.deaths);
		json.put("captures", user.stats.captures);
		json.put("polygons", user.stats.polygons);
		json.put("faculty", user.faculty.id);

		exchange.sendResponseHeaders(200, json.toString().length());
		exchange.getResponseBody().write(json.toString().getBytes());
	}

	public void setClasses(HttpExchange exchange, String facebookID, String class1, String class2) throws IOException {
		User user = Main.game.findUser(facebookID);

		if (user == null) {// The user does not exist and the call ends with a
							// failure
			FailureJsonObject fail = new FailureJsonObject(100, "FacebookId not found, please register first.");
			exchange.sendResponseHeaders(400, fail.getLength());
			exchange.getResponseBody().write(fail.getBytes());
			System.err.println("User [fbID " + facebookID + "] could not be found");
			return;
		}

		// parsing the String-values the client sends to the server to Integers
		int c1 = Integer.parseInt(class1);
		int c2 = Integer.parseInt(class2);

		user.setClass(c1, c2);
		System.out.println("Set the class of user >" + user.name + "< to [" + user.className + "]");

		// the user is now ready for the game and will be set to active
		user.setUserStatus("ACTIVE");
	}

	public void getAllData(HttpExchange exchange, String facebookID) throws IOException {
		User user = Main.game.findUser(facebookID);

		if (user == null) { // The user does not exist and the call ends with a failure
			FailureJsonObject fail = new FailureJsonObject(100, "FacebookId not found, please register first.");
			exchange.sendResponseHeaders(400, fail.getLength());
			exchange.getResponseBody().write(fail.getBytes());
			return;
		}
		JSONObject json = new JSONObject();

		JSONObject status = new JSONObject();
		status.put("hp", user.hp);
		status.put("maxhp", user.maxhp);
		status.put("ExP", user.getCurrentExP());
		status.put("ExPToNext", user.getExpToNextLevel());
		status.put("level", user.level);
		status.put("status", user.getStatus());
		json.put("Status", status);

		// The attributes-JSON collects all the attributes, which define the stats of a user
		JSONObject attr = new JSONObject();
		attr.put("life", user.life);
		attr.put("strength", user.getStrength());
		attr.put("intelligence", user.intelligence);
		attr.put("dominance", user.dominance);
		attr.put("sight", user.getSight());
		// booleans, whether certain stats are buffed
		attr.put("strengthBuffed", (user.strengthBuff != null));
		attr.put("sightBuffed", (user.sightBuff != null));
		attr.put("invisible", (user.invisibilityBuff != null));
		json.put("Attributes", attr);

		// The class-JSON defines the classes, which an user has chosen at the start of the game
		JSONObject cla = new JSONObject();
		cla.put("class1", user.classIndex[0]);
		cla.put("class2", user.classIndex[1]);
		cla.put("superclass", user.className);
		cla.put("cooldown1", user.cooldown[0]);
		cla.put("cooldown2", user.cooldown[1]);
		json.put("Class", cla);

		exchange.sendResponseHeaders(200, json.toString().length());
		exchange.getResponseBody().write(json.toString().getBytes());
	}

	public void updatePosition(HttpExchange exchange, String facebookID, String longitude, String latitude) throws IOException {
		// Trying to find the user in the game
		User user = Main.game.findUser(facebookID);

		if (user == null) { // The user does not exist and the call ends with a
							// failure
			FailureJsonObject fail = new FailureJsonObject(100, "FacebookId not found, please register first.");
			exchange.sendResponseHeaders(400, fail.getLength());
			exchange.getResponseBody().write(fail.getBytes());
			return;
		}

		// Long posLong = Util.getPosition(longitude), posLat =
		// Util.getPosition(latitude);
		int posLong = (int) (Double.parseDouble(longitude) * 1000000);
		int posLat = (int) (Double.parseDouble(latitude) * 1000000);

		user.position.x = posLong;
		user.position.y = posLat;

		// Checking, whether a user is now inside a capture-zone
		checkConquest(user);

		exchange.sendResponseHeaders(200, -1);
	}

	// Method to internally calculate, whether a user is now conquering a
	// CapturePoint or has ended a conquest by leaving the zone
	public static void checkConquest(User user) {
		if (user.CapturePoint == -1) { // Only check if a user is not already in
										// a CaptureZone
			for (int i = 0; i < Main.game.capturePoints.size(); i++)
				// Iterate through all CapturePoints
				Main.game.capturePoints.get(i).checkIn(user.position.x, user.position.y, user);
		} else
			// In case the user is already in a CaptureZone, check whether he
			// has left it
			Main.game.capturePoints.get(user.CapturePoint).checkOut(user);

	}

	// Method to receive all Players in the vicinity of a given user
	public void getPlayersInVicinity(HttpExchange exchange, String facebookID) throws IOException {
		// Trying to find the user in the game
		User user = Main.game.findUser(facebookID);

		if (user == null) { // The user does not exist and the call ends with a failure
			FailureJsonObject fail = new FailureJsonObject(100, "FacebookId not found, please register first.");
			exchange.sendResponseHeaders(400, fail.getLength());
			exchange.getResponseBody().write(fail.getBytes());
			return;
		}
		
		// Calculating the sight-distance from the sight-value of the player
		int d = 500 + user.sight * 7;

		// Returning an array of arrays (per faculty) with all the necessary information of every user
		JSONArray list = new JSONArray();
		for (Faculty faculty : Main.game.faculties.values())
			for (User u : faculty.getUsers().values()) {
				if (user.facebookID != u.facebookID) {
					if (user.position.distance(u.position) < d) {
						if(!(user.faculty.id != u.faculty.id && (u.invisibilityBuff != null))) {
							JSONObject nearUser = new JSONObject();
							nearUser.put("facebookID", u.facebookID);
							nearUser.put("classname", u.className);
							nearUser.put("name", u.name);
							nearUser.put("facultyID", u.faculty.id);
							nearUser.put("status", u.getStatus());
							nearUser.put("isInvisible", (u.invisibilityBuff != null));
							
							JSONObject position = new JSONObject();
							position.put("longitude", ((double) u.position.x) / 1000000);
							position.put("latitude", ((double) u.position.y) / 1000000);
							nearUser.put("position", position);
	
							list.put(nearUser);
						}
					}
				}
			}

		exchange.sendResponseHeaders(200, list.toString().length());
		exchange.getResponseBody().write(list.toString().getBytes());
	}

	public void modeAttack(HttpExchange exchange, String facebookID) throws IOException {

		User user = Main.game.findUser(facebookID);

		synchronized (user) {

			if (user == null) {
				FailureJsonObject fail = new FailureJsonObject(141, "Player not found, please register first.");
				exchange.sendResponseHeaders(Util.HTTPErrorCode, fail.getLength());
				exchange.getResponseBody().write(fail.getBytes());
			}

			if (user.isAttacking) {
				FailureJsonObject fail = new FailureJsonObject(142, "Already in Attack Mode.");
				exchange.sendResponseHeaders(Util.HTTPErrorCode, fail.getLength());
				exchange.getResponseBody().write(fail.getBytes());
			}

			if (user.energy <= user.minEnergyThreshold) {
				FailureJsonObject fail = new FailureJsonObject(143, "Not enough Energy.");
				exchange.sendResponseHeaders(Util.HTTPErrorCode, fail.getLength());
				exchange.getResponseBody().write(fail.getBytes());
			}

			if (user.attackCooldown > 0) {
				FailureJsonObject fail = new FailureJsonObject(144, "Cooldown remaining: " + user.attackCooldown);
				exchange.sendResponseHeaders(Util.HTTPErrorCode, fail.getLength());
				exchange.getResponseBody().write(fail.getBytes());
			}

			user.isAttacking = true;
			user.attackCooldown = 10;

			exchange.sendResponseHeaders(200, -1);
		}

	}

	public void modePassive(HttpExchange exchange, String facebookID) throws IOException {

		User user = Main.game.findUser(facebookID);

		synchronized (user) {

			if (user == null) {
				FailureJsonObject fail = new FailureJsonObject(141, "Player not found, please register first.");
				exchange.sendResponseHeaders(Util.HTTPErrorCode, fail.getLength());
				exchange.getResponseBody().write(fail.getBytes());
			}

			if (!user.isAttacking) {
				FailureJsonObject fail = new FailureJsonObject(142, "Already in Passive Mode.");
				exchange.sendResponseHeaders(Util.HTTPErrorCode, fail.getLength());
				exchange.getResponseBody().write(fail.getBytes());
			}

			if (user.attackCooldown > 0) {
				FailureJsonObject fail = new FailureJsonObject(144, "Cooldown remaining: " + user.attackCooldown);
				exchange.sendResponseHeaders(Util.HTTPErrorCode, fail.getLength());
				exchange.getResponseBody().write(fail.getBytes());
			}

			user.isAttacking = false;
			user.attackCooldown = user.attackCooldownMax;

			JsonObject response = new JsonObject();

			response.put("cooldown", user.attackCooldown);
			response.put("energyRemaining", user.energy);
			response.put("energyPerSecond", user.energyRegenerationRate);

			exchange.sendResponseHeaders(200, -1);
		}
	}
	
	public void setActive(HttpExchange exchange, String facebookID) throws IOException {

		User user = Main.game.findUser(facebookID);

		synchronized (user) {
			if (user == null) {
				FailureJsonObject fail = new FailureJsonObject(141, "Player not found, please register first.");
				exchange.sendResponseHeaders(Util.HTTPErrorCode, fail.getLength());
				exchange.getResponseBody().write(fail.getBytes());
			}

			user.setUserStatus("ACTIVE");

			exchange.sendResponseHeaders(200, -1);
		}
	}
	
	public void setInactive(HttpExchange exchange, String facebookID) throws IOException {

		User user = Main.game.findUser(facebookID);

		synchronized (user) {
			if (user == null) {
				FailureJsonObject fail = new FailureJsonObject(141, "Player not found, please register first.");
				exchange.sendResponseHeaders(Util.HTTPErrorCode, fail.getLength());
				exchange.getResponseBody().write(fail.getBytes());
			}

			user.setUserStatus("INACTIVE");

			exchange.sendResponseHeaders(200, -1);
		}
	}

	/**
	 * This method gives a client all information needed after a simulation
	 * step:
	 * 
	 * - Attack Meshes that exist
	 * 
	 * - User's health and energy
	 * 
	 * 
	 * 
	 * This JSON has the following structure:
	 * 
	 * Package:= {"hp":<value>,"allMeshes":<allmeshes>,"energy":<value>}
	 * 
	 * allmeshes:= [{"facultyID":<facultyID>, "meshes": <facultymeshes>},
	 * {"facultyID":<facultyID>, "meshes": <facultymeshes>}, ...]
	 * 
	 * facultymeshes:= [<mesh1>,<mesh2>, ... , <meshn>]
	 * 
	 * meshn:= [<point1>,<point2>, ..., <pointn>]
	 * 
	 * pointn:= ["facebookID":<faceboookID>,"x":<longitude>,"y":<latitude>]
	 * 
	 * @param exchange
	 * @param facebookID
	 *            : user facebook id
	 * @throws IOException
	 */
	public void pullUpdate(HttpExchange exchange, String facebookID) throws IOException {

		User user = Main.game.findUser(facebookID);

		// need to sync game too as we need the meshes.
		synchronized (Main.game) {
			synchronized (user) {

				if (user == null) {
					FailureJsonObject fail = new FailureJsonObject(161, "Player not found, please register first.");
					exchange.sendResponseHeaders(Util.HTTPErrorCode, fail.getLength());
					exchange.getResponseBody().write(fail.getBytes());
				}

				System.out.println("Pull Update from: " + user.name + " - ID:" + user.facebookID);
				JsonObject response = new JsonObject();

				response.put("energy", user.energy);
				response.put("hp", user.hp);
				response.put("level", user.level);
				JsonArray allMeshes = new JsonArray();

				/**
				 * Puts all the attack meshes in JSON - see the description of
				 * the method to unterstand it better
				 */
				// Cycles through faculties for AttackMeshes
				for (LinkedList<AttackMesh> list : Main.game.attackMeshes.values()) {
					if (!list.isEmpty()) {
						JsonObject faculty = new JsonObject();
						faculty.put("facultyID", list.getFirst().faculty.id);
						JsonArray meshes = new JsonArray();

						// Adds all the meshes from this faculty
						for (AttackMesh am : list) {
							JsonArray mesh = new JsonArray();

							// adds every point with the facebookID of the user
							// on it
							for (User u : am.getUsers()) {
								JsonObject point = new JsonObject();
								point.put("facebookID", u.facebookID);
								point.put("longitude", ((double) u.position.x) / 1000000);
								point.put("latitude", ((double) u.position.y) / 1000000);
								//System.out.println(u.toString() + "'s position (as sent to client in pullUpdate): x/long. " + ((double) u.position.x) / 1000000 + ", y/lat. " + ((double) u.position.y) / 1000000);
								mesh.put(point);
							}
							meshes.put(mesh);
						}
						faculty.put("meshes", meshes);
						allMeshes.put(faculty);
					}
				}
				response.put("allMeshes", allMeshes);

				exchange.sendResponseHeaders(200, response.toString().length());
				exchange.getResponseBody().write(response.toString().getBytes());
			}
		}
	}
	
	/* 
	 * ----------------------------------------------------------------------
	 *  ABILITIES
	 * ----------------------------------------------------------------------
	 */
	
	public void callAbility(HttpExchange exchange, String facebookID, String number) throws IOException {
		JSONObject response = new JSONObject();
		int n = Integer.parseInt(number);
		
		User user = Util.findUser(facebookID);
		if(user == null) {
			FailureJsonObject res = new FailureJsonObject(100, "The user does not exist. Check the given facebookID!");
			exchange.sendResponseHeaders(400, res.getLength());
			exchange.getResponseBody().write(res.getBytes());
			return;
		}
		
		if(user.className.equals("")) {
			FailureJsonObject res = new FailureJsonObject(101, "The user has not chosen classes yet - he therefor can not activate an ability!");
			System.err.println("The user has not chosen classes yet - he therefor can not activate an ability!");
			exchange.sendResponseHeaders(400, res.getLength());
			exchange.getResponseBody().write(res.getBytes());
			return;
		}
		
		if(n == 1 || n == 2) {
			boolean used = false;
			n = n - 1;
			if(n == 0 && user.level >= 4 && user.cooldown[0] <= 0 ||
					n == 1 && user.level >= 7 && user.cooldown[1] <= 0) {
				user.cooldown[n] = Main.game.abilities[user.classIndex[n]].cooldown - Main.game.abilities[user.classIndex[n]].intFactor * user.intelligence;
				
				switch(user.classIndex[n]) {
					case 0: griffinsWings(user); break;
					case 1: ironInquisition(user); break;
					case 2: atlasHands(user); break;
					case 3: golemsEye(user); break;
					case 4: twilightShard(user); break;
				}
				used = true;
			}
			
			response.put("cooldown1", user.cooldown[0]);
			response.put("cooldown2", user.cooldown[1]);
			response.put("fired", used);
		}
		else {
			FailureJsonObject res = new FailureJsonObject(102, "Called ability number >" + n + "< - but there is only ability number >1< and number >2<");
			System.err.println("The user has not chosen classes yet - he therefor can not activate an ability!");
			exchange.sendResponseHeaders(400, res.getLength());
			exchange.getResponseBody().write(res.getBytes());
			return;
		}
		
		exchange.sendResponseHeaders(200, response.toString().length());
		exchange.getResponseBody().write(response.toString().getBytes());
	}
	
	public void griffinsWings(User user) throws IOException {
		System.out.println(user.name + " opend the 'Griffins Wings'");
		
		int d = 800;
		Faculty own = Main.game.faculties.get(user.faculty.id);
		for(User u:own.getUsers().values()) {if(Util.distance(user.position.x, user.position.y, u.position.x, u.position.y) < d) {
				Buff griffinsWrath = new Buff(0, 5, 30);
				u.addBuff(griffinsWrath);
				System.out.println(" > " + u.name + " feals the wrath of the Griffin and sports " + u.getStrength() + " strength now");
			}
		}
	}
	
	public void ironInquisition(User user) throws IOException {
		System.out.print(user.name + " unleashed the 'Iron Inquisition' ");
		if(user.CapturePoint >= 0) {
			CapturePoint c = Main.game.capturePoints.get(user.CapturePoint);
			if(c.dominatingFaculty == user.faculty.id) {
				System.out.print(" and fortified the Point >" + c.name + "< from " + c.progress + " ");
				c.progress = c.progress + 10;
				System.out.println("to " + c.progress + "!");
			}
			else
				System.out.println(" but the Point >" + c.name + "< has not been taken yet");
		}
		else 
			System.out.println(" but he is not in any CaptureZone");
	}
	
	public void atlasHands(User user) throws IOException {
		System.out.println(user.name + " unfolded his 'Atlas Hands'");
		
		int d = 800;
		Faculty own = Main.game.faculties.get(user.faculty.id);
		for(User u:own.getUsers().values()) {
			if(Util.distance(user.position.x, user.position.y, u.position.x, u.position.y) < d) {
				u.heal(10 * user.intelligence*2);
				System.out.println(" > " + u.name + " was healed to " + u.hp + " HealthPoints");
			}
		}
	}
	
	public void golemsEye(User user) throws IOException {
		System.out.println(">" + user.name + "< sees through the 'Golems Eye' and sports a sight-value of " + user.sight);
		user.addBuff(new Buff(1, 200, 20));
	}
	
	public void twilightShard(User user) throws IOException {
		System.out.println(">" + user.name + "< is covered with the 'Twilight Shard' and is now invisible for 40 seconds");
		user.addBuff(new Buff(2, 0, 40));
	}
	
	/*
	 * ----------------------------------------------------------------------
	 *  END OF ABILITIES
	 * ----------------------------------------------------------------------
	 */

	public void poke(HttpExchange exchange, String facebookID) throws IOException {
		User user = Main.game.findUser(facebookID);

		if (user == null) {// The user does not exist and the call ends with a
							// failure
			FailureJsonObject fail = new FailureJsonObject(100, "FacebookId not found, please register first.");
			exchange.sendResponseHeaders(400, fail.getLength());
			exchange.getResponseBody().write(fail.getBytes());
			System.err.println("User [fbID " + facebookID + "] could not be found");
			return;
		}

		System.out.println("Poked " + user.toString());
		System.out.println("Positioned at " + user.position.y + "|" + user.position.x);
		System.out.println("");
	}
}