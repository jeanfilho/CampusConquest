package main;

import java.util.HashMap;
import java.util.LinkedList;

import data.XMLTool;

public class Game {

	/*
	 * Set of faculties playing this game
	 */
	public final HashMap<Integer, Faculty> faculties = new HashMap<Integer, Faculty>();

	/*
	 * Capture Points available at this game
	 */
	public final LinkedList<CapturePoint> capturePoints = new LinkedList<CapturePoint>();

	/*
	 * HashMap of lists of attack meshes Each faculty has its own "bucket" of
	 * attack meshes
	 */
	public final HashMap<Faculty, LinkedList<AttackMesh>> attackMeshes = new HashMap<Faculty, LinkedList<AttackMesh>>();

	/**
	 * Classes available to players in this game
	 */
	public final HashMap<Integer, UserClass> classes = new HashMap<Integer, UserClass>();

	/*
	 * Array of abilities, which can be acquired by each player
	 */
	public Ability[] abilities;

	public int[] levelBorders = { 0, 20, 50, 100, 170, 300, 500, 750, 1000, 1500 };
	public int[][] upgrades = { { 3, 3, 0, 0, 0 }, { 2, 1, 0, 3, 0 }, { 1, 0, 3, 1, 1 }, { 0, 0, 2, 1, 3 },
			{ 0, 4, 1, 0, 1 } };

	/*-----------------------------------------Methods-----------------------------------------------*/

	/**
	 * Initiates a game
	 * 
	 * @param faculties
	 *            : faculties in this game
	 * @param capturePoints
	 *            : capture points in this game
	 */

	public Game(Faculty[] faculties, CapturePoint[] capturePoints, UserClass[] classes, Ability[] abilities) {
		for (Faculty faculty : faculties) {
			this.faculties.put(faculty.id, faculty);
			attackMeshes.put(faculty, new LinkedList<AttackMesh>());
			System.out.println("Faculty '" + faculty.name + "' added to the game");
		}
		for (CapturePoint cp : capturePoints) {
			cp.createContestants(this.faculties.size());
			this.capturePoints.add(cp);
			System.out.println("CapturePoint '" + cp.name + "-" + cp.letter + "' added");
		}

		for (UserClass uc : classes) {
			this.classes.put(uc.id, uc);
			System.out.println("UserClass '" + uc.name + "' added");
		}

		this.abilities = abilities.clone();
		for (Ability a : this.abilities)
			System.out.println("Ability '" + a.name + "' added");

	}

