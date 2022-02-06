/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package models;

import java.util.*;

import com.avaje.ebean.*;

import play.db.ebean.*;
import play.Logger;

import play.twirl.api.Html;

public class CleanupData {

    public CleanupData() {
    }

    private ArrayList<String> mMessages = new ArrayList<String>();

    public int numRepaired = 0;
    public String commonResult = "";
    public int numRecordsFound = -1;
    public Date currentDate;
    public String currentDateString = "";

    public void clear() {
        numRepaired = 0;
        commonResult = "";
        numRecordsFound = -1;
        currentDateString = "";
        currentDate = null;
        mMessages.clear();
    }

    public List<String> getMessages() {
        return mMessages;
    }

    public void setMessages(String line) {
        mMessages.clear();
        if (line != null) {
            String[] strings = line.split("\n", 0);
            for (String str : strings) {
                mMessages.add(str);
            }
        }
    }

    public void clearMessages() {
        mMessages.clear();
    }

    public void addMessage(String msg) {
        mMessages.add(msg);
    }

}
