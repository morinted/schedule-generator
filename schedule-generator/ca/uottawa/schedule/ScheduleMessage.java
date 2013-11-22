package ca.uottawa.schedule;

import java.io.Serializable;
import java.util.List;

public class ScheduleMessage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	String command;
	String sortOrder;
	String semester;
    boolean internal;
	boolean ignoreExtras;
	List<Course> courses;
	List<String> strings;
	List<Schedule> schedules;

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public boolean isInternal() {
        return internal;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    public ScheduleMessage(String command, String sortOrder, String semester, boolean ignoreExtras, List<Course> courses, List<String> strings, List<Schedule> schedules) {
        this.command = command;
		this.sortOrder = sortOrder;
		this.ignoreExtras = ignoreExtras;
		this.courses = courses;
		this.strings = strings;
		this.schedules = schedules;
        this.internal = internal;

	}
	
	public ScheduleMessage() {
    this.internal = false;
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