	/**
	 * The object is synchronized at this point. Proceed to do anything that has
	 * to be done once every tick here.
	 */
	public void simulationStep() {
		for (User u : getUsersWithStatus("ACTIVE"))
			u.addPlaytimeSecond();
		
		for (CapturePoint cp : capturePoints)
			// int i = 0; i < capturePoints.size(); i++)
			capturePoints.get(cp.id).updateCaptureStatus();

		solveAttackMeshes();
		for (LinkedList<AttackMesh> meshes : attackMeshes.values())
			dealDamage(meshes);
		regeneratePlayers();

		for (Faculty faculty : faculties.values()) {
			faculty.healPlayersInArea();
			try {
				XMLTool.writeFaculty(faculty);
			} catch (Exception e) {
				System.err.println("Failed to store faculty data after this loop");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Adds a user into the game
	 * 
	 * @param user
	 *            : the player being added to the game
	 * @return true : if success, false otherwise
	 */

	public boolean addUser(User user) {
		boolean res = faculties.get(user.faculty.id).addUser(user);
		System.out.println("Adding user '" + user.name + "' with ID '" + user.facebookID + "' to game: " + res);
		return res;
	}

	public String SuperClass(int c1, int c2) {
		String c = "";

		if (c1 == -1 && c2 == -1)
			return "";

		if (c1 == 0) {
			switch (c2) {
			case 1:
				c = "Titan";
				break;
			case 2:
				c = "Strider";
				break;
			case 3:
				c = "Headhunter";
				break;
			case 4:
				c = "Recon";
				break;
			}
		} else if (c1 == 1) {
			switch (c2) {
			case 0:
				c = "Inquisitor";
				break;
			case 2:
				c = "Sentinel";
				break;
			case 3:
				c = "Vanguard";
				break;
			case 4:
				c = "Infiltrator";
				break;
			}
		} else if (c1 == 2) {
			switch (c2) {
			case 0:
				c = "Marine";
				break;
			case 1:
				c = "Guardian";
				break;
			case 3:
				c = "Agent";
				break;
			case 4:
				c = "Ghost";
				break;
			}
		} else if (c1 == 3) {
			switch (c2) {
			case 0:
				c = "Rogue";
				break;
			case 1:
				c = "Scout";
				break;
			case 2:
				c = "Pioneer";
				break;
			case 4:
				c = "Shadow";
				break;
			}
		} else if (c1 == 4) {
			switch (c2) {
			case 0:
				c = "Predator";
				break;
			case 1:
				c = "Ranger";
				break;
			case 2:
				c = "Buccaneer";
				break;
			case 3:
				c = "Assassin";
				break;
			}
		}
		return c;
	}

	/**
	 * Inserts, merges or removes attack meshes in the AttackMeshes Each player
	 * is part of only one mesh.
	 * 
	 */
	private void solveAttackMeshes() {
		// Removes empty attackMeshes
		for(LinkedList<AttackMesh> l : attackMeshes.values())
			for(AttackMesh am : l)
				if(am.getUsers().size() < 3){
					System.out.println("Attack Mesh from " + am.faculty.name +  " dismantled due to lack of players");
					attackMeshes.get(am.faculty).remove(am);
				}
		
		
		for (Faculty faculty : faculties.values()) {
			LinkedList<User> list = faculty.getUsersInAttackMode();
			for (User user : list) {
				// skips faculties that don't have any users yet or players that
				// are not attacking
				if (user == null || !user.isAttacking) 
					break;
				

				/*
				 * Try to fit player in any of the existing meshes. If none
				 * matches, create a new mesh for this player alone.
				 */
				for (AttackMesh mesh : attackMeshes.get(faculty))
					if (mesh.addIfInRange(user)) {
						// merge the meshes
						if (user.mesh != null)
							AttackMesh.merge(mesh, user.mesh);
					}
				if (user.mesh == null) {
					AttackMesh newMesh = new AttackMesh(new LinkedList<User>(), faculty);
					newMesh.addIfInRange(user);
					attackMeshes.get(faculty).add(newMesh);
				}
			}
		}
	}

	/**
	 * Deal the damage to each player inside another faculties mesh
	 * 
	 * @param attackMeshes
	 */
	void dealDamage(LinkedList<AttackMesh> attackMeshes) {
		for (AttackMesh mesh : attackMeshes) {
			mesh.dealDamage(faculties.values());

		}
	}

	/**
	 * All resting players regenerate.
	 */
	void regeneratePlayers() {
		for (Faculty faculty : faculties.values()) {
			for (User user : faculty.getUsers().values())
				if (!user.isAttacking) {
					user.energy += user.energyRegenerationRate;
					user.heal(user.hpRegenerationRate);
				}
		}
	}

	public User findUser(String facebookID) {
		for (Faculty faculty : faculties.values())
			for (User user : faculty.getUsers().values()) {
				if (user.facebookID.equals(facebookID)) {
					return user;
				}
			}
		return null;
	}

	/**
	 * OBSOLETE public int getNumberOfUsersOfFaculty(int facultyID) { return
	 * faculties.get(facultyID).getUsers().length; }
	 * 
	 * public int getNumberOfUsersWithStatus(String Status) { int n = 0; for
	 * (Faculty faculty : faculties.values()) for (User user :
	 * faculty.getUsers()) { if (user.Status.equals(Status)) n = n + 1; } return
	 * n; }
	 */

	/**
	 * @param facultyID
	 * @return an array with all the users of a faculty
	 */
	public User[] getUsersOfFaculty(int facultyID) {
		return faculties.get(facultyID).getUsers().values().toArray(new User[0]);
	}

	/**
	 * @param Status
	 *            : status to be checked
	 * @return an array containing users with the specified status
	 */
	public User[] getUsersWithStatus(String Status) {
		LinkedList<User> list = new LinkedList<User>();
		for (Faculty faculty : faculties.values())
			for (User user : faculty.getUsers().values()) {
				if (user.getStatus().equals(Status))
					list.add(user);
			}
		return list.toArray(new User[0]);
	}

	/**
	 * @return all users from all faculties
	 */
	public User[] getAllUsers() {
		LinkedList<User> ret = new LinkedList<User>();
		for (Faculty faculty : faculties.values())
			for (User user : faculty.getUsers().values())
				ret.add(user);

		return ret.toArray(new User[0]);
	}

	/**
	 * @return all users currently playing
	 */
	public User[] getAllActiveUsers() {
		LinkedList<User> ret = new LinkedList<User>();
		for (Faculty faculty : faculties.values())
			for (User user : faculty.getUsers().values())
				if (!user.getStatus().equals("INACTIVE"))
					ret.add(user);

		return ret.toArray(new User[0]);
	}
}
