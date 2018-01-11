package main;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/*
 * Auxiliary classes to store history entries
 */
@SuppressWarnings("serial")
public abstract class EntryData implements java.io.Serializable {
	public Calendar time;

	@Override
	public String toString() {
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		String ret = df.format(time.getTime());
		return ret;
	}
}