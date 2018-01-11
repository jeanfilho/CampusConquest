package main;

import java.awt.Polygon;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This class stores all the information related to a Faculty, for both long and
 * short term social context
 * 
 * @author Jean
 */

@SuppressWarnings("serial")
public class Faculty implements Comparable<Faculty>, java.io.Serializable {

	/*
	 * ------------------------ Faculty Game Data ------------------------
	 */
	// id and description
	public int id;
	public String name;
	public String description;

	// Users belonging to this faculty
	private HashMap<String, User> users = new HashMap<String, User>();

	// Points that currently belong to this faculty
	private HashMap<Integer, CapturePoint> capturedPoints = new HashMap<Integer, CapturePoint>();

	// Area where players of this faculty will regen health
	private Polygon healArea = new Polygon();
	private final static int healAmount = 5;

	/*
	 * -------------------- History of this faculty ------------------------
	 */
	// Points captured by this faculty over time
	private LinkedList<CaptureData> captureHistory = new LinkedList<CaptureData>();

	// Death and kill count by the users of this faculty
	private LinkedList<KillData> deathHistory = new LinkedList<KillData>();
	private LinkedList<KillData> killHistory = new LinkedList<KillData>();

	// Biggest polygon (number of verts) build by this faculty
	private int biggestPolygon = 0;

	/*
	 * --------------------- Methods----------------------------
	 */
	
