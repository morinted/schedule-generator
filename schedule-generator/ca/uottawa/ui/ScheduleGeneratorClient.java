package ca.uottawa.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ca.uottawa.schedule.ScheduleMessage;

import com.lloseng.ocsf.client.AbstractClient;

public class ScheduleGeneratorClient extends AbstractClient {

	private String studentNumber;
	private String semester;
	private ClientIF clientUI;
	

	public ScheduleGeneratorClient(String studentNumber, String host, int port, ClientConsole clientUI) throws IOException {
		    super(host, port); //Call the superclass constructor
		    this.clientUI = clientUI;
		    this.studentNumber = studentNumber;
		    openConnection();
		    
		    if (this.semester == null) {
		    	this.semester = clientUI.getSemester();
		    }
		    //Send message to log in
		  }
	
	protected void handleMessageFromServer(Object msg) {
		ScheduleMessage message = (ScheduleMessage) msg;
		String command = message.getCommand();
		switch (command) {
			case "SEARCHRESULTS": //Server has given us search results. Yay!
				clientUI.sendSearchResults(message.getStrings());
			break;
			default:;
		}
	}
	
	public void quit()
	  {
	    try
	    {
	      closeConnection();
	    }
	    catch(IOException e) {}
	    System.exit(0);
	  }
	
	  public void connectionClosed() {
		  clientUI.sendInfo("The connection has been closed.");
	  }
	  
	  public void connectionException(Exception exception) {
		  clientUI.sendInfo("Connection to server lost. Closing connection.");
		  quit();
		}

	public void handleMessageFromClientUI(String msg) throws IOException {
		//Woot.
		//Add <coursecode>
		//Search <partial couse-code
		//edit <course>
		//Other plans?
		String[] command = ((String) msg).split(" "); //Split string from client into a bunch of 
		switch (command[0].toUpperCase()) {
		case "SEARCH": 
			ScheduleMessage srchMsg = new ScheduleMessage();
			srchMsg.setCommand("SEARCH");
			List<String> query = new ArrayList<String>();
			query.add(command[1]);
			srchMsg.setStrings(query);
			sendToServer(srchMsg);
			break;
			default:
		}
	}

}
