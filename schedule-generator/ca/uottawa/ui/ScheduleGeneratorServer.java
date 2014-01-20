package ca.uottawa.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
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
		serverUI.display("tCourses updated.");

	}
	
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
		ScheduleMessage message = (ScheduleMessage) msg;
		String command = message.getCommand();

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
                break;
            case "ADDOPTIONAL":
            case "ADD":
                //We are adding a course, assuming that the string list contains the course to add.
                String courseCode = message.getStrings().get(0).toUpperCase();
                if (CourseSearch.search(courseCode, message.getSemester(), courses).size() == 1) {
                    ScheduleMessage courseMsg = new ScheduleMessage();
                    if (command.toUpperCase().equals("ADDOPTIONAL")) {
                    	courseMsg.setCommand("ADDOPTIONAL"); //If this is supposed to be an optional course as opposed to a normal one.
                    } else {
                    	courseMsg.setCommand("ADDCOURSE");
                    }
                    
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
            	//User is generating schedules
            	List<Course> mandatoryCourses = message.getCourses();
                int k = message.getK();
                List<Course> optional = message.getOptionalCourses();
                
                serverUI.display("Received " + mandatoryCourses.size() + " mandatory courses.");
                serverUI.display("Received " + optional.size() + " optional courses, choosing " + k);
                
                
                
                String sortOrder = message.getSortOrder();
            	boolean ignoreExtras = message.isIgnoreExtras();
            	List<Schedule> result;
            	if (k>0) {
            		result = Schedule.generateSchedules(mandatoryCourses, optional, k);
            	} else {
            		result = Schedule.generateSchedules(mandatoryCourses);
            	}
            	if (result.size() > 0) {
            	//We should update the schedule stats in preperation of sorting.
            	for (Schedule s: result) {
            		s.updateStats();
            	}
            	result = Schedule.sort(sortOrder, result, ignoreExtras);
            	}
            	
            	updateStats(client.getInetAddress().getHostAddress().toString(), mandatoryCourses.size(), optional.size(), k, result.size());
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

	private void updateStats(String user, int courses, int optional, int k,
			int schedules) {
		ServerStats statistics = null;
		File stats = new File("server.stat");
		serverUI.display(stats.getAbsolutePath());
		if(stats.exists()) {
			try {
				FileInputStream fis = new FileInputStream(stats);
				ObjectInputStream ois = new ObjectInputStream(fis);
				statistics = (ServerStats) ois.readObject();
			} catch (Exception e) {
				serverUI.display("Error reading in old stats file. Creating new one.");
			}
		}
		if (statistics == null) {
			statistics = new ServerStats();
		}
		statistics.addCourses(courses);
		statistics.addOptional(optional);
		statistics.addElectives(k);
		if (k > 0) {
			statistics.addOptionalGenerations(1);
		}
		statistics.addGenerations(1);
		statistics.addSchedules(schedules);
		statistics.addUser(user);
		try {
	         FileOutputStream fos = new FileOutputStream(stats);
	         ObjectOutputStream out = new ObjectOutputStream(fos);
	         out.writeObject(statistics);
	         out.flush();
	         out.close();
	      }
	      catch (IOException e) {
	          serverUI.display(e.toString()); 
	      }
		String currStats = "Current stats: " + Calendar.getInstance().getTime().toString();
		currStats = currStats + System.getProperty("line.separator") + "Users: " + statistics.getUsers().size();
		currStats = currStats + System.getProperty("line.separator") + "Mandatory Courses Chosen: " + statistics.getNumOfCourses();
		currStats = currStats + System.getProperty("line.separator") + "Optional Courses Chosen: " + statistics.getNumOfOptional();
		currStats = currStats + System.getProperty("line.separator") + "Total K Value: " + statistics.getNumOfElectives();
		currStats = currStats + System.getProperty("line.separator") + "Generations: " + statistics.getNumOfGenerations();
		currStats = currStats + System.getProperty("line.separator") + "Optional Generations: " + statistics.getNumOfOptionalGenerations();
		currStats = currStats + System.getProperty("line.separator") + "Schedules Generated: " + statistics.getNumOfSchedules();
		serverUI.display(currStats);
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
