package ca.uottawa.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormatSymbols;
import java.util.List;

public class ClientConsole implements ClientIF {
	  /**
	   * The default port to connect on.
	   */
	  final public static int DEFAULT_PORT = 5555;
    boolean internalUserInput;
	  
	  /**
	   * The instance of the client that created this ConsoleChat.
	   */
	  ScheduleGeneratorClient client;
	  
	  public ClientConsole(String studentNumber, String host, int port) 
	  {
	    try 
	    {
          internalUserInput = false;
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

	        message = fromConsole.readLine();
	        client.handleMessageFromClientUI(message);
	    } 
	    catch (Exception ex) 
	    {
	      display
	        ("Unexpected error while reading from console!");
	    }
	  }
	  
	  

		  private void display(String message) 
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
		  }

		public void sendSearchResults(List<String> results) {
			//Receiving search results. We must display them:
            if (results.size() == 0) {
                display("No results for your current semester.");
            } else {
                display("Found " + results.size() + " courses:");
                for (String s : results) {
                    display(s);
                }
            }

		}

		@Override
		public void sendInfo(String msg) {
			display(msg);
		}

		@Override
		public String getSemester(List<String> semesters) {
			//go through available semesters and get user choice.
            int choice = 0;
            display("What semester would you like to build schedules for?");
            for (int i = 0; i < semesters.size(); i++) {
                String year;
                String month;
                year = semesters.get(i).substring(0, 4);
                month = new DateFormatSymbols().getMonths()[Integer.parseInt(semesters.get(i).substring(4))-1];
                display((i+1) + ". " + month + " " + year);
            }
            display("Select your choice. (Number)");
            String response = readFromConsole();
            while (response.length() < 1) {
                display("Select your choice. To select the first semester on the list, write '1', etc.");
                response = readFromConsole();
            }
            boolean valid = false;
            while (!valid) {
            try {
                choice = Integer.parseInt(response);
                if ((choice <= semesters.size()) && (choice > 0)) {
                    valid = true;
                } else {
                    display("That number is not in the list.");
                    response = readFromConsole();
                }
            } catch (NumberFormatException e) {
                display("You must enter a valid number");
                response = readFromConsole();
            }
            }

            display("sending back " + semesters.get(choice - 1));
            return semesters.get(choice-1);
		}

    @Override
    public void done() {
        accept();
    }

    @Override
    public String getSortOrder() {
        String sortOrder = null;
        display("Please specify a sort order for your schedules");
        display("1. Earliest Starts");
        display("2. Latest Starts");
        display("3. Eariiest Ends");
        display("4. Latest Ends");
        display("5. Longest Days");
        display("6. Shortest Days");
        display("7. Days Per Week");
        display("8. Days Off Per Week");

        display("Note that options 1-6 will sort days off as a secondary priority. Select your choice. (Number)");
        String response = readFromConsole();
        while (response.length() < 1) {
            display("Select your choice. To select the first semester on the list, write '1', etc.");
            response = readFromConsole();
        }
        int choice = 0;
        boolean valid = false;
        while (!valid) {
            try {
                choice = Integer.parseInt(response);
                if ((choice <= 8) && (choice > 0)) {
                    valid = true;
                } else {
                    display("That number is not in the list.");
                    response = readFromConsole();
                }
            } catch (NumberFormatException e) {
                display("You must enter a valid number");
                response = readFromConsole();
            }
        }

        switch (choice) {
            case 1: sortOrder = "earliestStart";

            case 2: sortOrder = "latestStart";

            case 3:sortOrder = "earliestEnd";

            case 4:sortOrder = "latestEnd";

            case 5:sortOrder = "longestDay";

            case 6:sortOrder = "shortestDay";

            case 7:sortOrder = "days";

            case 8:sortOrder = "daysOff";
        }

        return sortOrder;

    }

    private String readFromConsole() {
            String message = null;
            try
            {
                BufferedReader fromConsole =
                        new BufferedReader(new InputStreamReader(System.in));

                message = fromConsole.readLine();
            }
            catch (Exception ex)
            {
                display("Unexpected error while reading from console!");
            }
            return message;
        }
}
