package ca.uottawa.schedule;

/*PLEASE DO NOT EDIT THIS CODE*/
/*This code was generated using the UMPLE 1.18.0.3209 modeling language!*/


import java.util.*;

// line 35 "model.ump"
public class Schedule
{
  @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
  public @interface umplesourcefile{int[] line();String[] file();int[] javaline();int[] length();}

  //------------------------
  // MEMBER VARIABLES
  //------------------------

  //Schedule Attributes
  private int avgStartingTime;
  private int avgEndingTime;
  private int avgLengthOfDay;
  private int numOfDaysOff;
  private int ignoreExtrasAvgStartingTime;
  private int ignoreExtrasAvgEndingTime;
  private int ignoreExtrasAvgLengthOfDay;
  private int ignoreExtrasNumOfDaysOff;

  //Schedule Associations
  private List<CourseSelection> courseSelections;

  //------------------------
  // CONSTRUCTOR
  //------------------------

  public Schedule(CourseSelection... allCourseSelections)
  {
    courseSelections = new ArrayList<CourseSelection>();
    boolean didAddCourseSelections = setCourseSelections(allCourseSelections);
    if (!didAddCourseSelections)
    {
      throw new RuntimeException("Unable to create Schedule, must have at least 1 courseSelections");
    }
    
    updateStats();
  }

  //------------------------
  // INTERFACE
  //------------------------

  public boolean setAvgStartingTime(int aAvgStartingTime)
  {
    boolean wasSet = false;
    avgStartingTime = aAvgStartingTime;
    wasSet = true;
    return wasSet;
  }

  public boolean setAvgEndingTime(int aAvgEndingTime)
  {
    boolean wasSet = false;
    avgEndingTime = aAvgEndingTime;
    wasSet = true;
    return wasSet;
  }

  public boolean setAvgLengthOfDay(int aAvgLengthOfDay)
  {
    boolean wasSet = false;
    avgLengthOfDay = aAvgLengthOfDay;
    wasSet = true;
    return wasSet;
  }

  public boolean setNumOfDaysOff(int aNumOfDaysOff)
  {
    boolean wasSet = false;
    numOfDaysOff = aNumOfDaysOff;
    wasSet = true;
    return wasSet;
  }

  public boolean setIgnoreExtrasAvgStartingTime(int aIgnoreExtrasAvgStartingTime)
  {
    boolean wasSet = false;
    ignoreExtrasAvgStartingTime = aIgnoreExtrasAvgStartingTime;
    wasSet = true;
    return wasSet;
  }

  public boolean setIgnoreExtrasAvgEndingTime(int aIgnoreExtrasAvgEndingTime)
  {
    boolean wasSet = false;
    ignoreExtrasAvgEndingTime = aIgnoreExtrasAvgEndingTime;
    wasSet = true;
    return wasSet;
  }

  public boolean setIgnoreExtrasAvgLengthOfDay(int aIgnoreExtrasAvgLengthOfDay)
  {
    boolean wasSet = false;
    ignoreExtrasAvgLengthOfDay = aIgnoreExtrasAvgLengthOfDay;
    wasSet = true;
    return wasSet;
  }

  public boolean setIgnoreExtrasNumOfDaysOff(int aIgnoreExtrasNumOfDaysOff)
  {
    boolean wasSet = false;
    ignoreExtrasNumOfDaysOff = aIgnoreExtrasNumOfDaysOff;
    wasSet = true;
    return wasSet;
  }

  public int getAvgStartingTime()
  {
    return avgStartingTime;
  }

  public int getAvgEndingTime()
  {
    return avgEndingTime;
  }

  public int getAvgLengthOfDay()
  {
    return avgLengthOfDay;
  }

  public int getNumOfDaysOff()
  {
    return numOfDaysOff;
  }

  /**
   * Used when ignoring DGDs / TUTs in sorting order.
   */
  public int getIgnoreExtrasAvgStartingTime()
  {
    return ignoreExtrasAvgStartingTime;
  }

  public int getIgnoreExtrasAvgEndingTime()
  {
    return ignoreExtrasAvgEndingTime;
  }

  public int getIgnoreExtrasAvgLengthOfDay()
  {
    return ignoreExtrasAvgLengthOfDay;
  }

  public int getIgnoreExtrasNumOfDaysOff()
  {
    return ignoreExtrasNumOfDaysOff;
  }

  public CourseSelection getCourseSelection(int index)
  {
    CourseSelection aCourseSelection = courseSelections.get(index);
    return aCourseSelection;
  }

  public List<CourseSelection> getCourseSelections()
  {
    List<CourseSelection> newCourseSelections = Collections.unmodifiableList(courseSelections);
    return newCourseSelections;
  }

  public int numberOfCourseSelections()
  {
    int number = courseSelections.size();
    return number;
  }

  public boolean hasCourseSelections()
  {
    boolean has = courseSelections.size() > 0;
    return has;
  }

  public int indexOfCourseSelection(CourseSelection aCourseSelection)
  {
    int index = courseSelections.indexOf(aCourseSelection);
    return index;
  }

  public static int minimumNumberOfCourseSelections()
  {
    return 1;
  }

