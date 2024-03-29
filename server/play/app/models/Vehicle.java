/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package models;

import com.avaje.ebean.PagedList;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.data.format.Formats;
import play.data.validation.Constraints;

import modules.TimeHelper;

/**
 * User entity managed by Ebean
 */
@Entity
public class Vehicle extends com.avaje.ebean.Model {

    private static final long serialVersionUID = 1L;

    public static Finder<Long, Vehicle> find = new Finder<Long, Vehicle>(Vehicle.class);

    public static PagedList<Vehicle> list(int page, int pageSize, String sortBy, String order) {
        return
                find.where()
                        .orderBy(sortBy + " " + order)
                        .findPagedList(page, pageSize);
    }

    @Id
    public Long id;

    @Constraints.Required
    public int tech_id;

    @Formats.DateTime(pattern = "yyyy-MM-dd kk:mm zzz")
    public Date entry_time;

    @Constraints.Required
    public String time_zone;

    @Constraints.Required
    public Long inspecting;

    @Constraints.Required
    public Long type_of_inspection;

    @Constraints.Required
    public int mileage;

    @Constraints.Required
    public String head_lights;

    @Constraints.Required
    public String tail_lights;

    @Constraints.Required
    public String exterior_light_issues;

    @Constraints.Required
    public String fluid_checks;

    @Constraints.Required
    public String fluid_problems_detected;

    @Constraints.Required
    public String tire_inspection;

    @Constraints.Required
    public String exterior_damage;

    @Constraints.Required
    public String other;

    public String getTechName() {
        Technician tech = null;
        try {
            tech = Technician.find.byId((long) tech_id);
        } catch (Exception ex) {
        }
        if (tech == null) {
            return Technician.RIP;
        }
        try {
            return tech.fullName();
        } catch (Exception ex) {
            return Technician.RIP;
        }
    }

    public String getDate() { return new TimeHelper().getDate(entry_time); }

    public String getTime() { return new TimeHelper().getTime(entry_time, time_zone); }

    public String getInspecting() {
        return Strings.get(inspecting);
    }

    public String getTypeOfInspection() {
        return Strings.get(type_of_inspection);
    }

    public String getMileage() {
        return Long.toString(mileage);
    }

    public String getHeadLights() {
        return expand(head_lights);
    }

    public String getTailLights() {
        return expand(tail_lights);
    }

    public String getExteriorLightIssues() {
        return exterior_light_issues;
    }

    public String getFluidChecks() {
        return expand(fluid_checks);
    }

    public String getFluidProblems() {
        return fluid_problems_detected;
    }

    public String getTireInspection() {
        return expand(tire_inspection);
    }

    public String getExteriorDamage() {
        return exterior_damage;
    }

    public String getOther() {
        return other;
    }

    private String expand(String text) {
        List<String> list = new ArrayList<>();
        String[] items = text.split(",");
        for (String item : items) {
            try {
                list.add(Strings.get(Long.parseLong(item)));
            } catch (NumberFormatException ex) {
                list.add("NOT LONG '" + item + "'");
            }
        }
        return String.join(",", list);
    }
}

