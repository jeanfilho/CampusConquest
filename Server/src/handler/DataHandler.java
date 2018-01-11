package handler;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

import main.Ability;
import main.Faculty;
import main.Main;
import main.UserClass;
import main.Util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/*
 * Authors: Jean Paul Vieira, Benedict Drechsler, Julian Frattini
 * The GameHandler is the main handler which transmits the static data of a game session
 */

public class DataHandler implements HttpHandler {

	public static final String httpContext = "/data";

	@Override
	public void handle(HttpExchange exchange) throws IOException {

		try {
			String[] url = exchange.getRequestURI().toString().split("/");

			switch (url[url.length - 1]) {
			case "faculties":
				getAllFaculties(exchange);
				break;
			case "getCapturePoints":
				getCapturePoints(exchange);
				break;
			case "getBaseClasses":
				getBaseClasses(exchange);
				break;
			case "getSuperClasses":
				getSuperClasses(exchange);
				break;
			case "getAbilities":
				getAbilities(exchange);
				break;
			default:
				Util.badRequest(exchange);
			}
		} catch (NullPointerException e) {
			Util.badRequest(exchange);
		} finally {
			exchange.close();
		}

	}

	// Method to receive an Array containing the Information about all Faculties
	public void getAllFaculties(HttpExchange exchange) throws IOException {
		JSONArray list = new JSONArray();
		JSONObject faculty;

		for (Faculty item : Main.game.faculties.values()) {
			if (item.id >= 0) {
				faculty = new JSONObject();
				faculty.put("id", item.id);
				faculty.put("name", item.name);
				faculty.put("description", item.description);

				list.put(faculty);
			}
		}

		exchange.sendResponseHeaders(200, list.toString().length());
		exchange.getResponseBody().write(list.toString().getBytes());
	}

	// Method to receive the general information, on which the client can build the CapturePoints
	public void getCapturePoints(HttpExchange exchange) throws IOException {
		JSONObject response = new JSONObject();
		// The number of CapturePoints is not final, there for it has to be transmitted aswell
		response.put("nCapturePoints", Main.game.capturePoints.size());

		JSONArray list = new JSONArray();
		JSONObject[] cap = new JSONObject[Main.game.capturePoints.size()];

		for (int i = 0; i < Main.game.capturePoints.size(); i++) {
			cap[i] = new JSONObject();
			// Every CapturePoint is defined by its ID, but displayed with a letter and a name
			cap[i].put("letter", Main.game.capturePoints.get(i).letter);
			cap[i].put("name", Main.game.capturePoints.get(i).name);
			cap[i].put("id", i);

			// The Flag is the marker indicating the CapturePoint
			JSONObject Flag = new JSONObject();
			Flag.put("x", ((double) Main.game.capturePoints.get(i).xFlag) / 1000000);
			Flag.put("y", ((double) Main.game.capturePoints.get(i).yFlag) / 1000000);
			cap[i].put("flag", Flag);

			// The border-points of the Zone define the polygon in which a conqueror has to stand
			JSONArray Zone = new JSONArray();
			for (int j = 0; j < 4; j++) {
				JSONObject p = new JSONObject();
				p.put("x", ((double) Main.game.capturePoints.get(i).xpoints[j]) / 1000000);
				p.put("y", ((double) Main.game.capturePoints.get(i).ypoints[j]) / 1000000);
				Zone.put(p);
			}
			cap[i].put("zone", Zone);

			list.put(cap[i]);
		}

		response.put("CapturePoints", list);

		exchange.sendResponseHeaders(200, response.toString().length());
		exchange.getResponseBody().write(response.toString().getBytes());
	}

	// Method to receive an array containing the information of the five base classes
	public void getBaseClasses(HttpExchange exchange) throws IOException {
		JSONArray list = new JSONArray();
		JSONObject cap = new JSONObject();

		// Every class is defined by its id, displayed with a name and explained with a description
		for (UserClass e : Main.game.classes.values()) {
			cap = new JSONObject();
			cap.put("id", e.id);
			cap.put("name", e.name);
			cap.put("description", e.description);

			list.put(cap);
		}

		exchange.sendResponseHeaders(200, list.toString().length());
		exchange.getResponseBody().write(list.toString().getBytes());
	}

	// Method to receive a matrix containing the name of all class-combinations
	public void getSuperClasses(HttpExchange exchange) throws IOException {
		JSONArray list = new JSONArray();
		JSONObject[] cap = new JSONObject[Main.game.classes.size()];

		for (int i = 0; i < Main.game.classes.size(); i++) {
			cap[i] = new JSONObject();
			cap[i].put("id", i);

			JSONArray c = new JSONArray();
			JSONObject[] subcap = new JSONObject[Main.game.classes.size()];

			for (int j = 0; j < Main.game.classes.size(); j++) {
				subcap[j] = new JSONObject();
				subcap[j].put("id2", j);
				subcap[j].put("name", Main.game.SuperClass(i, j));

				c.put(subcap[j]);
			}
			cap[i].put("Subclasses", c);
			list.put(cap[i]);
		}

		exchange.sendResponseHeaders(200, list.toString().length());
		exchange.getResponseBody().write(list.toString().getBytes());
	}
	
	public void getAbilities(HttpExchange exchange) throws IOException {
		JSONArray list = new JSONArray();

		JSONObject feat = new JSONObject();
		for (Ability a : Main.game.abilities) {
			feat = new JSONObject();
			
			feat.put("classID", a.classID);
			feat.put("name", a.name);
			feat.put("description", a.description);
			feat.put("cooldown", "" + a.cooldown + " - intelligence*" + a.intFactor);
			
			list.put(feat);
		}

		exchange.sendResponseHeaders(200, list.toString().length());
		exchange.getResponseBody().write(list.toString().getBytes());
	}
}