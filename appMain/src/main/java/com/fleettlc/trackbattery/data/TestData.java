package com.fleettlc.trackbattery.data;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by dug on 5/9/17.
 */

public class TestData {

    public static void Init()
    {
        TableProjects.getInstance().clear();
        TableProjects.getInstance().add(Arrays.asList(PROJECTS));
    }

    static final String [] PROJECTS = {
            "Digital",
            "Analog",
            "Bermuda"
    };
}
