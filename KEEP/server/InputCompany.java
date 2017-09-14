package models;

import java.util.*;

import javax.persistence.*;

import com.avaje.ebean.Model;

import play.data.format.*;
import play.data.validation.*;

import com.avaje.ebean.*;

public class InputCompany extends Model {

    private static final long serialVersionUID = 1L;

    public String name;
    public String street;
    public String city;
    public String state;
    public String zipcode;

    public InputCompany(long id) {
        Company company = Company.find.ref(id);
        name = company.getName();
        street = company.street;
        city = company.city;
        state = company.state;
        zipcode = company.zipcode;
    }
}

