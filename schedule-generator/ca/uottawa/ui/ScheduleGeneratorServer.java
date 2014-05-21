package ca.uottawa.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import com.lloseng.ocsf.server.AbstractServer;
import com.lloseng.ocsf.server.ConnectionToClient;

import ca.uottawa.schedule.*;
public class ScheduleGeneratorServer extends AbstractServer {

	  private ServerConsole serverUI;
	  private List<Course> courses;
	  private File flCourses;
	  private File flSections;
	  private File flActivities;
	  private ServerStats statistics = null;
	  private ServerStats runStats = null;
	  private File stats;
	  private int connections = 0;
	  private boolean autoRestart;

	  
	public ScheduleGeneratorServer(int port, ServerConsole serverUI, boolean autoRestart) {
		super(port);
		this.serverUI = serverUI;
		this.autoRestart = autoRestart;
		flCourses = new File("../course-download/db_courses.csv");
		flSections = new File("../course-download/db_sections.csv");
		flActivities = new File("../course-download/db_activities.csv");

		try {
		refreshCourses();
		} catch (FileNotFoundException e) {
			serverUI.display("Error processing course codes!");
			try {
				this.close();
			} catch (Exception ex) {
				ex.printStackTrace();
				System.err.println("Error while exiting program.");
			}
		}
	}

	
	private void refreshCourses() throws FileNotFoundException {
		serverUI.display("Refreshing courses from db files...");
		courses = TextParser.getCoursesFromDatabase(flCourses, flSections, flActivities);
		if (courses.size() < 1) {
			throw new FileNotFoundException();
		} else {
		serverUI.display(courses.size() + " courses loaded.");
		for (Course c: courses) {
			try {
			c.getSemesters().size();
			} catch (Exception e) {
				serverUI.display(c.getDescription());
			}
			}
		}
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
			
			serverUI.display(client.getInfo("ip") +  " queries> \"" + query + "\" \t(" + results.size() + " results for semester " + semester + ")");
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
                    Collections.sort(semesterList);
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
                    Course desiredCourse = courses.get(courseIndex);
                    courseList.add(desiredCourse);
                    courseMsg.setCourses(courseList);
                    try {
                        client.sendToClient(courseMsg);
                        serverUI.display(client.getInfo("ip") + " adds> \"" + desiredCourse.getDescription() + "\"");
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
                GenerateTask task = new GenerateTask(message, client, this);
                task.run();
			default:;
		}

	}
	
	public void display(String msg) {
		serverUI.display(msg);
	}
	
	public void updateStats(String user, int courses, int optional, int k,
			int schedules) {
		updateStats();
		if (statistics == null) {
			statistics = new ServerStats();
		}
		if (runStats == null) {
			runStats = new ServerStats();
		}
		statistics.addCourses(courses);
		statistics.addOptional(optional);
		statistics.addElectives(k);
		if (k > 0) {
			statistics.addOptionalGenerations(1);
		}
		if (courses > 0 || optional > 0 || k > 0 || schedules > 0) {
			statistics.addGenerations(1);
		}
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
	          e.printStackTrace();
	      }
		
