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

    public int upload_id;
    public long client_id;
    public long project_id;
    public int num_trucks;
    public int num_complete;
    public HashSet<Long> companyMap = new HashSet<Long>();
    public Date last_modified;

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

    public String getClientName() {
        Client client = Client.get(client_id);
        if (client != null) {
            return client.name;
        }
        return "";
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

