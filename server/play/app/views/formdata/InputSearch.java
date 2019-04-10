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

    public String search;

    public InputSearch(String search) {
        this.search = search;
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

}

