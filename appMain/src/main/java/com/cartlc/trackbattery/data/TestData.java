package com.cartlc.trackbattery.data;

import java.util.Arrays;

import timber.log.Timber;

/**
 * Created by dug on 5/9/17.
 */

public class TestData {

    public static void Init() {
        TableProjects.getInstance().clear();
        TableProjects.getInstance().add(Arrays.asList(PROJECTS));
        TableState.getInstance().clear();
        TableState.getInstance().add(Arrays.asList(STATES));
        TableCity.getInstance().clear();
        TableCity.getInstance().add(Arrays.asList(CITIES));
    }

    static final String[] PROJECTS = {
            "Five Cubits",
            "Digital Fleet",
            "Smart Witness",
            "Fed Ex",
            "Other"
    };

    static final Integer POS(String value, String[] values) {
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(value)) {
                return i;
            }
        }
        Timber.e("Could not find: " + value);
        return null;
    }

    static final Integer Project(String value) {
        return POS(value, PROJECTS);
    }

    static final String[] STATES = {
            "California",
            "Illinois",
            "Wisconsin",
            "Michigan",
            "Washington DC",
            "New York",
            "Florida",
            "Ohio",
            "Texas",
            "Utah",
            "Rhode Island",
            "Arkansas",
            "Nevada",
            "Oregon",
            "Arizona",
            "Washington",
            "North Dakota",
            "Tennesee",
            "Georgia"
    };

    static final Integer State(String value) {
        return POS(value, STATES);
    }

    static final String[] CITIES = {
            "San Francisco",
            "Los Angeles",
            "Sacramento",
            "San Jose",
            "Chicago",
            "Normal",
            "Springfield",
            "Aurora",
            "Milwaukee",
            "Racine",
            "Kenosha",
            "Madison",
            "New York",
            "Dallas",
            "Houston",
            "Orlando",
            "Detroit",
            "Grand Rapids",
            "San Salvador",
            "Antiqua",

    };

}
