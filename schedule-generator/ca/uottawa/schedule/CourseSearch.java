package ca.uottawa.schedule;

import java.util.*;

/**
 * Date Started: 11/20/13
 * Made by: Daniel Murdoch
 * Student ID: 6765413
 */

public class CourseSearch {
    public static List<String> search(String query, List<Course> list) {
        List<String> result = new ArrayList<String>();
        query = query.toUpperCase();
        int q = query.length();

        int h = list.size()-1;
        int l = 0;
        int m = h / 2;

        while(!list.get(m).getDescription().substring(0, q).toUpperCase().equals(query)) {
            if (list.get(m).getDescription().substring(0, q).toUpperCase().compareTo(query) > 0) {
                h = m-1;
            } else {
                l = m + 1;
            }

            m = (l+h) / 2;

        }
        if (l <= h) {
            while(list.get(m).getDescription().substring(0, q).toUpperCase().equals(query)) {
                m--;
                if (m < 0) { //LOL
                    break;
                }
            }

            m++;
            while(list.get(m).getDescription().substring(0, q).toUpperCase().equals(query)) {
                result.add(list.get(m).getDescription());
                m++;
                if (m >= list.size()) {
                	break;
                }
            }

            return result;
        } else {
            return result;
        }
    }
}