  public boolean addCourseSelection(CourseSelection aCourseSelection)
  {
    boolean wasAdded = false;
    if (courseSelections.contains(aCourseSelection)) { return false; }
    courseSelections.add(aCourseSelection);
    wasAdded = true;
    return wasAdded;
  }

  public boolean removeCourseSelection(CourseSelection aCourseSelection)
  {
    boolean wasRemoved = false;
    if (!courseSelections.contains(aCourseSelection))
    {
      return wasRemoved;
    }

    if (numberOfCourseSelections() <= minimumNumberOfCourseSelections())
    {
      return wasRemoved;
    }

    courseSelections.remove(aCourseSelection);
    wasRemoved = true;
    return wasRemoved;
  }

  public boolean setCourseSelections(CourseSelection... newCourseSelections)
  {
    boolean wasSet = false;
    ArrayList<CourseSelection> verifiedCourseSelections = new ArrayList<CourseSelection>();
    for (CourseSelection aCourseSelection : newCourseSelections)
    {
      if (verifiedCourseSelections.contains(aCourseSelection))
      {
        continue;
      }
      verifiedCourseSelections.add(aCourseSelection);
    }

    if (verifiedCourseSelections.size() != newCourseSelections.length || verifiedCourseSelections.size() < minimumNumberOfCourseSelections())
    {
      return wasSet;
    }

    courseSelections.clear();
    courseSelections.addAll(verifiedCourseSelections);
    wasSet = true;
    return wasSet;
  }

  public boolean addCourseSelectionAt(CourseSelection aCourseSelection, int index)
  {  
    boolean wasAdded = false;
    if(addCourseSelection(aCourseSelection))
    {
      if(index < 0 ) { index = 0; }
      if(index > numberOfCourseSelections()) { index = numberOfCourseSelections() - 1; }
      courseSelections.remove(aCourseSelection);
      courseSelections.add(index, aCourseSelection);
      wasAdded = true;
    }
    return wasAdded;
  }

  public boolean addOrMoveCourseSelectionAt(CourseSelection aCourseSelection, int index)
  {
    boolean wasAdded = false;
    if(courseSelections.contains(aCourseSelection))
    {
      if(index < 0 ) { index = 0; }
      if(index > numberOfCourseSelections()) { index = numberOfCourseSelections() - 1; }
      courseSelections.remove(aCourseSelection);
      courseSelections.add(index, aCourseSelection);
      wasAdded = true;
    } 
    else 
    {
      wasAdded = addCourseSelectionAt(aCourseSelection, index);
    }
    return wasAdded;
  }

  public void delete()
  {
    courseSelections.clear();
  }


  public String toString()
  {
	  String outputString = "";
    return super.toString() + "["+
            "avgStartingTime" + ":" + getAvgStartingTime()+ "," +
            "avgEndingTime" + ":" + getAvgEndingTime()+ "," +
            "avgLengthOfDay" + ":" + getAvgLengthOfDay()+ "," +
            "numOfDaysOff" + ":" + getNumOfDaysOff()+ "," +
            "ignoreExtrasAvgStartingTime" + ":" + getIgnoreExtrasAvgStartingTime()+ "," +
            "ignoreExtrasAvgEndingTime" + ":" + getIgnoreExtrasAvgEndingTime()+ "," +
            "ignoreExtrasAvgLengthOfDay" + ":" + getIgnoreExtrasAvgLengthOfDay()+ "," +
            "ignoreExtrasNumOfDaysOff" + ":" + getIgnoreExtrasNumOfDaysOff()+ "]"
     + outputString;
  }
  
  //------------------------
  // MANUALLY-MADE METHODS
  //------------------------
  
  public void updateStats() {
	  
  }

  
public static List<Schedule> generateSchedules(Course[] selectedCourses) {
	int count = selectedCourses.length;
	List<ArrayList<CourseSelection>> selections = new ArrayList<ArrayList<CourseSelection>>();
	for (int i = 0; i < count; i++) {
		System.out.println("count : " + count + " i: " + i);
		selections.add((ArrayList<CourseSelection>) selectedCourses[i].getCourseSelections());
	}
	List<Schedule> schedules = generateSchedules(selections, count-1);
	return schedules;
}
private static List<Schedule> generateSchedules(List<ArrayList<CourseSelection>> selections, int count) {
	List<Schedule> schedules = new ArrayList<Schedule>();
	if (count == 0) {
		for (CourseSelection cs : selections.get(count)) {
			//This is one course's course selections.
			schedules.add(new Schedule(cs));
		}
	} else {
		schedules = generateSchedules(selections, count-1);
		List<Schedule> nextSchedules = new ArrayList<Schedule>();
		for (Schedule s : schedules) {
			for (CourseSelection cs : selections.get(count)) {
				if (!s.collidesWith(cs)) {
					nextSchedules.add(new Schedule(cs));
				}
			}
		}
		return nextSchedules;
	}
	return schedules;
}

private boolean collidesWith(CourseSelection e) {
	for (CourseSelection cs : courseSelections) {
		if (e.overlaps(cs)) {
			return true;
		}
	}
	return false;
}


}