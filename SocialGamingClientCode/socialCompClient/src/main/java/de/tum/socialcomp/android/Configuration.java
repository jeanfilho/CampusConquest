package de.tum.socialcomp.android;

/**
 * Simple class that holds all configuration specific information for the game.
 * 
 * @author Niklas Klügel
 *
 */

public class Configuration {
	// URL of the Game Server
    // if the server runs on your PC use your IP with :9097 (!!!) in the end
    // get your IP by executing ipconfig in the cmd. Look at IPv4 for your IP.
	public static final String ServerURL = "http://188.195.13.111:9097";
	
	// TODO: This is the minimum distance the user should have moved to trigger a location update 
	public static final float MinimumDistanceForLocationUpdates = 1f; //might this cause some of our quite inaccurate position updates?

	/*lat/long of MI HS1 in Garching as default*/
	public static final double DefaultLongitude = 11.669094;
	public static final double DefaultLatitude = 48.262436;
	
	public static final String GoogleCloudMessagingSenderID = "585987882659";

}
