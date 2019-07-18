/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package models.flow;

public class Bit {

    public String id;
    public String name;
    public String detail;

    public Bit(String id, String name, String detail) {
        this.id = id;
        this.name = name;
        this.detail = detail;
    }

    public String getName() {
        return name + ": " + Collect.reduce(detail);
    }

}
