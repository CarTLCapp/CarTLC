/*
 * Copyright 2019-2021, FleetTLC. All rights reserved
 */
package models.flow;

import java.util.*;

import javax.persistence.*;

import play.db.ebean.*;
import play.data.validation.*;
import play.db.ebean.Transactional;
import play.data.format.*;

import models.Project;

/**
 * Flow entity managed by Ebean
 */
@Entity
public class Flow extends com.avaje.ebean.Model {

    private static final long serialVersionUID = 1L;
    private static final int FLAG_TRUCK_NUMBER = 0x01;
    private static final int FLAG_TRUCK_DAMAGE = 0x02;

    @Id
    public Long id;

    @Constraints.Required
    public Long sub_project_id;

    @Constraints.Required
    public int flags;

    public static Finder<Long, Flow> find = new Finder<Long, Flow>(Flow.class);

    public static List<Flow> list() {
        return find.findList();
    }

    private static class SortByName implements Comparator<Flow> {
        public int compare(Flow a, Flow b) {
            int c = a.getRootProjectName().compareTo(b.getRootProjectName());
            if (c != 0) {
                return c;
            }
            return a.getSubProjectName().compareTo(b.getSubProjectName());
        }
    }

    public static List<Flow> listSorted() {
        List<Flow> items = list();
        Collections.sort(items, new SortByName());
        return items;
    }

    public static Flow get(long id) {
        if (id > 0) {
            return find.byId(id);
        }
        return null;
    }

    public static Flow getByProjectId(long project_id) {
        List<Flow> list = find.where()
                .eq("sub_project_id", project_id)
                .findList();
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public List<FlowElement> getFlowElements() {
        return FlowElementCollection.findElementsByFlowId(id);
    }

    public String getRootProjectName() {
        if (sub_project_id != null) {
            Project project = Project.get(sub_project_id);
            if (project != null) {
                return project.getRootProjectName();
            }
        }
        return "";
    }

    public String getSubProjectName() {
        if (sub_project_id != null) {
            Project project = Project.get(sub_project_id);
            if (project != null) {
                return project.name;
            }
        }
        return "";
    }

    public Project getSubProject() {
        return Project.get(sub_project_id);
    }

    public int getChainSize() {
        return FlowElementCollection.getNumElements(id);
    }

    public int getSubFlowCount() {
        return FlowElementCollection.getSubFlowCount(id);
    }

    public String hasTruck() {
        if (hasFlagTruckNumber() || hasFlagTruckDamage()) {
            return "YES";
        }
        return "NO";
    }

    public static boolean hasElements(long id) {
        return FlowElementCollection.getNumElements(id) > 0;
    }

    public static void delete(long id) {
        FlowElementCollection.deleteByFlowId(id);
        Flow.find.ref(id).delete();
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "Flow{" +
                "id=" + id +
                ", sub_project_id=" + sub_project_id +
                ", flags=" + flags +
                '}';
    }

    public boolean hasFlagTruckNumber() {
        return hasFlag(FLAG_TRUCK_NUMBER);
    }

    public boolean hasFlagTruckDamage() {
        return hasFlag(FLAG_TRUCK_DAMAGE);
    }

    public static boolean hasFlagTruckNumber(long flowId) {
        Flow flow = get(flowId);
        if (flow == null) {
            return false;
        }
        return flow.hasFlagTruckNumber();
    }

    public static boolean hasFlagTruckDamage(long flowId) {
        Flow flow = get(flowId);
        if (flow == null) {
            return false;
        }
        return flow.hasFlagTruckDamage();
    }

    public void setFlagTruckNumber(boolean value) {
        if (value) {
            setFlag(FLAG_TRUCK_NUMBER);
        } else {
            clearFlag(FLAG_TRUCK_NUMBER);
        }
    }

    public void setFlagTruckDamage(boolean value) {
        if (value) {
            setFlag(FLAG_TRUCK_DAMAGE);
        } else {
            clearFlag(FLAG_TRUCK_DAMAGE);
        }
    }

    private boolean hasFlag(int flag) {
        return (flags & flag) == flag;
    }

    private void setFlag(int flag) {
        flags |= flag;
    }

    private void clearFlag(int flag) {
        flags &= ~flag;
    }

}

