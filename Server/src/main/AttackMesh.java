package main;

import java.awt.Polygon;
import java.util.Collection;
import java.util.LinkedList;

/**
 * 
 * @author Benedict, Jean
 *
 */
public class AttackMesh {

	/*
	 * Faculty to which this mesh belongs to
	 */
	public final Faculty faculty;

	/**
	 * users and their points building the mesh
	 */
	private LinkedList<User> users = new LinkedList<User>();

	/**
	 * Amount of damage dealt to enemies within
	 */
	private long strength;

	/**
	 * Polygon built by this mesh
	 */
	private Polygon polygon = new Polygon();

	/*
	 * -----------------------------Methods----------------------------
	 */
	public AttackMesh(LinkedList<User> users, Faculty faculty) {
		System.out.println("Created attack mesh for faculty: " + faculty.name);
		for (User user : users) {
			System.out.println(user.name);
			this.users.add(user);
		}
		this.faculty = faculty;
	}

	/**
	 * Adds the users position if he is in range of any of other player from
	 * this mesh. If this mesh is empty, it just adds the player
	 * 
	 * @return true if user was added, else false
	 */
	public boolean addIfInRange(User user) {
		if (users.isEmpty()) {
			users.add(user);
			user.mesh = this;
			System.out.println(user.name + " joined a mesh");
			user.stats.polygons += 1;
			return true;
		}

		for (User meshUser : users) {
			if (!users.contains(user) && user.position.distance(meshUser.position) < Configuration.MESHDISTANCE) {
				users.add(user);
				user.mesh = this;
				System.out.println(user.name + " merged mesh with " + meshUser.name);
				meshUser.stats.polygons += 1;
				return true;
			}
		}
		return false;

	}

	/**
	 * Sort users and rebuild polygon, removes any player from the polygon if
	 * necessary and calculates the new attack mesh strength.
	 * 
	 * After it, deal damage to players inside this mesh that are from other
	 * faculties and if a kill happens, register it
	 * 
	 */
	public void dealDamage(Collection<Faculty> faculties) {

		// Destroy the previous polygon, sort the users and create a new one
		polygon = new Polygon();
		sortUserList();
		strength = 0;
		@SuppressWarnings("unchecked")
		LinkedList<User> nl = (LinkedList<User>) users.clone();
		for (User user : nl) {
			if (user.attackCooldown <= 0)
				users.remove(user);
			else {
				strength += user.getStrength();
				polygon.addPoint(user.position.x, user.position.y);
			}
		}

		// Check if it's the biggest polygon ever created by this faculty
		faculty.checkBiggestPolygon(polygon.npoints);

		// Deal damage to players
		for (Faculty fac : faculties) {
			if (fac.id != faculty.id) {
				for (User user : fac.getUsers().values()) {
					if (isInside(user)) {
						if (user.takeDamage(strength, faculty)) {
							faculty.addKillHistoryEntry(user);
							for(User u : users) {
								u.stats.score += 20;
								u.stats.kills += 1;
								u.addExp(30);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * This method sorts the linkedlist of users accourding to the following
	 * rule: 1.Start with first User of the list 2.Find the closest User to this
	 * one with index bigger than this 3.Swap the "next" user with the closest
	 * one 4.Advance one index and repeat step 2 until the end of the list
	 */
	private void sortUserList() {

		User nearest, current, h;
		int nIndex;

		//Remove all users that are not actively attacking
		for(User user: users){
			if(!user.isAttacking)
				users.remove(user);
		}
		
		for (int i = 0; i < users.size() - 1; i++) {
			current = users.get(i);
			nIndex = i + 1;
			nearest = users.get(nIndex);

			// find the nearest user to the current one
			for (int j = i + 2; j < users.size() - 2; j++) {
				h = users.get(j);
				if (h.position.distance(current.position) < nearest.position.distance(current.position)) {
					nearest = h;
					nIndex = j;
				}
			}
			// swap positions
			h = users.get(i + 1);
			users.set(i + 1, nearest);
			users.set(nIndex, h);
		}

	}

	/**
	 * Copies all users from one AttackMesh to another one
	 * 
	 * @target: AttackMesh receiving the values
	 * @source: AttackMesh giving the values
	 */
	public static void merge(AttackMesh target, AttackMesh source) {
		// merge the source into the target.
		for (User user : source.getUsers())
			if (!target.users.contains(user))
				user.mesh = target;
	}

	/**
	 * returns true if the user is inside the mesh. Exactly on the line counts
	 * as outside.
	 */
	public boolean isInside(User user) {
		if (polygon.contains(user.position))
			return true;
		else
			return false;
	}

	/**
	 * @return a list of sorted users in this attack mesh
	 */
	public LinkedList<User> getUsers() {
		if (!users.isEmpty())
			sortUserList();
		return users;
	}
	
	public boolean isEmpty(){
		return users.isEmpty();
	}
}
