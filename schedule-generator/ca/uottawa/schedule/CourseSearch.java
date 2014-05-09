package ca.uottawa.schedule;

import java.util.*;

/**
 * Date Started: 11/20/13
 * Made by: Daniel Murdoch
 * Student ID: 6765413
 */

public class CourseSearch {
    public static List<String> search(String query, String semester, List<Course> list) {
        query = query.replace(" ", ""); //Remove spaces
    	if (query.length()>8) {
            query = query.substring(0, 8);
        }
        List<String> result = new ArrayList<String>();
        query = query.toUpperCase();

        int h = list.size()-1;
        int l = 0;
        int m = h / 2;

        while(!list.get(m).getDescription().toUpperCase().startsWith(query) && (l<=h)) {
        	int k = list.get(m).getDescription().length();
            if (list.get(m).getDescription().substring(0, k).toUpperCase().compareTo(query) > 0) {
                h = m-1;
            } else {
                l = m + 1;
            }
            m = (l+h) / 2;

        }


        if (l <= h) {
        	int k = list.get(m).getDescription().length();
            while(list.get(m).getDescription().substring(0, k).toUpperCase().startsWith(query)) {
                m--;
                if (m < 0) { //LOL
                    break;
                }
                k = list.get(m).getDescription().length();
            }

            m++;
            k = list.get(m).getDescription().length();
            while(list.get(m).getDescription().substring(0, k).toUpperCase().startsWith(query)) {
                if (list.get(m).getSemesters().contains(semester)) {
                    result.add(list.get(m).getDescription());
                }
                m++;
                if (m >= list.size()) {
                	break;
                }
                k = list.get(m).getDescription().length();
            }

            return result;
        } else {

            return result;
        }
    }

    public static int indexOf(String query, String semester, List<Course> list) {
        if (query.length()>8) {
            query = query.substring(0, 8);
        }
        query = query.toUpperCase();
        int q = query.length();

        int h = list.size()-1;
        int l = 0;
        int m = h / 2;
        boolean courseCodeMatch = list.get(m).getDescription().substring(0, q).toUpperCase().equals(query);
        while(!courseCodeMatch && (l<=h)) {
            if (list.get(m).getDescription().substring(0, q).toUpperCase().compareTo(query) > 0) {
                h = m-1;
            } else {
                l = m + 1;
            }
            m = (l+h) / 2;
            courseCodeMatch = list.get(m).getDescription().substring(0, q).toUpperCase().equals(query);
        }


        if ((l <= h) && (list.get(m).getSemesters().contains(semester))) {
            return m;
        } else {
            return -1;
        }
    }
}
