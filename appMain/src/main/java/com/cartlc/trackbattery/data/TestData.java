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
        TableAddress.getInstance().clear();
        TableAddress.getInstance().add(Arrays.asList(ADDRESSES));
    }

    static final String[] PROJECTS = {
            "Five Cubits",
            "Digital Fleet",
            "Smart Witness",
            "Fed Ex",
            "Other"
    };

    static final Address[] ADDRESSES = {
            new Address("Cable Car Museum", "1201 Mason St", "San Francisco", "California"),
            new Address("Levi's Plaza", "1155 Battery St", "San Francisco", "California"),
            new Address("Nordstrum", "865 Market St", "San Francisco", "California"),
            new Address("Starbucks", "120 4th St", "San Francisco", "California"),
            new Address("Starbucks", "2222 Fillmore St", "San Francisco", "California"),
            new Address("Starbucks", "499 Bay St", "San Francisco", "California"),
            new Address("STAPLES Center", "1111 S Figueroa St", "Los Angeles", "California"),
            new Address("Walt Disney Concert Hal", "111 S Grand Ave", "Los Angeles", "California"),
            new Address("Target", "1 S State St", "Chicago", "Illinois"),
            new Address("Macy's", "111 N State St", "Chicago", "Illinois"),
            new Address("Dunkin Donuts", "200 E Ohio St #1", "Chicago", "Illinois"),
            new Address("Dunkin Donuts", "521 N State St", "Chicago", "Illinois"),
            new Address("Kohl Center", "601 W Dayton St", "Madison", "Wisconsin"),
            new Address("Best Western Plus Inntowner", "2424 University Ave", "Madison", "Wisconsin"),
            new Address("Majestic Theatre", "2115 King St", "Madison", "Wisconsin"),
            new Address("Greyhound", "1001 Howard St", "Detroit", "Michigan"),
            new Address("Burger King", "1425 W Lafayette Blvd", "Detroit", "Michigan"),
            new Address("Burger King", "100 Renaissance Center", "Detroit", "Michigan"),
            new Address("Burger King", "2155 Gratiot Ave", "Detroit", "Michigan"),
    };

}
