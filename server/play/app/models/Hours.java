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

import java.text.SimpleDateFormat;

/**
 * Daily Hours Report
 */
@Entity
public class Hours extends com.avaje.ebean.Model {

    private static final long serialVersionUID = 1L;

    @Id
    public Long id;

    @Constraints.Required
    public int tech_id;

    @Formats.DateTime(pattern = "yyyy-MM-dd zzz")
    public Date entry_time;

    @Constraints.Required
    public long project_id;

    @Constraints.Required
    public String project_desc;

    @Constraints.Required
    public int start_time; // minutes into day

    @Constraints.Required
    public int end_time; // minutes into day

    @Constraints.Required
    public int lunch_time; // minutes of day

    @Constraints.Required
    public int break_time; // minutes of day

    @Constraints.Required
    public int drive_time; // minutes of day

    @Constraints.Required
    public String notes;

    @Constraints.Required
    public String time_zone;

    @Constraints.Required
    public boolean viewed;

    public static Finder<Long, Hours> find = new Finder<Long, Hours>(Hours.class);

    public static PagedList<Hours> list(int page, int pageSize, String sortBy, String order) {
        return find.where()
                .orderBy(sortBy + " " + order)
                .findPagedList(page, pageSize);
    }

    public static Hours findByDate(int tech_id, Date date) {
        List<Hours> list = find.where()
                .eq("tech_id", tech_id)
                .eq("entry_time", date)
                .findList();
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
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

    public String getEntryTime() {
        return new TimeHelper().getDateTime(entry_time, time_zone);
    }

    public int getStartMinutesIntoDay() {
        return start_time;
    }

    public String getStartTime() {
        return getTimeOfDayString(start_time);
    }

    public int getEndMinutesIntoDay() {
        return end_time;
    }

    public String getEndTime() {
        return getTimeOfDayString(end_time);
    }

    public int getLunchTimeInMinutes() {
        return lunch_time;
    }

    public String getLunchTime() {
        return getTimeLengthString(lunch_time);
    }

    public int getBreakTimeInMinutes() {
        return break_time;
    }

    public String getBreakTime() {
        return getTimeLengthString(break_time);
    }

    public int getDriveTimeInMinutes() {
        return drive_time;
    }

    public String getDriveTime() {
        return getTimeLengthString(drive_time);
    }

    public String getNotes() {
        return notes;
    }

    private String getTimeOfDayString(int minutesIntoDay) {
        int hourIntoDay = minutesIntoDay / 24; // 0-23
        int minutesOfHour = minutesIntoDay % 60;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hourIntoDay);
        cal.set(Calendar.MINUTE, minutesOfHour);

        return new SimpleDateFormat("hh:mm aa").format(cal.getTime());
    }

    private String getTimeLengthString(int minutesOfDay) {
        int hours = minutesOfDay / 60;
        int minutes = minutesOfDay % 60;
        return String.format("%02d:%02d", hours, minutes);
    }

    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(id);
        sbuf.append(":");
        sbuf.append(getEntryTime());
        sbuf.append(",");
        sbuf.append(getTechName());
        if (project_id != 0) {
            sbuf.append(",");
            sbuf.append(getRootProjectName());
            sbuf.append("/");
            sbuf.append(getSubProjectName());
        }
        sbuf.append(", START=");
        sbuf.append(getStartTime());
        sbuf.append(", END=");
        sbuf.append(getEndTime());
        sbuf.append(", LUNCH=");
        sbuf.append(getLunchTime());
        sbuf.append(", BREAK=");
        sbuf.append(getBreakTime());
        sbuf.append(", DRIVE=");
        sbuf.append(getDriveTime());
        if (viewed) {
            sbuf.append(", VIEWED");
        }
        if (notes != null && notes.length() > 0) {
            sbuf.append(", NOTES='");
            sbuf.append(notes);
            sbuf.append("'");
        }
        return sbuf.toString();
    }

}

