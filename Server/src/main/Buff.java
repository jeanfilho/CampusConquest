package main;

public class Buff {
	public int type;
	/*
	 * 0 = strength
	 * 1 = sight
	 * 2 = invisibility
	 */
	public int amount;
	public int duration; // in seconds
	
	public boolean active;
	
	public Buff(){
		active = false;
		amount = 0;
		type = 0;
		duration = 1;
	}
	
	public Buff(int type, int amount, int duration) {
		this.type = type;
		this.amount = amount;
		this.duration = duration;
		
		active = true;
	}
	
	public void tick() {
		duration = duration - ((int) Configuration.simulationTick/1000);
		if(duration <= 0)
			active = false;
	}
}