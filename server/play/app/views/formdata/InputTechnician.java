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

import models.Technician;

public class InputTechnician extends Model {
    private static final long serialVersionUID = 1L;
    public String first_name;
    public String last_name;
    public String code;

    public InputTechnician(Technician tech) {
        first_name = tech.first_name;
        last_name = tech.last_name;
        code = Integer.valueOf(tech.code).toString();
    }

    public InputTechnician() {
        code = Integer.valueOf(Technician.findLastCode()+1).toString();
    }
}

