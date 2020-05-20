package ca.uottawa.schedule;

/*PLEASE DO NOT EDIT THIS CODE*/
/*This code was generated using the UMPLE 1.18.0.3214 modeling language!*/


import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

// line 35 "model.ump"
public class Schedule implements Serializable
{
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    public @interface umplesourcefile {
        int[] line();

        String[] file();

        int[] javaline();

        int[] length();
    }

    //------------------------
    // MEMBER VARIABLES
    //------------------------
    private static final long serialVersionUID = 1L;


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

    public Schedule(CourseSelection... allCourseSelections) {
        courseSelections = new ArrayList<CourseSelection>();
        boolean didAddCourseSelections = setCourseSelections(allCourseSelections);
        if (!didAddCourseSelections) {
            throw new RuntimeException("Unable to create Schedule, must have at least 1 courseSelections");
        }
        updateStats();
    }

    //------------------------
    // INTERFACE
    //------------------------

    public boolean setAvgStartingTime(Date aAvgStartingTime) {
        boolean wasSet = false;
        avgStartingTime = aAvgStartingTime;
        wasSet = true;
        return wasSet;
    }

    public boolean setAvgEndingTime(Date aAvgEndingTime) {
        boolean wasSet = false;
        avgEndingTime = aAvgEndingTime;
        wasSet = true;
        return wasSet;
    }

    public boolean setAvgLengthOfDay(Date aAvgLengthOfDay) {
        boolean wasSet = false;
        avgLengthOfDay = aAvgLengthOfDay;
        wasSet = true;
        return wasSet;
    }

    public boolean setNumOfDaysOff(int aNumOfDaysOff) {
        boolean wasSet = false;
        numOfDaysOff = aNumOfDaysOff;
        wasSet = true;
        return wasSet;
    }

    public boolean setIgnoreExtrasAvgStartingTime(Date aIgnoreExtrasAvgStartingTime) {
        boolean wasSet = false;
        ignoreExtrasAvgStartingTime = aIgnoreExtrasAvgStartingTime;
        wasSet = true;
        return wasSet;
    }

    public boolean setIgnoreExtrasAvgEndingTime(Date aIgnoreExtrasAvgEndingTime) {
        boolean wasSet = false;
        ignoreExtrasAvgEndingTime = aIgnoreExtrasAvgEndingTime;
        wasSet = true;
        return wasSet;
    }

    public boolean setIgnoreExtrasAvgLengthOfDay(Date aIgnoreExtrasAvgLengthOfDay) {
        boolean wasSet = false;
        ignoreExtrasAvgLengthOfDay = aIgnoreExtrasAvgLengthOfDay;
        wasSet = true;
        return wasSet;
    }

    public boolean setIgnoreExtrasNumOfDaysOff(int aIgnoreExtrasNumOfDaysOff) {
        boolean wasSet = false;
        ignoreExtrasNumOfDaysOff = aIgnoreExtrasNumOfDaysOff;
        wasSet = true;
        return wasSet;
    }

    public Date getAvgStartingTime() {
        return avgStartingTime;
    }

    public Date getAvgEndingTime() {
        return avgEndingTime;
    }

    public Date getAvgLengthOfDay() {
        return avgLengthOfDay;
    }

    public int getNumOfDaysOff() {
        return numOfDaysOff;
    }

    /**
     * Used when ignoring DGDs / TUTs in sorting order.
     */
    public Date getIgnoreExtrasAvgStartingTime() {
        return ignoreExtrasAvgStartingTime;
    }

    public Date getIgnoreExtrasAvgEndingTime() {
        return ignoreExtrasAvgEndingTime;
    }

    public Date getIgnoreExtrasAvgLengthOfDay() {
        return ignoreExtrasAvgLengthOfDay;
    }

    public int getIgnoreExtrasNumOfDaysOff() {
        return ignoreExtrasNumOfDaysOff;
    }

