package ca.uottawa.schedule;

/*PLEASE DO NOT EDIT THIS CODE*/
/*This code was generated using the UMPLE 1.18.0.3214 modeling language!*/


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
  private Date avgStartingTime;
  private Date avgEndingTime;
  private Date avgLengthOfDay;
  private int numOfDaysOff;
  private Date ignoreExtrasAvgStartingTime;
  private Date ignoreExtrasAvgEndingTime;
  private Date ignoreExtrasAvgLengthOfDay;
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

  public boolean setAvgStartingTime(Date aAvgStartingTime)
  {
    boolean wasSet = false;
    avgStartingTime = aAvgStartingTime;
    wasSet = true;
    return wasSet;
  }

  public boolean setAvgEndingTime(Date aAvgEndingTime)
  {
    boolean wasSet = false;
    avgEndingTime = aAvgEndingTime;
    wasSet = true;
    return wasSet;
  }

  public boolean setAvgLengthOfDay(Date aAvgLengthOfDay)
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

  public boolean setIgnoreExtrasAvgStartingTime(Date aIgnoreExtrasAvgStartingTime)
  {
    boolean wasSet = false;
    ignoreExtrasAvgStartingTime = aIgnoreExtrasAvgStartingTime;
    wasSet = true;
    return wasSet;
  }

  public boolean setIgnoreExtrasAvgEndingTime(Date aIgnoreExtrasAvgEndingTime)
  {
    boolean wasSet = false;
    ignoreExtrasAvgEndingTime = aIgnoreExtrasAvgEndingTime;
    wasSet = true;
    return wasSet;
  }

  public boolean setIgnoreExtrasAvgLengthOfDay(Date aIgnoreExtrasAvgLengthOfDay)
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

  public Date getAvgStartingTime()
  {
    return avgStartingTime;
  }

  public Date getAvgEndingTime()
  {
    return avgEndingTime;
  }

  public Date getAvgLengthOfDay()
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
  public Date getIgnoreExtrasAvgStartingTime()
  {
    return ignoreExtrasAvgStartingTime;
  }

  public Date getIgnoreExtrasAvgEndingTime()
  {
    return ignoreExtrasAvgEndingTime;
  }

  public Date getIgnoreExtrasAvgLengthOfDay()
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
	  for (CourseSelection cs : courseSelections) {
		  for (Activity a : cs.getActivities()){
			outputString = new String(outputString + a.toString());
		  }
	  }
	  
    return super.toString() + "["+
            "numOfDaysOff" + ":" + getNumOfDaysOff()+ "," +
            "ignoreExtrasNumOfDaysOff" + ":" + getIgnoreExtrasNumOfDaysOff()+ "]" + System.getProperties().getProperty("line.separator") +
            "  " + "avgStartingTime" + "=" + (getAvgStartingTime() != null ? !getAvgStartingTime().equals(this)  ? getAvgStartingTime().toString().replaceAll("  ","    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
            "  " + "avgEndingTime" + "=" + (getAvgEndingTime() != null ? !getAvgEndingTime().equals(this)  ? getAvgEndingTime().toString().replaceAll("  ","    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
            "  " + "avgLengthOfDay" + "=" + (getAvgLengthOfDay() != null ? !getAvgLengthOfDay().equals(this)  ? getAvgLengthOfDay().toString().replaceAll("  ","    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
            "  " + "ignoreExtrasAvgStartingTime" + "=" + (getIgnoreExtrasAvgStartingTime() != null ? !getIgnoreExtrasAvgStartingTime().equals(this)  ? getIgnoreExtrasAvgStartingTime().toString().replaceAll("  ","    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
            "  " + "ignoreExtrasAvgEndingTime" + "=" + (getIgnoreExtrasAvgEndingTime() != null ? !getIgnoreExtrasAvgEndingTime().equals(this)  ? getIgnoreExtrasAvgEndingTime().toString().replaceAll("  ","    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
            "  " + "ignoreExtrasAvgLengthOfDay" + "=" + (getIgnoreExtrasAvgLengthOfDay() != null ? !getIgnoreExtrasAvgLengthOfDay().equals(this)  ? getIgnoreExtrasAvgLengthOfDay().toString().replaceAll("  ","    ") : "this" : "null")
     + outputString;
  }

  
  //------------------------
  // MANUALLY-MADE METHODS
  //------------------------
  
  public void updateStats() {
	  numOfDaysOff = 0;
	  ignoreExtrasNumOfDaysOff = 0;
	  
	  Long totalStartTime = (long) 0;
	  Long totalEndTime= (long) 0;
	  Long totalLengthOfDay= (long) 0;
	  
	  Long ieTotalStartTime = (long) 0;
	  Long ieTotalEndTime= (long) 0;
	  Long ieTotalLengthOfDay= (long) 0;
	  
	  for (int i = 1; i <= 7; i++) {
		  System.out.println("Looking for day " + i);
		  Date earliestStartTime = null;
	  	  Date latestEndTime = null;
	  	  
	  	  //ie: Ignore Extras (DGD, TUT)
	  	  Date ieEarliestStartTime = null;
	  	  Date ieLatestEndTime = null;
	  	  
	  	  boolean dayOff = true;
	  	  boolean ieDayOff = true;
		  for (CourseSelection cs : courseSelections) {
			  for (Activity a : cs.getActivities()) {
				  if (a.getDay()==i) {
					  System.out.println(a.getDay() + " i: " + i);
					  if (dayOff) {
						  //System.out.println("Day of week: " + i);
						  dayOff = false;
						  earliestStartTime = a.getStartTime();
						  latestEndTime = a.getEndTime();
					  } else {
						  if (a.getStartTime().before(earliestStartTime)) {
							  earliestStartTime = a.getStartTime();
						  }
						  if (a.getEndTime().after(latestEndTime)) {
							  latestEndTime = a.getEndTime();
						  }
					  }
					  
					  if (!(a.getType().equals("DGD") || a.getType().equals("TUT"))) {
						  if (ieDayOff) {
							  ieDayOff = false;
							  ieEarliestStartTime = a.getStartTime();
							  ieLatestEndTime = a.getEndTime();
						  } else {
							  if (a.getStartTime().before(ieEarliestStartTime)) {
								  ieEarliestStartTime = a.getStartTime();
							  }
							  if (a.getEndTime().after(latestEndTime)) {
								  ieLatestEndTime = a.getEndTime();
							  }
						  }
					  }	  
				  }
			  }
		  }
		  
		  if (dayOff) {
			  numOfDaysOff++;
			  System.out.println("numOfDaysOff = " + numOfDaysOff);
		  } else {
			  totalStartTime += earliestStartTime.getTime();
			  totalEndTime += latestEndTime.getTime();
			  totalLengthOfDay += (latestEndTime.getTime() - earliestStartTime.getTime());
		  }
		  
		  if (ieDayOff) {
			  ignoreExtrasNumOfDaysOff++;
		  } else {
			  ieTotalStartTime += ieEarliestStartTime.getTime();
			  ieTotalEndTime += ieLatestEndTime.getTime();
			  ieTotalLengthOfDay += (ieLatestEndTime.getTime() - ieEarliestStartTime.getTime());
		  }
	  }
	  if (numOfDaysOff!=7) {
		  avgStartingTime = new Date(totalStartTime / (7-numOfDaysOff));
		  avgEndingTime = new Date(totalEndTime / (7-numOfDaysOff));
		  avgLengthOfDay = new Date(totalLengthOfDay / (7-numOfDaysOff));
		  
		  ignoreExtrasAvgStartingTime = new Date(ieTotalStartTime / (7-ignoreExtrasNumOfDaysOff));
		  ignoreExtrasAvgEndingTime = new Date(ieTotalEndTime / (7-ignoreExtrasNumOfDaysOff));
		  ignoreExtrasAvgLengthOfDay = new Date(ieTotalLengthOfDay / (7-ignoreExtrasNumOfDaysOff));
	  }
  }

  public static List<Schedule> generateSchedules(Course[] selectedCourses, Course[] optionalCourses, int requiredOptional) {
	  //Welcome to the worst sub of your life.
	  
	  //Original schedules: generate your mandatory course schedules. Easy.
	  List<Schedule> originalSchedules = generateSchedules(selectedCourses);
	  
	  //A string list to hold gray code.
	  List<String[]> nChooseK = new ArrayList<String[]>();
	  
	  //Creates the digits that will be used, depending on how many optional courses there are.
	  String[] inputs = new String[optionalCourses.length];
	  for (int j = 0; j < optionalCourses.length; j++) {
		  inputs[j] = Integer.toString(j);
	  }
	  
	  //Run the gray code nChooseK sub.
	  nChooseK = nChooseK(inputs, requiredOptional, 0, new String[requiredOptional], nChooseK);
	  
	  //Final schedules will be returned to the user.
	  List<Schedule> finalSchedules = new ArrayList<Schedule>();
	  
	  //For each string array in nChooseK, meaning for each possible gray code...
	  for (String[] sArray : nChooseK) {
		  //A temporary schedule holder.
		  List<Schedule> tempSchedules = new ArrayList<Schedule>();
		  
		  //A temporary selections holder
		  List<ArrayList<CourseSelection>> selections = new ArrayList<ArrayList<CourseSelection>>();
		  
		  //Of the nChooseK string, we add k courses to selections.
		  for (String s : sArray) {
			  selections.add((ArrayList<CourseSelection>) optionalCourses[Integer.parseInt(s)].getCourseSelections());
		  }
		  tempSchedules = generateSchedules(selections, requiredOptional-1);
		  //Now we must merge tempSchedules with original schedules into final schedules.
		  boolean valid = true;
		  for (Schedule s1 : originalSchedules) { //For every schedule generated previously,
			  for (Schedule s2 : tempSchedules) {
				  for (CourseSelection cs : s2.getCourseSelections()) {
					  if (s1.collidesWith(cs)) {
						  valid=false;
					  }
				  }
				  //We found a valid match. Let's add them to the final list.
				  List<CourseSelection> temp1 = s1.getCourseSelections();
				  List<CourseSelection> temp2 = s2.getCourseSelections();
				  Schedule result = null;
				  for (CourseSelection cs : temp1) {
					  if (result == null) {
						  result=new Schedule(cs);
					  } else {
					  result.addCourseSelection(cs);
					  }
				  }
				  for (CourseSelection cs : temp2) {
					  result.addCourseSelection(cs);
				  }
				  finalSchedules.add(result);
			  }
			  
		  }
	  }
	  
		return finalSchedules;
	}
  
private static List<String[]> nChooseK(String[] arr, int len, int startPosition, String[] result, List<String[]> list) {
		 if (len == 0){
	            list.add(result.clone());
	            return list;
	        }       
	        for (int i = startPosition; i <= arr.length-len; i++){
	            result[result.length - len] = arr[i];
	            nChooseK(arr, len-1, i+1, result, list);
	        }
	        return list;
}

public static List<Schedule> generateSchedules(Course[] selectedCourses) {
	int count = selectedCourses.length;
	List<ArrayList<CourseSelection>> selections = new ArrayList<ArrayList<CourseSelection>>();
	for (int i = 0; i < count; i++) {
		selections.add((ArrayList<CourseSelection>) selectedCourses[i].getCourseSelections());
	}
	List<Schedule> schedules = generateSchedules(selections, count-1);
	for (Schedule s : schedules) {
		s.updateStats(); //Load all stats for sorting.
	}
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
					List<CourseSelection> temp = s.getCourseSelections();
					Schedule result = new Schedule(cs);
					for (int i = 0; i < temp.size(); i++) {
						result.addCourseSelection(temp.get(i));
					}
					nextSchedules.add(result);
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