package models;

import java.util.*;

import javax.persistence.*;

import play.db.ebean.*;
import play.data.validation.*;
import play.db.ebean.Transactional;
import play.data.format.*;

import play.Logger;

import com.avaje.ebean.*;

public class WorkOrderSummaryList {

    protected List<WorkOrderSummary> mList;

    public WorkOrderSummaryList() {
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
        mList = ArrayList(ap.values());
    }
    
}

