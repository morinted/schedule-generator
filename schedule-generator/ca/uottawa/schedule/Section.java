package ca.uottawa.schedule;

/*PLEASE DO NOT EDIT THIS CODE*/
/*This code was generated using the UMPLE 1.18.0.3209 modeling language!*/


import java.util.*;
import java.sql.Date;

// line 7 "model.ump"
// line 55 "model.ump"
public class Section
{
  @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
  public @interface umplesourcefile{int[] line();String[] file();int[] javaline();int[] length();}

  //------------------------
  // MEMBER VARIABLES
  //------------------------

  //Section Attributes
  private String name;
  private String semester;
  private int requiredDGD;
  private int requiredTUT;
  private int requiredLAB;
  private boolean selected;

  //Section Associations
  private List<Activity> activities;
  private Course course;

  //------------------------
  // CONSTRUCTOR
  //------------------------

  public Section(String aName, String aSemester, int aRequiredDGD, int aRequiredTUT, int aRequiredLAB, boolean aSelected, Course aCourse)
  {
    name = aName;
    semester = aSemester;
    requiredDGD = aRequiredDGD;
    requiredTUT = aRequiredTUT;
    requiredLAB = aRequiredLAB;
    selected = aSelected;
    activities = new ArrayList<Activity>();
    boolean didAddCourse = setCourse(aCourse);
    if (!didAddCourse)
    {
      throw new RuntimeException("Unable to create section due to course");
    }
  }

  //------------------------
  // INTERFACE
  //------------------------

  public boolean setName(String aName)
  {
    boolean wasSet = false;
    name = aName;
    wasSet = true;
    return wasSet;
  }

  public boolean setSemester(String aSemester)
  {
    boolean wasSet = false;
    semester = aSemester;
    wasSet = true;
    return wasSet;
  }

  public boolean setRequiredDGD(int aRequiredDGD)
  {
    boolean wasSet = false;
    requiredDGD = aRequiredDGD;
    wasSet = true;
    return wasSet;
  }

  public boolean setRequiredTUT(int aRequiredTUT)
  {
    boolean wasSet = false;
    requiredTUT = aRequiredTUT;
    wasSet = true;
    return wasSet;
  }

  public boolean setRequiredLAB(int aRequiredLAB)
  {
    boolean wasSet = false;
    requiredLAB = aRequiredLAB;
    wasSet = true;
    return wasSet;
  }

  public boolean setSelected(boolean aSelected)
  {
    boolean wasSet = false;
    selected = aSelected;
    wasSet = true;
    return wasSet;
  }

  public String getName()
  {
    return name;
  }

  public String getSemester()
  {
    return semester;
  }

  public int getRequiredDGD()
  {
    return requiredDGD;
  }

  public int getRequiredTUT()
  {
    return requiredTUT;
  }

  public int getRequiredLAB()
  {
    return requiredLAB;
  }

  public boolean getSelected()
  {
    return selected;
  }

  public boolean isSelected()
  {
    return selected;
  }

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

  public Course getCourse()
  {
    return course;
  }

  public boolean isNumberOfActivitiesValid()
  {
    boolean isValid = numberOfActivities() >= minimumNumberOfActivities();
    return isValid;
  }

  public static int minimumNumberOfActivities()
  {
    return 1;
  }

  public Activity addActivity(String aType, int aNumber, int aDay, Date aStartTime, Date aEndTime, String aPlace, String aProfessor, boolean aSelected)
  {
    Activity aNewActivity = new Activity(aType, aNumber, aDay, aStartTime, aEndTime, aPlace, aProfessor, aSelected, this);
    return aNewActivity;
  }

  public boolean addActivity(Activity aActivity)
  {
    boolean wasAdded = false;
    if (activities.contains(aActivity)) { return false; }
    Section existingSection = aActivity.getSection();
    boolean isNewSection = existingSection != null && !this.equals(existingSection);

    if (isNewSection && existingSection.numberOfActivities() <= minimumNumberOfActivities())
    {
      return wasAdded;
    }

    if (isNewSection)
    {
      aActivity.setSection(this);
    }
    else
    {
      activities.add(aActivity);
    }
    wasAdded = true;
    return wasAdded;
  }

