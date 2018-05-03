/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package models;

import java.util.*;
import javax.persistence.*;

import com.avaje.ebean.Model;
import play.data.format.*;
import play.data.validation.*;

import com.avaje.ebean.*;

public class InputSearch extends Model {
    private static final long serialVersionUID = 1L;
    private static final String OPTION_OR = "OR";
    private static final String OPTION_AND = "AND";
    private static final ArrayList<String> OPTIONS = new ArrayList<>();

    static {
        OPTIONS.add(OPTION_OR);
        OPTIONS.add(OPTION_AND);
    }

    public String search;
    public String logic = OPTION_OR;

    public InputSearch(String search) {
        if (search == null) {
            this.search = null;
        } else {
            this.search = search;
        }
    }

    public InputSearch() {
        this.search = "";
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public boolean isLogicalAnd() {
        return logic.equals(OPTION_AND);
    }

    public ArrayList<String> getOptions() {
        return OPTIONS;
    }
}

