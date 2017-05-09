package com.fleettlc.trackbattery.data;

import java.util.ArrayList;
import java.util.Arrays;

import timber.log.Timber;

/**
 * Created by dug on 5/9/17.
 */

public class TestData {

    public static void Init() {
        TableProjects.getInstance().clear();
        TableProjects.getInstance().addStrings(Arrays.asList(PROJECTS));
        TableState.getInstance().clear();
        TableState.getInstance().add(Arrays.asList(STATES));
        TableCity.getInstance().clear();
        TableCity.getInstance().add(Arrays.asList(CITIES));
    }

    static final String[] PROJECTS = {
            "US",
            "Canada",
            "Central America"
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

    static final Entry[] STATES = {
            new Entry("California", Project("US")),
            new Entry("Illinois", Project("US")),
            new Entry("Wisconsin", Project("US")),
            new Entry("Michigan", Project("US")),
            new Entry("Washington DC", Project("US")),
            new Entry("New York", Project("US")),
            new Entry("Florida", Project("US")),
            new Entry("Ohio", Project("US")),
            new Entry("Texas", Project("US")),
            new Entry("Utah", Project("US")),
            new Entry("Rhode Island", Project("US")),
            new Entry("Arkansas", Project("US")),
            new Entry("Nevada", Project("US")),
            new Entry("Oregon", Project("US")),

            new Entry("Manitoba", Project("Canada")),
            new Entry("Alberta", Project("Canada")),
            new Entry("British Columbia", Project("Canada")),
            new Entry("Saskatchewan", Project("Canada")),
            new Entry("Ontario", Project("Canada")),
            new Entry("Quebec", Project("Canada")),
            new Entry("Nova Scotia", Project("Canada")),

            new Entry("Guatemala", Project("Central America")),
            new Entry("Honduras", Project("Central America")),
            new Entry("El Salvador", Project("Central America")),
            new Entry("Costa Rica", Project("Central America")),
            new Entry("Panama", Project("Central America")),
            new Entry("Belize", Project("Central America")),
            new Entry("Central America", Project("Central America")),
    };

    static final Integer POS(String value, Entry[] values) {
        for (int i = 0; i < values.length; i++) {
            if (values[i].value.equals(value)) {
                return i;
            }
        }
        Timber.e("Could not find: " + value);
        return null;
    }

    static final Integer State(String value) {
        return POS(value, STATES);
    }

    static final Entry[] CITIES = {
            new Entry("San Francisco", State("California")),
            new Entry("Los Angeles", State("California")),
            new Entry("Sacramento", State("California")),
            new Entry("San Jose", State("California")),
            new Entry("Chicago", State("Illinois")),
            new Entry("Normal", State("Illinois")),
            new Entry("Springfield", State("Illinois")),
            new Entry("Aurora", State("Illinois")),
            new Entry("Milwaukee", State("Wisconsin")),
            new Entry("Racine", State("Wisconsin")),
            new Entry("Kenosha", State("Wisconsin")),
            new Entry("Madison", State("Wisconsin")),
            new Entry("New York", State("New York")),
            new Entry("Dallas", State("Texas")),
            new Entry("Houston", State("Texas")),
            new Entry("Orlando", State("Florida")),
            new Entry("Detroit", State("Michigan")),
            new Entry("Grand Rapids", State("Michigan")),
            new Entry("San Salvador", State("El Salvador")),
            new Entry("Antiqua", State("Guatemala")),

    };

}
