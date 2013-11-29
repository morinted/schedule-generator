/**
 * A message to be send between the server and client. It has containers to send all kinds of messages.
 */
package ca.uottawa.schedule;

import java.io.Serializable;
import java.util.List;

public class ScheduleMessage implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * A container for all types of messages. Includes command,
	 * sort order, semester, ignore extras, a list of courses,
	 * a list of optionalCourses, an int k (of n), a list of strings
	 * and a list of schedules.
	 */
	String command;
	String sortOrder;
	String semester;
	boolean ignoreExtras;
	List<Course> courses;
	List<Course> optionalCourses;
	int k;
	List<String> strings;
	List<Schedule> schedules;

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    /**
     * A full constructor.
     * @param command
     * @param sortOrder
     * @param semester
     * @param ignoreExtras
     * @param courses
     * @param strings
     * @param schedules
     * @param optionalCourses
     * @param k
     */
    public ScheduleMessage(String command, String sortOrder, String semester, boolean ignoreExtras, List<Course> courses, List<String> strings, List<Schedule> schedules, List<Course> optionalCourses, int k) {
        this.command = command;
		this.sortOrder = sortOrder;
		this.ignoreExtras = ignoreExtras;
		this.courses = courses;
		this.strings = strings;
		this.schedules = schedules;
        this.optionalCourses = optionalCourses;
        
	}
	
	public List<Course> getOptionalCourses() {
		return optionalCourses;
	}

	public void setOptionalCourses(List<Course> optionalCourses) {
		this.optionalCourses = optionalCourses;
	}

	public int getK() {
		return k;
	}

	public void setK(int k) {
		this.k = k;
	}

	/**
	 * A blank constructor.
	 */
	public ScheduleMessage() {
	this.command = null;
	this.sortOrder = null;
	this.semester = null;
	this.ignoreExtras = false;
	this.courses = null;
	this.strings = null;
	this.schedules = null;
}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}

	public boolean isIgnoreExtras() {
		return ignoreExtras;
	}

	public void setIgnoreExtras(boolean ignoreExtras) {
		this.ignoreExtras = ignoreExtras;
	}

	public List<Course> getCourses() {
		return courses;
	}

	public void setCourses(List<Course> courses) {
		this.courses = courses;
	}

	public List<String> getStrings() {
		return strings;
	}

	public void setStrings(List<String> strings) {
		this.strings = strings;
	}

	public List<Schedule> getSchedules() {
		return schedules;
	}

	public void setSchedules(List<Schedule> schedules) {
		this.schedules = schedules;
	}
}
