/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package models;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.text.SimpleDateFormat;

import javax.persistence.*;

import play.db.ebean.*;
import play.data.validation.*;
import play.db.ebean.Transactional;
import play.data.format.*;

import play.Logger;

import com.avaje.ebean.*;

public class WorkOrderSummary {

    static final int COMPANY_LINE_LIMIT = 63;

    public int upload_id;
    public long client_id;
    public long project_id;
    public int num_trucks;
    public int num_complete;
    public int num_techs;
    public Date first_date;
    public Date last_date;
    private HashSet<String> companyMap = new HashSet<String>();

    public int getUploadId() {
        return upload_id;
    }

    public String getProjectName() {
        Project project = Project.get(project_id);
        if (project != null) {
            return project.name;
        }
        return "";
    }

    public String getCompanyLine() {
        StringBuilder sbuf = new StringBuilder();
        Iterator<String> iterator = companyMap.iterator();
        while (iterator.hasNext()) {
            String company_name = iterator.next();
            if (sbuf.length() > 0) {
                sbuf.append(", ");
            }
            sbuf.append(company_name);
        }
        if (sbuf.length() > COMPANY_LINE_LIMIT) {
            sbuf.setLength(COMPANY_LINE_LIMIT-7);
            sbuf.append("...[");
            sbuf.append(companyMap.size());
            sbuf.append("]");
        }
        return sbuf.toString();
    }

    public String getPercentCompleteLine() {
        StringBuilder sbuf = new StringBuilder();
        if (num_trucks == 0) {
            return "";
        }
        long percent = (long) ((float) num_complete / (float) num_trucks * 100f);
        sbuf.append(percent);
        sbuf.append("% Complete (");
        sbuf.append(num_complete);
        sbuf.append("/");
        sbuf.append(num_trucks);
        sbuf.append(")");
        return sbuf.toString();
    }

    public String getNumTechs() {
        if (num_techs == 0) {
            return "";
        }
        return Integer.toString(num_techs);
    }

    public String getDaysActive() {
        if (first_date == null && last_date == null) {
            return "";
        }
        if (first_date == null || last_date == null) {
            return "1";
        }
        long diff = last_date.getTime() - first_date.getTime();
        if (diff == 0) {
            return "1";
        }
        long oneDay = TimeUnit.MILLISECONDS.convert(1L, TimeUnit.DAYS);
        long days = diff / oneDay + (((diff % oneDay) > 0) ? 1 : 0);
        return Long.toString(days);
    }

    public String getClientName() {
        Client client = Client.get(client_id);
        if (client != null) {
            return client.name;
        }
        return "";
    }

    public void addCompany(long company_id) {
        Company company = Company.get(company_id);
        if (company != null) {
            companyMap.add(company.name);
        }
    }

    public String getNumCompanies() {
        return Integer.toString(companyMap.size());
    }

    public String getLastModified() {
        if (last_date == null) {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd kk:mm").format(last_date);
    }

}

