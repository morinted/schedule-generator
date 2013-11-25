package ca.uottawa.ui;

import java.util.List;

import ca.uottawa.schedule.Course;
import ca.uottawa.schedule.Schedule;

public interface ClientIF {
	//Client must display search results.
	public abstract void sendSearchResults(List<String> results);
	
	//Receive info from client.
	public abstract void sendInfo(String msg);
	
	//Client is requesting semester.
	public abstract String getSemester(List<String> semesters);

    //Server is done action.
    public abstract void done();

    public abstract String getSortOrder();
    
    //the client can use this method to show the user schedules.
    //In the GUI, the schedules are always visible, so this won't be used.
    //In the console, it is actually useful.
    public abstract void displaySchedules(List<Schedule> schedules);

    //Sends a list of courses to the user.
	public abstract void setCourses(List<Course> courses, List<Course> nCourses);

	//A course to be edited
	public abstract void editCourse(Course edit, String semester);

	//Client confirms that the program want to change semesters.
	public abstract boolean confirmSemester();

	//Client lets the UI know that a course was added.
	public abstract void courseAdded(String description);

	//Client lets the UI know that a course was not added because it already exists.
	public abstract void courseExists(String description);

	//Client lets the UI know that a course was not removed because it doesn't exist.
	public abstract void courseNotExists(String description);
	
	//Client lets the UI know that a course was removed.
	public abstract void courseRemoved(String description);

	//Client lets the UI know that schedules were genearated.
	public abstract void schedulesGenerated(int count);

	//Client lets the UI know that you can't generate without courses.
	public abstract void courseNone();
}
