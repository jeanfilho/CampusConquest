package handler;
import java.io.IOException;

import main.CapturePoint;
import main.Main;
import main.User;
import main.Util;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class AdminHandler implements HttpHandler{

	public static final String httpContext = "/admin";
	public boolean unleashedThe8thFleat = false;
	
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		
		try{
			String[] url = exchange.getRequestURI().toString().split("/");
			
			switch(url[url.length-1]) {
				case "releaseTheKraken": releaseTheKraken(exchange, url[2], url[3]); break;
				case "sendTheRaven": sendTheRaven(exchange, url[2]); break;
				case "goldDust": goldDust(exchange, url[2], url[3]); break;
				case "callTheEagles": callTheEagles(exchange, url[2], url[3]); break;
				case "forArchadia": forArchadia(exchange, url[2]); break;
				default: Util.badRequest(exchange);
			}
		} catch(NullPointerException e) {
			Util.badRequest(exchange);
		} finally {
			exchange.close();	
		}
	}
	
	public void releaseTheKraken(HttpExchange exchange, String faculty, String CapID) throws IOException {
		int cap = Integer.parseInt(CapID);
		
		double fbID = Math.random()*1000000;
		User dummy = new User("Kraken" + fbID, "" + ((int) fbID), Main.game.faculties.get(Integer.parseInt(faculty)));
		Main.game.addUser(dummy);
		dummy.setUserStatus("ACTIVE");
		//dummy.faculty = Main.game.faculties.get(Integer.parseInt(faculty));
		
		int posLong = Main.game.capturePoints.get(cap).xFlag;
		int posLat = Main.game.capturePoints.get(cap).yFlag;
		
		dummy.position.y = posLat;
		dummy.position.x = posLong;
		
		System.out.println("Released a " + dummy.faculty.name + "-Kraken [fbID " + dummy.facebookID + "] at >" + 
				Main.game.capturePoints.get(cap).name + "<");
	
		if(dummy.CapturePoint == -1) {
			for(int i = 0; i < Main.game.capturePoints.size(); i++)
				Main.game.capturePoints.get(i).checkIn(dummy.position.x, dummy.position.y, dummy);
		}
		else {
			Main.game.capturePoints.get(dummy.CapturePoint).checkOut(dummy);
		}
	}
	
	public void sendTheRaven(HttpExchange exchange, String CapID) throws IOException {
		int cap = Integer.parseInt(CapID);
		
		double[] d = new double[Main.game.capturePoints.size()];
		for(int i = 0; i < Main.game.capturePoints.size(); i++)
			d[i] = Util.distance(Main.game.capturePoints.get(cap).xFlag, Main.game.capturePoints.get(cap).yFlag, Main.game.capturePoints.get(i).xFlag, Main.game.capturePoints.get(i).yFlag);
	
		JSONArray list = new JSONArray();
		JSONObject[] c = new JSONObject[Main.game.capturePoints.size()];
		
		for(int i = 0; i < Main.game.capturePoints.size(); i++) {
			c[i] = new JSONObject();
			c[i].put("id", i);
			c[i].put("distance", d[i]);
			
			list.put(c[i]);
		}

		exchange.sendResponseHeaders(200, list.toString().length());
		exchange.getResponseBody().write(list.toString().getBytes());
	}
	
	public void goldDust(HttpExchange exchange, String facebookID, String exp) throws IOException {
		User user = Util.findUser(facebookID);
		
		if(user != null) {
			System.out.print(user.name + " showers in a gold dust of " + Integer.parseInt(exp) + "ExP, going from Level " + user.level);
			user.addExp(Integer.parseInt(exp));
			System.out.println(" to Level " + user.level);
		}
	}
	
	public void callTheEagles(HttpExchange exchange, String facebookID, String CapID) throws IOException {
		int cap = Integer.parseInt(CapID);
		
		User user = Util.findUser(facebookID);
		CapturePoint c = Main.game.capturePoints.get(cap);
		
		user.position.x = c.xFlag;
		user.position.y = c.yFlag;
		
		if(user.CapturePoint == -1) {
			for(int i = 0; i < Main.game.capturePoints.size(); i++)
				Main.game.capturePoints.get(i).checkIn(user.position.x, user.position.y, user);
		}
		else {
			Main.game.capturePoints.get(user.CapturePoint).checkOut(user);
		}
	}
	
	public void forArchadia(HttpExchange exchange, String facebookID) throws IOException {
		User user = Util.findUser(facebookID);
		if(user == null)
			return;
		
		User Gabranth = new User("Gabranth", "jmg", Main.game.faculties.get(3));
		User Zargabaath = new User("Zargabaath", "jmz", Main.game.faculties.get(3));
		User Drace = new User("Drace", "jmd", Main.game.faculties.get(3));
		
		if(!unleashedThe8thFleat) {
			// Adding Gabranth
			Gabranth.setClass(1, 2);
			Gabranth.setUserStatus("ACTIVE");
			Gabranth.addExp(800);
			Gabranth.position.x = user.position.x;
			Gabranth.position.y = user.position.y - 100;
			
			Main.game.faculties.get(3).removeUser(Gabranth);
			Main.game.addUser(Gabranth);
			
			// Adding Zargabath
			Zargabaath.setClass(1, 0);
			Zargabaath.setUserStatus("ACTIVE");
			Zargabaath.addExp(800);
			Zargabaath.position.x = user.position.x - 90;
			Zargabaath.position.y = user.position.y - 10;
			
			Main.game.faculties.get(3).removeUser(Zargabaath);
			Main.game.addUser(Zargabaath);
			
			// Adding Drace
			Drace.setClass(2, 0);
			Drace.setUserStatus("ACTIVE");
			Drace.addExp(800);
			Drace.position.x = user.position.x + 90;
			Drace.position.y = user.position.y - 10;
			
			Main.game.faculties.get(3).removeUser(Drace);
			Main.game.addUser(Drace);
			
			unleashedThe8thFleat = true;
		}
		else {
			Gabranth = Main.game.faculties.get(3).findUser("jmg");
			Zargabaath = Main.game.faculties.get(3).findUser("jmz");
			Drace = Main.game.faculties.get(3).findUser("jmd");
		}
		
		// Attack
		Gabranth.isAttacking = true;
		Zargabaath.isAttacking = true;
		Drace.isAttacking = true;
	}
}