/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package modules;

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Date;
import java.util.Calendar;
import java.lang.StringBuilder;

import play.Logger;

public class TimeHelper {

    public static final String DATE_FORMAT = "MM/dd/yyyy";              // relative to user's local time
    public static final String TIME_FORMAT = "kk:mm";                   // relative to user's local time
    public static final String DATE_TIME_FORMAT = "MM/dd/yyyy kk:mm"; // relative to user's local time

    public TimeHelper() {
    }

    /**
     * @param entry_time
     * @return date relative to the user's local time, with the time zone they are in appended on.
     */
    public String getDateTime(Date entry_time, String time_zone) {
        if (entry_time == null) {
            return "";
        }
        return getFormatAppendTimeZone(DATE_TIME_FORMAT, entry_time, time_zone);
    }

    /**
     * @param entry_time
     * @return date relative to the user's local time. (i.e. ignore time zone)
     */
    public String getDate(Date entry_time) {
        if (entry_time == null) {
            return "";
        }
        return new SimpleDateFormat(DATE_FORMAT).format(entry_time);
    }

    public String getTime(Date entry_time, String time_zone) {
        if (entry_time == null) {
            return "";
        }
        return getFormatAppendTimeZone(TIME_FORMAT, entry_time, time_zone);
    }

    private String getFormatAppendTimeZone(String the_format, Date entry_time, String time_zone) {
        SimpleDateFormat format = new SimpleDateFormat(the_format);
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(format.format(entry_time));
        sbuf.append(" ");
        sbuf.append(time_zone);
        return sbuf.toString();
    }

    private TimeZone getTimeZone(String time_zone) {
        if (time_zone != null) {
            if (time_zone.startsWith("-") || time_zone.startsWith("+")) {
                return TimeZone.getTimeZone("GMT" + time_zone);
            } else if (time_zone.equals("CDT") || time_zone.equals("Central Daylight Time")) {
                return TimeZone.getTimeZone("GMT-5:00");
            } else if (time_zone.equals("EDT")) {
                return TimeZone.getTimeZone("GMT-4:00");
            } else {
                return TimeZone.getTimeZone(time_zone);
            }
        }
        return null;
    }


}
