package main;

import java.util.Calendar;

@SuppressWarnings("serial")
public class KillData extends EntryData {

	public int killerID = 0;
	public String victimID = "";

	public KillData() {
	}

	public KillData(Faculty killerFaculty, User victim) {
		time = Calendar.getInstance();
		this.killerID = killerFaculty.id;
		this.victimID = victim.facebookID;
	}

	public int getKillerID() {
		return killerID;
	}

	public void setKillerID(int killerID) {
		this.killerID = killerID;
	}

	public String getVictimID() {
		return victimID;
	}

	public void setVictimID(String victimID) {
		this.victimID = victimID;
	}
	
	public Calendar getTime() {
		return time;
	}

	public void setTime(Calendar time) {
		this.time = time;
	}
}