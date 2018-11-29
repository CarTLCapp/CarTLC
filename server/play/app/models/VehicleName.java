/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package models;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.Logger;
import play.data.validation.Constraints;

@Entity
public class VehicleName extends com.avaje.ebean.Model {

    private static final long serialVersionUID = 1L;

    public static Finder<Long, VehicleName> find = new Finder<>(VehicleName.class);

    static class SortByNumber implements Comparator<VehicleName> {
        @Override
        public int compare(VehicleName x, VehicleName y) {
            return x.number - y.number;
        }
    }

    @Id
    public Long id;

    @Constraints.Required
    public String name;

    @Constraints.Required
    public int number;

    public static List<VehicleName> list() {
        List<VehicleName> list = find.all();
        list.sort(new SortByNumber());
        return list;
    }

    public String getName() {
        return name;
    }

    public String getOrder() {
        return Integer.toString(number);
    }

    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("NAME=");
        sbuf.append(name);
        sbuf.append(", NUMBER=");
        sbuf.append(number);
        return sbuf.toString();
    }

    public static String getLines() {
        StringBuffer sbuf = new StringBuffer();
        List<VehicleName> list = list();
        boolean comma = false;
        for (VehicleName name : list) {
            if (comma) {
                sbuf.append("\n");
            } else {
                comma = true;
            }
            sbuf.append(name.name);
            sbuf.append(" #");
            sbuf.append(name.number);
        }
        return sbuf.toString();
    }

    public static void setLines(String[] lines) {
        // Parse out names.
        ArrayList<VehicleName> names = new ArrayList<>();
        int number;
        for (String line : lines) {
            line = line.trim();
            number = 0;
            int pos = line.indexOf("#");
            if (pos >= 0) {
                int space = line.indexOf(" ", pos);
                String numstr;
                if (space > 0) {
                    numstr = line.substring(pos + 1, space);
                } else {
                    numstr = line.substring(pos + 1);
                }
                try {
                    number = Integer.parseInt(numstr);
                    String before = line.substring(0, pos);
                    if (space > 0) {
                        String after = line.substring(space + 1);
                        line = (before + after).trim();
                    } else {
                        line = before.trim();
                    }
                } catch (NumberFormatException ex) {
                    Logger.error("While parsing: " + ex.getMessage());
                }
            }
            VehicleName name = new VehicleName();
            name.name = line;
            name.number = number;
            names.add(name);
        }
        // Find used numbers
        HashSet<Integer> used = new HashSet<>();
        for (VehicleName name : names) {
            used.add(name.number);
        }
        // Assign numbers that were not set
        for (VehicleName name : names) {
            if (name.number == 0) {
                name.setNextNumber(used);
            }
        }
        // If Vehicle name already in database, reuse ID, otherwise create new entry.
        List<VehicleName> unprocessed = list();
        for (VehicleName name : names) {
            VehicleName match = name.match(unprocessed);
            if (match != null) {
                name.id = match.id;
                unprocessed.remove(match);
                name.update();
            } else {
                name.save();
            }
        }
        // Delete obsolete elements
        for (VehicleName name : unprocessed) {
            name.delete();
        }
    }

    private void setNextNumber(HashSet<Integer> used) {
        for (int count = 1; ; count++) {
            if (!used.contains(count)) {
                number = count;
                used.add(count);
                return;
            }
        }
    }

    private VehicleName match(List<VehicleName> scanlist) {
        for (VehicleName scan : scanlist) {
            if (number == scan.number) {
                return scan;
            }
        }
        return null;
    }
}
