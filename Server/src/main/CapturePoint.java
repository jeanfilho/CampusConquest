package main;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.List;

/*
 *  The class CapturePoint represents one capture point aswell as the zone
 *  which has to be dominated in order to conquer the point
 *  
 *  NOTE: All coordinates are scaled by the factor 1000000 = 10^6
 *  since Polygon can't deal with doubles. All values are scaled from
 *  a form like xx.xxxxxx to xxxxxxxx internally
 *  The coordinates of the flag and the zone are already scaled up by default
 */

public class CapturePoint {
	public final int id;
	
	public int[] xpoints;
	public int[] ypoints;
	
	public int xFlag;
	public int yFlag;
	
	public String name;
	public String letter;
	
	public Polygon p;
	
	public class Contestants {
		public List<User> contestants;
	}
	public Contestants[] c;
	
	public double progress = 0.0;
	public int dominatingFaculty = -1;
	public boolean captured = false;	
	
	public CapturePoint(int id, String name, String letter, int xFlag, int yFlag, int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4) {
		this.id = id;
		this.name = name;
		this.letter = letter;
		
		this.xFlag = xFlag;
		this.yFlag = yFlag;
		
		xpoints = new int[] {x1, x2, x3, x4};
		ypoints = new int[] {y1, y2, y3, y4};
		p = new Polygon(xpoints, ypoints, 4);
	}
	
	
	public void createContestants(int nFaculties){
		c = new Contestants[nFaculties];
		for(int i = 0; i < c.length; i++) {
			c[i] = new Contestants();
			c[i].contestants = new ArrayList<User>();
		}	
	}
	
	/*
	 * called by a user during his update-method
	 * checks, whether a user is inside the capture-zone
	 * if yes, he is assigned to the list of his faculty
	 */
	public void checkIn(double x, double y, User u) {
		//int xn = ((int) x * 1000000);
		//int yn = ((int) y * 1000000);
		
		if(p.contains(x, y)) {
			System.out.println("User " + u.name + " is now in " + letter);
			c[u.faculty.id].contestants.add(u);
			u.CapturePoint = id;
		}
	}
	
	/*
	 *  called by a user during his update-method
	 *  checks, whether a user has left the capture-zone,
	 *  if yes, he gets removed from the lift of his faculty
	 */
	public void checkOut(User u) {
		//if(!p.contains(u.position.longitude*1000000, u.position.latitude*1000000)) {
		if(!p.contains(u.position)) {
			c[u.faculty.id].contestants.remove(u);
			u.CapturePoint = -1;
		}
	}
	
	/*
	 * This method is used to update the capture status of this point
	 * It changes :
	 *  - the progress on domination
	 *  - whether it gives points to a team
	 *  - which team is currently dominating
	 *  
	 */
	public void updateCaptureStatus() {
		// called once to reduce processing effort
		int dominating = dominatingFaculty();
		
		// if no faculty dominates, nothing happens
		if(dominating == -1)
			return;
		
		// if the faction is already taking the point
		if(dominating == dominatingFaculty) {
			// nothing happens, if the point is already captured
			if(progress >= 100)
				return;
			else { // otherwise the point will be captured
				progress = progress + getDominance(dominating);
				
				if(progress >= 100 && !captured) {
					progress = 100;
					System.out.println("Point " + this.letter + " dominated by " + Main.game.faculties.get(dominating).name + ":" + (int)progress + "%");
					for(User u:c[dominatingFaculty].contestants) {
						u.addExp(30);
						u.stats.score = u.stats.score + 100;
						u.stats.captures = u.stats.captures + 1;
					}
					captured = true;
					
					//Registering event on Faculty
					Main.game.faculties.get(dominatingFaculty).addCaptureHistoryEntry(this);
					return;
				}
				System.out.println("Point " + this.letter + " is being captured by " + Main.game.faculties.get(dominating).name + ":" + (int)progress + "%");
			}
		}
		// if the point momentarily belongs to another faction
		else {
			if(progress <= 0) {
				dominatingFaculty = dominating; // now its the turn of the dominating Faculty
				return;
			}
			progress = progress - getDominance(dominating);
			if(captured)
				captured = false; // the point is set free
			
		}
	}
	
	// determines the dominating faction, returns -1 if there is none
	public int dominatingFaculty() {
		int[] size = {c[0].contestants.size(), 
				c[1].contestants.size(), 
				c[2].contestants.size(), 
				c[3].contestants.size(), 
				c[4].contestants.size()};
		if(XBiggerThanYZVW(size[0], size[1], size[2], size[3], size[4]))
			return 0;
		else if(XBiggerThanYZVW(size[1], size[0], size[2], size[3], size[4]))
			return 1;
		else if(XBiggerThanYZVW(size[2], size[0], size[1], size[3], size[4]))
			return 2;
		else if(XBiggerThanYZVW(size[3], size[0], size[1], size[2], size[4]))
			return 3;
		else if(XBiggerThanYZVW(size[4], size[0], size[1], size[2], size[3]))
			return 4;
		return -1;
	}
	
	// Mathematical function, checks whether one value is higher than 4 others
	public boolean XBiggerThanYZVW(int x, int y, int z, int v, int w) {
		if(x > y && x > z && x > v && x > w)
			return true;
		return false;
	}
	
	public double getDominance(int faction) {
		double dominance = 0;
		for(User u : c[faction].contestants)
			dominance = dominance + (((double) u.dominance)/7.0 + 1);
		return dominance;
	}
}
