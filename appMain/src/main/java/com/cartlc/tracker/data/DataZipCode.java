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
        if (zipCode == null || stateLongName == null || stateShortName == null || city == null) {
            return false;
        }
        return zipCode.length() > 0 && stateLongName.length() > 0 && stateShortName.length() > 0 && city.length() > 0;
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
        sbuf.append(stateShortName);
        return sbuf.toString();
    }

}