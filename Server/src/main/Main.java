package main;

import handler.*;

import java.awt.Polygon;
import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

import data.XMLTool;

public class Main {

	/**
	 * Any access on this object, its attributes, its methods should be
	 * synchronized, except if a method called explicit says it is not
	 * necessary. This is especially true for all accesses in an HttpHandler.
	 */
	public static Game game;

	private final static Faculty[] listOfFaculties = { new Faculty(-1, "default", "no Faculty chosen", new Polygon()),
			new Faculty(0, "Informatics", "Faculty of Informatics",
					new Polygon(new int[] { 10, 10, 15, 15 }, new int[] { 10, 20, 20, 10 }, 4)),
			new Faculty(1, "Chemistry", "Faculty of Chemistry",
					new Polygon(new int[] { 10, 10, 15, 15 }, new int[] { 10, 20, 20, 10 }, 4)),
			new Faculty(2, "Maschinenbau", "Faculty of Maschinenbau", new Polygon()),
			new Faculty(3, "Physics", "Faculty of  Physics", new Polygon()),
			new Faculty(4, "Mathematics", "Faculty of  Mathematics", new Polygon()) };

	private final static CapturePoint[] capturePoints = {
			new CapturePoint(0, "Parabelrutsche", "A", 48262555, 11667958, 48262734, 11667341, 48262584, 11668575,
					48262302, 11668580, 48262612, 11667319),
			new CapturePoint(1, "IPP Mensa", "B", 48262187, 11672795, 48262466, 11672549, 48262025, 11672374, 48261871,
					11673088, 48262321, 11673289),
			new CapturePoint(2, "Onkel Luu\'s Asia Imbiss", "C", 48264115, 11670625, 48264205, 11670490, 48264162,
					11670957, 48263737, 11670769, 48263837, 11670318),
			new CapturePoint(3, "MW0001", "D", 48265393, 11671012, 48265673, 11670490, 48265658, 11671359, 48265176,
					11671246, 48265233, 11670404),
			new CapturePoint(4, "StuCafe MW", "E", 48265703, 11667415, 48265831, 11667629, 48265910, 11666879, 48264843,
					11666990, 48265530, 11667752),
			new CapturePoint(5, "Mensa", "F", 48267346, 11671363, 48267862, 11670839, 48267747, 11671858, 48267158,
					11671638, 48267287, 11670629),
			new CapturePoint(6, "Chemie", "G", 48268377, 11669014, 48268671, 11668680, 48267615, 11668047, 48267401,
					11669407, 48268577, 11669291),
			new CapturePoint(7, "Physik", "H", 48267285, 11675732, 48267460, 11675213, 48266728, 11675089, 48266585,
					11676159, 48267390, 11676054) };

	private final static UserClass[] classes = {
			new UserClass(0, "Assault",
					"offensive, strong class with focus on buffing allies and himself for the fight"),
			new UserClass(1, "Conquerer", "defensive class focused on conquering and defending capture points"),
			new UserClass(2, "Medic", "class dedicated to healing, regenerating and reviving teammates"),
			new UserClass(3, "Spy", "squishy class focused on surveillance and keeping control of the map"),
			new UserClass(4, "Saboteur", "squishy class with focus on stealth and quick interference") };

	private final static Ability[] abilities = { new Ability(0, "Griffins Wings",
			"The Assault buffs himself and his allies and increases their strength for a short amount of time", 180, 3),
			new Ability(1, "Iron Inquisition",
					"The Conqueror either speeds up a conquest or fortifies a captured point by adding points to the progress value",
					200, 4),
			new Ability(2, "Atlas Hands", "The Medic restores a portion of health of every ally nearby", 60, 1),
			new Ability(3, "Golems Eye",
					"The Spy briefly increases his radius of sight and gains vision over a large area around him", 180,
					3),
			new Ability(4, "Twilight Shard",
					"The Saboteur becomes invisible for all enemy players and disappears from the radar", 240, 5) };

	/**
	 * The Thread the simulation runs on. As a static object, so we can stop it
	 * from any point in code.
	 */
	public static Thread simulationThread;

	public static void main(String[] args) throws IOException {
		startServer();
		System.out.println("-------------------------------------");
		loadSimulationData();
		System.out.println("-------------------------------------");
		initializeGame();
		//System.out.println("-------------------------------------");
		testRelatedStuff();
		System.out.println("-------------------------------------");
		startSimulation();
	}

