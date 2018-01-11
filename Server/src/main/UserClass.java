package main;


/*
 * This class contains a class (ingame) that a player can be with different attributes to it
 */
public final class UserClass {
	
	public final int id;
	public final String name;
	public final String description;
	
	public UserClass(int id, String name, String description){
		this.id = id;
		this.name = name;
		this.description = description;
	}

}