		runStats.addCourses(courses);
		runStats.addOptional(optional);
		runStats.addElectives(k);
		if (k > 0) {
			runStats.addOptionalGenerations(1);
		}
		if (courses > 0 || optional > 0 || k > 0 || schedules > 0) {
			runStats.addGenerations(1);
		}
		runStats.addSchedules(schedules);
		runStats.addUser(user);
	}
	
	public void updateStats() {
		stats = new File("server.stat");
		if(stats.exists()) {
			try {
				FileInputStream fis = new FileInputStream(stats);
				ObjectInputStream ois = new ObjectInputStream(fis);
				statistics = (ServerStats) ois.readObject();
				ois.close();
			} catch (Exception e) {
				serverUI.display("Error reading in old stats file. Creating new one.");
			}
		}
		if (runStats == null) {
			runStats = new ServerStats();
		}
	}
	
	public void printCurrentStats() {
		if (statistics != null && runStats != null) {
			String newLine = System.getProperty("line.separator");
			
			String currStats = newLine + "---------------------------------Global Statistics-----------------------------";
			currStats = currStats + newLine + "Users: " + statistics.getUsers().size();
			currStats = currStats + newLine + "Mandatory Courses Chosen:\t" + statistics.getNumOfCourses();
			currStats = currStats + "\tOptional Courses Chosen:\t" + statistics.getNumOfOptional();
			currStats = currStats + newLine + "Total K Value:\t\t\t" + statistics.getNumOfElectives();
			currStats = currStats + "\tGenerations:\t\t\t" + statistics.getNumOfGenerations();
			currStats = currStats + newLine + "Optional Generations:\t\t" + statistics.getNumOfOptionalGenerations();
			currStats = currStats + "\tSchedules Generated:\t\t" + statistics.getNumOfSchedules();
			currStats = currStats + newLine + "--------------------------------This Run----------------------------------------";
			currStats = currStats + newLine + "Unique Users: " + runStats.getUsers().size() + "\t\t\t\tConnections:\t\t\t" + connections;
			currStats = currStats + newLine + "Mandatory Courses Chosen:\t" + runStats.getNumOfCourses();
			currStats = currStats + "\tOptional Courses Chosen:\t" + runStats.getNumOfOptional();
			currStats = currStats + newLine + "Total K Value:\t\t\t" + runStats.getNumOfElectives();
			currStats = currStats + "\tGenerations:\t\t\t" + runStats.getNumOfGenerations();
			currStats = currStats + newLine + "Optional Generations:\t\t" + runStats.getNumOfOptionalGenerations();
			currStats = currStats + "\tSchedules Generated:\t\t" + runStats.getNumOfSchedules();
			currStats = currStats + newLine + "--------------------------------------------------------------------------------";
			serverUI.display(currStats); }
	}


	public void handleMessageFromServerUI(String message) {
		//Some plans:
		//refresh database
		//stop server
		//Get list of active users
		//...more ideas?
		String[] args = message.split(" ");
		
		switch (args[0].toUpperCase()) {
		case "OUTPUTDB":
			String nl = System.getProperty("line.separator");
			String query = "";
			
			query = "SET search_path = \"Schedules\";" + nl;
			query += "DROP TABLE activities CASCADE;" + nl;
			query += "DROP TABLE courses CASCADE;" + nl;
			query += "DROP TABLE sections CASCADE;" + nl;
			
			  query += "CREATE TABLE courses" + nl
					 + "(" + nl + "code TEXT," + nl
					 + "description TEXT," + nl
					 + "PRIMARY KEY (code)" + nl
					 + ");" + nl;
			  
			  query += "CREATE TABLE sections" + nl
						 + "(" + nl + "SName TEXT," + nl
						 + "code TEXT," + nl
						 + "semester INTEGER," + nl
						 + "requiredDGD INTEGER," + nl
						 + "requiredTUT INTEGER," + nl
						 + "requiredLAB INTEGER," + nl
						 + "PRIMARY KEY(sname,semester)," + nl
						 + "FOREIGN KEY(code) REFERENCES course" + nl
						 + ");" + nl;
			  
			  query += "CREATE TABLE activities" + nl
					  + "(" + nl + "atype TEXT," + nl
					  + "aNumber INTEGER," + nl
					  + "sname TEXT," + nl
					  + "semester INTEGER," + nl
					  + "dayOfWeek TEXT," + nl
					  + "startTime TIME," + nl
					  + "endTime TIME," + nl
					  + "place TEXT," + nl
					  + "professor TEXT," + nl
					  + "PRIMARY KEY(sname,semester,atype,anumber)," + nl
					  + "FOREIGN KEY(sname,semester) REFERENCES section" + nl
					  + ");" + nl;
			  
			  serverUI.display("Creating query...");
			  
			  int count = 0;
			  int tenth = courses.size()/10;
			  for (Course c: courses) {
				  query += c.getPgSQLQuery();
				  
				  if (((count)%(tenth)) == 0) {
					  serverUI.display(10*(count/tenth) + "% complete.");
				  }
				  count++;
				  
			  }
			  
			  serverUI.display("Saving query...");
			  PrintWriter out;
			try {
				out = new PrintWriter("pgSQLquery.sql");
				out.println(query);
				out.close();
				serverUI.display("Query saved to pgSQLquery.sql!");
			} catch (FileNotFoundException e) {
				serverUI.display("Error saving pgSQLquery.sql! Is it in use?");
			}
			break;
			default:
				
		}
	}
	
	  protected void serverStarted()
	  {
	    serverUI.display("Server listening for connections on port " + getPort());
	    updateStats();
	    printCurrentStats();
	  }
	  
	  protected void serverStopped()
	  {
	    serverUI.display("Server has stopped listening for connections.");
	    if (autoRestart) {
		    serverUI.display("Attempt to restart in 10 seconds.");
		    try {
				Thread.sleep(10000);
				listen();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	  }
	  
	  protected void serverClosed() {
		  serverUI.display("The server has been closed");
		  System.exit(0);
	  }

	  protected void clientConnected(ConnectionToClient client) {
		  serverUI.display(client.toString() + " has connected.");
		  connections++;
		  updateStats(client.getInetAddress().getHostAddress().toString(), 0, 0, 0, 0);
		  printCurrentStats();
	  }
	  
	  synchronized protected void clientDisconnected(ConnectionToClient client) {
		  serverUI.display(client.getInfo("ip") + " has disconnected.");
	  }
	  
	  synchronized protected void clientException(ConnectionToClient client, Throwable exception) {
		  //serverUI.display(client.getInetAddress() + " has disconnected by exception.");
	  }


	public boolean isAutoRestart() {
		return autoRestart;
	}


	public void setAutoRestart(boolean autoRestart) {
		this.autoRestart = autoRestart;
	}
}
