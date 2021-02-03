/**
 * Copyright 2021, FleetTLC. All rights reserved
 */
package models;

import java.util.*;
import java.io.File;

import javax.persistence.*;

import play.data.validation.*;
import play.data.format.*;
import play.data.Form;

import com.avaje.ebean.*;

import modules.AmazonHelper;
import modules.AmazonHelper.OnDownloadComplete;
import modules.TimeHelper;
import models.flow.*;
import play.Logger;

/**
 * Daily After Action Report entity managed by Ebean
 */
@Entity
public class Daar extends com.avaje.ebean.Model {

    private static final long serialVersionUID = 1L;

    @Id
    public Long id;

    @Constraints.Required
    public int tech_id;

    @Formats.DateTime(pattern = "yyyy-MM-dd kk:mm zzz")
    public Date entry_time;

    @Formats.DateTime(pattern = "yyyy-MM-dd kk:mm zzz")
    public Date start_time;

    @Constraints.Required
    public long project_id;

    @Constraints.Required
    public String project_desc;

    @Constraints.Required
    public String work_completed_desc;

    @Constraints.Required
    public String missed_units_desc;

    @Constraints.Required
    public String issues_desc;

    @Constraints.Required
    public String injuries_desc;

    @Constraints.Required
    public String time_zone;

    @Constraints.Required
    public boolean viewed;

    public static Finder<Long, Daar> find = new Finder<Long, Daar>(Daar.class);

    public static PagedList<Daar> list(int page, int pageSize, String sortBy, String order) {
        return find.where()
                .orderBy(sortBy + " " + order)
                .findPagedList(page, pageSize);
    }

    public static Daar findByDate(int tech_id, Date date) {
        List<Daar> list = find.where()
                .eq("tech_id", tech_id)
                .eq("entry_time", date)
                .findList();
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public String getProjectDesc() {
        if (project_desc == null) {
            return "";
        }
        return project_desc;
    }

    public String getSubProjectName() {
        if (project_id == 0) {
            return "";
        }
        Project project = Project.find.byId(project_id);
        if (project == null) {
            return "?" + project_id;
        }
        if (project.name == null) {
            return "";
        }
        return project.name;
    }

    public String getRootProjectName() {
        if (project_id == 0) {
            return "";
        }
        Project project = Project.find.byId(project_id);
        if (project == null) {
            return "?" + project_id;
        }
        return project.getRootProjectName();
    }

    public String getTechName() {
        Technician tech = null;
        try {
            tech = Technician.find.byId((long) tech_id);
        } catch (Exception ex) {
        }
        if (tech == null) {
            return Technician.RIP;
        }
        return tech.fullName();
    }

    public String getDateTime() {
        return new TimeHelper().getDateTime(entry_time, time_zone);
    }

    public String getStartTimeTomorrow() {
        return new TimeHelper().getDateTime(start_time, time_zone);
    }

    public boolean hasWorkCompleted() {
        return work_completed_desc != null;
    }

    public String getWorkCompleted() {
        if (work_completed_desc == null) {
            return "";
        }
        return work_completed_desc;
    }

    public boolean hasMissedUnits() {
        return missed_units_desc != null;
    }

    public String getMissedUnits() {
        if (missed_units_desc == null) {
            return "";
        }
        return missed_units_desc;
    }

    public boolean hasIssues() {
        return issues_desc != null;
    }

    public String getIssues() {
        if (issues_desc == null) {
            return "";
        }
        return issues_desc;
    }

    public boolean hasInjuries() {
        return injuries_desc != null;
    }

    public String getInjuries() {
        if (injuries_desc == null) {
            return "";
        }
        return injuries_desc;
    }

    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(id);
        sbuf.append(":");
        sbuf.append(getDateTime());
        sbuf.append(",");
        sbuf.append(getTechName());
        if (project_id != 0) {
            sbuf.append(",");
            sbuf.append(getRootProjectName());
            sbuf.append("/");
            sbuf.append(getSubProjectName());
        }
        if (project_desc != null) {
            sbuf.append(", PROJECT='");
            sbuf.append(project_desc);
            sbuf.append("'");
        }
        if (hasWorkCompleted()) {
            sbuf.append(", WORK='");
            sbuf.append(getWorkCompleted());
            sbuf.append("'");
        }
        if (hasMissedUnits()) {
            sbuf.append(", MISSED='");
            sbuf.append(getMissedUnits());
            sbuf.append("'");
        }
        if (hasIssues()) {
            sbuf.append(", ISSUES='");
            sbuf.append(getIssues());
            sbuf.append("'");
        }
        if (hasInjuries()) {
            sbuf.append(", INJURIES='");
            sbuf.append(getInjuries());
            sbuf.append("'");
        }
        sbuf.append(", START=");
        sbuf.append(getStartTimeTomorrow());

        return sbuf.toString();
    }

}

