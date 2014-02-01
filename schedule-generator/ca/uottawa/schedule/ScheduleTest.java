package ca.uottawa.schedule;

import static org.junit.Assert.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;


public class ScheduleTest {

	public void test() {
		fail("Not yet implemented");
	}
	@Test
	public void testCourseHasSection() {
		Course c1 = new Course("COURSE1", "ASET234");
		Section s1 = new Section("COURSE1A", "SEM1", 0,0,0, true, c1);
		DateFormat sdf = new SimpleDateFormat("HH:mm");
		Date d1 = null;
		Date d2 = null;
		try {
			d1 = sdf.parse("8:30");
			d2 = sdf.parse("10:00");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Activity a1 = new Activity("LEC", 1, 2, d1, d2, "PLACE", "PROF", true, s1);
		assertEquals(c1.getSection(0), s1);
	}
	
	@Test
	public void testSectionHasActivity() {
		Course c1 = new Course("COURSE1", "ASET234");
		Section s1 = new Section("COURSE1A", "SEM1", 0,0,0, true, c1);
		DateFormat sdf = new SimpleDateFormat("HH:mm");
		Date d1 = null;
		Date d2 = null;
		try {
			d1 = sdf.parse("8:30");
			d2 = sdf.parse("10:00");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Activity a1 = new Activity("LEC", 1, 2, d1, d2, "PLACE", "PROF", true, s1);
		assertEquals(s1.getActivity(0), a1);
	}
	
	
	@Test
	public void testSingleCourseSelectionGeneration() {
		Course c1 = new Course("COURSE1", "ASET234");
		Section s1 = new Section("COURSE1A", "SEM1", 0,0,0, true, c1);
		DateFormat sdf = new SimpleDateFormat("HH:mm");
		Date d1 = null;
		Date d2 = null;
		try {
			d1 = sdf.parse("8:30");
			d2 = sdf.parse("10:00");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Activity a1 = new Activity("LEC", 1, 2, d1, d2, "PLACE", "PROF", true, s1);
		List<CourseSelection> cs1 = c1.getCourseSelections();
		assertEquals(1, cs1.size());
	}
	
	@Test
	public void testTwoCourseSelectionGeneration() {
		Course c1 = new Course("COURSE1", "ASET234");
		Section s1 = new Section("COURSE1A", "SEM1", 0,0,0, true, c1);
		Section s2 = new Section("COURSE1B", "SEM1", 0,0,0, true, c1);
		DateFormat sdf = new SimpleDateFormat("HH:mm");
		Date d1 = null;
		Date d2 = null;
		try {
			d1 = sdf.parse("8:30");
			d2 = sdf.parse("10:00");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Activity a1 = new Activity("LEC", 1, 2, d1, d2, "PLACE", "PROF", true, s1);
		Activity a2 = new Activity("LEC", 1, 2, d1, d2, "PLACE", "PROF", true, s2);
		List<CourseSelection> cs1 = c1.getCourseSelections();
		assertEquals(2, cs1.size());
	}
	
	@Test
	public void testScheduleOneCourse() {
		Course c1 = new Course("COURSE1", "ASET234");
		Section s1 = new Section("COURSE1A", "SEM1", 0,0,0, true, c1);
		Section s2 = new Section("COURSE1B", "SEM1", 0,0,0, true, c1);
		DateFormat sdf = new SimpleDateFormat("HH:mm");
		Date d1 = null;
		Date d2 = null;
		try {
			d1 = sdf.parse("8:30");
			d2 = sdf.parse("10:00");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Activity a1 = new Activity("LEC", 1, 2, d1, d2, "PLACE", "PROF", true, s1);
		Activity a2 = new Activity("LEC", 1, 2, d1, d2, "PLACE", "PROF", true, s2);
		List<Course> courses = new ArrayList<Course>();
		courses.add(c1); //Adding course with two sections. Should have 2 schedules.
		List<Schedule> schedules = Schedule.generateSchedules(courses);
		
		assertEquals(2, schedules.size());
	}
	
	@Test
	public void testScheduleTwoCourseConflict() {
		Course c1 = new Course("COURSE1", "ASET234");
		Course c2 = new Course("COURSE2", "ASET235");
		Section s1 = new Section("COURSE1A", "SEM1", 0,0,0, true, c1);
		Section s2 = new Section("COURSE2A", "SEM1", 0,0,0, true, c2);
		DateFormat sdf = new SimpleDateFormat("HH:mm");
		Date d1 = null;
		Date d2 = null;
		try {
			d1 = sdf.parse("8:30");
			d2 = sdf.parse("10:00");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Activity a1 = new Activity("LEC", 1, 2, d1, d2, "PLACE", "PROF", true, s1);
		Activity a2 = new Activity("LEC", 1, 2, d1, d2, "PLACE", "PROF", true, s2);
		List<Course> courses = new ArrayList<Course>();
		courses.add(c1); //Adding course
		courses.add(c2); //adding course with same times.
		List<Schedule> schedules = Schedule.generateSchedules(courses);
		//There is a conflict. Return no schedulse.
		assertEquals(0, schedules.size());
	}
	
	@Test
	public void testScheduleOneCourseTwoLabs() {
		Course c1 = new Course("COURSE1", "ASET234");
		Section s1 = new Section("COURSE1A", "SEM1", 0,0,1, true, c1);
		DateFormat sdf = new SimpleDateFormat("HH:mm");
		Date d1 = null;
		Date d2 = null;
		try {
			d1 = sdf.parse("8:30");
			d2 = sdf.parse("10:00");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Activity a1 = new Activity("LAB", 1, 2, d1, d2, "PLACE", "PROF", true, s1);
		Activity a2 = new Activity("LAB", 1, 2, d1, d2, "PLACE", "PROF", true, s1);
		List<Course> courses = new ArrayList<Course>();
		courses.add(c1); //Adding course with two labs, only one required.
		List<Schedule> schedules = Schedule.generateSchedules(courses);
		//Should generate two schedules.
		assertEquals(2, schedules.size());
	}
	
	@Test
	public void testScheduleTwoCoursesFourLabsEachTouchingTimes() {
		Course c1 = new Course("COURSE1", "ASET234");
		Course c2 = new Course("COURSE2", "ASET235");
		Section s1 = new Section("COURSE1A", "SEM1", 0,0,1, true, c1);
		Section s2 = new Section("COURSE2A", "SEM1", 0,0,1, true, c2);
		DateFormat sdf = new SimpleDateFormat("HH:mm");
		Date d1 = null;
		Date d2 = null;
		Date d3 = null;
		Date d4 = null;
		try {
			d1 = sdf.parse("8:30");
			d2 = sdf.parse("10:00");
			d3 = sdf.parse("10:00");
			d4 = sdf.parse("14:00"); //Really long lab (6 hours)
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		
		Activity a1 = new Activity("LAB", 1, 2, d1, d2, "PLACE", "PROF", true, s1);
		Activity a2 = new Activity("LAB", 2, 2, d1, d2, "PLACE", "PROF", true, s1);
		Activity a3 = new Activity("LAB", 3, 2, d1, d2, "PLACE", "PROF", true, s1);
		Activity a4 = new Activity("LAB", 4, 2, d1, d2, "PLACE", "PROF", true, s1);
		Activity a5 = new Activity("LAB", 1, 2, d3, d4, "PLACE", "PROF", true, s2);
		Activity a6 = new Activity("LAB", 2, 2, d3, d4, "PLACE", "PROF", true, s2);
		Activity a7 = new Activity("LAB", 3, 2, d3, d4, "PLACE", "PROF", true, s2);
		Activity a8 = new Activity("LAB", 4, 2, d3, d4, "PLACE", "PROF", true, s2);
		List<Course> courses = new ArrayList<Course>();
		courses.add(c1); //Adding course with 4 labs, 1 required. Should generate 4 schedules alone.
		courses.add(c2); //Should generate 4 schedules alone.
		//Total should be 4*4 = 16 schedules.
		List<Schedule> schedules = Schedule.generateSchedules(courses);
		//Should generate two schedules.
		assertEquals(16, schedules.size());
	}
	
	@Test
	public void testScheduleTwoCoursesFourLabsEachOverlappingTimes() {
		Course c1 = new Course("COURSE1", "ASET234");
		Course c2 = new Course("COURSE2", "ASET235");
		Section s1 = new Section("COURSE1A", "SEM1", 0,0,1, true, c1);
		Section s2 = new Section("COURSE2A", "SEM1", 0,0,1, true, c2);
		DateFormat sdf = new SimpleDateFormat("HH:mm");
		Date d1 = null;
		Date d2 = null;
		Date d3 = null;
		Date d4 = null;
		try {
			d1 = sdf.parse("8:30");
			d2 = sdf.parse("10:00");
			d3 = sdf.parse("9:00"); //The labs are now overlaping. 
			d4 = sdf.parse("14:00"); //Really long lab (7 hours)
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		
		Activity a1 = new Activity("LAB", 1, 2, d1, d2, "PLACE", "PROF", true, s1);
		Activity a2 = new Activity("LAB", 2, 2, d1, d2, "PLACE", "PROF", true, s1);
		Activity a3 = new Activity("LAB", 3, 2, d1, d2, "PLACE", "PROF", true, s1);
		Activity a4 = new Activity("LAB", 4, 2, d1, d2, "PLACE", "PROF", true, s1);
		Activity a5 = new Activity("LAB", 1, 2, d3, d4, "PLACE", "PROF", true, s2);
		Activity a6 = new Activity("LAB", 2, 2, d3, d4, "PLACE", "PROF", true, s2);
		Activity a7 = new Activity("LAB", 3, 2, d3, d4, "PLACE", "PROF", true, s2);
		Activity a8 = new Activity("LAB", 4, 2, d3, d4, "PLACE", "PROF", true, s2);
		List<Course> courses = new ArrayList<Course>();
		courses.add(c1); //Adding course with 4 labs, 1 required. Should generate 4 schedules alone.
		courses.add(c2); //Should generate 4 schedules alone.
		//Total should be 4*4 = 16 schedules, but with conflicts: 0.
		List<Schedule> schedules = Schedule.generateSchedules(courses);
		//Should generate no schedules.
		assertEquals(0, schedules.size());
	}
	
	@Test
	public void testScheduleStatistics() {
		Course c1 = new Course("COURSE1", "ASET234");
		Course c2 = new Course("COURSE2", "ASET235");
		Section s1 = new Section("COURSE1A", "SEM1", 0,0,1, true, c1);
		Section s2 = new Section("COURSE2A", "SEM1", 0,0,1, true, c2);
		DateFormat sdf = new SimpleDateFormat("HH:mm");
		Date d1 = null;
		Date d2 = null;
		Date d3 = null;
		Date d4 = null;
		try {
			d1 = sdf.parse("8:30");
			d2 = sdf.parse("10:00");
			d3 = sdf.parse("10:00");
			d4 = sdf.parse("14:00"); //Really long lab (6 hours)
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		
		Activity a1 = new Activity("LAB", 1, 2, d1, d2, "PLACE", "PROF", true, s1);
		Activity a2 = new Activity("LAB", 2, 2, d1, d2, "PLACE", "PROF", true, s1);
		Activity a3 = new Activity("LAB", 3, 2, d1, d2, "PLACE", "PROF", true, s1);
		Activity a4 = new Activity("LAB", 4, 2, d1, d2, "PLACE", "PROF", true, s1);
		Activity a5 = new Activity("LAB", 1, 2, d3, d4, "PLACE", "PROF", true, s2);
		Activity a6 = new Activity("LAB", 2, 2, d3, d4, "PLACE", "PROF", true, s2);
		Activity a7 = new Activity("LAB", 3, 2, d3, d4, "PLACE", "PROF", true, s2);
		Activity a8 = new Activity("LAB", 4, 2, d3, d4, "PLACE", "PROF", true, s2);
		List<Course> courses = new ArrayList<Course>();
		courses.add(c1); //Adding course with 4 labs, 1 required. Should generate 4 schedules alone.
		courses.add(c2); //Should generate 4 schedules alone.
		//Total should be 4*4 = 16 schedules, but with conflicts: 0.
		List<Schedule> schedules = Schedule.generateSchedules(courses);
		//Should generate no schedules.
		//Since all classes are the same day, all stats should say 6 days off.
		for (Schedule s: schedules) {
			s.updateStats();
			assertEquals(6, s.getNumOfDaysOff()); //Check days off
			assertEquals(d1.getTime(), s.getAvgStartingTime().getTime()); //Avg starting time is 8:30
			assertEquals(d4.getTime(), s.getAvgEndingTime().getTime()); //Avg ending time is 14
			assertEquals((d4.getTime()-d1.getTime()), s.getAvgLengthOfDay().getTime()); //Avg length is 7.5 hours
		}
	}
	
	@Test
	public void testPartialSearch() {
		Course c1 = new Course("COURSE1", "ASET234");
		Course c2 = new Course("COURSE2", "ASET235");
		Section s1 = new Section("COURSE1A", "SEM1", 0,0,1, true, c1);
		Section s2 = new Section("COURSE2A", "SEM1", 0,0,1, true, c2);
		
		List<Course> courses = new ArrayList<Course>();
		courses.add(c1); //ADD COURSE1
		courses.add(c2);  //ADD COURSE2
		List<String> searchResults = CourseSearch.search("C", "SEM1", courses);
		//Searching for "C" should return BOTH courses using our binary search algorithm.
		assertEquals(2, searchResults.size());
		assertEquals("COURSE1", searchResults.get(0));
		assertEquals("COURSE2", searchResults.get(1));
	}
	
	@Test
	public void testFullSearch() {
		Course c1 = new Course("COURSE1", "ASET234");
		Course c2 = new Course("COURSE2", "ASET235");
		Section s1 = new Section("COURSE1A", "SEM1", 0,0,1, true, c1);
		Section s2 = new Section("COURSE2A", "SEM1", 0,0,1, true, c2);
		
		List<Course> courses = new ArrayList<Course>();
		courses.add(c1); //ADD COURSE1
		courses.add(c2);  //ADD COURSE2
		List<String> searchResults = CourseSearch.search("COURSE1", "SEM1", courses);
		//Searching for "COURSE1" should return ONE course using our binary search algorithm.
		assertEquals(1, searchResults.size());
		assertEquals("COURSE1", searchResults.get(0));
	}
	
	@Test
	public void testFailedSearch() {
		Course c1 = new Course("COURSE1", "ASET234");
		Course c2 = new Course("COURSE2", "ASET235");
		Section s1 = new Section("COURSE1A", "SEM1", 0,0,1, true, c1);
		Section s2 = new Section("COURSE2A", "SEM1", 0,0,1, true, c2);
		
		List<Course> courses = new ArrayList<Course>();
		courses.add(c1); //ADD COURSE1
		courses.add(c2);  //ADD COURSE2
		List<String> searchResults = CourseSearch.search("THIS", "SEM1", courses);
		//Searching for this should return nothing
		assertEquals(0, searchResults.size());
	}
	
	@Test
	public void testFailedSearchLongWithSpaces() {
		Course c1 = new Course("COURSE1", "ASET234");
		Course c2 = new Course("COURSE2", "ASET235");
		Section s1 = new Section("COURSE1A", "SEM1", 0,0,1, true, c1);
		Section s2 = new Section("COURSE2A", "SEM1", 0,0,1, true, c2);
		
		List<Course> courses = new ArrayList<Course>();
		courses.add(c1); //ADD COURSE1
		courses.add(c2);  //ADD COURSE2
		List<String> searchResults = CourseSearch.search("THIS IS A SMELLY STRING", "SEM1", courses);
		//Searching for this should return nothing
		assertEquals(0, searchResults.size());
	}
	
	@Test
	public void testSearchWrongSemester() {
		Course c1 = new Course("COURSE1", "ASET234");
		Course c2 = new Course("COURSE2", "ASET235");
		Section s1 = new Section("COURSE1A", "SEM1", 0,0,1, true, c1);
		Section s2 = new Section("COURSE2A", "SEM1", 0,0,1, true, c2);
		
		List<Course> courses = new ArrayList<Course>();
		courses.add(c1); //ADD COURSE1
		courses.add(c2);  //ADD COURSE2
		List<String> searchResults = CourseSearch.search("C", "SEM2", courses);
		//Searching for this should return nothing because of different semester
		assertEquals(0, searchResults.size());
	}
	
}
