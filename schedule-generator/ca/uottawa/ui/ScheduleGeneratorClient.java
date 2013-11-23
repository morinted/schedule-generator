package ca.uottawa.ui;

import java.io.IOException;
import java.sql.ClientInfoStatus;
import java.util.ArrayList;
import java.util.List;

import ca.uottawa.schedule.Course;
import ca.uottawa.schedule.Schedule;
import ca.uottawa.schedule.ScheduleMessage;
import ca.uottawa.schedule.Section;

import com.lloseng.ocsf.client.AbstractClient;

public class ScheduleGeneratorClient extends AbstractClient {

	private String studentNumber;
	private boolean ignoreExtras;
    private List<Course> courses;
    //Variables for n choose k courses.
    private List<Course> nCourses;
    private int k;
    private List<Schedule> schedules;
    private String sortOrder;
	private String semester;
	private ClientIF clientUI;
	
	
	
	public List<Schedule> getSchedules() {
		return schedules;
	}

	public void setSchedules(List<Schedule> schedules) {
		this.schedules = schedules;
	}

	public ScheduleGeneratorClient(String studentNumber, String host, int port, ClientConsole clientUI) throws IOException {
            super(host, port); //Call the superclass constructor
		    this.clientUI = clientUI;
		    this.ignoreExtras = false;
		    this.studentNumber = studentNumber;
            this.sortOrder = null;
            this.courses = new ArrayList<Course>();
            this.nCourses = new ArrayList<Course>();
            this.k = 0;
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
                	//We will add the course to the result
                    courses.add(result);
                    //And now we disable the sections that are not for the current semester.
                    for (Course c: courses) {
                    	for (Section s: c.getSections()) {
                    		if (!s.getSemester().equals(semester)) {
                    			s.setSelected(false);
                    		}
                    	}
                    }
                    clientUI.sendInfo(result.getDescription() + " added.");
                }
                break;

            case "ADDCOURSE-MULTIPLECOURSES":
                clientUI.sendInfo("No single course matches that course code. Type the course code exactly. If you can't remember it, use SEARCH");
                break;
            case "SCHEDULES":
            	//The server has provided us with a generated list of schedules, sorted in sortOrder
            	schedules = message.getSchedules();
            	clientUI.sendInfo("Schedules generated.");
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
                    sendToServer(addCourse);
                }
			break;
            case "GENERATE":
                //Awww yeeah. We get to generate courses now.
                //First, check for n choose k option:
            	if (sortOrder == null) {
            		clientUI.sendInfo("You haven't specified a sort order yet. To choose one, use: SORTORDER");
            	}
            	if (nCourses.size() > 0) {
            	} else {
            	ScheduleMessage generateMsg = new ScheduleMessage();
                generateMsg.setCommand("GENERATE");
                generateMsg.setCourses(courses);
                generateMsg.setIgnoreExtras(ignoreExtras);
                generateMsg.setSortOrder(sortOrder);
                sendToServer(generateMsg);
            	}
                
                break;

            case "SORTORDER":
                sortOrder = clientUI.getSortOrder();
                clientUI.sendInfo("Sort order set to: " + sortOrder);
                clientUI.done();
            case "DISPLAY":
            	if (schedules.size() < 1) {
            		clientUI.sendInfo("Cannot display schedules as none have been generated.");
            		clientUI.sendInfo("To generate a schedule, first use SEARCH to find out what courses are offered this semester.");
            		clientUI.sendInfo("Then use ADD to build a list of courses.");
            		clientUI.sendInfo("You may EDIT the courses to exclude certain sections or activities.");
            		clientUI.sendInfo("You can select a SORTORDER for your courses, to prioritize your schedule");
            		clientUI.sendInfo("Finally, use the keyword GENERATE to make your schedules.");
            		clientUI.sendInfo("Then you can use DISPLAY to sift through your schedules.");
            		} else {
            			//There are schedules to be displayed.
            			//We will let the client display the schedules.
            			clientUI.displaySchedules(schedules);
            		}
			default:
                clientUI.sendInfo("Unknown command: " + msg);
                clientUI.done();
		}

	}

}
