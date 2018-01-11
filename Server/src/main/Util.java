package main;

import java.io.IOException;
import java.io.InputStream;

import com.sun.net.httpserver.HttpExchange;

public class Util {
	
	public static final int HTTPErrorCode = 400;
	public static final int IllegalParametersCode = 104;
	
	public static String getPayload(HttpExchange exchange){
		
		String payload = null;
		
		if(exchange!=null){
			
			InputStream is = exchange.getRequestBody();
			
			if(is != null){
				
				int b;
				try {
					b = is.read();
					
					String tmp = "";
					while(b!=-1){
						
						tmp += (char)b;
						
						b=is.read();
					}
					
					payload = tmp;
				} catch (IOException e) {
					//do naught
				}
			}
		}
		
		return payload;
	}
	
	public static void badRequest(HttpExchange exchange) throws IOException {
		 
		FailureJsonObject fail = new FailureJsonObject(000, "Bad Request: " + exchange.getRequestURI().toString());
		exchange.sendResponseHeaders(Util.HTTPErrorCode, fail.getLength());
		exchange.getResponseBody().write(fail.getBytes());
		
	}

	public static Long getPosition(String pos){
		try{
			return Long.parseLong(pos);
		} catch (Exception e) {
			return null;
		}
	}
	/**@OBSOLETE - Do not Use
	 * Compares if the distance between two points is smaller than a value
	 * @param pos1
	 * @param pos2
	 * @param d : value to be compared to
	 * @return true if the distance is smaller than d
//	 */
//	public static boolean distanceCheck(Point pos1, Position pos2, long value) {
//		long dx = Math.abs(pos1.latitude - pos2.latitude);
//		long dy = Math.abs(pos1.longitude - pos2.longitude);
//		double l = Math.sqrt(dx*dx + dy*dy);
//		if(l < value) 
//			return true;
//		return false;
//	}
	
	public static double distance(long x1, long y1, long x2, long y2) {
		long dx = Math.abs(x1 - x2);
		long dy = Math.abs(y1 - y2);
		double l = Math.sqrt(dx*dx + dy*dy);
		return l;
	}
	
	public static User findUser(String facebookID) {
		// Trying to find the user in the game
		User user = Main.game.findUser(facebookID);

		if (user == null) { // The user does not exist and the call ends with a failure
			System.err.println("User [fbID " + facebookID + "] could not be found");
			return null;
		}
		return user;
	}
}