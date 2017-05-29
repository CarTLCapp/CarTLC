package com.cartlc.tracker.data;

/**
 * Created by dug on 5/10/17.
 */

public class DataAddress {
    public long id;
    public int server_id;
    public String company;
    public String street;
    public String city;
    public String state;
    public boolean disabled;

    public DataAddress(String company, String street, String city, String state) {
        this.company = company;
        this.street = street;
        this.city = city;
        this.state = state;
    }

    public DataAddress(int server_id, String company, String street, String city, String state) {
        this.server_id = server_id;
        this.company = company;
        this.street = street;
        this.city = city;
        this.state = state;
    }

    public DataAddress(long id, int server_id, String company, String street, String city, String state) {
        this.id = id;
        this.server_id = server_id;
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DataAddress) {
            return equals((DataAddress) obj);
        }
        if (obj instanceof Integer) {
            return id == (Integer) obj;
        }
        return false;
    }

    public boolean equals(DataAddress item) {
        return company.equals(item.company) &&
               street.equals(item.street) &&
               city.equals(item.city) &&
               state.equals(item.state);
    }
}
