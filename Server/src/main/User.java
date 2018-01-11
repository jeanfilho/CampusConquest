package main;

import java.awt.Point;

@SuppressWarnings("serial")
public class User implements Comparable<User>, java.io.Serializable {
	public String name;
	public String facebookID;
	public Stats stats;
	

	public Faculty faculty;
	protected AttackMesh mesh;
	private String Status = "INACTIVE";

	public int level = 1;
	public int ExP;

	public int maxhp = 20;
	public int hp = 20;
	public int hpRegenerationRate = 1;
	public boolean isAttacking;
	public long attackCooldown;
	public long energy;

	// Classes
	public String className = "";
	public int[] classIndex = { -1, -1 };
	public int[] cooldown = new int[2];

	// Attributes
	public int life = 1;
	public int strength = 1;
	public int intelligence = 1;
	public int dominance = 1;
	public int sight = 1;

	public Buff strengthBuff;
	public Buff sightBuff;
	public Buff invisibilityBuff;


	public Point position;

	// Capture-Data
	public int CapturePoint = -1;

	public void addPlaytimeSecond() {
		stats.seconds = stats.seconds + ((int) Configuration.simulationTick/1000);
		//System.out.println(stats.seconds);
		if (stats.seconds == 60) {
			stats.seconds = 0;
			stats.minutes = stats.minutes + 1;
			if (stats.minutes == 60) {
				stats.minutes = 0;
				stats.hours = stats.hours + 1;

			}
		}

		// Expire Buffs
		if (strengthBuff != null) {
			strengthBuff.tick();
			if (!strengthBuff.active)
				strengthBuff = null;
		}
		if (sightBuff != null) {
			sightBuff.tick();
			if (!sightBuff.active)
				sightBuff = null;
		}
		if (invisibilityBuff != null) {
			invisibilityBuff.tick();
			if (!invisibilityBuff.active)
				invisibilityBuff = null;
		}

		// Reduce Cooldown
		for (int i = 0; i < 2; i++) {
			if (cooldown[i] > 0)
				cooldown[i] = cooldown[i] - ((int) Configuration.simulationTick/1000);
		}
		
		// Reduce AttackCooldown 
		if(attackCooldown > 0)
			attackCooldown -= ((int) Configuration.simulationTick/1000);
		else{
			isAttacking = false;
		}
			
	}

	/**
	 * Per second
	 */
	public long energyRegenerationRate = 4;
	/**
	 * Energy needed to ENTER attack mode.
	 */
	public long minEnergyThreshold = 60;
	/**
	 * In milliseconds
	 */
	public long attackCooldownMax = 5000;

	/**
	 * DO NOT USE THIS CONSTRUCTOR - ONLY FOR XML GENERATION
	 */
	public User() {
	}

	/*
	 * REMOVED - boolean variable replaces this public enum Mode{ ATTACK,
	 * PASSIVE; }
	 */

	public User(String name, String facebookID, Faculty f) {
		this.name = name;
		this.facebookID = facebookID;
		this.faculty = f;
		position = new Point();
		stats = new Stats();
	}

	@Override
	public String toString() {
		String json = "{\"name\":\"" + name + "\"," + "\"facebookID\":\"" + facebookID + "\"" + "\"faculty\":\"" + faculty.id + "}";
		return json;
	}

	public void setFaculty(Faculty faculty) {
		this.faculty = faculty;
	}

	public void setUserStatus(String nStatus) {
		if (nStatus.equals("ACTIVE"))
			Status = "ACTIVE";
		else if (nStatus.equals("INACTIVE"))
			Status = "INACTIVE";
		else if (nStatus.equals("DEAD"))
			Status = "DEAD";
		System.out.println(name + " is now " + Status);
	}

	public String getStatus() {
		return Status;
	}

