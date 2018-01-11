import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;


public class MainOld {
	
	static Game game;
	
	static List<Task> tasks1, tasks2;
	
	public static void main(String args[]) throws Exception {
		
		HttpServer server = HttpServer.create(new InetSocketAddress(9797), 0);
		
		server.createContext("/getTime", new MyHandler() );
		
		server.start();
		
		game = new Game();
		tasks1 = new ArrayList();
		tasks2 = new ArrayList();
		
		while(true) {
			
			
			for(int i=0; i<tasks1.size(); i++){
				
				Task task = tasks1.get(i);
				
				switch(task.id){
					case Task.USERTASK: 
//						userList.getUser(((UserTaskClass)task.data).userid).updatePosition(task.data.position);
						break;
				}
			}
			
			game.simulate();
			
			//sendTOUsers();
			
			sendAllGCM();
			
			Thread.sleep(1000);
		}
		
	}
	
	static class MyHandler implements com.sun.net.httpserver.HttpHandler{

		@Override
		public void handle(HttpExchange exchange) throws IOException {
			
			int newPosition = (int) (Math.random()*100); //getFrom http
			game.updatePosition(newPosition);
			
			
			OutputStream os = exchange.getResponseBody();
			
			String response = "No this is Patrick. Time: " + game.timeSeconds + "s";
			
			

			exchange.sendResponseHeaders(200, response.length());
			os.write(response.getBytes());
//			exchange.close();
			
			//leftone line dunno what
			
			Task task = new Task();
			User user = new User();
			user.name = (String) exchange.getAttribute("name");
			
			System.out.println(user.name);
			
			task.id = Task.USERTASK;
			task.data = user;
			
			synchronized (tasks2) {
				tasks2.add(task);	
			}
			
		}
		
	}

	static class Game{
		
		int position;
		int timeSeconds;
		
		void simulate(){
			System.out.println("position: " + position);
			timeSeconds ++;
			position ++;
		}
		
		void updatePosition(int pos){
			position = pos;
		}
	}
	
	static class User{
		String name;
	}
	
	static class Task{
		
		final static int TIMETASK = 1;
		final static int USERTASK = 2;
		
		int id;
		Object data;
		
		void process(){
			
		}
	}
	
	static void sendAllGCM(){
		
//		HttpRequest request = new Request(GOoogle IP);
	}
}























