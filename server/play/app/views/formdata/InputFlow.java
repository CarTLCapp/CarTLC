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

import models.flow.Flow;
import models.Project;

public class InputFlow extends Model {
    private static final long serialVersionUID = 1L;
    public String root_project_name;
    public String sub_project_name;

    public InputFlow(Flow flow) {
        if (flow != null) {
            root_project_name = flow.getRootProjectName();
            sub_project_name = flow.getSubProjectName();
        }
    }

    public InputFlow() {
    }

    public InputFlow(InputFlow other) {
        root_project_name = other.root_project_name;
        sub_project_name = other.sub_project_name;
    }

    public List<String> optionsSubProject() {
        return Project.listSubProjectNamesWithBlank(root_project_name);
    }

}

