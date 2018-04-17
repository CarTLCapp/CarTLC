/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.data;

/**
 * Created by dug on 8/24/17.
 */

public class DataZipCode {

    public String zipCode;
    public String stateLongName;
    public String stateShortName;
    public String city;

    public boolean isValid() {
        if (zipCode == null || stateLongName == null || city == null) {
            return false;
        }
        return zipCode.length() > 0 && stateLongName.length() > 0 && city.length() > 0;
    }

    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("ZIP=");
        sbuf.append(zipCode);
        sbuf.append(",ST=");
        sbuf.append(stateShortName);
        sbuf.append(",STATE=");
        sbuf.append(stateLongName);
        sbuf.append(",CITY=");
        sbuf.append(city);
        return sbuf.toString();
    }

    public String getHint() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(city);
        sbuf.append(", ");
        if (stateShortName == null) {
            sbuf.append(stateLongName);
        } else {
            sbuf.append(stateShortName);
        }
        return sbuf.toString();
    }

    public void check() {
        if (DataStates.isValid(city)) {
            if (stateShortName.equals(stateLongName)) {
                stateLongName = city;
                city = stateShortName;
                stateShortName = null;
            }
        }
    }

}
