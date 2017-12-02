package models;

import java.util.*;
import javax.persistence.*;

import com.avaje.ebean.Model;
import play.data.format.*;
import play.data.validation.*;

import com.avaje.ebean.*;

public class InputTruck extends Model {
    private static final long serialVersionUID = 1L;
    public String truck_number;
    public String license_plate;
    public String project_name;
    public String company_name;
    boolean canDelete;

    public InputTruck(Truck truck) {
        truck_number = truck.getTruckNumber();
        license_plate = truck.getLicensePlate();
        project_name = truck.getProjectName();
        company_name = truck.getCompanyName();
        canDelete = truck.canDelete();
    }

    public InputTruck() {
    }

    public boolean canDelete() {
        return canDelete;
    }
}

