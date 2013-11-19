package ca.uottawa.schedule;

/*PLEASE DO NOT EDIT THIS CODE*/
/*This code was generated using the UMPLE 1.18.0.3209 modeling language!*/


import java.util.Date;

// line 18 "model.ump"
// line 50 "model.ump"
public class Activity
{
  @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
  public @interface umplesourcefile{int[] line();String[] file();int[] javaline();int[] length();}

  //------------------------
  // MEMBER VARIABLES
  //------------------------

  //Activity Attributes
  private String type;
  private int number;
  private int day;
  private Date startTime;
  private Date endTime;
  private String place;
  private String professor;
  private boolean selected;

  //Activity Associations
  private Section section;

  //------------------------
  // CONSTRUCTOR
  //------------------------

  public Activity(String aType, int aNumber, int aDay, Date aStartTime, Date aEndTime, String aPlace, String aProfessor, boolean aSelected, Section aSection)
  {
    type = aType;
    number = aNumber;
    day = aDay;
    startTime = aStartTime;
    endTime = aEndTime;
    place = aPlace;
    professor = aProfessor;
    selected = aSelected;
    boolean didAddSection = setSection(aSection);
    if (!didAddSection)
    {
      throw new RuntimeException("Unable to create activity due to section");
    }
  }

  //------------------------
  // INTERFACE
  //------------------------

  public boolean setType(String aType)
  {
    boolean wasSet = false;
    type = aType;
    wasSet = true;
    return wasSet;
  }

  public boolean setNumber(int aNumber)
  {
    boolean wasSet = false;
    number = aNumber;
    wasSet = true;
    return wasSet;
  }

  public boolean setDay(int aDay)
  {
    boolean wasSet = false;
    day = aDay;
    wasSet = true;
    return wasSet;
  }

  public boolean setStartTime(Date aStartTime)
  {
    boolean wasSet = false;
    startTime = aStartTime;
    wasSet = true;
    return wasSet;
  }

  public boolean setEndTime(Date aEndTime)
  {
    boolean wasSet = false;
    endTime = aEndTime;
    wasSet = true;
    return wasSet;
  }

  public boolean setPlace(String aPlace)
  {
    boolean wasSet = false;
    place = aPlace;
    wasSet = true;
    return wasSet;
  }

  public boolean setProfessor(String aProfessor)
  {
    boolean wasSet = false;
    professor = aProfessor;
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

  public String getType()
  {
    return type;
  }

  public int getNumber()
  {
    return number;
  }

  public int getDay()
  {
    return day;
  }

  public Date getStartTime()
  {
    return startTime;
  }

  public Date getEndTime()
  {
    return endTime;
  }

  public String getPlace()
  {
    return place;
  }

  public String getProfessor()
  {
    return professor;
  }

  public boolean getSelected()
  {
    return selected;
  }

  public boolean isSelected()
  {
    return selected;
  }

  public Section getSection()
  {
    return section;
  }

  public boolean setSection(Section aSection)
  {
    boolean wasSet = false;
    //Must provide section to activity
    if (aSection == null)
    {
      return wasSet;
    }

    if (section != null && section.numberOfActivities() <= Section.minimumNumberOfActivities())
    {
      return wasSet;
    }

    Section existingSection = section;
    section = aSection;
    if (existingSection != null && !existingSection.equals(aSection))
    {
      boolean didRemove = existingSection.removeActivity(this);
      if (!didRemove)
      {
        section = existingSection;
        return wasSet;
      }
    }
    section.addActivity(this);
    wasSet = true;
    return wasSet;
  }

  public void delete()
  {
    Section placeholderSection = section;
    this.section = null;
    placeholderSection.removeActivity(this);
  }


  public String toString()
  {
	  String outputString = "";
    return super.toString() + "["+
            "type" + ":" + getType()+ "," +
            "number" + ":" + getNumber()+ "," +
            "day" + ":" + getDay()+ "," +
            "place" + ":" + getPlace()+ "," +
            "professor" + ":" + getProfessor()+ "," +
            "selected" + ":" + getSelected()+ "]" + System.getProperties().getProperty("line.separator") +
            "  " + "startTime" + "=" + (getStartTime() != null ? !getStartTime().equals(this)  ? getStartTime().toString().replaceAll("  ","    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
            "  " + "endTime" + "=" + (getEndTime() != null ? !getEndTime().equals(this)  ? getEndTime().toString().replaceAll("  ","    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
            "  " + "section = "+(getSection()!=null?Integer.toHexString(System.identityHashCode(getSection())):"null")
     + outputString;
  }
  
  //------------------------
  // MANUALLY-MADE METHODS
  //------------------------
  
  public boolean overlaps(Activity e) {
	return (getDay() == e.getDay())&&(getStartTime().compareTo(e.getEndTime()) < 0) && (e.getStartTime().compareTo(getEndTime()) < 0); 
  }
  
  
}