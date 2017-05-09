package com.fleettlc.trackbattery.data;

/**
 * Created by dug on 5/9/17.
 */

public class Entry {
    public String value;
    public Integer father;

    public Entry(String value, int father) {
        this.value = value;
        this.father = father;
    }

    public Entry(String value) {
        this.value = value;
    }
}