	/**
	 * DO NOT USE THIS CONSTRUCTOR!
	 */
	public Faculty(){
	}
	
	
	/**
	 * use this constructor if creating a new object
	 * @param id
	 * @param name
	 * @param description
	 * @param healArea
	 */
	public Faculty(int id, String name, String description, Polygon healArea) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.healArea = healArea;
	}

	/**
	 * @return the users of this faculty
	 */
	public HashMap<String, User> getUsers() {
		return users;
	}

	/**
	 * 
	 * @param status
	 *            : the status to be looked for in the users
	 * @return the users of this faculty that have the given status
	 */
	public User[] getUsers(String status) {
		LinkedList<User> ret = new LinkedList<User>();
		for (User user : users.values())
			if (user.getStatus().equals(status))
				ret.add(user);

		return ret.toArray(new User[0]);
	}

	/**
	 * @return the users that are currently dead in this faculty
	 */
	public User[] getDeadUsers() {
		LinkedList<User> ret = new LinkedList<User>();
		for (User user : users.values())
			if (user.hp <= 0)
				ret.add(user);

		return ret.toArray(new User[0]);
	}

	/**
	 * @return a list of only the users of this faculty IN ATTACK MODE
	 */
	public LinkedList<User> getUsersInAttackMode() {
		LinkedList<User> list = new LinkedList<User>();
		for (User user : users.values()) {
			if (user.isAttacking)
				list.add(user);
		}
		return list;
	}

	/**
	 * @return the dominatedPoints
	 */
	public HashMap<Integer, CapturePoint> getCapturedPoints() {
		return capturedPoints;
	}

	/**
	 * @param point
	 *            that is now taken
	 * @return true if success, false otherwise
	 */
	public boolean addCapturedPoint(CapturePoint cp) {
		capturedPoints.put(cp.id, cp);
		return capturedPoints.containsKey(cp.id);
	}

	/**
	 * @param point
	 *            that is now lost
	 * @return true if success, false otherwise
	 */
	public boolean removeCapturedPoint(CapturePoint cp) {
		capturedPoints.remove(cp.id);
		return capturedPoints.containsKey(cp.id);
	}

	/**
	 * @param user
	 *            to be added to the user set
	 * @return true if succesfully added false otherwise
	 */
	public boolean addUser(User user) {
		if(users.containsKey(user.facebookID)){
			System.err.println("***WARNING*** User already exists: " + user.name);
			return false;
		}
		users.put(user.facebookID, user);
		return users.containsKey(user.facebookID);
	}

	/**
	 * @param user
	 *            to be removed from the user set
	 * @return true if succesfully added false otherwise
	 */
	public boolean removeUser(User user) {
		users.remove(user.facebookID);
		return !users.containsKey(user.facebookID);
	}

	/**
	 * @param time
	 *            : the date to which the midnight will be returned
	 * @return midnight time
	 */
	private Calendar getMidnight(Calendar time) {
		Calendar temp = Calendar.getInstance();
		temp.set(time.get(Calendar.YEAR), time.get(Calendar.MONTH), time.get(Calendar.DATE), 0, 0, 0);
		return temp;
	}

	/**
	 * @return the capture history of this faculty
	 */
	public LinkedList<CaptureData> getCaptureHistory() {
		return captureHistory;
	}

	/**
	 * @return the capture history for today (for the Scoreboard)
	 */
	@SuppressWarnings("unchecked")
	public LinkedList<CaptureData> getCaptureHistoryToday() {

		// Get current date and set the time to midnight
		Calendar midnight = getMidnight(Calendar.getInstance());

		// retrieve all the data that happened only today
		Iterator<CaptureData> it = captureHistory.descendingIterator();
		while (it.next().time.after(midnight))
			it = (Iterator<CaptureData>) it.next();

		return (LinkedList<CaptureData>) captureHistory.subList(captureHistory.lastIndexOf(it), captureHistory.size());
	}

	/**
	 * @param point
	 *            that's captured by this faculty
	 * @return true if succesfull, false otherwise
	 */
	public boolean addCaptureHistoryEntry(CapturePoint cp) {
		return captureHistory.add(new CaptureData(cp));
	}

	/**
	 * @return the kill history of this faculty
	 */
	public LinkedList<KillData> getKillHistory() {
		return killHistory;
	}

	/**
	 * @return the kill history for today (for the Scoreboard)
	 */
	@SuppressWarnings("unchecked")
	public LinkedList<KillData> getKillHistoryToday() {

		// Get current date and set the time to midnight
		Calendar midnight = getMidnight(Calendar.getInstance());

		// retrieve all the data that happened only today
		Iterator<KillData> it = killHistory.descendingIterator();
		while (it.next().time.after(midnight))
			it = (Iterator<KillData>) it.next();

		return (LinkedList<KillData>) killHistory.subList(killHistory.lastIndexOf(it), killHistory.size());
	}

	/**
	 * @param a
	 *            kill by a member of this faculty
	 * @return true if succesfull, false otherwise
	 */
	public boolean addKillHistoryEntry(User victim) {
		boolean ret = deathHistory.add(new KillData(this, victim));
		System.out.println("Added killHistory entry in faculty '" + name + "'. User killed '" + victim.name + "': " + ret);
		return ret;
	}

	/**
	 * @return the death history for today (for the Scoreboard)
	 */
	@SuppressWarnings("unchecked")
	public LinkedList<KillData> getDeathHistoryToday() {

		// Get current date and set the time to midnight
		Calendar midnight = getMidnight(Calendar.getInstance());

		// retrieve all the data that happened only today
		Iterator<KillData> it = deathHistory.descendingIterator();
		while (it.next().time.after(midnight))
			it = (Iterator<KillData>) it.next();

		return (LinkedList<KillData>) deathHistory.subList(deathHistory.lastIndexOf(it), deathHistory.size());
	}

	/**
	 * @param a
	 *            death by a member of this faculty
	 * @return true if succesfull, false otherwise
	 */
	public boolean addDeathHistoryEntry(Faculty killerFaculty, User victim) {
		boolean ret = deathHistory.add(new KillData(killerFaculty, victim));
		System.out.println("Added deathHistory entry in faculty '" + name + "'. Death from '" + victim.name + "': " + ret);
		return ret;
	}

	/**
	 * @return the biggestPolygon ever made by this faculty
	 */
	public int getBiggestPolygon() {
		return biggestPolygon;
	}

	/**
	 * @param biggestPolygon
	 *            the new biggestPolygon ever
	 */
	public void checkBiggestPolygon(int size) {
		if (size > this.biggestPolygon) {
			System.out.println("Biggest polygon from '" + name + "' registered with size: " + size);
			this.biggestPolygon = size;
		}
	}

	/**
	 * Gives the score of this faculty
	 * 
	 * @return the sum of the scores of each player
	 */
	public int getScore() {
		int res = 0;
		for (User user : users.values())
			res += user.stats.score;
		return res;
	}

	/**
	 * @return the top ten or less players in this class
	 */
	public List<User> getTopTenUsers() {
		LinkedList<User> list = new LinkedList<User>();
		list.addAll(users.values());
		sortUsersByScore(list);
		if (list.size() >= 10)
			return list.subList(0, 10);
		else
			return list.subList(0, list.size());
	}

	/**
	 * Sorts the given list of users through score Biggest comes first
	 */
	private void sortUsersByScore(LinkedList<User> users) {
		Collections.sort(users);
	}

	/**
	 * Heals players inside the heal area
	 */
	public void healPlayersInArea() {
		for (User user : users.values())
			if (healArea.contains(user.position))
				user.heal(healAmount);
	}

	public User findUser(String facebookID) {
		return users.get(facebookID);
	}

	@Override
	public int compareTo(Faculty other) {
		return Integer.compare(id, other.id);
	}

	/*---------------------------------FOR XML STORAGE ONLY---------------------------*/

	public Polygon getHealArea() {
		return healArea;
	}

	public void setHealArea(Polygon healArea) {
		this.healArea = healArea;
	}

	public LinkedList<KillData> getDeathHistory() {
		return deathHistory;
	}

	public void setDeathHistory(LinkedList<KillData> deathHistory) {
		this.deathHistory = deathHistory;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public static int getHealamount() {
		return healAmount;
	}

	public void setUsers(HashMap<String, User> users) {
		this.users = users;
	}

	public void setCaptureHistory(LinkedList<CaptureData> captureHistory) {
		this.captureHistory = captureHistory;
	}

	public void setKillHistory(LinkedList<KillData> killHistory) {
		this.killHistory = killHistory;
	}

	public void setBiggestPolygon(int biggestPolygon) {
		this.biggestPolygon = biggestPolygon;
	}
}
