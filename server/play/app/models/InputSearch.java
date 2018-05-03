/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package models;

import java.util.*;
import javax.persistence.*;

import com.avaje.ebean.Model;
import play.data.format.*;
import play.data.validation.*;
import models.EntryPagedList.Logic;

import com.avaje.ebean.*;

public class InputSearch extends Model {
    private static final long serialVersionUID = 1L;

    public String search;
    public String logic;

    public InputSearch(String search) {
        this.search = search;
        this.logic = EntryPagedList.Logic.OR.getDisplay();
    }

    public InputSearch(String search, String logic) {
        this.search = search;
        this.logic = logic;
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

    public String getLogic() {
        return logic;
    }

    public static ArrayList<String> options() {
        return Logic.items();
    }
}

