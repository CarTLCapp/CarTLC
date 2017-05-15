package com.cartlc.trackbattery.data;

/**
 * Created by dug on 5/10/17.
 */

public class DataAddress {
    public String company;
    public String street;
    public String city;
    public String state;

    public DataAddress(String company, String street, String city, String state) {
        this.company = company;
        this.street = street;
        this.city = city;
        this.state = state;
    }

    public String getBlock() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(company);
        sbuf.append("\n");
        sbuf.append(street);
        sbuf.append(",\n");
        sbuf.append(city);
        sbuf.append(", ");
        sbuf.append(state);
        return sbuf.toString();
    }

    public String getLine() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(company);
        sbuf.append(", ");
        sbuf.append(street);
        sbuf.append(", ");
        sbuf.append(city);
        sbuf.append(", ");
        sbuf.append(state);
        return sbuf.toString();
    }
}
