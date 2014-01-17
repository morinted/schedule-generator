package ca.uottawa.ui;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.util.Recurrence;
import biweekly.util.Recurrence.Frequency;
import ca.uottawa.schedule.Activity;
import ca.uottawa.schedule.Course;
import ca.uottawa.schedule.CourseSelection;
import ca.uottawa.schedule.Schedule;
import ca.uottawa.schedule.ScheduleMessage;
import ca.uottawa.schedule.Section;

import com.lloseng.ocsf.client.AbstractClient;

public class ScheduleGeneratorClient extends AbstractClient {

	private boolean ignoreExtras;
	private List<Course> courses;
	// Variables for n choose k courses.
	private List<Course> nCourses;
	private int k;
	private List<Schedule> schedules;
	private String sortOrder;
	private String semester;
	private ClientIF clientUI;

	/**
	 * Return schedules
	 * @return The list of schedules.
	 */
	public List<Schedule> getSchedules() {
		return schedules;
	}

	/**
	 * Set the list of schedules
	 * @param schedules: The list of schedules to the set.
	 */
	public void setSchedules(List<Schedule> schedules) {
		this.schedules = schedules;
	}

	/**
	 * Constructor
	 * @param host: The host to connect with.
	 * @param port: The port to connect on.
	 * @param clientUI: The ClientIF to UI with.
	 * @throws IOException
	 */
	public ScheduleGeneratorClient(String host, int port,
			ClientIF clientUI) throws IOException {
		super(host, port); // Call the superclass constructor
		this.clientUI = clientUI;
		this.ignoreExtras = false;
		this.sortOrder = null;
		this.schedules = new ArrayList<Schedule>();
		this.courses = new ArrayList<Course>();
		this.nCourses = new ArrayList<Course>();
		this.k = 0;
		openConnection();

		if (this.semester == null) {
			//Select semester from the UI.
			ScheduleMessage request = new ScheduleMessage();
			request.setCommand("SEMESTERS");
			sendToServer(request); //Get list of semesters from the server.
		} else {
			clientUI.done(); //Get input from server if the semester exists.
		}
	}

	/**
	 * Handles message from the server and relays information to the client.
	 */
	protected void handleMessageFromServer(Object msg) {
		ScheduleMessage message = (ScheduleMessage) msg;
		String command = message.getCommand().toUpperCase();
		switch (command) {
		case "SEARCHRESULTS": // Server has given us search results. Yay!
			clientUI.sendSearchResults(message.getStrings());
			break;
		case "SEMESTERLIST": // Server sent list of semesters from internal call
			semester = clientUI.getSemester(message.getStrings());
			break;
		case "ADDOPTIONAL":
			//we are adding an optional course.
		case "ADDCOURSE":
			Course result = message.getCourses().get(0);

			boolean exists = false;
			//Check if courses exists in either course list. If it does, cancel the add.
			for (Course c : courses) {
				if (c.getDescription().equals(result.getDescription())) {
					clientUI.sendInfo("Course " + result.getDescription()
							+ " is already in the list of courses.");
					exists = true;
				}
			}
			for (Course c : nCourses) {
				if (c.getDescription().equals(result.getDescription())) {
					clientUI.sendInfo("Course " + result.getDescription()
							+ " is already in the list of courses.");
					exists = true;
				}
			}
			if (!exists) {
				//We disable the sections that are not for the current
				// semester.
					for (Section s : result.getSections()) {
						if (!s.getSemester().equals(semester)) {
							s.setSelected(false);
						}
					}
				// We will add the course to the list it belongs in
				if (command.equals("ADDOPTIONAL")) {
					nCourses.add(result);
				} else {
				courses.add(result);
				}
				clientUI.courseAdded(result.getDescription());
			} else {
				clientUI.courseExists(result.getDescription());
			}
			break;
		case "ADDCOURSE-MULTIPLECOURSES":
			clientUI.sendInfo("No single course matches that course code. Type the course code exactly. If you can't remember it, use SEARCH");
			break;
		case "SCHEDULES":
			// The server has provided us with a generated list of schedules,
			// sorted in sortOrder
			schedules = message.getSchedules();
			clientUI.schedulesGenerated(schedules.size());
			break;
		
		default:
		}
		clientUI.done();
	}

	/**
	 * Quits the application.
	 */
	public void quit() {
		try {
			closeConnection();
		} catch (IOException e) {
		}
		System.exit(0);
	}

	/**
	 * Connection to server closed.
	 */
	public void connectionClosed() {
		clientUI.sendInfo("The connection has been closed.");
	}

