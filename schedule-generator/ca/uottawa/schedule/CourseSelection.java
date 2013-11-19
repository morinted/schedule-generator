package ca.uottawa.schedule;

/*PLEASE DO NOT EDIT THIS CODE*/
/*This code was generated using the UMPLE 1.18.0.3209 modeling language!*/


import java.util.*;

// line 30 "model.ump"
// line 60 "model.ump"
public class CourseSelection
{
  @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
  public @interface umplesourcefile{int[] line();String[] file();int[] javaline();int[] length();}

  //------------------------
  // MEMBER VARIABLES
  //------------------------

  //CourseSelection Associations
  private List<Activity> activities;

  //------------------------
  // CONSTRUCTOR
  //------------------------

  public CourseSelection(Activity... allActivities)
  {
    activities = new ArrayList<Activity>();
    boolean didAddActivities = setActivities(allActivities);
    if (!didAddActivities)
    {
      throw new RuntimeException("Unable to create CourseSelection, must have at least 1 activities");
    }
  }

  //------------------------
  // INTERFACE
  //------------------------

  public Activity getActivity(int index)
  {
    Activity aActivity = activities.get(index);
    return aActivity;
  }

  public List<Activity> getActivities()
  {
    List<Activity> newActivities = Collections.unmodifiableList(activities);
    return newActivities;
  }

  public int numberOfActivities()
  {
    int number = activities.size();
    return number;
  }

  public boolean hasActivities()
  {
    boolean has = activities.size() > 0;
    return has;
  }

  public int indexOfActivity(Activity aActivity)
  {
    int index = activities.indexOf(aActivity);
    return index;
  }

  public static int minimumNumberOfActivities()
  {
    return 1;
  }

  public boolean addActivity(Activity aActivity)
  {
    boolean wasAdded = false;
    if (activities.contains(aActivity)) { return false; }
    activities.add(aActivity);
    wasAdded = true;
    return wasAdded;
  }

  public boolean removeActivity(Activity aActivity)
  {
    boolean wasRemoved = false;
    if (!activities.contains(aActivity))
    {
      return wasRemoved;
    }

    if (numberOfActivities() <= minimumNumberOfActivities())
    {
      return wasRemoved;
    }

    activities.remove(aActivity);
    wasRemoved = true;
    return wasRemoved;
  }

  public boolean setActivities(Activity... newActivities)
  {
    boolean wasSet = false;
    ArrayList<Activity> verifiedActivities = new ArrayList<Activity>();
    for (Activity aActivity : newActivities)
    {
      if (verifiedActivities.contains(aActivity))
      {
        continue;
      }
      verifiedActivities.add(aActivity);
    }

    if (verifiedActivities.size() != newActivities.length || verifiedActivities.size() < minimumNumberOfActivities())
    {
      return wasSet;
    }

    activities.clear();
    activities.addAll(verifiedActivities);
    wasSet = true;
    return wasSet;
  }

  public boolean addActivityAt(Activity aActivity, int index)
  {  
    boolean wasAdded = false;
    if(addActivity(aActivity))
    {
      if(index < 0 ) { index = 0; }
      if(index > numberOfActivities()) { index = numberOfActivities() - 1; }
      activities.remove(aActivity);
      activities.add(index, aActivity);
      wasAdded = true;
    }
    return wasAdded;
  }

  public boolean addOrMoveActivityAt(Activity aActivity, int index)
  {
    boolean wasAdded = false;
    if(activities.contains(aActivity))
    {
      if(index < 0 ) { index = 0; }
      if(index > numberOfActivities()) { index = numberOfActivities() - 1; }
      activities.remove(aActivity);
      activities.add(index, aActivity);
      wasAdded = true;
    } 
    else 
    {
      wasAdded = addActivityAt(aActivity, index);
    }
    return wasAdded;
  }

  public void delete()
  {
    activities.clear();
  }
  
  //------------------------
  // MANUALLY-MADE METHODS
  //------------------------
  
  public boolean overlaps(CourseSelection e) {
	boolean overlaps = false;
	for (Activity a : getActivities()) {
		for (Activity b : e.getActivities()) {
			if (a.overlaps(b)) {
				overlaps = true;
			}
		}
	}
	  
	  return overlaps;
  }

}