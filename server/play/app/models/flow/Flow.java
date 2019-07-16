/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package models.flow;

import java.util.*;

import javax.persistence.*;

import play.db.ebean.*;
import play.data.validation.*;
import play.db.ebean.Transactional;
import play.data.format.*;

import play.Logger;

import models.Project;

/**
 * Flow entity managed by Ebean
 */
@Entity
public class Flow extends com.avaje.ebean.Model {

    private static final long serialVersionUID = 1L;

    @Id
    public Long id;

    @Constraints.Required
    public String name;

    @Constraints.Required
    public Long sub_project_id;

    public static Finder<Long, Flow> find = new Finder<Long, Flow>(Flow.class);

    public static List<Flow> list() {
        return find.orderBy("name asc").findList();
    }

    public static Flow get(long id) {
        if (id > 0) {
            return find.byId(id);
        }
        return null;
    }

    public static Flow getByName(String name) {
        List<Flow> list = find.where().eq("name", name).findList();
        if (list.size() == 0) {
            return null;
        }
        if (list.size() > 1) {
            Logger.error("More than one flow named " + name);
        }
        return list.get(0);
    }

    public static boolean hasFlowName(String name) {
        return find.where().eq("name", name).findRowCount() > 0;
    }

    public static String generateFlowName() {
        int counter;
        for (counter = 1; true; counter++) {
            String name = String.format("Flow %d", counter);
            if (!hasFlowName(name)) {
                return name;
            }
        }
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

    public static boolean hasElements(long id) {
        return FlowElementCollection.getNumElements(id) > 0;
    }

    public static void delete(long id) {
        FlowElementCollection.deleteByFlowId(id);
        Flow.find.ref(id).delete();
    }
}