  public boolean removeActivity(Activity aActivity)
  {
    boolean wasRemoved = false;
    //Unable to remove aActivity, as it must always have a section
    if (this.equals(aActivity.getSection()))
    {
      return wasRemoved;
    }

    //section already at minimum (1)
    if (numberOfActivities() <= minimumNumberOfActivities())
    {
      return wasRemoved;
    }

    activities.remove(aActivity);
    wasRemoved = true;
    return wasRemoved;
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

  public boolean setCourse(Course aCourse)
  {
    boolean wasSet = false;
    //Must provide course to section
    if (aCourse == null)
    {
      return wasSet;
    }

    if (course != null && course.numberOfSections() <= Course.minimumNumberOfSections())
    {
      return wasSet;
    }

    Course existingCourse = course;
    course = aCourse;
    if (existingCourse != null && !existingCourse.equals(aCourse))
    {
      boolean didRemove = existingCourse.removeSection(this);
      if (!didRemove)
      {
        course = existingCourse;
        return wasSet;
      }
    }
    course.addSection(this);
    wasSet = true;
    return wasSet;
  }

  public void delete()
  {
    for(int i=activities.size(); i > 0; i--)
    {
      Activity aActivity = activities.get(i - 1);
      aActivity.delete();
    }
    Course placeholderCourse = course;
    this.course = null;
    placeholderCourse.removeSection(this);
  }


  public String toString()
  {
	  String outputString = "";
    return super.toString() + "["+
            "name" + ":" + getName()+ "," +
            "semester" + ":" + getSemester()+ "," +
            "requiredDGD" + ":" + getRequiredDGD()+ "," +
            "requiredTUT" + ":" + getRequiredTUT()+ "," +
            "requiredLAB" + ":" + getRequiredLAB()+ "," +
            "selected" + ":" + getSelected()+ "]" + System.getProperties().getProperty("line.separator") +
            "  " + "course = "+(getCourse()!=null?Integer.toHexString(System.identityHashCode(getCourse())):"null")
     + outputString;
  }

  //------------------------
  // MANUALLY-MADE METHODS
  //------------------------
  
public List<CourseSelection> getCourseSelections() {
	List<CourseSelection> selections = new ArrayList<CourseSelection>();
	  //Basicaly, we need to generate as many possible course selections for this section.
	  //Each activity has a boolean 'selected' which will help in this process.
	
	//Start by looking at the requirements (no optionals like DGDs, TUTs, and LABs sometimes are)
	boolean skipDGDs = requiredDGD > 0 ? true : false;
	boolean skipTUTs = requiredTUT > 0 ? true : false;
	boolean skipLABs = requiredLAB > 0 ? true : false;
	
	List<Activity> DGDs = new ArrayList<Activity>();
	List<Activity> TUTs = new ArrayList<Activity>();
	List<Activity> LABs = new ArrayList<Activity>();
	
	List<Activity> requiredActivities = new ArrayList<Activity>();
	for (Activity a : activities) {
		if (a.isSelected()) {
			String type = a.getType();
			if (!((skipDGDs && type.equals("DGD")) || (skipTUTs && type.equals("TUT")) || (skipLABs && type.equals("LAB")))) {
				requiredActivities.add(a);
			} else if (skipDGDs && type.equals("DGD")) {
				DGDs.add(a);
			} else if (skipTUTs && type.equals("TUT")) {
				TUTs.add(a);
			} else if (skipLABs && type.equals("LAB")) {
				LABs.add(a);
			}
		}
	}
	//All required activities have been added to the list. Now, if there are any optionals, we need to create some course selections.
	
	//We will convert the list to an array for operations done later.
	int rASize = requiredActivities.size();
	Activity[] reqActArray = new Activity[rASize];
	for (int i = 0; i < rASize; i++) {
		reqActArray[i] = requiredActivities.get(i);
	}
	
	
	if (!(skipDGDs || skipTUTs || skipLABs)) {
		selections.add(new CourseSelection(reqActArray));
	} else {
		//Okay, so I'm a little confused as to how to do this. I'd like to have a nested for statement for each possible combination of the
		//above (DGD, TUT, and LAB), but I can't figure out how to make it adaptable. That means that I'm going to need to make 8 for state-
		//ments. Kind of inefficient, but it's the only way I can think of solving the problem.
		
		//Currently, I've never seen a class that requires _TWO_ DGDs, TUTs, or LABs! Therefore, I'll only code for one.
		List<Activity> secondaryActivities;
		if (skipDGDs && skipTUTs && skipLABs) {
			for (Activity d : DGDs) {
				for (Activity t : TUTs) {
					//Check if TUT and DGD overlap
					if (!t.overlaps(d)) {
						//No overlap! Just one more!
						for (Activity l : LABs) {
							if (!t.overlaps(l) && !l.overlaps(d)) {
									//We have check D against T, T against L, and L against D. Since NONE of them overlap, we almost have a selection.
								
									//Now we check to see if d, t, or l interfere with any of the MANDATORY courses for this section.
									//Don't believe that can happen? Think again. Go check out BCH2333. They share DGDs and LABs across 2 sections.
								    //It's dumb, but it happens.
									//It's cool. With the completed collision-detection, the system generates 376 CourseSelections for BCH2333. Ignoring
									//collisions, it's 432. Ignoring collisions between DGDs, LABs, and mandatory courses makes it around 330. Goes to show you.
									
									boolean valid = true;
									for (Activity a : reqActArray) {
										if (a.overlaps(l) || a.overlaps(t) || a.overlaps(d)) {
											valid = false;
										}
									}
									if (valid) {
										if (reqActArray.length > 0) {
											CourseSelection result = new CourseSelection(reqActArray);
										    result.addActivity(t);
										    result.addActivity(l);
										    result.addActivity(d);
											selections.add(result);
										} else {
											//Sometimes a course has no mandatory classes. Take MUS2943 - only 1 DGD required. That's it.
											selections.add(new CourseSelection(new Activity[]{t, l, d}));
										}
									
									}
									//And now we have a good result, pretty much guaranteed.
									//The only exception is if mandatory courses overlap, but that's the school's stupid problem.
							}
						}
					}
				}
			}
		} else if (skipDGDs && skipTUTs) {
			for (Activity d : DGDs) {
				for (Activity t : TUTs) {
					//Check if TUT and DGD overlap
					if (!t.overlaps(d)) {
						//No overlap!
						boolean valid = true;
						for (Activity a : reqActArray) {
							if (a.overlaps(t) || a.overlaps(d)) {
								valid = false;
							}
						}
						if (valid) {
							if (reqActArray.length > 0) {
								CourseSelection result = new CourseSelection(reqActArray);
							    result.addActivity(t);
							    result.addActivity(d);
								selections.add(result);
							} else {
								selections.add(new CourseSelection(new Activity[]{t, d}));
							}
						}
					}
						
				}
			}
		} else if (skipTUTs && skipLABs) {
			for (Activity l : LABs) {
				for (Activity t : TUTs) {
					//Check if TUT and LAB overlap
					if (!t.overlaps(l)) {
						//No overlap!
						boolean valid = true;
						for (Activity a : reqActArray) {
							if (a.overlaps(l) || a.overlaps(t)) {
								valid = false;
							}
						}
						if (valid) {
							if (reqActArray.length > 0) {
								CourseSelection result = new CourseSelection(reqActArray);
							    result.addActivity(t);
							    result.addActivity(l);
								selections.add(result);
							} else {
								selections.add(new CourseSelection(new Activity[]{t, l}));
							}
							
									
						}
					}
				}
			}
		} else if (skipDGDs && skipLABs) {
			for (Activity d : DGDs) {
				for (Activity l : LABs) {
					//Check if LAB and DGD overlap
					if (!l.overlaps(d)) {
						//No overlap!
						boolean valid = true;
						for (Activity a : reqActArray) {
							if (a.overlaps(l) || a.overlaps(d)) {
								valid = false;
							}
						}
						if (valid) {
							if (reqActArray.length > 0) {
								CourseSelection result = new CourseSelection(reqActArray);
							    result.addActivity(l);
							    result.addActivity(d);
								selections.add(result);
							} else {
								selections.add(new CourseSelection(new Activity[]{d, l}));
							}
							
									
						}
					}
				}
			}
		} else if (skipDGDs) {
			for (Activity d : DGDs) {
						//No overlap!
				boolean valid = true;
				for (Activity a : reqActArray) {
					if (a.overlaps(d)) {
						valid = false;
					}
				}
				if (valid) {
					if (reqActArray.length > 0) {
							CourseSelection result = new CourseSelection(reqActArray);
						    result.addActivity(d);
							selections.add(result);
					} else {
						selections.add(new CourseSelection(new Activity[]{d}));
					}
					
									
				}
			}
		} else if (skipTUTs) {
			for (Activity t : TUTs) {
				//No overlap!
				boolean valid = true;
				for (Activity a : reqActArray) {
					if (a.overlaps(t)) {
						valid = false;
					}
				}
				if (valid) {
					if (reqActArray.length > 0) {
						CourseSelection result = new CourseSelection(reqActArray);
					    result.addActivity(t);
						selections.add(result);
				} else {
					selections.add(new CourseSelection(new Activity[]{t}));
				}
				}
			}
		} else if (skipLABs) {
			for (Activity l : LABs) {
				//No overlap!
				boolean valid = true;
				for (Activity a : reqActArray) {
					if (a.overlaps(l)) {
						System.out.println(a + " " + l);
						valid = false;
					}
				}
				if (valid) {
					if (reqActArray.length > 0) {
						CourseSelection result = new CourseSelection(reqActArray);
					    result.addActivity(l);
						selections.add(result);
				} else {
					selections.add(new CourseSelection(new Activity[]{l}));
				}
				}
			}
		}
	}
	  return selections;
}
}