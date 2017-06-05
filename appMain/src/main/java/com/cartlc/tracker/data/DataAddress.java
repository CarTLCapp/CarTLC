package com.cartlc.tracker.data;

import android.text.TextUtils;

/**
 * Created by dug on 5/10/17.
 */

public class DataAddress {
    public long    id;
    public int     server_id;
    public String  company;
    public String  street;
    public String  city;
    public String  state;
    public String  zipcode;
    public boolean disabled;
    public boolean isLocal;

    public DataAddress(String company, String street, String city, String state) {
        this.company = company;
        this.street = street;
        this.city = city;
        this.state = state;
    }

    public DataAddress(String company, String street, String city, String state, String zipcode) {
        this.company = company;
        this.street = street;
        this.city = city;
        this.state = state;
        this.zipcode = zipcode;
    }

    public DataAddress(String company, String zipcode) {
        this.company = company;
        this.zipcode = zipcode;
    }

    public DataAddress(int server_id, String company, String street, String city, String state, String zipcode) {
        this.server_id = server_id;
        this.company = company;
        this.street = street;
        this.city = city;
        this.state = state;
        this.zipcode = zipcode;
    }

    public DataAddress(long id, int server_id, String company, String street, String city, String state, String zipcode) {
        this.id = id;
        this.server_id = server_id;
        this.company = company;
        this.street = street;
        this.city = city;
        this.state = state;
        this.zipcode = zipcode;
    }

    public String getBlock() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(company);

        if (hasAddress()) {
            sbuf.append("\n");
            sbuf.append(street);
            sbuf.append(",\n");
            sbuf.append(city);
            sbuf.append(", ");
            sbuf.append(state);
        }
        if (!TextUtils.isEmpty(zipcode)) {
            sbuf.append(" ");
            sbuf.append(zipcode);
        }
        return sbuf.toString();
    }

    public String getLine() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(company);

        if (hasAddress()) {
            sbuf.append(", ");
            sbuf.append(street);
            sbuf.append(", ");
            sbuf.append(city);
            sbuf.append(", ");
            sbuf.append(state);
        }
        if (!TextUtils.isEmpty(zipcode)) {
            sbuf.append(" ");
            sbuf.append(zipcode);
        }
        return sbuf.toString();
    }

    boolean hasAddress() {
        return (street != null && street.length() > 0) ||
                (city != null && city.length() > 0) ||
                (state != null && state.length() > 0);
    }

    boolean hasZipCode() {
        return !TextUtils.isEmpty(zipcode);
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
        if (hasZipCode()) {
            return company.equals(item.company) &&
                    zipcode.equals(item.zipcode);
        }
        return company.equals(item.company) &&
                street.equals(item.street) &&
                city.equals(item.city) &&
                state.equals(item.state);
    }
}
