package ca.uottawa.ui;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;



public class ServerConsole {
	  //Class variables *************************************************

	  /**
	   * The default port to listen on.
	   */
	  final public static int DEFAULT_PORT = 5555;
	  
	  //Instance variables **********************************************
	  
	  /**
	   * The instance of the server that created this ServerConsole.
	   */
	  ScheduleGeneratorServer server;

	  
	  //Constructors ****************************************************

	  /**
	   * Constructs an instance of the ServerConsole UI, starting a server.
	   *k
	   * @param port The port to connect on.
	   */
	  public ServerConsole(int port, boolean autoRestart) 
	  {
	      server = new ScheduleGeneratorServer(port, this, autoRestart);
	      try 
		    {
		      server.listen(); //Start listening for connections
		    } 
		    catch (Exception ex) 
		    {
		      System.out.println("ERROR - Could not listen for clients!");
		      System.out.println("Quitting application");
		      ex.printStackTrace(System.out);
		      System.exit(0);
		    }
	  }

	  
	  //Instance methods ************************************************
	  
	  /**
	   * This method waits for input from the console.  Once it is 
	   * received, it sends it to the client's message handler.
	   */
	  public void accept() 
	  {
	    try
	    {
	      BufferedReader fromConsole = 
	        new BufferedReader(new InputStreamReader(System.in));
	      String message;

	      while (true) 
	      {
	    	  //E50 b) i)
	        message = fromConsole.readLine();
	        server.handleMessageFromServerUI(message);
	      }
	    } 
	    catch (Exception ex) 
	    {
	      System.out.println
	        ("Unexpected error while reading from console!");
              ex.printStackTrace(System.out);
	    }
	  }


		/**
		 * Displays items in the server's console.
		 * @param message
		 */
	  public void display(String message) 
	  {
		  Calendar cal = Calendar.getInstance();
	    	cal.getTime();
	    	SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
	    System.out.println(sdf.format(cal.getTime()) + "> " + message);
	  }
	  
	  /**
	   * This method is responsible for the creation of 
	   * the ServerConsole UI, which will then start a server.
	   *
	   * @param args[0] The port number to listen on.  Defaults to DEFAULT_PORT constant
	   *          if no argument is entered.
	   */
	  public static void main(String[] args) 
	  {
	    int port = 0; //Port to listen on
	    boolean autoRestart = true;
	    try
	    {
	      port = Integer.parseInt(args[0]); //Get port from command line
	    }
	    catch(Throwable t)
	    {
	      port = DEFAULT_PORT; //Set port to the default
	    }
	    try
	    {
	      autoRestart = Boolean.parseBoolean(args[1]); //Get port from command line
	    }
	    catch(Throwable t)
	    {
	      autoRestart = true;
	    }
		//Create new server console.
	    ServerConsole server = new ServerConsole(port, autoRestart);
	    //Start accepting connections on server console.
	    server.accept();
	  }
}