    public CourseSelection getCourseSelection(int index) {
        CourseSelection aCourseSelection = courseSelections.get(index);
        return aCourseSelection;
    }

    public List<CourseSelection> getCourseSelections() {
        List<CourseSelection> newCourseSelections = Collections.unmodifiableList(courseSelections);
        return newCourseSelections;
    }

    public int numberOfCourseSelections() {
        int number = courseSelections.size();
        return number;
    }

    public boolean hasCourseSelections() {
        boolean has = courseSelections.size() > 0;
        return has;
    }

    public int indexOfCourseSelection(CourseSelection aCourseSelection) {
        int index = courseSelections.indexOf(aCourseSelection);
        return index;
    }

    public static int minimumNumberOfCourseSelections() {
        return 1;
    }

    public boolean addCourseSelection(CourseSelection aCourseSelection) {
        boolean wasAdded = false;
        if (courseSelections.contains(aCourseSelection)) {
            return false;
        }
        courseSelections.add(aCourseSelection);
        wasAdded = true;
        return wasAdded;
    }

    public boolean removeCourseSelection(CourseSelection aCourseSelection) {
        boolean wasRemoved = false;
        if (!courseSelections.contains(aCourseSelection)) {
            return wasRemoved;
        }

        if (numberOfCourseSelections() <= minimumNumberOfCourseSelections()) {
            return wasRemoved;
        }

        courseSelections.remove(aCourseSelection);
        wasRemoved = true;
        return wasRemoved;
    }

    public boolean setCourseSelections(CourseSelection... newCourseSelections) {
        boolean wasSet = false;
        ArrayList<CourseSelection> verifiedCourseSelections = new ArrayList<CourseSelection>();
        for (CourseSelection aCourseSelection : newCourseSelections) {
            if (verifiedCourseSelections.contains(aCourseSelection)) {
                continue;
            }
            verifiedCourseSelections.add(aCourseSelection);
        }

        if (verifiedCourseSelections.size() != newCourseSelections.length || verifiedCourseSelections.size() < minimumNumberOfCourseSelections()) {
            return wasSet;
        }

        courseSelections.clear();
        courseSelections.addAll(verifiedCourseSelections);
        wasSet = true;
        return wasSet;
    }

    public boolean addCourseSelectionAt(CourseSelection aCourseSelection, int index) {
        boolean wasAdded = false;
        if (addCourseSelection(aCourseSelection)) {
            if (index < 0) {
                index = 0;
            }
            if (index > numberOfCourseSelections()) {
                index = numberOfCourseSelections() - 1;
            }
            courseSelections.remove(aCourseSelection);
            courseSelections.add(index, aCourseSelection);
            wasAdded = true;
        }
        return wasAdded;
    }

    public boolean addOrMoveCourseSelectionAt(CourseSelection aCourseSelection, int index) {
        boolean wasAdded = false;
        if (courseSelections.contains(aCourseSelection)) {
            if (index < 0) {
                index = 0;
            }
            if (index > numberOfCourseSelections()) {
                index = numberOfCourseSelections() - 1;
            }
            courseSelections.remove(aCourseSelection);
            courseSelections.add(index, aCourseSelection);
            wasAdded = true;
        } else {
            wasAdded = addCourseSelectionAt(aCourseSelection, index);
        }
        return wasAdded;
    }

    public void delete() {
        courseSelections.clear();
    }


