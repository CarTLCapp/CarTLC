/**
 * Copyright 2021, FleetTLC. All rights reserved
 */
package modules;

import play.Logger;

import org.apache.commons.text.StringEscapeUtils;

public class StringHelper {

    public StringHelper() {
    }

    public static String decode(String ele) {
        try {
            return StringEscapeUtils.unescapeHtml4(ele);
        } catch (Exception ex) {
            error(ex.getMessage());
            return ele;
        }
    }

    public static String pickOutTimeZone(String value, char sp) {
        int pos = value.indexOf(sp);
        if (pos >= 0) {
            return value.substring(pos + 1);
        }
        return null;
    }

    public static String pickOutTimeWithoutTimeZone(String value, char sp) {
        int pos = value.indexOf(sp);
        if (pos >= 0) {
            return value.substring(0, pos);
        }
        return value;
    }

    // region Logger

    private static void error(String msg) {
        Logger.error(msg);
    }

    private static void warn(String msg) {
        Logger.warn(msg);
    }

    private static void info(String msg) {
        Logger.info(msg);
    }

    private static void debug(String msg) {
        Logger.debug(msg);
    }

    // endregion Logger


}
