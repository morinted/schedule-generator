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
	 * The instance of the client that communicates with this ClientIF
	 */
	ScheduleGeneratorClient client;
	
	/**
	 * The selected schedule's index
	 */
	int currSchedule;

	/**
	 * The constructor creates a ScheduleGeneratorClient to open a connection with the server.
	 * @param studentNumber: The student number that the client will log in with
	 * @param host: The host to connect with.
	 * @param port: The port to connect on.
	 */
	public ClientConsole(String host, int port) 
	{
		try 
		{
			client= new ScheduleGeneratorClient(host, port, this);
		} 
		catch(IOException exception) 
		{
			display("Error: Can't setup connection!"
					+ " Terminating client.");
			System.exit(1);
		}
	}

	/**
	 * Reads in 1 line from the console and performs its command.
	 * accept is called each time a command is done().
	 */
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


	/**
	 * Displays a message to the user through the console.
	 * @param message: Message to be displayed.
	 */
	public void display(String message) 
	{
		System.out.println(">  " + message);
	}

	/**
	 * The main method. This is how you start the client console.
	 * @param args
	 */
	public static void main(String[] args) 
	{
		String host = "";
		int port;
		try //Gets host param is necessary.
		{
			host = args[0];
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			host = "schlachter.ca";
		}

		try //Get port if needed
		{
			port = Integer.parseInt(args[1]); //Try to get it after the host
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			port = DEFAULT_PORT; //Else default to the default port.
		} 

		new ClientConsole(host, port); //Create instance of ClientConsole to start the client.
	}
	
	/**
	 * A method called after the Client has received a list of search results from the server.
	 */
	public void sendSearchResults(List<String> results) {
		//Receiving search results. We must display them:
		if (results.size() == 0) {
			display("No results for your current semester.");
		} else {
			//Show the results received from the server
			display("Found " + results.size() + " courses:");
			for (String s : results) {
				display(s);
			}
		}

	}

	/**
	 * The server can send relevant information and we will relay it to the user.
	 */
	public void sendInfo(String msg) {
		display(msg);
	}

	/**
	 * The client can request for the semester. 
	 * @param Semesters: The list of semesters to choose from.
	 */
	public String getSemester(List<String> semesters) {
		//go through available semesters and get user choice.j
		String year;
		String month;
		int choice = 0;
		//Ask user
		display("What semester would you like to build schedules for?");
		//display available schedules.
		for (int i = 0; i < semesters.size(); i++) {
			year = semesters.get(i).substring(0, 4);
			Integer monthInt = Integer.parseInt(semesters.get(i).substring((4)));
            if (monthInt <= 4) {
            	month = "Winter";
            } else if (monthInt <= 8) {
            	month = "Spring/Summer";
            } else {
            	month = "Fall";
            }
			display((i+1) + ". " + month + " " + year);
		}
		display("Select your choice. (Number)");
		//Get user response.
		String response = readFromConsole();
		//We will loop until the response is valid.
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
		//Once the response is valid, we display their choice and return it.
		year = semesters.get(choice-1).substring(0, 4);
		month = new DateFormatSymbols().getMonths()[Integer.parseInt(semesters.get(choice-1).substring(4))-1];
		display("You have selected semester: " + month + " " + year);
		return semesters.get(choice-1);
	}

	/**
	 * Lets the client specify when it is done with a command and can accept more.
	 */
	public void done() {
		accept(); //Read from console.
	}

	/**
	 * Return the sort order of choice.
	 */
	public String getSortOrder() {
		String sortOrder = null;
		//Display choices.
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
		//Get input
		String response = readFromConsole();
		int choice = 0;
		//Loop until we have a valid choice.
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
		//Once the choice is valid, we convert the string into a server-readable format.
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
		//Return our choice.
		return sortOrder;
	}
	
	/**
	 * Read user input from the console.
	 * @return The user inputed value
	 */
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

	/**
	 * The client has received a list of schedules from the server.
	 * It is our job to display them.
	 * @param The list of schedules from the servers.
	 */
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

		//Let the user navigate through schedules.
		while(!stop) {
			currSchedule = index;
			//Get schedule.
			display(schedules.get(index).toString());
			//Display which schedule we're showing
			display("Showing schedule " + (index+1) + "/" + size + ". NEXT to see next, PREV to go back, FIRST to see first, LAST to see last, or EXIT to stop. You can export the current calendar as an ICS file for importing into Google Calendar using 'EXPORT'");
			//Get user input
			response = readFromConsole().toUpperCase();
			//See if command is valid. If not, we'll loop.
			if (response == null) {
			} else if (response.equals("NEXT") || response.equals("PREV") || response.equals("FIRST") || response.equals("LAST") || response.equals("EXIT") || response.equals("EXPORT")) {
				valid = true;
			}
			while (!valid) {
				display("Sorry, " + "\"" + response + "\" is not recognized. Use NEXT to see next, PREV to go back, FIRST to see first, LAST to see last, and EXIT or STOP to stop. Export an ICS file with EXPORT");
				response = readFromConsole().toUpperCase();
				if (response == null) {
				} else if (response.equals("NEXT") || response.equals("PREV") || response.equals("FIRST") || response.equals("LAST") || response.equals("EXIT") || response.equals("EXPORT")) {
					valid = true;
				}
			}
			//We have a valid response.
			switch(response) {
			//Go to next schedule
			case "NEXT":
				index = (index+1) % (size); //Should allow for infinite traversal.
				break;
			//Go to prev schedule
			case "PREV":
				index--; //Should allow for infinite traversal.
				if (index < 0) {
					index = index + size;
				}
				break;
			//Go to first schedule
			case "FIRST":
				index = 0;
				break;
			//Go to last schedule
			case "LAST":
				index = size-1;
				break;
			//Export ICS
			case "EXPORT":
				try {
					client.handleMessageFromClientUI("EXPORT " + index);
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			//Stop asking for input.
			case "EXIT":
			case "STOP":
				stop = true;
				break;
			}
		}
		//Display that we're done.
		display("No longer viewing schedules.");
	}

	/**
	 * Lists the courses currently available.
	 */
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

	/**
	 * The client sends a course to be edited by the client.
	 */
	public void editCourse(Course edit, String semester) {
		display("Editing course " + edit.getDescription());
		display("Here are the sections in this course for your semester:");

		//Displays sections. Counts to make sure there's more than 1 selected.
		int enabledSections = 0;
		List<Section> editSections = new ArrayList<Section>();
		for (Section s : edit.getSections()) {
			if (s.getSemester().equals(semester)) {
				editSections.add(s);
			}
		}
		//We now have a list of current-semester sections.
		for (int i = 0; i < editSections.size(); i++) {
			Section currSection = editSections.get(i);
			if (currSection.isSelected()) { 
				enabledSections++; 
			}
			display("     " + (i+1) + ": " + currSection.getName() + ". Selected: " + currSection.isSelected());
		}
		display("Please select a section to edit. [Use line number].");

		//Get a valid input from user
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
		if (selected == false && enabledSections==1) {
			display("Sorry! You can't disable your only active section.");
		} else {
			editSection.setSelected(selected);
		}
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
					//Only toggle if it's not a minimum.
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

	/**
	 * Get user confirmation that they'd like to change semesters and lose all settings.
	 */
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

	/**
	 * Client sends us a course that has been added. We display it.
	 */
	public void courseAdded(String description) {
		display(description + " added to list of courses.");
	}
	
	/**
	 * Cannot add course, we relay the information to the user.
	 */
	public void courseExists(String description) {
		display("Cannot add course " + description + ". It is already in the list of courses.");
	}

	/**
	 * Can't remove a course if it doesn't exist.
	 */
	public void courseNotExists(String description) {
		display("Can't remove " + description + " because it is not in the list of courses.");
	}

	/**
	 * A course has been removed, relay the information.
	 */
	public void courseRemoved(String description) {
		display("Course " + description + " removed.");
	}

	/**
	 * Client sounds a count of schedules generated.
	 */
	public void schedulesGenerated(int count) {
		display(count + " schedules generated.");
	}

	/**
	 * No courses selected so we can't generate.
	 */
	public void courseNone() {
		display("No courses selected - can't generate until you ADD some.");
	}

	public int getScheduleIndex() {
		return currSchedule;
	}

	public void savedFile(String path) {
		display("File saved to " + path);
	}
}
