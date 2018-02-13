package models;

import java.util.*;
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
    public Date last_modified;
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

    public String getNumTrucks() {
        return Integer.toString(num_trucks);
    }

    public String getLastModified() {
        if (last_modified == null) {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd kk:mm").format(last_modified);
    }

    public String getNumComplete() {
        return Integer.toString(num_complete);
    }

}

