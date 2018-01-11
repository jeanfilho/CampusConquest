package data;

import java.beans.XMLEncoder;
import java.beans.XMLDecoder;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;

import main.Faculty;
import main.User;

/**
 * @author Jean
 * 
 *         This class stores data from a simulation and allows it to be loaded
 *         later on. The storage location is ./data
 */

public class XMLTool {
	/**
	 * Stores a faculty and it's data on a XML file
	 * 
	 * @param faculty
	 * @param id
	 * @throws Exception
	 */
	public static void writeFaculty(Faculty faculty) throws Exception {
		XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream("./src/data/faculty" + faculty.id)));
		encoder.writeObject(faculty);
		encoder.close();
	}

	/**
	 * Reads a Faculty XML file previously stored
	 * 
	 * @param id
	 *            : faculty ID
	 * @return a faculty object
	 * @throws Exception
	 * 
	 */
	public static Faculty readFaculty(int facultyID) throws Exception {
		XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream("./src/data/faculty" + facultyID)));
		Faculty faculty = (Faculty) decoder.readObject();
		decoder.close();
		return faculty;
	}

	/**
	 * Looks for all the faculties files stored in the data folder
	 * 
	 * @return all faculty files found as a Faculty object
	 * @throws Exception
	 */
	public static LinkedList<Faculty> readAllFaculties() throws Exception {

		LinkedList<Faculty> faculties = new LinkedList<Faculty>();

		for (String facFile : getFilesInData("faculty")) {
			faculties.add(readFaculty(Integer.parseInt(facFile.substring("faculty".length(), facFile.length()))));
		}
		return faculties;
	}

	/**
	 * Stores a user object as XML object
	 * 
	 * @param user
	 * @param facebookID
	 * @throws Exception
	 */
	public static void writeUser(User user) throws Exception {
		XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream("./src/data/user" + user.facebookID)));
		encoder.writeObject(user);
		encoder.close();
	}

	/**
	 * Reads a user object stored as XML object
	 * 
	 * @param facebookID
	 * @return
	 * @throws Exception
	 */
	public static User readUser(String facebookID) throws Exception {
		XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream("./src/data/user" + facebookID)));
		User user = (User) decoder.readObject();
		decoder.close();
		return user;
	}

	/**
	 * Reads all user objects stored as XML object in the data directory
	 * 
	 * @return
	 * @throws Exception
	 */
	public static LinkedList<User> readAllUsers() throws Exception {
		LinkedList<User> users = new LinkedList<User>();

		for (String userFile : getFilesInData("user")) {
			users.add(readUser(userFile.substring("user".length(), userFile.length())));
		}

		return users;
	}

	/**
	 * Get all the files in data directory with the specified prefix
	 * 
	 * @param str
	 *            : the prefix
	 * @return files
	 */
	public static ArrayList<String> getFilesInData(String prefix) {
		ArrayList<String> results = new ArrayList<String>();

		File[] files = new File("./src/data/").listFiles();
		// If this pathname does not denote a directory, then listFiles()
		// returns null.
		if (files != null)
			for (File file : files) {
				String name = file.getName().substring(0, prefix.length());
				if (file.isFile() && name.equals(prefix)) {
					results.add(file.getName());
				}
			}
		return results;
	}
}