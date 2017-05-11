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
            "Other",
            "Verifi"
    };

    static final DataAddress[] ADDRESSES = {
            new DataAddress("IMI", "1201 Mason St", "San Francisco", "California"),
            new DataAddress("Alamo RM", "1155 Battery St", "San Francisco", "California"),
            new DataAddress("Ozinga", "865 Market St", "San Francisco", "California"),
            new DataAddress("CW Roberts", "120 4th St", "San Francisco", "California"),
            new DataAddress("CW Roberts", "2222 Fillmore St", "San Francisco", "California"),
            new DataAddress("CW Roberts", "499 Bay St", "San Francisco", "California"),
            new DataAddress("Point RM", "1111 S Figueroa St", "Los Angeles", "California"),
            new DataAddress("Central Concrete", "111 S Grand Ave", "Los Angeles", "California"),
            new DataAddress("IMI", "1 S State St", "Chicago", "Illinois"),
            new DataAddress("Alamo RM", "111 N State St", "Chicago", "Illinois"),
            new DataAddress("Ozinga", "200 E Ohio St #1", "Chicago", "Illinois"),
            new DataAddress("Ozinga", "521 N State St", "Chicago", "Illinois"),
            new DataAddress("CW Roberts", "601 W Dayton St", "Madison", "Wisconsin"),
            new DataAddress("Point RM", "2424 University Ave", "Madison", "Wisconsin"),
            new DataAddress("Central Concrete", "2115 King St", "Madison", "Wisconsin"),
            new DataAddress("IMI", "1001 Howard St", "Detroit", "Michigan"),
            new DataAddress("Alamo RM", "1425 W Lafayette Blvd", "Detroit", "Michigan"),
            new DataAddress("Alamo RM", "100 Renaissance Center", "Detroit", "Michigan"),
            new DataAddress("Alamo RM", "2155 Gratiot Ave", "Detroit", "Michigan"),
    };

}
