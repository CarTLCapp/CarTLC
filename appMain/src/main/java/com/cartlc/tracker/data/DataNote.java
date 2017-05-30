package com.cartlc.tracker.data;

/**
 * Created by dug on 5/16/17.
 */

public class DataNote {

    public enum Type {
        TEXT,
        NUMERIC,
        ALPHANUMERIC,
        NUMERIC_WITH_SPACES,
        MULTILINE;

        public static Type from(int ord) {
            for (Type value : values()) {
                if (value.ordinal() == ord) {
                    return value;
                }
            }
            return Type.TEXT;
        }

        public static Type from(String item) {
            String match = item.toLowerCase().trim();
            for (Type value : values()) {
                if (value.toString().toLowerCase().equals(match)) {
                    return value;
                }
            }
            return Type.TEXT;
        }
    }

    public long   id;
    public String name;
    public String value;
    public Type   type;
    public int    server_id;

    public DataNote() {
    }

    public DataNote(String name) {
        this.name = name;
    }

    public DataNote(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public DataNote(String name, Type type, int server_id) {
        this.name = name;
        this.type = type;
        this.server_id = server_id;
    }

    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("ID=");
        sbuf.append(id);
        sbuf.append(", NAME=");
        sbuf.append(name);
        sbuf.append(", VALUE=");
        sbuf.append(value);
        sbuf.append(", TYPE=");
        sbuf.append(type.toString());
        return sbuf.toString();
    }

    @Override
    public boolean equals(Object item) {
        if (item instanceof DataNote) {
            return equals((DataNote) item);
        }
        return super.equals(item);
    }

    public boolean equals(DataNote item) {
        return name.equals(item.name);
    }

}
