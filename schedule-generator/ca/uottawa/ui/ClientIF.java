package ca.uottawa.ui;

import java.util.List;

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
}