	/**
	 * Connection exception
	 */
	public void connectionException(Exception exception) {
		clientUI.sendInfo("Connection to server lost. Closing connection.");
		quit();
	}

	/**
	 * Handle a command from the client UI.
	 * @param msg: The message from the client.
	 * @throws IOException
	 */
	public void handleMessageFromClientUI(String msg) throws IOException {
		// Woot.
		// Add <coursecode>
		// Search <partial couse-code
		// edit <course>
		// Other plans?
		String[] command = ((String) msg).split(" "); // Split string from
														// client into a bunch
														// of

		switch (command[0].toUpperCase()) {
		case "SEARCH": //search command
			if (command.length != 2) { //Make sure it's a correct command
				clientUI.sendInfo("To use search, type: SEARCH [partial or full course-code]");
				clientUI.sendInfo("For example: SEARCH SEG2105");
				clientUI.done();
			} else { //If search is valid, send search message to the server.
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
			boolean issue = false; //This is a flag in case the command length is 3 but [2] is not OPTIONAL
			if (command.length == 3) {
				if (command[1].toUpperCase().equals("OPTIONAL")) {
					//We have an optional course.
					ScheduleMessage addCourse = new ScheduleMessage();
					addCourse.setCommand("ADDOPTIONAL");
					String courseToAdd = command[2];
					List<String> stringList = new ArrayList<String>();
					stringList.add(courseToAdd);
					addCourse.setStrings(stringList);
					addCourse.setSemester(semester);
					sendToServer(addCourse);
					break;
				} else {
					issue = true;
				}
			} 
			if (((command.length != 2)&&(issue)) || issue) {
				clientUI.sendInfo("To use add, type: ADD [course-code]");
				clientUI.sendInfo("You may also use the nChooseK option with ADD OPTIONAL [course-code]");
				clientUI.sendInfo("For example: ADD SEG2105");
				clientUI.done();
			} else {
				ScheduleMessage addCourse = new ScheduleMessage();
				addCourse.setCommand("ADD");
				String courseToAdd = command[1];
				List<String> stringList = new ArrayList<String>();
				stringList.add(courseToAdd);
				addCourse.setStrings(stringList);
				addCourse.setSemester(semester);
				sendToServer(addCourse);
			}
			break;
		case "REMOVE": //Remove command
			// Now the user wants to remove a course.
			if (command.length != 2) {
				clientUI.sendInfo("To use add, type: REMOVE [partial or full course-code]");
				clientUI.sendInfo("For example: REMOVE SEG2105");
				clientUI.sendInfo("Use LIST to see your active courses.");
				clientUI.done();
			} else {
				String toRemove = command[1].toUpperCase();
				if (toRemove.length() > 8) {
					toRemove = toRemove.substring(0, 8);
				}
				Course removeMe = null;
				for (Course c : courses) {
					String courseCode = c.getDescription().split(" ")[0];
					if (courseCode.equals(toRemove)) {
						removeMe = c;
					}
				}
				for (Course c : nCourses) {
					String courseCode = c.getDescription().split(" ")[0];
					if (courseCode.equals(toRemove)) {
						removeMe = c;
					}
				}
				if (removeMe == null) {
					clientUI.courseExists(toRemove);
					clientUI.sendInfo("Use LIST to see your active courses.");
				} else {
					//Remove it from both, JUST IN CASE (?)
					courses.remove(removeMe);
					nCourses.remove(removeMe);
					clientUI.courseRemoved(removeMe.getDescription());
				}
				clientUI.done();
			}
			break;
		case "SETK": //Set K command
			if (command.length != 2) {
				clientUI.sendInfo("To set K, which is the number of courses you'd like to have out of your optional courses, use: SETK [int]");
			} else {
				try {
				k = Integer.parseInt(command[1]);
				} catch (NumberFormatException e) {
					clientUI.sendInfo("You have not specified a valid value for k. " + k + " is not understood.");
					clientUI.sendInfo("To set K, which is the number of courses you'd like to have out of your optional courses, use: SETK [int]");
				} finally {
					clientUI.done();
				}
			}
		break;
		case "GENERATE": //Generate command
			// Awww yeeah. We get to generate courses now.
			// First, check for n choose k option:
			int nCourseSize = nCourses.size();
			if ((nCourseSize > 0) || (k > 1)) {
				if ((k<1) || (k>nCourseSize)) {
					clientUI.sendInfo("k, value " + k + ", is not a valid number of courses to choose from your optional set, which is size " + nCourseSize);
					clientUI.sendInfo("Use the command SETK [int]");
					clientUI.done();
					break;
				}
			} 
			//The we make sure that there's a sort order selected.
			if (sortOrder == null) {
				sortOrder = clientUI.getSortOrder();
			}
			clientUI.sendInfo("IgnoringExtras: " + ignoreExtras);
			clientUI.sendInfo("Sort order is set to: " + sortOrder);
				ScheduleMessage generateMsg = new ScheduleMessage();
				generateMsg.setOptionalCourses(nCourses);
				generateMsg.setK(k);
				generateMsg.setCommand("GENERATE");
				generateMsg.setCourses(courses);
				generateMsg.setIgnoreExtras(ignoreExtras);
				generateMsg.setSortOrder(sortOrder);
				if (courses.size() > 0 || nCourses.size() > 0) {
				sendToServer(generateMsg);
				} else {
					clientUI.courseNone();
					clientUI.done();
				}
			break;
		case "SORTORDER": //Sort order command
			sortOrder = clientUI.getSortOrder();
			clientUI.sendInfo("Sort order set to: " + sortOrder);
			clientUI.done();
			break;
		case "DISPLAY": //Display command
			if (schedules.size() < 1) {
				clientUI.sendInfo("Cannot display schedules, as none have been generated.");
			} else {
				// There are schedules to be displayed.
				// We will let the client display the schedules.
				clientUI.displaySchedules(schedules);
			}
			clientUI.done();
			break;
		case "LIST": //List command
			clientUI.setCourses(courses, nCourses);
			clientUI.done();
			break;
		case "IGNOREEXTRAS": //Ignore Extras command
			if (command.length != 2) {
				clientUI.sendInfo("To set ignore extras, which ignores DGDs/TUTs while sorting, use:");
				clientUI.sendInfo("IGNOREEXTRAS [0/1]");
				clientUI.sendInfo("0 disables the feature, 1 enables it.");
			} else {
				try {
				int input = Integer.parseInt(command[1]);
				if (input == 1) {
					ignoreExtras = true;
				} else if (input == 0) {
					ignoreExtras = false;
				} else {
					throw new NumberFormatException();
				}
				clientUI.sendInfo("Ignore Extras set to " + ignoreExtras);
				} catch (NumberFormatException e) {
					clientUI.sendInfo("You have not specified 1 or 0.");
					} finally {
					clientUI.done();
				}
			}
			break;
		case "EDIT": //Edit command
		if (command.length != 2) {
			clientUI.sendInfo("To use edit, type EDIT <coursecode>. Note, to edit a course, you must have it in your list of courses. To add it, use ADD <coursecode>.");
		} else {
			//let's see if this course exists.
			String toEdit = command[1].toUpperCase();
			if (toEdit.length() > 8) {
				toEdit = toEdit.substring(0, 8);
			}
			Course editMe = null;
			//Check for the course in both regular and nCourses.
			for (Course c : courses) {
				String courseCode = c.getDescription().split(" ")[0];
				if (courseCode.equals(toEdit)) {
					editMe = c;
				}
			}
			for (Course c : nCourses) {
				String courseCode = c.getDescription().split(" ")[0];
				if (courseCode.equals(toEdit)) {
					editMe = c;
				}
			}
			if (editMe == null) {
				clientUI.sendInfo("You do not have a course called "
						+ toEdit);
				clientUI.sendInfo("Use LIST to see your active courses.");
			} else {
				clientUI.editCourse(editMe, semester);
			}
		}
		clientUI.done();
		break;
		case "SEMESTER": //Semester setting command.
			if (clientUI.confirmSemester()) { //The user really wants to change semesters?
				//If they do, they lose all previous courses and settings.
				ignoreExtras = true;
				courses = new ArrayList<Course>();
				nCourses = new ArrayList<Course>();
				k = 0;
				schedules = new ArrayList<Schedule>();
				sortOrder = null;
				semester = null;
				ScheduleMessage request = new ScheduleMessage();
				request.setCommand("SEMESTERS");
				sendToServer(request);
			}
			break;
		case "HELP": //Help/? command: display commands.
		case "?":
			clientUI.sendInfo("There are several commands. To learn about each command, type it without any arguments.");
			clientUI.sendInfo("     ADD <COURSE-CODE> - add a course");
			clientUI.sendInfo("     ADD OPTIONAL <COURSE-CODE> - add an optional course");
			clientUI.sendInfo("     DISPLAY - displays generated schedules");
			clientUI.sendInfo("     EDIT <COURSE-CODE> - disable/enable sections and activities");
			clientUI.sendInfo("     GENERATE - generates courses after selections are made");
			clientUI.sendInfo("     LIST - lists selected courses");
			clientUI.sendInfo("     REMOVE <COURSE-CODE> - removes selected course");
			clientUI.sendInfo("     SEARCH <PREFIX> - displays a list of course codes with that prefix");
			clientUI.sendInfo("     SEMESTER - lose all settings to change semester");
			clientUI.sendInfo("     SORTORDER - set the sort order for generating a schedule");
			clientUI.done();
			break;
		case "EXPORT":
			//Get the currently viewed schedule from the GUI.
			int index = -1;
			if (command.length != 2) {
				clientUI.sendInfo("Error! Received invalid export argument: " + msg);
			} else {
				try {
				index = Integer.parseInt(command[1]);
				} catch (NumberFormatException e) {
					clientUI.sendInfo("Invalid index: " + index);
					}
			}
			Schedule sched = schedules.get(index);
			//We have the schedule that the user selected.
			String year = semester.substring(0, 4);
			String month = semester.substring(4);
			//month = String.format("%02d", month); //Month should have leading 0
			//int numDay = 1;
			//String day = "01";
			Calendar c = Calendar.getInstance();
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.YEAR, Integer.parseInt(year));
			c.set(Calendar.MONTH, Integer.parseInt(month)-1);
			c.set(Calendar.DAY_OF_MONTH, 0);
			
			boolean mon = false;
			while (!mon) {
				c.add(Calendar.DAY_OF_MONTH, 1);
				if (c.get(Calendar.DAY_OF_WEEK)== Calendar.MONDAY ) {
					mon = true;
				}
			}
			
			//Now we have a calendar with time being the first Monday of the month of the semester.
			//We'll modify the time+date for each activity.
			ICalendar ical = new ICalendar();
			
			for (CourseSelection cs: sched.getCourseSelections()) {
				for (Activity a: cs.getActivities()) {
					SimpleDateFormat hour = new SimpleDateFormat("HH");
					SimpleDateFormat minu = new SimpleDateFormat("mm");

					int startHour = Integer.parseInt(hour.format(a.getStartTime()));
					int startMinu = Integer.parseInt(minu.format(a.getStartTime()));
					
					int endHour = Integer.parseInt(hour.format(a.getEndTime()));
					int endMinu = Integer.parseInt(minu.format(a.getEndTime()));
					
					int day = a.getDay();
					/* 
					 * We must convert the day from the activity format to the offset of Monday.
					 * Right now, Sunday = 1, Saturday = 7.
					 * We need to convert Monday to 0, Sunday to 6:
					 * +5 mod 7
					 */
					day = (day+5)%7;
					
					Calendar startTime = (Calendar) c.clone();
					//We must adjust the day to be correct.
					while (day > 0) {
						day--;
						startTime.add(Calendar.DAY_OF_MONTH, 1);
					}
					
					Calendar endTime = (Calendar) startTime.clone(); //Make a correct-day clone
					
					startTime.set(Calendar.HOUR_OF_DAY, startHour);
					startTime.set(Calendar.MINUTE, startMinu);
					
					endTime.set(Calendar.HOUR_OF_DAY, endHour);
					endTime.set(Calendar.MINUTE, endMinu);
					
					VEvent event = new VEvent();
					
					event.setSummary(a.getSection().getName() + " " + a.getType() + a.getNumber());
					event.setLocation(a.getPlace());
					event.setDescription(a.getSection().getName() + " - " + a.getSection().getCourse().getDescription() + ", Professor: " + a.getProfessor());
					event.setDateStart(startTime.getTime());
					event.setDateEnd(endTime.getTime());
					Calendar cEnd = (Calendar) c.clone();
					cEnd.add(Calendar.MONTH, 3);
					Recurrence recur = new Recurrence.Builder(Frequency.WEEKLY).until(cEnd.getTime()).build();
					
					event.setRecurrenceRule(recur);
					
					ical.addEvent(event);
				}
			}
			
			
			
			File cal = new File("uOttawa " + semester+ ".ics");
			try {
				Biweekly.write(ical).go(cal);
				clientUI.savedFile(cal.getAbsolutePath());
			} catch (IOException e) {
				clientUI.sendInfo("Error writing ICS file with schedule.");
				clientUI.sendInfo("ICS Data: " + ical.toString());
			}
			clientUI.done();
			break;
		default: //Unknown command:
			clientUI.sendInfo("Unknown command: " + msg);
			clientUI.done();
		}

	}

}
