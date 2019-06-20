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

import models.Project;

public class ImportWorkOrder extends Model {
    private static final long serialVersionUID = 1L;
    public String rootProject;
    public String subProject;
    public String company;

    public ImportWorkOrder() {
    }

    public List<String> optionsSubProject() {
        return Project.listSubProjectNamesWithBlank(rootProject);
    }
}

