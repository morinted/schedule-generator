package ca.uottawa.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ServerStats implements Serializable {

	/**
	 * Default serial ID
	 */
	private static final long serialVersionUID = 1L;
	private List<String> users;
	private int numOfCourses;
	private int numOfOptional;
	private int	numOfElectives;
	private int numOfGenerations;
	private int numOfSchedules;
	private int numOfOptionalGenerations;
	
	public ServerStats() {
		users = new ArrayList<String>();
		numOfCourses = 0;
		numOfOptional = 0;
		numOfElectives = 0;
		numOfGenerations = 0;
		setNumOfOptionalGenerations(0);
		numOfSchedules = 0;
	}
	
	public void addUser(String user) {
		boolean exists = false;
		for (String s: users) {
			if (s.equals(user)) {
				exists = true;
			}
		}
		if (!exists) {
			users.add(user);
		}
	}
	
	public void addCourses(int num) {
		numOfCourses += num;
	}
	
	public void addOptional(int num) {
		numOfOptional += num;
	}
	
	public void addElectives(int num) {
		numOfElectives += num;
	}
	
	public void addGenerations(int num) {
		numOfGenerations += num;
	}
	
	public void addOptionalGenerations(int num) {
		numOfOptionalGenerations += num;
	}
	
	public void addSchedules(int num) {
		numOfSchedules += num;
	}

	public List<String> getUsers() {
		return users;
	}

	public void setUsers(List<String> users) {
		this.users = users;
	}

	public int getNumOfCourses() {
		return numOfCourses;
	}

	public void setNumOfCourses(int numOfCourses) {
		this.numOfCourses = numOfCourses;
	}

	public int getNumOfOptional() {
		return numOfOptional;
	}

	public void setNumOfOptional(int numOfOptional) {
		this.numOfOptional = numOfOptional;
	}

	public int getNumOfElectives() {
		return numOfElectives;
	}

	public void setNumOfElectives(int numOfElectives) {
		this.numOfElectives = numOfElectives;
	}

	public int getNumOfGenerations() {
		return numOfGenerations;
	}

	public void setNumOfGenerations(int numOfGenerations) {
		this.numOfGenerations = numOfGenerations;
	}

	public int getNumOfSchedules() {
		return numOfSchedules;
	}

	public void setNumOfSchedules(int numOfSchedules) {
		this.numOfSchedules = numOfSchedules;
	}

	public int getNumOfOptionalGenerations() {
		return numOfOptionalGenerations;
	}

	public void setNumOfOptionalGenerations(int numOfOptionalGenerations) {
		this.numOfOptionalGenerations = numOfOptionalGenerations;
	}
	
	
}