    public String toString() {
    	String plural = courseSelections.size() > 1 ? "es" : "";
        String outputString = ("Schedule: " + courseSelections.size() + " class" + plural + ".");
        SimpleDateFormat date_format = new SimpleDateFormat("HH:mm");
        outputString = new String(outputString + System.getProperty("line.separator") + "Statistics:\t\tAvg Start: " + date_format.format(avgStartingTime) + ", Avg End: " + date_format.format(avgEndingTime)) +
                ", Avg Length: " + (avgLengthOfDay.getTime() / 1000 / 60 / 60) + " hours, Days off: " + numOfDaysOff;

        outputString = new String(outputString + System.getProperty("line.separator") + "Ignoring DGDs/TUTs:\tAvg Start: " + date_format.format(ignoreExtrasAvgStartingTime) + ", Avg End: " + date_format.format(ignoreExtrasAvgEndingTime)) +
                ", Avg Length: " + (ignoreExtrasAvgLengthOfDay.getTime() / 1000 / 60 / 60) + " hours, Days off: " + ignoreExtrasNumOfDaysOff + System.getProperty("line.separator");


        for (CourseSelection cs : courseSelections) {
            outputString = new String(outputString + cs.getActivity(0).getSection().getName() + System.getProperty("line.separator"));
            for (Activity a : cs.getActivities()) {
                outputString = new String(outputString + a.toString());
            }
        }


        return outputString;
    }


    //------------------------
    // MANUALLY-MADE METHODS
    //------------------------


    public static List<Schedule> sort(String sortOrder, List<Schedule> unsorted, boolean ignoreExtras) {
        int n = unsorted.size();

        Schedule[] anArray = new Schedule[n];

        for (int i = 0; i < n; i++) {
            anArray[i] = unsorted.get(i);
        }

        List<Schedule> result = new ArrayList<Schedule>();
        Schedule[] arrResult;
        arrResult = sort(sortOrder, anArray, ignoreExtras);
        for (int i = 0; i < n; i++) {
            result.add(arrResult[i]);
            SimpleDateFormat df = new SimpleDateFormat("HH:mm");
        }
        return result;
    }

