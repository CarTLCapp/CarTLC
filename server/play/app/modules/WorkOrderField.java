package modules;

import java.util.ArrayList;

public enum WorkOrderField {
    COMPANY("Company"),
    STREET("Street", "address line 1", "address 1"),
    CITY("City"),
    STATE("State"),
    ZIP("ZIP", "zip code"),
    TRUCK_NUMBER("Truck #", "UNIT #"),
    LICENSE("License Plate", "License Number");

    public static WorkOrderField find(String name) {
        for (WorkOrderField field : values()) {
            if (field.match(name)) {
                return field;
            }
        }
        return null;
    }

    ArrayList<String> matches = new ArrayList<String>();

    WorkOrderField(String ... values) {
        for (int i = 0; i < values.length; i++) {
            matches.add(values[i]);
        }
    }

    public String getName() {
        return matches.get(0);
    }

    boolean match(String name) {
        for (String match : matches) {
            if (match.compareToIgnoreCase(name.trim()) == 0) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        for (String name : matches) {
            if (sbuf.length() > 0) {
                sbuf.append(",");
            }
            sbuf.append(name);
        }
        return sbuf.toString();
    }
}