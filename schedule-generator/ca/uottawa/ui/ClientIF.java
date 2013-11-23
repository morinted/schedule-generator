package ca.uottawa.ui;

import java.util.List;

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
}
