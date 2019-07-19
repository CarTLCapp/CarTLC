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
    public Long sub_project_id;

    public static Finder<Long, Flow> find = new Finder<Long, Flow>(Flow.class);

    public static List<Flow> list() {
        return find.orderBy("id asc").findList();
    }

    public static Flow get(long id) {
        if (id > 0) {
            return find.byId(id);
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
                '}';
    }
}

