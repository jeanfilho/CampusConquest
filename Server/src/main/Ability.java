package main;

public class Ability {
	public int classID;
	public String name;
	public String description;
	public int cooldown;
	public int intFactor;
	
	public Ability(int classID, String name, String description, int cooldown, int intFactor) {
		this.classID = classID;
		this.name = name;
		this.description = description;
		this.cooldown = cooldown;
		this.intFactor = intFactor;
	}
}
