package handler;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import main.Faculty;
import main.Main;
import main.User;
import main.Util;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/*
 * Authors: Jean Paul Vieira, Benedict Drechsler, Julian Frattini
 * The GameHandler is the main handler which transmits the live-data of a game session
 */

public class GameHandler implements HttpHandler {

	public static final String httpContext = "/game";

	@Override
	public void handle(HttpExchange exchange) throws IOException {

		try {
			String[] url = exchange.getRequestURI().toString().split("/");

			switch (url[url.length - 1]) {
			case "getPlayerCount":
				getPlayerCount(exchange);
				break;
			case "getCaptureStats":
				getCaptureStats(exchange);
				break;
			case "getTeamScoreboard":
				getTeamScoreboard(exchange);
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

	// Method to receive a general information about the currently playing users
	public void getPlayerCount(HttpExchange exchange) throws IOException {
		JSONObject json = new JSONObject();

		// Returning the number of currently active players
		json.put("playerCount", "" + Main.game.getAllUsers().length);

		JSONArray list = new JSONArray();
		for (int i = -1; i < Main.game.faculties.size() - 1; i++) {
			JSONObject fac = new JSONObject();
			// Returning the id and name of a faculty as well as the number of
			// its currently active players
			fac.put("id", Main.game.faculties.get(i).id);
			fac.put("name", Main.game.faculties.get(i).name);
			fac.put("playerCount", Main.game.getUsersOfFaculty(i).length);

			// Returning a list of the facebookID and name of all users of that
			// faculty
			JSONArray users = new JSONArray();
			User[] u = Main.game.getUsersOfFaculty(i);
			for (int j = 0; j < u.length; j++) {
				JSONObject user = new JSONObject();
				user.put("facebookID", u[j].facebookID);
				user.put("name", u[j].name);
				users.put(user);
			}
			fac.put("players", users);

			list.put(fac);
		}

		json.put("faculties", list);
		exchange.sendResponseHeaders(200, json.toString().length());
		exchange.getResponseBody().write(json.toString().getBytes());
	}

	

	// Method to receive the live data of the Capture Points
	public void getCaptureStats(HttpExchange exchange) throws IOException {
		JSONArray list = new JSONArray();
		JSONObject[] cap = new JSONObject[Main.game.capturePoints.size()];

		// The status of every CapturePoint is defined by the current progress, the faculty 
		// who dominates the zone aswell as whether the point has already been fully captured
		for (int i = 0; i < Main.game.capturePoints.size(); i++) {
			cap[i] = new JSONObject();
			cap[i].put("id", i);
			cap[i].put("dominatingFaculty", Main.game.capturePoints.get(i).dominatingFaculty);
			cap[i].put("captured", Main.game.capturePoints.get(i).captured);
			cap[i].put("progress", Main.game.capturePoints.get(i).progress);

			list.put(cap[i]);
		}

		exchange.sendResponseHeaders(200, list.toString().length());
		exchange.getResponseBody().write(list.toString().getBytes());
	}

	/**
	 * Gathers all the information about all faculties and sends it to the user
	 * 
	 * @param exchange
	 * @throws IOException
	 */
	public void getTeamScoreboard(HttpExchange exchange) throws IOException {
		JSONObject fin = new JSONObject();
		JSONArray response = new JSONArray();
		
		for (Faculty faculty : Main.game.faculties.values()) {
			JSONObject json = new JSONObject();
			json.put("id", faculty.id);
			json.put("score", faculty.getScore());
			json.put("players_registered", faculty.getUsers().size());
			json.put("players_active", faculty.getUsers("ACTIVE").length);
			json.put("players_dead", faculty.getDeadUsers().length);
			
			System.out.println(faculty.id + ": " + faculty.getUsers().size());
			
			/*JsonObject topTenPlayers = new JsonObject();
			List<User> list = faculty.getTopTenUsers();
			for (int j = 0; j < 10 && j < list.size(); j++) {
				if (!list.get(j).name.isEmpty())
					topTenPlayers.put("" + j, list.get(j).name);
			}

			json.put("topPlayers", topTenPlayers);*/
			response.put(json);
		}

		// The TopPlayers are now drafted from all faculties alike into one bige scoreboard
		int numberOfTopPlayers = 10;
		JSONObject top = new JSONObject();
		JSONArray topPlayers = new JSONArray();
		List<User> all = new LinkedList<User>();
		// merging all the topPlayers of all faculties
		for (Faculty faculty : Main.game.faculties.values()) {
			all.addAll(faculty.getTopTenUsers());
		}
		// sorting the new, big list of topPlayers
		Collections.sort(all);
		
		// adding the numberOfTopPlayers best topPlayers into the scoreboard
		for(int i = 0; i < numberOfTopPlayers && i < all.size(); i++) {
			JSONObject user = new JSONObject();
			user.put("position", i);
			user.put("name", all.get(i).name);
			user.put("facultyID", + all.get(i).faculty.id);
			user.put("score", all.get(i).stats.score);
			topPlayers.put(user);
		}
		
		top.put("numberOfTopPlayers", Math.min(10, all.size()));
		top.put("scoreboard", topPlayers);
		
		fin.put("TopPlayers", top);
		fin.put("Faculties", response);
		
		exchange.sendResponseHeaders(200, fin.toString().length());
		exchange.getResponseBody().write(fin.toString().getBytes());
	}
}
