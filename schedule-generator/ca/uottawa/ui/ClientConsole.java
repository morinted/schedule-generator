package ca.uottawa.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.List;

import ca.uottawa.schedule.Activity;
import ca.uottawa.schedule.Course;
import ca.uottawa.schedule.Schedule;
import ca.uottawa.schedule.Section;

public class ClientConsole implements ClientIF {
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
		    System.out.println(">  " + message);
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
			//go through available semesters and get user choice.j
            String year;
            String month;
            int choice = 0;
            display("What semester would you like to build schedules for?");
            for (int i = 0; i < semesters.size(); i++) {

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
            year = semesters.get(choice-1).substring(0, 4);
            month = new DateFormatSymbols().getMonths()[Integer.parseInt(semesters.get(choice-1).substring(4))-1];
            display("You have selected semester: " + month + " " + year);
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
        display("3. Earliest Ends");
        display("4. Latest Ends");
        display("5. Longest Days");
        display("6. Shortest Days");
        display("7. Days Per Week");
        display("8. Days Off Per Week");

        display("Note that options 1-6 will sort days off as a secondary priority. Select your choice. (Number)");
        String response = readFromConsole();
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
break;
            case 2: sortOrder = "latestStart";
            break;
            case 3:sortOrder = "earliestEnd";
            break;
            case 4:sortOrder = "latestEnd";
            break;
            case 5:sortOrder = "longestDay";
            break;
            case 6:sortOrder = "shortestDay";
            break;
            case 7:sortOrder = "days";
            break;
            case 8:sortOrder = "daysOff";
            break;
            default:;
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

	@Override
	public void displaySchedules(List<Schedule> schedules) {
		//It's time to display schedules.
		//We'll start by displaying the first, then offering the user to go to the NEXT,
		//the PREV, the FIRST, the LAST, and to EXIT.
		boolean valid = false; //used to determine if the user input is valid.
		boolean stop = false; //used to determine if we should stop displaying schedules.
		String response; //holds user's input for next action.
		
		//Displaying the first schedule.
		int index = 0;
		int size = schedules.size();

		while(!stop) {
			display(schedules.get(index).toString());
			display("Showing schedule " + (index+1) + "/" + size + ". NEXT to see next, PREV to go back, FIRST to see first, LAST to see last, or EXIT to stop.");
			response = readFromConsole().toUpperCase();
			if (response == null) {
			} else if (response.equals("NEXT") || response.equals("PREV") || response.equals("FIRST") || response.equals("LAST") || response.equals("EXIT")) {
				valid = true;
			}
			while (!valid) {
				display("Sorry, " + "\"" + response + "\" is not recognized. Use NEXT to see next, PREV to go back, FIRST to see first, LAST to see last, and EXIT or STOP to stop.");
				response = readFromConsole().toUpperCase();
				if (response == null) {
				} else if (response.equals("NEXT") || response.equals("PREV") || response.equals("FIRST") || response.equals("LAST") || response.equals("EXIT")) {
					valid = true;
				}
			}
			//We have a valid response.
			switch(response) {
			case "NEXT":
				index = (index+1) % (size); //Should allow for infinite traversal.
				break;
			case "PREV":
				index--; //Should allow for infinite traversal.
				if (index < 0) {
					index = index + size;
				}
				break;
			case "FIRST":
				index = 0;
				break;
			case "LAST":
				index = size-1;
				break;
			case "EXIT":
			case "STOP":
					stop = true;
					break;
			}
		}
		display("No longer viewing schedules.");
		
	}
 
	public void setCourses(List<Course> courses, List<Course> nCourses) {
		if ((courses.size() < 1) && (nCourses.size() < 1)) {
			display("You haven't added any courses yet. To add a course, use ADD <course code>.");
		} else {
			int n = courses.size();
			int m = nCourses.size();
			if (n > 0) {
			String plural = (n > 1) ? "s" : "";
			display("You currently have " + courses.size() + " course" + plural + ".");
			for (Course c : courses) {
				display(c.getDescription());
			}
			}
			if (m > 0) {
				String plural = (m > 1) ? "s" : "";
				display("You currently have " + nCourses.size() + " optional course" + plural + ".");
				for (Course c : nCourses) {
					display(c.getDescription());
				}
			}
			
		}
	}

	public void editCourse(Course edit, String semester) {
		display("Editing course " + edit.getDescription());
		display("Here are the sections in this course for your semester:");
		
		
		List<Section> editSections = new ArrayList<Section>();
		for (Section s : edit.getSections()) {
			if (s.getSemester().equals(semester)) {
				editSections.add(s);
			}
		}
		//We now have a list of current-semester sections.
		for (int i = 0; i < editSections.size(); i++) {
			Section currSection = editSections.get(i);
			display("     " + (i+1) + ": " + currSection.getName() + ". Selected: " + currSection.isSelected());
		}
		display("Please select a section to edit. [Use line number].");
		
        String response = readFromConsole();
        int choice = 0;
        boolean valid = false;
        while (!valid) {
            try {
                choice = Integer.parseInt(response);
                if ((choice <= editSections.size()) && (choice > 0)) {
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
        //We have selected a section.
        Section editSection = editSections.get(choice-1);
        boolean selected = editSection.isSelected();
        String inquiry = selected ? "disable" : "enable";
        display("Would you like to " + inquiry + " the entire section? (y/n)");
        response = readFromConsole().toUpperCase();
        while (!response.startsWith("Y") && !response.startsWith("N")){
        	display("Would you like to " + inquiry + " the entire section? (y/n)");
        }
        selected = response.startsWith("Y") ? !selected : selected;
        editSection.setSelected(selected);
        //If selected is now false, then we can stop here. Otherwise, we'll see if they want to edit the activities further down.
        if (selected) {
        	display("Would you like to edit the activities in this section? (y/n)");
        	response = readFromConsole().toUpperCase();
        	 while (!response.startsWith("Y") && !response.startsWith("N")){
             	display("Would you like to " + inquiry + " the entire section? (y/n)");
             	response = readFromConsole().toUpperCase();
             }
        	 //if yes, then continue the action. Else, exit.
        	 if (response.startsWith("Y")) {
        		 List<Activity> activities = editSection.getActivities();
        		int requiredDGD = editSection.getRequiredDGD();
        		int requiredLAB = editSection.getRequiredLAB();
        		int requiredTUT = editSection.getRequiredTUT();
        		int selectedDGD = 0;
        		int selectedLAB = 0;
        		int selectedTUT = 0;
        		 boolean stop = false; //We'll let the user edit as many activities as they want.
 				while (!stop) { //don't stop, baby.
 					 for (int i = 0; i < activities.size(); i++) {
         				Activity currAct = activities.get(i);
         				if (currAct.isSelected()) {
         				if (currAct.getType().equals("DGD")) {
         					selectedDGD++;
         				} else if (currAct.getType().equals("LAB")) {
         					selectedLAB++;
         				} else if (currAct.getType().equals("TUT")) {
         					selectedTUT++;
         				}
         				}
         				display("     " + (i+1) + ": " + currAct.toString());
         				}
 				display("Please select an activity to toggle selected state. [Use line number]. Type EXIT or STOP to stop editing activities.");
 				if (requiredDGD > 0) {
 					display("This course requires only 1 DGD.");
 				}
 				if (requiredLAB > 0) {
 					display("This course requires only 1 LAB.");
 				}
 				if (requiredTUT > 0) {
 					display("This course requires only 1 TUT.");
 				}
 				response = readFromConsole().toUpperCase();
 				if (response.equals("EXIT") || response.equals("STOP")) { //HIT THE FLAG
	            		stop = true;
	            		break;
	            	}
 				choice = 0;
 		        valid = false;
 		        while (!valid) {
 		            try {
 		            	choice = Integer.parseInt(response);
 		                if ((choice <= activities.size()) && (choice > 0)) {
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
 		        //We have a valid activity chosen. Let's toggle it.
 		        Activity chosenAct = activities.get(choice-1);
 		        boolean togglable = true;
 		        if (chosenAct.isSelected()) {
 		        	switch (chosenAct.getType()) {
 		        	case "DGD": if (selectedDGD == 1) {
 		        		togglable = false;
 		        		display("You cannot toggle that activity. You require at least 1 DGD");
 		        	} else if (requiredDGD == 0) {
 		        		togglable = false;
 		        		display("That activity is mandatory.");
 		        	}
 		        	break;
 		        	case "LAB": 
 		        		display("Selected lab: " + selectedLAB);
 		        		if (selectedLAB == 1) {
 		        		togglable = false;
 		        		display("You cannot toggle that activity. You require at least 1 LAB");
 		        	} else if (requiredLAB == 0) {
 		        		togglable = false;
 		        		display("That activity is mandatory.");
 		        	}
 		        	break;
 		        	case "TUT": if (selectedTUT == 1) {
 		        		togglable = false;
 		        		display("You cannot toggle that activity. You require at least 1 TUT");
 		        	} else if (requiredTUT == 0) {
 		        		togglable = false;
 		        		display("That activity is mandatory.");
 		        	}
 		        	break;
 		        	default: display("That activity is mandatory.");
 		        	togglable=false;
 		        }
 		        }
 		        	selectedDGD = 0;
 		        	selectedLAB = 0;
 		        	selectedTUT = 0; //Reset these for next go-around.
 		        	
 		        	if (togglable) {
 		        activities.get(choice-1).setSelected(!activities.get(choice-1).isSelected()); //Toggled, yo.
 		        	} else {
 		        		display("Press any key to continue...");
 		        		readFromConsole();
 		        	}
        			}
        	 }
        }
        display("Done editing course.");
	}

	@Override
	public boolean confirmSemester() {
		display("Are you sure you want to change semesters? You will lose all courses and settings made so far. (y/n)");
		String response = readFromConsole().toUpperCase();
	   	if (response.startsWith("Y")) {
	   		display("Are you REALLY certain? There's no going back! (y/n)");
			response = readFromConsole().toUpperCase();
			return (response.startsWith("Y"));
	   	}
	   	display("Canceled changing semester.");
		return false;
	}
}
