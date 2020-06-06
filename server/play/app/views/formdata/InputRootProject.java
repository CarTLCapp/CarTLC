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

import models.RootProject;

public class InputRootProject extends Model {
    private static final long serialVersionUID = 1L;

    public String name;

    public InputRootProject(RootProject project) {
        name = project.name;
    }

    public InputRootProject() {
    }
}

