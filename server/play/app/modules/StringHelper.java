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
            Logger.error(ex.getMessage());
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

}
