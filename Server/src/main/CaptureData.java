package main;

import java.util.Calendar;

@SuppressWarnings("serial")
public class CaptureData extends EntryData implements java.io.Serializable{

	private String pName = "";
	private String pLetter = "";

	public CaptureData(CapturePoint c) {
		time = Calendar.getInstance();
		this.pName = c.name;
		this.pLetter = c.letter;
	}

	public CaptureData() {
	}

	public String getPName() {
		return pName;
	}

	public void setPName(String pName) {
		this.pName = pName;
	}

	public String getPLetter() {
		return pLetter;
	}

	public void setPLetter(String pLetter) {
		this.pLetter = pLetter;
	}
	public Calendar getTime() {
		return time;
	}

	public void setTime(Calendar time) {
		this.time = time;
	}
}