    private static Schedule[] sort(String sortOrder, Schedule[] unsorted, boolean ignoreExtras) {
        int n = unsorted.length;
        int m = n / 2;

        if (n == 1) {
            return unsorted;
        } else {

            Schedule[] a = sort(sortOrder, Arrays.copyOfRange(unsorted, 0, m), ignoreExtras);
            Schedule[] b = sort(sortOrder, Arrays.copyOfRange(unsorted, m, n), ignoreExtras);


            int i = 0;
            int j = 0;
            int k = 0;

            Schedule[] c = new Schedule[n];

            while ((j < a.length) && (i < b.length)) {
                Date aValue;
                Date bValue;
                switch (sortOrder) {

                    case "earliestStart":
                        if (ignoreExtras) {
                            aValue = a[j].getIgnoreExtrasAvgStartingTime();
                            bValue = b[i].getIgnoreExtrasAvgStartingTime();
                        } else {
                            aValue = a[j].getAvgStartingTime();
                            bValue = b[i].getAvgStartingTime();
                        }
                        break;
                    case "latestStart":
                        if (ignoreExtras) {
                            bValue = a[j].getIgnoreExtrasAvgStartingTime();
                            aValue = b[i].getIgnoreExtrasAvgStartingTime();
                        } else {
                            bValue = a[j].getAvgStartingTime();
                            aValue = b[i].getAvgStartingTime();
                        }
                        break;
                    case "earliestEnd":
                        if (ignoreExtras) {
                            aValue = a[j].getIgnoreExtrasAvgEndingTime();
                            bValue = b[i].getIgnoreExtrasAvgEndingTime();
                        } else {
                            aValue = a[j].getAvgEndingTime();
                            bValue = b[i].getAvgEndingTime();
                        }
                        break;
                    case "latestEnd":
                        if (ignoreExtras) {
                            bValue = a[j].getIgnoreExtrasAvgEndingTime();
                            aValue = b[i].getIgnoreExtrasAvgEndingTime();
                        } else {
                            bValue = a[j].getAvgEndingTime();
                            aValue = b[i].getAvgEndingTime();
                        }
                        break;
                    case "shortestDay":
                        if (ignoreExtras) {
                            aValue = a[j].getIgnoreExtrasAvgLengthOfDay();
                            bValue = b[i].getIgnoreExtrasAvgLengthOfDay();
                        } else {
                            aValue = a[j].getAvgLengthOfDay();
                            bValue = b[i].getAvgLengthOfDay();
                        }
                        break;
                    case "longestDay":
                        if (ignoreExtras) {
                            bValue = a[j].getIgnoreExtrasAvgLengthOfDay();
                            aValue = b[i].getIgnoreExtrasAvgLengthOfDay();
                        } else {
                            bValue = a[j].getAvgLengthOfDay();
                            aValue = b[i].getAvgLengthOfDay();
                        }
                        break;
                    case "days":
                    case "daysOff":
                    	aValue=null;
                        bValue=null;
                    	break;
                    default:
                        System.err.println("Invalid sortOrder");
                        return(null);
                }

                if (sortOrder.equals("days")) {
                    if (ignoreExtras) {
                        c[k++] = a[j].getNumOfDaysOff() < b[i].getIgnoreExtrasNumOfDaysOff() ? a[j++] : b[i++];
                    } else {
                        c[k++] = a[j].getNumOfDaysOff() < b[i].getNumOfDaysOff() ? a[j++] : b[i++];
                    }
                } else if (sortOrder.equals("daysOff")) {
                    if (ignoreExtras) {
                        c[k++] = a[j].getNumOfDaysOff() > b[i].getIgnoreExtrasNumOfDaysOff() ? a[j++] : b[i++];
                    } else {
                        c[k++] = a[j].getNumOfDaysOff() > b[i].getNumOfDaysOff() ? a[j++] : b[i++];
                    }
                } else {
                    int compare = aValue.compareTo(bValue);
                    if (compare < 0) {
                        c[k++] = a[j++];
                    } else if (compare > 0) {
                        c[k++] = b[i++];
                    } else {
                        if (ignoreExtras) {
                            c[k++] = a[j].getNumOfDaysOff() > b[i].getIgnoreExtrasNumOfDaysOff() ? a[j++] : b[i++];
                        } else {
                            c[k++] = a[j].getNumOfDaysOff() > b[i].getNumOfDaysOff() ? a[j++] : b[i++];
                        }

                    }
                }

            }
            while (j < a.length) {
                c[k++] = a[j++];
            }
            while (i < b.length) {
                c[k++] = b[i++];
            }
            return (c);
        }
    }


