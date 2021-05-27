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
 * EntryFail entity managed by Ebean
 */
@Entity
public class EntryFail extends com.avaje.ebean.Model {

    private static final long serialVersionUID = 1L;

    private static final String DATE_FORMAT = "yyyy-MM-dd kk:mm zzz";

    @Id
    public Long id;

    @Constraints.Required
    public int tech_id;

    @Formats.DateTime(pattern = DATE_FORMAT)
    public Date report_time;

    @Constraints.Required
    public long entry_id;

    @Constraints.Required
    public String problem_desc;

    @Constraints.Required
    public int times_encountered;

    public static Finder<Long, EntryFail> find = new Finder<Long, EntryFail>(EntryFail.class);

    public static PagedList<EntryFail> list(int page, int pageSize, String sortBy, String order) {
        return find.where()
                .orderBy(sortBy + " " + order)
                .findPagedList(page, pageSize);
    }

    public String getDate() {
        return new TimeHelper().getDate(entry_time, time_zone);
    }

    public String getTime() {
        return new TimeHelper().getTime(entry_time, time_zone);
    }

    public String getDateTime() {
        return new TimeHelper().getDateTime(entry_time, time_zone);
    }

    public static Entry findByDate(int tech_id, Date date) {
        List<Entry> list = find.where()
                .eq("tech_id", tech_id)
                .eq("entry_time", date)
                .findList();
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public String toString(long client_id) {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(id);
        sbuf.append(":");
        sbuf.append(getDate());
        return sbuf.toString();
    }

}