	/**
	 * Start server on Port
	 * 
	 * @throws IOException
	 * @author Benedict
	 */
	static void startServer() throws IOException {
		System.out.println("Starting server...");
		HttpServer server = HttpServer.create(new InetSocketAddress(Configuration.port), 0);

		server.createContext(GameHandler.httpContext, new GameHandler());
		server.createContext(UserHandler.httpContext, new UserHandler());
		server.createContext(DataHandler.httpContext, new DataHandler());
		server.createContext(AdminHandler.httpContext, new AdminHandler());

		server.start();
	}

	static void testRelatedStuff() {
		System.out.println("Creating test dummies...");

		game.addUser(new User("Saileru", "1337M45T3R", Main.game.faculties.get(0)));
		User[] uArray = game.getUsersOfFaculty(0);
		uArray[0].position.setLocation(0, 0);
		uArray[0].setUserStatus("ACTIVE");
		uArray[0].stats.score = 30;
		
		game.addUser(new User("all", "illu", Main.game.faculties.get(1)));
		game.addUser(new User("seeing", "mi", Main.game.faculties.get(1)));
		game.addUser(new User("eye", "nati", Main.game.faculties.get(1)));
		
		uArray = game.getUsersOfFaculty(1);
		uArray[0].position.setLocation(1, -1);
		uArray[0].isAttacking = true;
		uArray[0].attackCooldown = 10;
		uArray[0].setUserStatus("ACTIVE");
		uArray[0].stats.score = 50;
		
		uArray[1].position.setLocation(-1, -1);
		uArray[1].isAttacking = true;
		uArray[1].attackCooldown = 10;
		uArray[1].setUserStatus("ACTIVE");
		uArray[1].stats.score = 20;
		
		uArray[2].position.setLocation(0, 1);
		uArray[2].isAttacking = true;
		uArray[2].attackCooldown = 10;
		uArray[2].setUserStatus("ACTIVE");
		uArray[2].stats.score = 10;
		
		game.addUser(new User("MAYAAAA", "1563395845", Main.game.faculties.get(-1)));
		
		User Ghis = new User("Judge Magister Ghis", "jmgh", Main.game.faculties.get(2));
		Ghis.setClass(1, 1);
		Ghis.setUserStatus("ACTIVE");
		Ghis.addExp(1500);
		Ghis.position.x = 48262555;
		Ghis.position.y = 11667958;
		Main.game.faculties.get(2).removeUser(Ghis);
		Main.game.faculties.get(2).addUser(Ghis);
		handler.UserHandler.checkConquest(Ghis);

	}

	static void initializeGame() {
		System.out.println("Launching game...");
		game = new Game(listOfFaculties, capturePoints, classes, abilities);
	}

	/**
	 * @author Benedict
	 */
	static void startSimulation() {
		System.out.println("Simulation is running...");
		simulationThread = new Thread() {
			long startTime;
			long tickCounter;

			@Override
			public void run() {

				startTime = currentMs();
				tickCounter = 0;

				while (true) {

					synchronized (game) {
						game.simulationStep();
					}

					propagateGameDataToMobileClients();

					endSimulationStep();
				}
			}

			void endSimulationStep() {

				long sleepTime = (startTime + Configuration.simulationTick * ++tickCounter) - currentMs();

				if (sleepTime <= 0) {
					System.err.println("TIME STEP ERROR.");

					// Throw exception to actually notice that the simulation
					// took a long time,
					// If we didn't throw an exception, we would never get the
					// feedback
					// (unless we log it somewhere or something)

					throw new IllegalStateException("The simulation was too slow, increase the"
							+ " timeStep for the simulation or balance load on multiple threads");
				}

				try {
					sleep(sleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			long currentMs() {
				return System.nanoTime() / 1000000;
			}
		};
		simulationThread.start();
	}

	/**
	 * Send all data necessary to the user. i.e. Positions and state of nearby
	 * players Scores
	 * 
	 * @author Benedict
	 */
	static void propagateGameDataToMobileClients() {
		// Nothing for now as the client polls all data via http methods.

		// The alternative to above mentioned http polls would be via GCM push
		// notifications.
	}

	/**
	 * Loads data of previous simulations into this one
	 */
	static void loadSimulationData() {
		System.out.println("Loading saved data from previous simulations...");
		loadFaculties();
	}

	/**
	 * Loads stored faculty data
	 */
	static void loadFaculties() {
		try {
			for (Faculty faculty : XMLTool.readAllFaculties()) {
				listOfFaculties[faculty.id + 1] = faculty;
				System.out.println("Faculty loaded: " + faculty.name + ":" + faculty.id);
			}
		} catch (Exception e) {
			System.err.println("Faculties and Users: FAILED");
			e.printStackTrace();
		}

	}

}
