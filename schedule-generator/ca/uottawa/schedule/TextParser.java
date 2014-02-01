/**
 * The text parser for the csv of course codes.
 */
package ca.uottawa.schedule;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TextParser {
public static List<Course> getCoursesFromDatabase(File courseCodes) {
	List<Course> courses = new ArrayList<Course>();
	BufferedReader br;
	try {
		br = new BufferedReader(new FileReader(courseCodes));
		String line;
		Course activeCourse = new Course("Initializing", "Not set");
		Section activeSection = new Section(null, null, 0, 0, 0, false, activeCourse);
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
