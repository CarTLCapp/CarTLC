package models;

import java.util.*;

import javax.persistence.*;

import play.db.ebean.*;
import play.data.validation.*;
import play.db.ebean.Transactional;
import play.data.format.*;

import play.Logger;

import com.avaje.ebean.*;

public class WorkOrderSummaryList extends BaseList<WorkOrderSummary> implements Comparator<WorkOrderSummary>  {

    protected Client client;

    public WorkOrderSummaryList() { super(); }

    public void setClient(Client client) {
        this.client = client;
    }

    protected List<WorkOrderSummary> convert(List<WorkOrder> list) {
        HashMap<Integer,WorkOrderSummary> map = new HashMap<Integer,WorkOrderSummary>();
        ArrayList<WorkOrderSummary> result = new ArrayList<WorkOrderSummary>();
        for (WorkOrder work : list) {
            WorkOrderSummary summary;
            if (!map.containsKey(work.upload_id)) {
                summary = new WorkOrderSummary();
                summary.upload_id = work.upload_id;
                summary.client_id = work.client_id;
                summary.project_id = work.project_id;
                summary.num_trucks = 0;
                map.put(work.upload_id, summary);
                result.add(summary);
            } else {
                summary = map.get(work.upload_id);
            }
            summary.companyMap.add(work.company_id);
            summary.num_trucks++;

            Entry entry = Entry.getFulfilledBy(work);
            if (entry != null) {
                Date date = entry.entry_time;
                if ((summary.last_modified == null) || (date.getTime() > summary.last_modified.getTime())) {
                    summary.last_modified = date;
                }
                if (entry.status == Entry.Status.COMPLETE) {
                    summary.num_complete++;
                }
            }
        }
        return result;
    }

    public int getTotalRowCount() {
        return mComputed.size();
    }

    protected ExpressionList<WorkOrder> getWorkOrders() {
        ExpressionList<WorkOrder> query = WorkOrder.find.where();
        if (client != null && !client.is_admin) {
            query = query.eq("client_id", client.id);
        }
        return query;
    }

    @Override
    protected List<WorkOrderSummary> getOrderedList() {
        ExpressionList<WorkOrder> query = getWorkOrders();
        List<WorkOrder> list = query.orderBy(getOrderBy()).findList();
        return convert(list);
    }

    @Override
    protected List<WorkOrderSummary> getRawList() {
        ExpressionList<WorkOrder> query = getWorkOrders();
        List<WorkOrder> list = query.findList();
        return convert(list);
    }

    @Override
    protected void sort(List<WorkOrderSummary> list) {
        list.sort(this);
    }

    @Override
    protected long getProjectId(WorkOrderSummary obj) {
        return obj.project_id;
    }

    @Override
    protected String getCompanyName(WorkOrderSummary obj) {
        return null;
    }

    public void computeFilters() {
        super.computeFilters(client);
    }

    public int compare(WorkOrderSummary o1, WorkOrderSummary o2) {
        int value;
        if (mNextParameters.sortBy == SortBy.PROJECT) {
            value = o1.getProjectName().compareTo(o2.getProjectName());
        } else if (mNextParameters.sortBy == SortBy.CLIENT) {
            Client l1 = Client.get(o1.client_id);
            Client l2 = Client.get(o2.client_id);
            if (l1 != null && l2 != null && l1.name != null && l2.name != null) {
                value = l1.name.compareTo(l2.name);
            } else if (l1 != null && l1.name != null) {
                value = -1;
            } else if (l2 != null && l2.name != null) {
                value = 1;
            } else {
                value = 0;
            }
        } else {
            value = o1.upload_id - o2.upload_id;
        }
        if (mNextParameters.order == Order.DESC) {
            value *= -1;
        }
        return value;
    }
}

