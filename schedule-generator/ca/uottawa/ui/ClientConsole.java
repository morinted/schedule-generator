package ca.uottawa.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ClientConsole {
	  /**
	   * The default port to connect on.
	   */
	  final public static int DEFAULT_PORT = 5555;
	  
	  /**
	   * The instance of the client that created this ConsoleChat.
	   */
	  ScheduleGeneratorClient client;
	  
	  public ClientConsole(String studentNumber, String host, int port) 
	  {
	    try 
	    {
	      client= new ScheduleGeneratorClient(studentNumber, host, port, this);
	    } 
	    catch(IOException exception) 
	    {
	      display("Error: Can't setup connection!"
	                + " Terminating client.");
	      System.exit(1);
	    }
	  }
	  
	  public void accept() 
	  {
	    try
	    {
	      BufferedReader fromConsole = 
	        new BufferedReader(new InputStreamReader(System.in));
	      String message;

	      while (true) 
	      {
	        message = fromConsole.readLine();
	        client.handleMessageFromClientUI(message);
	      }
	    } 
	    catch (Exception ex) 
	    {
	      display
	        ("Unexpected error while reading from console!");
	    }
	  }
	  
	  

		  public void display(String message) 
		  {
		    System.out.println(message);
		  }
		  
		  public static void main(String[] args) 
		  {
			String studentNumber = ""; //For storing the student number
		    String host = "";
		    int port;
			//S# is a required parameter.
		    try
		    {
		      studentNumber = args[0]; //Gets the login ID.
		    }
		    catch(ArrayIndexOutOfBoundsException e) //If the user didn't provide one, then the client disconnects.
		    {
		    	System.out.println("You need to provide a student number. Aborting");
		    	System.exit(0);
		    }
		    try //Gets host param is necessary.
		    {
		      host = args[1];
		    }
		    catch(ArrayIndexOutOfBoundsException e)
		    {
		      host = "localhost";
		    }
		    
		    try //Get port if needed
		    {
		    	port = Integer.parseInt(args[2]); //Try to get it after the host
		    }
		    catch(ArrayIndexOutOfBoundsException e)
		    {
		    	port = DEFAULT_PORT; //Else default to the default port.
		    } 
		    
		    ClientConsole chat= new ClientConsole(studentNumber, host, port);
		    chat.accept();  //Wait for console data
		  }
}
