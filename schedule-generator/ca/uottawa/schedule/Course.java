package ca.uottawa.schedule;

/*PLEASE DO NOT EDIT THIS CODE*/
/*This code was generated using the UMPLE 1.18.0.3209 modeling language!*/


import java.io.Serializable;
import java.util.*;

// line 1 "model.ump"
public class Course implements Serializable
{

  @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
  public @interface umplesourcefile{int[] line();String[] file();int[] javaline();int[] length();}

  //------------------------
  // MEMBER VARIABLES
  //------------------------

  //Course Attributes
  private String description;

  //Course Associations
  private List<Section> sections;

  private static final long serialVersionUID = 1L;

  //------------------------
  // CONSTRUCTOR
  //------------------------

  public Course(String aDescription)
  {
    description = aDescription;
    sections = new ArrayList<Section>();
  }

  //------------------------
  // INTERFACE
  //------------------------

  public boolean setDescription(String aDescription)
  {
    boolean wasSet = false;
    description = aDescription;
    wasSet = true;
    return wasSet;
  }

  public String getDescription()
  {
    return description;
  }

  public Section getSection(int index)
  {
    Section aSection = sections.get(index);
    return aSection;
  }

  public List<Section> getSections()
  {
    List<Section> newSections = Collections.unmodifiableList(sections);
    return newSections;
  }

  public int numberOfSections()
  {
    int number = sections.size();
    return number;
  }

  public boolean hasSections()
  {
    boolean has = sections.size() > 0;
    return has;
  }

  public int indexOfSection(Section aSection)
  {
    int index = sections.indexOf(aSection);
    return index;
  }

  public boolean isNumberOfSectionsValid()
  {
    boolean isValid = numberOfSections() >= minimumNumberOfSections();
    return isValid;
  }

  public static int minimumNumberOfSections()
  {
    return 1;
  }

  public Section addSection(String aName, String aSemester, int aRequiredDGD, int aRequiredTUT, int aRequiredLAB, boolean aSelected)
  {
    Section aNewSection = new Section(aName, aSemester, aRequiredDGD, aRequiredTUT, aRequiredLAB, aSelected, this);
    return aNewSection;
  }

  public boolean addSection(Section aSection)
  {
    boolean wasAdded = false;
    if (sections.contains(aSection)) { return false; }
    Course existingCourse = aSection.getCourse();
    boolean isNewCourse = existingCourse != null && !this.equals(existingCourse);

    if (isNewCourse && existingCourse.numberOfSections() <= minimumNumberOfSections())
    {
      return wasAdded;
    }

    if (isNewCourse)
    {
      aSection.setCourse(this);
    }
    else
    {
      sections.add(aSection);
    }
    wasAdded = true;
    return wasAdded;
  }

  public boolean removeSection(Section aSection)
  {
    boolean wasRemoved = false;
    //Unable to remove aSection, as it must always have a course
    if (this.equals(aSection.getCourse()))
    {
      return wasRemoved;
    }

    //course already at minimum (1)
    if (numberOfSections() <= minimumNumberOfSections())
    {
      return wasRemoved;
    }

    sections.remove(aSection);
    wasRemoved = true;
    return wasRemoved;
  }

  public boolean addSectionAt(Section aSection, int index)
  {  
    boolean wasAdded = false;
    if(addSection(aSection))
    {
      if(index < 0 ) { index = 0; }
      if(index > numberOfSections()) { index = numberOfSections() - 1; }
      sections.remove(aSection);
      sections.add(index, aSection);
      wasAdded = true;
    }
    return wasAdded;
  }

  public boolean addOrMoveSectionAt(Section aSection, int index)
  {
    boolean wasAdded = false;
    if(sections.contains(aSection))
    {
      if(index < 0 ) { index = 0; }
      if(index > numberOfSections()) { index = numberOfSections() - 1; }
      sections.remove(aSection);
      sections.add(index, aSection);
      wasAdded = true;
    } 
    else 
    {
      wasAdded = addSectionAt(aSection, index);
    }
    return wasAdded;
  }

  public void delete()
  {
    for(int i=sections.size(); i > 0; i--)
    {
      Section aSection = sections.get(i - 1);
      aSection.delete();
    }
  }


  public String toString()
  {
	  String outputString = description + System.getProperty("line.separator");
	  for (Section s : sections) {
		  outputString = new String(outputString + s);
	  }
    return outputString;
  }

    public List<String> getSemesters() {
        List<String> semesters = new ArrayList<String>();
        semesters.add(sections.get(0).getSemester());
        int sectionSize = sections.size();
        for (int i = 1; i < sectionSize; i++) {
            String sem = sections.get(i).getSemester();
            boolean exists = false;
            for (String uniqueSemester : semesters) {
                if (sem.equals(uniqueSemester)) {
                    exists = true;
                }
            }
            if (!exists) {
                semesters.add(sem);
            }
        }
        return semesters;
    }
  
  
  //------------------------
  // MANUALLY-MADE METHODS
  //------------------------
  
  public List<CourseSelection> getCourseSelections() {
	  List<CourseSelection> selections = new ArrayList<CourseSelection>();
	  //Basicaly, we need to generate as many possible course selections for this course.
	  //Each section and activity have a boolean selected which will help in this process.
	  for (Section s : sections) {
		  if (s.isSelected()) {
			  List<CourseSelection> sectionSelections = s.getCourseSelections();
			  selections.addAll(sectionSelections);
		  }
	  }
	  return selections;
  }
  
}