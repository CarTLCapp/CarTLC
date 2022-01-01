/**
 * Copyright 2021, FleetTLC. All rights reserved
 */
package modules;

import java.util.ArrayList;
import models.Entry;
import org.apache.commons.text.StringEscapeUtils;

public enum Status {
    // Warning: this string version must match the APP side.
    COMPLETE("Complete"),
    PARTIAL("Partial Install"),
    NEEDS_REPAIR("Needs Repair"),
    UNKNOWN("Unknown");

    public final String name;

    Status(String name) {
        this.name = name;
    }

    public static Status from(int ord) {
        for (Status value : values()) {
            if (value.ordinal() == ord) {
                return value;
            }
        }
        return Status.UNKNOWN;
    }

    public static Status from(String match) {
        if (match == null) {
            return Status.UNKNOWN;
        }
        for (Status value : values()) {
            if (value.toString().compareToIgnoreCase(match) == 0) {
                return value;
            }
        }
        return Status.UNKNOWN;
    }

    public String getCellColor() {
        if (this == COMPLETE) {
            return "#00ff00";
        } else if (this == PARTIAL) {
            return "#ff6b4b";
        } else if (this == NEEDS_REPAIR) {
            return "#ff01ff";
        }
        return "";
    }

    public String getName() {
        return name;
    }
}

