/**
 * The text parser for the csv of course codes.
 */
package ca.uottawa.schedule;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class TextParser {

	/**
	 * The current method for reading in courses.
	 * @param courses: File holding course information.
	 * @param sections: File holding section information.
	 * @param activities: File holding activity information.
	 * @return
	 */
	public static List<Course> getCoursesFromDatabase(File courses, File sections, File activities) {
		List<Course> lstCourses = new ArrayList<Course>();
		List<Section> lstSections = new ArrayList<Section>();
		List<Activity> lstActivities = new ArrayList<Activity>();
		BufferedReader brCourses;
		try {
			brCourses = new BufferedReader(new FileReader(courses));

			String courseLine = "init";

			while ((courseLine = brCourses.readLine()) != null) {
				String[] courseInfo = courseLine.split(",", 2);
				courseInfo[1] = courseInfo[1].replaceAll("\"","");
				Course aCourse = new Course(courseInfo[0] + " - " + courseInfo[1], courseInfo[0]);
				lstCourses.add(aCourse);

        try (BufferedReader brSections = new BufferedReader(new FileReader(sections))) {
            String sectionLine;
            while ((sectionLine = brSections.readLine()) != null) {
               if (sectionLine.contains(courseInfo[0])) {

       					String[] sectionInfo = sectionLine.split(",");
       					Section aSection = new Section(sectionInfo[0], sectionInfo[2], Integer.parseInt(sectionInfo[3]), Integer.parseInt(sectionInfo[4]), Integer.parseInt(sectionInfo[5]), true, aCourse);
       					aCourse.addSection(aSection);
       					sectionLine = brSections.readLine();
       					if (sectionLine==null) {
       						break;
       					}
                
                try (BufferedReader brActivities = new BufferedReader(new FileReader(activities))) {
                    String activityLine;
                    while ((activityLine = brActivities.readLine()) != null) {
                      if (activityLine.contains(","+sectionInfo[0]+",") && activityLine.contains(sectionInfo[2])) {
                        

             						String[] activityInfo = activityLine.split(",");

             						//Process new ACTIVITY:
             						String aType = new String(activityInfo[0]);
						
             						if (aType.equals("Lecture")) {
             							aType="LEC";
             						} else if (aType.equals("Seminar")) {
             						    aType="SEM";
             						} else if (aType.startsWith("Discussion")) {
             						    aType="DGD";
             						} else if (aType.equals("Tutorial")) {
             						    aType="TUT";
             						} else if (aType.equals("Laboratory")) {
             						    aType="LAB";
             						} else if (aType.startsWith("Videoconference")) {
             						    aType="VID";
             						} else if (aType.startsWith("Work")) {
             						    aType="WRK";
             						} else if (aType.equals("Research")) {
             						    aType="RSH";
             						} else if (aType.startsWith("Theory")) {
             						    aType="THR";
             						} else if (aType.startsWith("Audioconference")) {
             						    aType="AUD";
             						} else if (aType.startsWith("Course")) {
             							if (aType.contains("Internet")) {
             								aType="WEB";
             							} else if (aType.contains("activities")) {
             								aType="WOC";
             							}
             						} else {
             							System.out.println("Unknown activity found! " + aType);
             							System.exit(0);
             						}
						
             						int aNum = Integer.parseInt(activityInfo[1]);
             						int dayOfWeek;
             						switch(activityInfo[4]) {
             						case "Sunday": dayOfWeek = 1;
             						break;

             						case "Monday":dayOfWeek = 2;
             						break;

             						case "Tuesday":dayOfWeek = 3;
             						break;

             						case "Wednesday":dayOfWeek = 4;
             						break;

             						case "Thursday":dayOfWeek = 5;
             						break;

             						case "Friday":dayOfWeek = 6;
             						break;

             						case "Saturday":dayOfWeek = 7;
             						break;
             						default: dayOfWeek = 0;
             						break;
             						}
             						Date startTime, endTime;
             						SimpleDateFormat df = new SimpleDateFormat("HH:mm");
             						try {
             							startTime = df.parse(activityInfo[5]);
             							endTime = df.parse(activityInfo[6]);
             						} catch (ParseException e) {
             							startTime = new Date(0);
             							endTime = new Date(0);
             						}

             						String place = activityInfo[7];
             						String prof = null;
             						if (activityInfo.length >= 9) {
             							prof = activityInfo[8];
             						} else {
             							prof = "Not available";

             						}
             						//Create activity. The activity is automatically added to the active section.
             						Activity anActivity = new Activity(aType, aNum, dayOfWeek, startTime, endTime, place, prof, true, aSection);
       					
                        
                        
                      }
                    }
                
                brActivities.close();
                
                }
                
                
                
                
                

               }
            }
        
        brSections.close();
        
        }
        
			}
			brCourses.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (Course aCourse: lstCourses) {
			for (Section aSection: aCourse.getSections()) {
				for (Activity firstActivity: aSection.getActivities()) {
					for (Activity secondActivity: aSection.getActivities()) {
						if (firstActivity!=secondActivity) {
							if (aSection.getRequiredDGD() == 0 && firstActivity.getType().equals("DGD") && secondActivity.getType().equals("DGD")) {
								if (firstActivity.overlaps(secondActivity)) {
									System.err.println("Setting course " + aSection.getName() + " to have 1 required DGD");
									aSection.setRequiredDGD(1); // conflicting multiple DGDs!
								}
							} else if (aSection.getRequiredLAB() == 0 && firstActivity.getType().equals("LAB") && secondActivity.getType().equals("LAB")) {
								if (firstActivity.overlaps(secondActivity)) {
									aSection.setRequiredLAB(1); // conflicting multiple LABs!
								}
							} else if (aSection.getRequiredTUT() == 0 && firstActivity.getType().equals("TUT") && secondActivity.getType().equals("TUT")) {
								if (firstActivity.overlaps(secondActivity)) {
									aSection.setRequiredTUT(1); // conflicting multiple LABs!
								}
							} else if (firstActivity.getType().equals("LEC") && secondActivity.getType().equals("LEC")) {
								if (firstActivity.overlaps(secondActivity)) {
									System.err.println("Warning! Removing section because it has conflicting lectures! Section: " + aSection.getName());
									aCourse.removeSection(aSection);
								}
							}
						}
					}
				}
			}
		}
		Collections.sort(lstCourses);
		return lstCourses;
	}

	/**
	 * This is the old method of reading course codes. Since switching to a database format, this is deprecated.
	 * @param courseCodes: A file containing all activity and course information.
	 * @return A list of course objects used by the program for all its operations.
	 */
	public static List<Course> getCoursesFromOldDatabase(File courseCodes) {
		List<Course> courses = new ArrayList<Course>();
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(courseCodes));
			String line;
			Course activeCourse = new Course("Initializing", "Not set");
			Section activeSection = new Section(null, null, 0, 0, 0, true, activeCourse);
			String semester = null;

			while ((line = br.readLine()) != null) {
				String[] items = line.split(",");

				/*Each course has its first line dedicated to the name of the course.
				 *If the items list is only 1, then we know that we have a course.
				 */
				if (items.length < 9) { //This is going to be a course.
					//First, add the old, now completed course, into our global course list.
					if (!activeCourse.getDescription().equals("Initializing")) { //We don't want to add the dummy course.
						courses.add(activeCourse);
					}

					String courseTitle = items[0];
					if (courseTitle.startsWith("\"")) {
						courseTitle = new String(courseTitle.substring(1));

						int title = 1;
						while (!items[title].endsWith("\"")) {
							courseTitle = new String(courseTitle + ", " + items[title]);
							title++;
						}
						courseTitle = new String(courseTitle + "," + items[title]);
						courseTitle.substring(0, courseTitle.length()-1);
					}
					String courseCode = courseTitle.substring(0, courseTitle.indexOf(' '));
					activeCourse = new Course(courseTitle, courseCode);
					//We are done with this line.
				} else {
					//Process new SEMESTER
					if(items[0].length() > 0) {
						/*
						 * If the first item in the line is not the course, but still has a length,
						 * then it marks the start of a new semester.
						 */
						semester = items[0];
					}
					if (items[1].length() > 0) {
						//Process new course SECTION:
						//Next is the course code. This is where limits like 1DGD 1TUT are placed.
						String courseLimits[] = items[1].split(" ");
						String sectionName = courseLimits[0]; //Index 0 will be the course code.
						Integer requiredDGD = 0;
						Integer requiredTUT = 0;
						Integer requiredLAB = 0;
						for (int i = 1; i < courseLimits.length; i++) { //Check rest of array for limits

							int count = Integer.parseInt(String.valueOf(courseLimits[i].charAt(0))); //Get the relevant number
							char startingChar = courseLimits[i].charAt(1);
							if (startingChar == 'D') {
								requiredDGD = count;
							} else if (startingChar == 'T') {
								requiredTUT = count;
							} else if (startingChar == 'L') {
								requiredLAB = count;
							}
						}
						//Now we have all the tools we need to create the section
						activeSection = new Section(sectionName, semester, requiredDGD, requiredTUT, requiredLAB, true, activeCourse);
						//The course automatically gets pointed to the section, thank you Umple!

					} //The rest of the line of the section is an activity (like LAB, SEM, etc)

					//Process new ACTIVITY:
					String aType = new String(items[2]);
					int aNum = Integer.parseInt(items[3]);
					int dayOfWeek;
					switch(items[4]) {
					case "U": dayOfWeek = 1;
					break;

					case "M":dayOfWeek = 2;
					break;

					case "T":dayOfWeek = 3;
					break;

					case "W":dayOfWeek = 4;
					break;

					case "R":dayOfWeek = 5;
					break;

					case "F":dayOfWeek = 6;
					break;

					case "S":dayOfWeek = 7;
					break;
					default: dayOfWeek = 0;
					break;
					}
					Date startTime, endTime;
					SimpleDateFormat df = new SimpleDateFormat("HH:mm");
					try {
						startTime = df.parse(items[5]);
						endTime = df.parse(items[6]);
					} catch (ParseException e) {
						startTime = new Date(0);
						endTime = new Date(0);
					}

					String place = items[7];
					String prof = items[8];
					//Create activity. The activity is automatically added to the active section.
					Activity currentActivity = new Activity(aType, aNum, dayOfWeek, startTime, endTime, place, prof, true, activeSection);
					activeCourse.addSection(activeSection);
				}
			}
			//Add final course to list
			courses.add(activeCourse);
			br.close();
		} catch (FileNotFoundException e) {
			System.err.println("Error reading in course codes. No such file " + courseCodes + " found.");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//Return the database of courses.
		return courses;
	}
}