    public void updateStats() {
        numOfDaysOff = 0;
        ignoreExtrasNumOfDaysOff = 0;

        Long totalStartTime = (long) 0;
        Long totalEndTime = (long) 0;
        Long totalLengthOfDay = (long) 0;

        Long ieTotalStartTime = (long) 0;
        Long ieTotalEndTime = (long) 0;
        Long ieTotalLengthOfDay = (long) 0;

        for (int i = 1; i <= 7; i++) {
            Date earliestStartTime = null;
            Date latestEndTime = null;

            //ie: Ignore Extras (DGD, TUT)
            Date ieEarliestStartTime = null;
            Date ieLatestEndTime = null;

            boolean dayOff = true;
            boolean ieDayOff = true;
            for (CourseSelection cs : courseSelections) {
                for (Activity a : cs.getActivities()) {
                    if (a.getDay() == i) {
                        if (dayOff) {
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
        if (numOfDaysOff != 7) {
            avgStartingTime = new Date(totalStartTime / (7 - numOfDaysOff));
            avgEndingTime = new Date(totalEndTime / (7 - numOfDaysOff));
            avgLengthOfDay = new Date(totalLengthOfDay / (7 - numOfDaysOff));
            if (ieTotalStartTime == 0) {
            	ignoreExtrasAvgStartingTime = avgStartingTime;
            	ignoreExtrasAvgEndingTime = avgEndingTime;
            	ignoreExtrasAvgLengthOfDay = avgLengthOfDay;
            } else {
            	ignoreExtrasAvgStartingTime = new Date(ieTotalStartTime / (7 - ignoreExtrasNumOfDaysOff));
                ignoreExtrasAvgEndingTime = new Date(ieTotalEndTime / (7 - ignoreExtrasNumOfDaysOff));
                ignoreExtrasAvgLengthOfDay = new Date(ieTotalLengthOfDay / (7 - ignoreExtrasNumOfDaysOff));
            }
        }
    }

    public static List<Schedule> generateSchedules(List<Course> selectedCourses, List<Course> optionalCourses, int requiredOptional) {
        //Welcome to the worst sub of your life.

        //Original schedules: generate your mandatory course schedules. Easy.
    	List<Schedule> originalSchedules = new ArrayList<Schedule>();
    	if (selectedCourses.size() > 0) {
    		originalSchedules = generateSchedules(selectedCourses);
    	}
    	
        //A string list to hold gray code.
        List<String[]> nChooseK = new ArrayList<String[]>();

        //Creates the digits that will be used, depending on how many optional courses there are.
        String[] inputs = new String[optionalCourses.size()];
        for (int j = 0; j < optionalCourses.size(); j++) {
            inputs[j] = Integer.toString(j);
        }

        //Run the gray code nChooseK sub.
        nChooseK = nChooseK(inputs, requiredOptional, 0, new String[requiredOptional], nChooseK);

        //Final schedules will be returned to the user.
        List<Schedule> finalSchedules = new ArrayList<Schedule>();

        //For each string array in nChooseK, meaning for each possible gray code...
        for (String[] sArray : nChooseK) {
            //A temporary schedule holder.
            List<Schedule> tempSchedules;

            //A temporary selections holder
            List<ArrayList<CourseSelection>> selections = new ArrayList<ArrayList<CourseSelection>>();

            //Of the nChooseK string, we add k courses to selections.
            for (String s : sArray) {
                selections.add((ArrayList<CourseSelection>) optionalCourses.get(Integer.parseInt(s)).getCourseSelections());
            }
            tempSchedules = generateSchedules(selections, requiredOptional - 1);
            //Now we must merge tempSchedules with original schedules into final schedules.
            if (originalSchedules.size() == 0) {
            	for (Schedule s: tempSchedules) {
            		finalSchedules.add(s);
            	}
            } else {
	            for (Schedule s1 : originalSchedules) { //For every schedule generated previously,
	                for (Schedule s2 : tempSchedules) {
	                    boolean valid = true;
	                    for (CourseSelection cs : s2.getCourseSelections()) {
	                        if (s1.collidesWith(cs)) {
	                            valid = false;
	                        }
	                    }
	                    if (valid) {
	                        //We found a valid match. Let's add them to the final list.
	                        List<CourseSelection> temp1 = s1.getCourseSelections();
	                        List<CourseSelection> temp2 = s2.getCourseSelections();
	                        Schedule result = null;
	                        for (CourseSelection cs : temp1) {
	                            if (result == null) {
	                                result = new Schedule(cs);
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
            }
        }
        return finalSchedules;
    }

    private static List<String[]> nChooseK(String[] arr, int len, int startPosition, String[] result, List<String[]> list) {
        if (len == 0) {
            list.add(result.clone());
            return list;
        }
        for (int i = startPosition; i <= arr.length - len; i++) {
            result[result.length - len] = arr[i];
            nChooseK(arr, len - 1, i + 1, result, list);
        }
        return list;
    }

    public static List<Schedule> generateSchedules(List<Course> selectedCourses) {
        int count = selectedCourses.size();
        List<ArrayList<CourseSelection>> selections = new ArrayList<ArrayList<CourseSelection>>();
        for (int i = 0; i < count; i++) {
            selections.add((ArrayList<CourseSelection>) selectedCourses.get(i).getCourseSelections());
        }
        List<Schedule> schedules = generateSchedules(selections, count - 1);

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
            schedules = generateSchedules(selections, count - 1);
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