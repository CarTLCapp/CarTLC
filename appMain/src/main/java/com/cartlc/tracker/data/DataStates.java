package com.cartlc.tracker.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by dug on 5/11/17.
 */

public class DataStates {

    static final String[] STATES = {
            "Alabama",
            "Alaska",
            "Arizona",
            "Arkansas",
            "California",
            "Colorado",
            "Connecticut",
            "Delaware",
            "Florida",
            "Georgia",
            "Hawaii",
            "Idaho",
            "Illinois",
            "Indiana",
            "Iowa",
            "Kansas",
            "Kentucky",
            "Louisiana",
            "Maine",
            "Maryland",
            "Massachusetts",
            "Michigan",
            "Minnesota",
            "Mississippi",
            "Missouri",
            "Montana Nebraska",
            "Nevada",
            "New Hampshire",
            "New Jersey",
            "New Mexico",
            "New York",
            "North Carolina",
            "North Dakota",
            "Ohio",
            "Oklahoma",
            "Oregon",
            "Pennsylvania",
            "Rhode Island",
            "South Carolina",
            "South Dakota",
            "Tennessee",
            "Texas",
            "Utah",
            "Vermont",
            "Virginia",
            "Washington",
            "West Virginia",
            "Wisconsin",
            "Wyoming",
    };

    public static List<String> getUnusedStates(List<String> usedStates) {
        List<String> unused = new ArrayList();
        for (String state : STATES) {
            if (!usedStates.contains(state)) {
                unused.add(state);
            }
        }
        Collections.sort(unused);
        return unused;
    }

    public static boolean isValid(String scan) {
        for (String state : STATES) {
            if (state.equalsIgnoreCase(scan)) {
                return true;
            }
        }
        return false;
    }
}
