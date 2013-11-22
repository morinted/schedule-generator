package ca.uottawa.ui;

import java.io.IOException;

import com.lloseng.ocsf.client.AbstractClient;

public class ScheduleGeneratorClient extends AbstractClient {

	private String studentNumber;
	private String semester;
	private ClientConsole clientUI;
	

	public ScheduleGeneratorClient(String studentNumber, String host, int port, ClientConsole clientUI) throws IOException {
		    super(host, port); //Call the superclass constructor
		    this.clientUI = clientUI;
		    this.studentNumber = studentNumber;
		    openConnection();
		    //Send message to log in
		  }
	
	protected void handleMessageFromServer(Object msg) {
		// TODO Auto-generated method stub

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
		  clientUI.display("The connection has been closed.");
	  }
	  
	  public void connectionException(Exception exception) {
		  clientUI.display("Connection to server lost. Closing connection.");
		  quit();
		}

	public void handleMessageFromClientUI(String message) {
		//Woot.
		//Add <coursecode>
		//Search <partial couse-code
		//edit <course>
		//Other plans?
	}

}
