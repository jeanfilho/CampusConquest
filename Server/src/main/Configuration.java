package main;

public class Configuration {
	
	/**
	 * port number the server will listen to for http connections
	 */
	public static final int port = 9097;
	
	/**
	 * milliseconds that each tick will take,
	 * if the sim. step finishes faster, we will wait the rest of the tick
	 * 
	 * If the simulation finishes slower, increase timestep or balance load on multiple threads
	 */
	public static final int simulationTick = 2000;


	/**
	 * Maximum distance between two players to connect both into a mesh.
	 * TODO: confirm a good distance
	 */
	public static final long MESHDISTANCE = 1000;
}
