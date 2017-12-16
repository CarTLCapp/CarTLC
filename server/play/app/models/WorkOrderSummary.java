package models;

import java.util.*;

import javax.persistence.*;

import play.db.ebean.*;
import play.data.validation.*;
import play.db.ebean.Transactional;
import play.data.format.*;

import play.Logger;

import com.avaje.ebean.*;

public class WorkOrderSummary {

    protected int upload_id;
    protected long client_id;
    protected long project_id;
    protected int num_trucks;
    protected HashSet<Long> companyMap = new HashSet<Long>();

    public static List<WorkOrderSummary> list() {
        HashMap<Integer,WorkOrderSummary> map = new HashMap();
        for (WorkOrder work : WorkOrder.list()) {
            WorkOrderSummary summary;
            if (!map.containsKey(work.upload_id)) {
                summary = new WorkOrderSummary();
                summary.upload_id = work.upload_id;
                summary.client_id = work.client_id;
                summary.project_id = work.project_id;
                summary.num_trucks = 0;
                map.put(work.upload_id, summary);
            } else {
                summary = map.get(work.upload_id);
            }
            summary.companyMap.add(work.company_id);
            summary.num_trucks++;
        }
        return new ArrayList(map.values();)
    }

    public int getNumCompanies() {
        return companyMap.size();
    }

    public int getNumTrucks() {
        return num_trucks;
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
}

