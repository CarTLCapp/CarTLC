package com.cartlc.trackbattery.data;

import java.util.Arrays;

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

    static final DataAddress[] ADDRESSES = {
            new DataAddress("Cable Car Museum", "1201 Mason St", "San Francisco", "California"),
            new DataAddress("Levi's Plaza", "1155 Battery St", "San Francisco", "California"),
            new DataAddress("Nordstrum", "865 Market St", "San Francisco", "California"),
            new DataAddress("Starbucks", "120 4th St", "San Francisco", "California"),
            new DataAddress("Starbucks", "2222 Fillmore St", "San Francisco", "California"),
            new DataAddress("Starbucks", "499 Bay St", "San Francisco", "California"),
            new DataAddress("STAPLES Center", "1111 S Figueroa St", "Los Angeles", "California"),
            new DataAddress("Walt Disney Concert Hal", "111 S Grand Ave", "Los Angeles", "California"),
            new DataAddress("Target", "1 S State St", "Chicago", "Illinois"),
            new DataAddress("Macy's", "111 N State St", "Chicago", "Illinois"),
            new DataAddress("Dunkin Donuts", "200 E Ohio St #1", "Chicago", "Illinois"),
            new DataAddress("Dunkin Donuts", "521 N State St", "Chicago", "Illinois"),
            new DataAddress("Kohl Center", "601 W Dayton St", "Madison", "Wisconsin"),
            new DataAddress("Best Western Plus Inntowner", "2424 University Ave", "Madison", "Wisconsin"),
            new DataAddress("Majestic Theatre", "2115 King St", "Madison", "Wisconsin"),
            new DataAddress("Greyhound", "1001 Howard St", "Detroit", "Michigan"),
            new DataAddress("Burger King", "1425 W Lafayette Blvd", "Detroit", "Michigan"),
            new DataAddress("Burger King", "100 Renaissance Center", "Detroit", "Michigan"),
            new DataAddress("Burger King", "2155 Gratiot Ave", "Detroit", "Michigan"),
    };

}
