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
	// Variables for n choose k courses.
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

	public ScheduleGeneratorClient(String studentNumber, String host, int port,
			ClientConsole clientUI) throws IOException {
		super(host, port); // Call the superclass constructor
		this.clientUI = clientUI;
		this.ignoreExtras = false;
		this.studentNumber = studentNumber;
		this.sortOrder = null;
		this.schedules = new ArrayList<Schedule>();
		this.courses = new ArrayList<Course>();
		this.nCourses = new ArrayList<Course>();
		this.k = 0;
		openConnection();

		if (this.semester == null) {
			ScheduleMessage request = new ScheduleMessage();
			request.setCommand("SEMESTERS");
			sendToServer(request);
		} else {
			clientUI.done();
		}

		// Send message to log in
	}

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
				clientUI.sendInfo(result.getDescription() + " added.");
			}
			break;

		case "ADDCOURSE-MULTIPLECOURSES":
			clientUI.sendInfo("No single course matches that course code. Type the course code exactly. If you can't remember it, use SEARCH");
			break;
		case "SCHEDULES":
			// The server has provided us with a generated list of schedules,
			// sorted in sortOrder
			schedules = message.getSchedules();
			clientUI.sendInfo(schedules.size() + " schedules generated.");
			break;
		default:
			;
		}
		clientUI.done();
	}

	public void quit() {
		try {
			closeConnection();
		} catch (IOException e) {
		}
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
		// Woot.
		// Add <coursecode>
		// Search <partial couse-code
		// edit <course>
		// Other plans?
		String[] command = ((String) msg).split(" "); // Split string from
														// client into a bunch
														// of

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
		case "REMOVE":
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
				if (removeMe == null) {
					clientUI.sendInfo("You do not have a course called "
							+ toRemove);
					clientUI.sendInfo("Use LIST to see your active courses.");
				} else {
					courses.remove(removeMe);
					clientUI.sendInfo("Removed " + removeMe.getDescription());
				}
				clientUI.done();
			}
			break;
		case "SETK":
			if (command.length != 2) {
				clientUI.sendInfo("To set K, which is the number of courses you'd like to have out of your optional courses, use: SETK [int]");
			} else {
				try {
				k = Integer.parseInt(command[1]);
				clientUI.sendInfo("k set to " + k);
				} catch (NumberFormatException e) {
					clientUI.sendInfo("You have not specified a valid value for k. " + k + " is not understood.");
					clientUI.sendInfo("To set K, which is the number of courses you'd like to have out of your optional courses, use: SETK [int]");
				} finally {
					clientUI.done();
				}
			}
		break;
		case "GENERATE":
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
				clientUI.sendInfo("You haven't specified a sort order yet. To choose one, use: SORTORDER");
				clientUI.done();
			} else {
				ScheduleMessage generateMsg = new ScheduleMessage();
				generateMsg.setOptionalCourses(nCourses);
				generateMsg.setK(k);
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
			break;
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
				// There are schedules to be displayed.
				// We will let the client display the schedules.
				clientUI.displaySchedules(schedules);
			}
			clientUI.done();
			break;
		case "LIST":
			clientUI.setCourses(courses, nCourses);
			clientUI.done();
			break;
		case "IGNOREEXTRAS":
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
				}
				clientUI.sendInfo("Ignore Extras set to " + ignoreExtras);
				} catch (NumberFormatException e) {
					clientUI.sendInfo("You have not specified a valid value for k. " + k + " is not understood.");
					clientUI.sendInfo("To set K, which is the number of courses you'd like to have out of your optional courses, use: SETK [int]");
				} finally {
					clientUI.done();
				}
			}
			break;
		case "EDIT":
		if (command.length != 2) {
			clientUI.sendInfo("To use edit, type EDIT <coursecode>. Note, to edit a course, you must have it in your list of courses. To add it, use ADD <coursecode>.");
		} else {
			//let's see if this course exists.
			String toEdit = command[1].toUpperCase();
			if (toEdit.length() > 8) {
				toEdit = toEdit.substring(0, 8);
			}
			Course editMe = null;
			for (Course c : courses) {
				String courseCode = c.getDescription().split(" ")[0];
				if (courseCode.equals(toEdit)) {
					editMe = c;
				}
			}
			if (toEdit == null) {
				clientUI.sendInfo("You do not have a course called "
						+ toEdit);
				clientUI.sendInfo("Use LIST to see your active courses.");
			} else {
				clientUI.editCourse(editMe, semester);
			}
			clientUI.done();
		}
		break;
		case "SEMESTER":
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
		case "HELP":
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
			break;
		default:
			clientUI.sendInfo("Unknown command: " + msg);
			clientUI.done();
		}

	}

}
