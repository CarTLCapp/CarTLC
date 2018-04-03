package com.cartlc.tracker.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by dug on 5/11/17.
 */

public class DataStates {

    public static class State {
        String full;
        String abbr;

        State(String full, String abbr) {
            this.full = full;
            this.abbr = abbr;
        }
    }

    static final State[] STATES = {
            new State("Alabama", "AL"),
            new State("Alaska", "AK"),
            new State("Arizona", "AZ"),
            new State("Arkansas", "AR"),
            new State("California", "CA"),
            new State("Colorado", "CO"),
            new State("Connecticut", "CT"),
            new State("Delaware", "DE"),
            new State("Florida", "FL"),
            new State("Georgia", "GA"),
            new State("Hawaii", "HI"),
            new State("Idaho", "ID"),
            new State("Illinois", "IL"),
            new State("Indiana", "IN"),
            new State("Iowa", "IA"),
            new State("Kansas", "KS"),
            new State("Kentucky", "KY"),
            new State("Louisiana", "LA"),
            new State("Maine", "ME"),
            new State("Maryland", "MD"),
            new State("Massachusetts", "MA"),
            new State("Michigan", "MI"),
            new State("Minnesota", "MN"),
            new State("Mississippi", "MS"),
            new State("Missouri", "MO"),
            new State("Montana", "MT"),
            new State("Nebraska", "NE"),
            new State("Nevada", "NV"),
            new State("New Hampshire", "NH"),
            new State("New Jersey", "NJ"),
            new State("New Mexico", "NM"),
            new State("New York", "NY"),
            new State("North Carolina", "NC"),
            new State("North Dakota", "ND"),
            new State("Ohio", "OH"),
            new State("Oklahoma", "OK"),
            new State( "Oregon", "OR"),
            new State("Pennsylvania", "PA"),
            new State("Rhode Island", "RI"),
            new State("South Carolina", "SC"),
            new State("South Dakota", "SD"),
            new State("Tennessee", "TN"),
            new State("Texas", "TX"),
            new State("Utah", "UT"),
            new State("Vermont", "VT"),
            new State("Virginia", "VA"),
            new State("Washington", "WA"),
            new State("West Virginia", "WV"),
            new State("Wisconsin", "WI"),
            new State("Wyoming", "WY"),
    };

    public static List<String> getUnusedStates(List<String> usedStates) {
        List<String> unused = new ArrayList();
        for (State state : STATES) {
            if (!usedStates.contains(state.full)) {
                unused.add(state.full);
            }
        }
        Collections.sort(unused);
        return unused;
    }

    public static boolean isValid(String scan) {
        return get(scan) != null;
    }

    public static State get(String scan) {
        for (State state : STATES) {
            if (state.full.equalsIgnoreCase(scan) || state.abbr.equalsIgnoreCase(scan)) {
                return state;
            }
        }
        return null;
    }

    public static String getAbbr(String scan) {
        State state = get(scan);
        if (state != null) {
            return state.abbr;
        }
        return null;
    }

    public static String getFull(String scan) {
        State state = get(scan);
        if (state != null) {
            return state.full;
        }
        return null;
    }
}
