package ca.uottawa.ui;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.lloseng.ocsf.server.AbstractServer;
import com.lloseng.ocsf.server.ConnectionToClient;

import ca.uottawa.schedule.*;
public class ScheduleGeneratorServer extends AbstractServer {

	  private ServerConsole serverUI;
	  private List<Course> courses;
	  private File courseCodes;
	
	public ScheduleGeneratorServer(int port, ServerConsole serverUI) {
		super(port);
		this.serverUI = serverUI;
		courseCodes = new File("ca/uottawa/schedule/courseCodes.csv");
		refreshCourses();
	}

	
	private void refreshCourses() {
		serverUI.display("Refreshing courses from " + courseCodes);
		courses = TextParser.getCoursesFromDatabase(courseCodes);
		serverUI.display("Courses updated.");
	}
	
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
		ScheduleMessage message = (ScheduleMessage) msg;
		String command = message.getCommand();
		
		switch(command.toUpperCase()) {
		case "SEARCH":
			String query = message.getStrings().get(0);
			List<String> results = CourseSearch.search(query, courses);
			ScheduleMessage reply = new ScheduleMessage();
			reply.setCommand("SEARCHRESULTS");
			reply.setStrings(results);
			try {
				client.sendToClient(reply);
			} catch (IOException e) {
				System.err.println("Error sending search results to client. Possible connection lost.");
				System.out.println(client);
			}
			break;
			default:;
		}

	}

	public void handleMessageFromServerUI(String message) {
		//Some plans:
		//refresh database
		//stop server
		//Get list of active users
		//...more ideas?
		String[] args = message.split(" ");
		
		switch (args[0].toUpperCase()) {
		case "SEARCH": List<String> searchResults = CourseSearch.search(args[1], courses);
		for (String s : searchResults) {
			serverUI.display(s);
		}
			break;
			default:
				
		}
	}
	
	  protected void serverStarted()
	  {
	    serverUI.display("Server listening for connections on port " + getPort());
	  }
	  
	  protected void serverStopped()
	  {
	    serverUI.display("Server has stopped listening for connections.");
	  }
	  
	  protected void serverClosed() {
		  serverUI.display("The server has been closed");
		  System.exit(0);
	  }

	  protected void clientConnected(ConnectionToClient client) {
		  serverUI.display("The client " + client.toString() + " has connected.");
	  }
	  
	  synchronized protected void clientDisconnected(ConnectionToClient client) {
		  serverUI.display(client.getInfo("loginID") + " has disconnected.");
	  }
	  
	  synchronized protected void clientException(ConnectionToClient client, Throwable exception) {
		  clientDisconnected(client);
	  }
}
