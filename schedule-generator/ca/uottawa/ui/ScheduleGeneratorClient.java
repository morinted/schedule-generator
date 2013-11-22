package ca.uottawa.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ca.uottawa.schedule.Course;
import ca.uottawa.schedule.Schedule;
import ca.uottawa.schedule.ScheduleMessage;

import com.lloseng.ocsf.client.AbstractClient;

public class ScheduleGeneratorClient extends AbstractClient {

	private String studentNumber;
    private List<Course> courses;
    private List<Schedule> schedules;
    private String sortOrder;
	private String semester;
	private ClientIF clientUI;
	

	public ScheduleGeneratorClient(String studentNumber, String host, int port, ClientConsole clientUI) throws IOException {
            super(host, port); //Call the superclass constructor
		    this.clientUI = clientUI;
		    this.studentNumber = studentNumber;
            this.sortOrder = null;
            this.courses = new ArrayList<Course>();
		    openConnection();
		    
		    if (this.semester == null) {
		    	ScheduleMessage request = new ScheduleMessage();
                request.setCommand("SEMESTERS");
                request.setInternal(true);

                sendToServer(request);
		    } else {
                clientUI.done();
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
            case "SEMESTERLIST": //Server sent list of semesters from internal call
                semester = clientUI.getSemester(message.getStrings());

                clientUI.sendInfo("Semester selected: " + semester);
                break;
            case "ADDCOURSE":
                Course result = message.getCourses().get(0);

                boolean exists = false;
                for (Course c: courses) {
                    if (c.getDescription().equals(result.getDescription())) {
                        clientUI.sendInfo("Course " + result.getDescription() + " is already in the list of courses.");
                        exists = true;
                    }
                }
                if (!exists) {
                    courses.add(result);
                    clientUI.sendInfo(result.getDescription() + " added.");
                }
                break;

            case "ADDCOURSE-MULTIPLECOURSES":
                clientUI.sendInfo("More than one course found. Type the course code exactly. If you can't remember it, use SEARCH");
                break;
			default:;
		}
        clientUI.done();
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
            if (command.length != 2) {
            clientUI.sendInfo("To use search, type: SEARCH [partial or full course-code]");
            clientUI.sendInfo("For example: SEARCH SEG2105");
            clientUI.done();
            } else {
			ScheduleMessage srchMsg = new ScheduleMessage();
			srchMsg.setCommand("SEARCH");
			List<String> query = new ArrayList<String>();
			query.add(command[1]);
			srchMsg.setStrings(query);
            srchMsg.setSemester(semester);
			sendToServer(srchMsg);
            break;
        }
            case "ADD":
                if (command.length != 2) {
                    clientUI.sendInfo("To use add, type: ADD [partial or full course-code]");
                    clientUI.sendInfo("For example: ADD SEG2105");
                    clientUI.done();
                } else {
                    ScheduleMessage addCourse  = new ScheduleMessage();
                    addCourse.setCommand("ADD");
                    String courseToAdd = command[1];
                    List<String> stringList = new ArrayList<String>();
                    stringList.add(courseToAdd);
                    addCourse.setStrings(stringList);
                    addCourse.setSemester(semester);
                    clientUI.sendInfo("Client sending course to be added");
                    sendToServer(addCourse);
                }
			break;
            case "GENERATE":
                //Awww yeeah. We get to generate courses now.
                ScheduleMessage generateMsg = new ScheduleMessage();
                generateMsg.setCommand("GENERATE");
                break;

            case "SORTORDER":
                sortOrder = clientUI.getSortOrder();
                clientUI.sendInfo("Sort order set to: " + sortOrder);
                clientUI.done();
			default:
                clientUI.sendInfo("Unknown command: " + msg);
                clientUI.done();
		}

	}

}