	/**
	 * Damages the player
	 * 
	 * @parameter damage: the amount of damage received
	 * @parameter dealer: the id of the faculty dealing damage
	 * 
	 * @return true if it's a kill, false otherwise
	 */
	public boolean takeDamage(long damage, Faculty dealer) {
		if (Status.equals("ACTIVE")) {
			hp -= damage;
			System.out.println(name + " took " + damage + " damage, remains at " + hp + " health");

			// Death routine
			if (hp <= 0) {
				hp = 0;
				stats.deaths = stats.deaths + 1;
				faculty.addDeathHistoryEntry(dealer, this);
				// isAttacking = false;
				energy = 0;
				setUserStatus("DEAD");
				attackCooldown = attackCooldownMax;
				return true;
			}
		}
		return false;
	}

	public void addExp(int amount) {
		ExP = ExP + amount;
		while (level < 10 && ExP >= Main.game.levelBorders[level]) {
			level = level + 1;
			levelUp();
		}
		if(level >= 10){
			System.out.println(name + " received XP but is already at max level.");
		}
	}

	public void levelUp() {
		if(!className.equals("")) {
			// Adding bonus to the attributes
			life = life + Main.game.upgrades[classIndex[0]][0] + Main.game.upgrades[classIndex[1]][0];
			strength = strength + Main.game.upgrades[classIndex[0]][1] + Main.game.upgrades[classIndex[1]][1];
			intelligence = intelligence + Main.game.upgrades[classIndex[0]][2] + Main.game.upgrades[classIndex[1]][2];
			dominance = dominance + Main.game.upgrades[classIndex[0]][3] + Main.game.upgrades[classIndex[1]][3];
			sight = sight + Main.game.upgrades[classIndex[0]][4] + Main.game.upgrades[classIndex[1]][4];
	
			// Recalculating values
			maxhp = 16 + life * 4;
			hp = maxhp;
			System.out.println("User " + name + " leveled up!");
		}
	}

	public int getExpToNextLevel() {
		if (level < 10)
			return Main.game.levelBorders[level] - ExP;
		return 0;
	}

	public int getCurrentExP() {
		if (level < 10)
			return ExP - Main.game.levelBorders[level - 1];
		return 0;
	}

	public int getStrength() {
		int s = strength;
		if (strengthBuff != null)
			s = s + strengthBuff.amount;
		return s;
	}

	public int getSight() {
		int s = sight;
		if (sightBuff != null)
			s = s + sightBuff.amount;
		return s;
	}

	public void addBuff(Buff b) {
		if (b.type == 0) {
			if (strengthBuff != null) {
				b.amount = strengthBuff.amount + ((int) b.amount / 2);
				b.duration = Math.max(strengthBuff.duration, b.duration);
			}
			strengthBuff = b;
		} else if (b.type == 1) {
			if (sightBuff != null) {
				b.amount = sightBuff.amount + ((int) b.amount / 2);
				sightBuff.duration = Math.max(sightBuff.duration, b.duration);
			}
			sightBuff = b;
		} else if (b.type == 2) {
			if (invisibilityBuff != null) {
				b.duration = Math.max(invisibilityBuff.duration, b.duration);
			}
			invisibilityBuff = b;
		}
	}

	public void heal(int amount) {
		if (hp < maxhp) {
			hp = hp + amount;
			System.out.println(name + " healed " + amount + " hp");
			if (hp > maxhp)
				hp = maxhp;
			
			if(Status.equals("DEAD")) {
				if(hp >= 20)
					setUserStatus("ACTIVE");
			}
		}
	}

	public void setClass(int c1, int c2) {
		// setting the classes
		classIndex[0] = c1;
		classIndex[1] = c2;
		className = Main.game.SuperClass(c1, c2);
	}

	@Override
	public int compareTo(User o) {
		return Integer.compare(o.stats.score, stats.score);
	}

	/*-------------------XML STUFF--------------------------*/

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFacebookID() {
		return facebookID;
	}

	public void setFacebookID(String facebookID) {
		this.facebookID = facebookID;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		for (int i = 0; i < level; i++)
			levelUp();
	}

	public int getExP() {
		return ExP;
	}

	public void setExP(int exP) {
		ExP = exP;
	}

	public Faculty getFaculty() {
		return faculty;
	}
}