/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package views.formdata;

import java.util.*;
import javax.persistence.*;

import com.avaje.ebean.Model;
import play.data.format.*;
import play.data.validation.*;

import com.avaje.ebean.*;

public class InputSearch extends Model {
    private static final long serialVersionUID = 1L;

    public String searchTerm;
    public String searchField;

    public InputSearch(String search, String field) {
        setSearch(search, field);
    }

    public InputSearch() {
        this.searchTerm = "null";
        this.searchField = "null";
    }

    public String getTerm() {
        return searchTerm;
    }

    public String getField() { return searchField; }

    public void setSearch(String search, String field) {
        if (search.equals("null")) {
            searchTerm = null;
        } else {
            searchTerm = search;
        }
        if (field.equals("null")) {
            searchField = null;
        } else {
            searchField = field;
        }
    }

}

