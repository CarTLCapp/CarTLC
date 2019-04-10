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

import models.Truck;
import models.Project;

public class InputTruck extends Model {
    private static final long serialVersionUID = 1L;
    public String truck_number;
    public String license_plate;
    public String root_project_name;
    public String sub_project_name;
    public String company_name;
    boolean canDelete;

    public InputTruck(Truck truck) {
        truck_number = truck.getTruckNumber();
        license_plate = truck.getLicensePlate();
        root_project_name = truck.getRootProjectName();
        sub_project_name = truck.getSubProjectName();
        company_name = truck.getCompanyName();
        canDelete = truck.canDelete();
    }

    public InputTruck() {
    }

    public boolean canDelete() {
        return canDelete;
    }

    public List<String> optionsSubProject() {
        return Project.listSubProjectsWithBlank(root_project_name);
    }
}

