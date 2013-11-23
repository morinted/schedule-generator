package ca.uottawa.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
		boolean internal = message.isInternal();

		switch(command.toUpperCase()) {
		case "SEARCH":
			String query = message.getStrings().get(0);
            String semester = message.getSemester();
			List<String> results = CourseSearch.search(query, semester, courses);
			ScheduleMessage reply = new ScheduleMessage();
			reply.setCommand("SEARCHRESULTS");
			reply.setStrings(results);
			try {
				client.sendToClient(reply);
			} catch (IOException e) {
				serverUI.display("Error sending search results to client. Possible connection lost.");
				serverUI.display(client.toString());
			}
			break;
            case "SEMESTERS":
                if (internal) {
                ScheduleMessage semesters = new ScheduleMessage();
                    semesters.setCommand("SEMESTERLIST");
                    List<String> semesterList = new ArrayList<String>();
                    for (Course c : courses) {
                        List<String> tempList = c.getSemesters();
                        int tmpLstSize = tempList.size();
                        for (int i = 0; i < tmpLstSize; i++) {
                            String sem = tempList.get(i);
                            boolean exists = false;
                            for (String uniqueSemester : semesterList) {
                                if (sem.equals(uniqueSemester)) {
                                    exists = true;
                                }
                            }
                            if (!exists) {
                                semesterList.add(sem);
                            }
                        }
                    }
                    semesters.setStrings(semesterList);
                    try {
                        client.sendToClient(semesters);
                    } catch (IOException e) {
                    	serverUI.display("Error sending list of semesters to client. Possible connection lost.");
                        serverUI.display(client.toString());
                    }
                }
                break;
            case "ADD":
                //We are adding a course, assuming that the string list contains the course to add.
                String courseCode = message.getStrings().get(0).toUpperCase();
                if (CourseSearch.search(courseCode, message.getSemester(), courses).size() == 1) {
                    ScheduleMessage courseMsg = new ScheduleMessage();
                    courseMsg.setCommand("ADDCOURSE");
                    int courseIndex = CourseSearch.indexOf(courseCode, message.getSemester(), courses);
                    List<Course> courseList = new ArrayList<Course>();
                    courseList.add(courses.get(courseIndex));
                    courseMsg.setCourses(courseList);
                    try {
                        client.sendToClient(courseMsg);
                    } catch (IOException e) {
                    	serverUI.display("Error sending course back to client. Possible connection lost.");
                    	serverUI.display(client.toString());
                    }
                } else {
                    ScheduleMessage failureMsg = new ScheduleMessage();
                    failureMsg.setCommand("ADDCOURSE-MULTIPLECOURSES");
                    List<String> failedQuery = new ArrayList<String>();
                    failedQuery.add(message.getStrings().get(0));
                    failureMsg.setStrings(failedQuery);
                    try {
                        client.sendToClient(failureMsg);
                    } catch (IOException e) {
                    	serverUI.display("Unable to report back add course action to client. Possible connection lost.");
                    	serverUI.display(client.toString());
                    }
                }
                break;
            case "GENERATE":
            	//User is generating schedules with no nChooseK option. Easy to do.
            	List<Course> mandatoryCourses = message.getCourses();
            	String sortOrder = message.getSortOrder();
            	boolean ignoreExtras = message.isIgnoreExtras();
            	List<Schedule> result = Schedule.generateSchedules(mandatoryCourses);
            	result = Schedule.sort(sortOrder, result, ignoreExtras);
            	ScheduleMessage schedulesMsg = new ScheduleMessage();
            	schedulesMsg.setCommand("SCHEDULES");
            	schedulesMsg.setSchedules(result);
			try {
				client.sendToClient(schedulesMsg);
			} catch (IOException e) {
				serverUI.display("Unable to report back generated schedules. Possible connection lost.");
				serverUI.display(client.toString());
			}
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
		case "SEARCH":
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
