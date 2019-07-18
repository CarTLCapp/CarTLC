/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package models.flow;

import java.util.*;

import javax.persistence.*;

import com.avaje.ebean.Model;

import play.data.validation.*;
import modules.AmazonHelper;

import java.io.File;
import models.Note;

import play.db.ebean.Transactional;
/**
 * A list of flow elements for a flow managed by Ebean
 */
@Entity
public class FlowElementCollection extends Model implements Comparable<FlowElementCollection> {

    private static final long serialVersionUID = 1L;

    @Id
    public Long id;

    @Constraints.Required
    public Long flow_id;

    @Constraints.Required
    public Long flow_element_id;

    @Constraints.Required
    public int chain_order;

    public static Finder<Long, FlowElementCollection> find = new Finder<Long, FlowElementCollection>(FlowElementCollection.class);

    public static List<FlowElementCollection> list() {
        return find.all();
    }

    public static FlowElementCollection get(long id) {
        return find.byId(id);
    }

    public static int getNumElements(long flow_id) {
        return find.where().eq("flow_id", flow_id).findRowCount();
    }

    public static List<FlowElementCollection> findByFlowId(long flow_id) {
        return find.where()
                .eq("flow_id", flow_id)
                .orderBy("chain_order asc")
                .findList();
    }

    public static List<FlowElement> findElementsByFlowId(long flow_id) {
        List<FlowElementCollection> items = findByFlowId(flow_id);
        List<FlowElement> list = new ArrayList<FlowElement>();
        for (FlowElementCollection item : items) {
            list.add(item.getFlowElement());
        }
        if (list.size() > 0) {
            list.get(0).isFirst = true;
            list.get(list.size()-1).isLast = true;
        }
        return list;
    }

    @Transactional
    public static void deleteByFlowId(long flow_id) {
        List<FlowElementCollection> items = find.where()
                .eq("flow_id", flow_id)
                .findList();
        for (FlowElementCollection item : items) {
            FlowNoteCollection.deleteByFlowElementId(item.flow_element_id);
            FlowElement element = item.getFlowElement();
            item.delete();
            element.deleteMe();
        }
    }

    public static void deleteByFlowElementId(long flow_element_id) {
        List<FlowElementCollection> items = find.where()
                .eq("flow_element_id", flow_element_id)
                .findList();
        for (FlowElementCollection item : items) {
            item.delete();
        }
    }

    public static boolean hasFlowElement(long flow_id, long flow_element_id) {
        return find.where()
                .eq("flow_id", flow_id)
                .eq("flow_element_id", flow_element_id)
                .findRowCount() > 0;
    }

    public static void create(long flow_id, long flow_element_id) {
        FlowElementCollection flowElementCollection = new FlowElementCollection();
        flowElementCollection.flow_id = flow_id;
        flowElementCollection.flow_element_id = flow_element_id;
        flowElementCollection.chain_order = getNextChainOrder(flow_id);
        flowElementCollection.save();
    }

    private static int getNextChainOrder(long flow_id) {
        List<FlowElementCollection> items = findByFlowId(flow_id);
        int last_chain_order = 0;
        for (FlowElementCollection item : items) {
            if (item.chain_order > last_chain_order) {
                last_chain_order = item.chain_order;
            }
        }
        return last_chain_order + 1;
    }

    public FlowElement getFlowElement() {
        return FlowElement.get(flow_element_id);
    }

    @Transactional
    public static boolean moveUp(long flow_id, long flow_element_id) {
        List<FlowElementCollection> items = findByFlowId(flow_id);
        int element_pos = -1;
        int pos = 0;
        for (FlowElementCollection element : items) {
            if (element.id == flow_element_id) {
                element_pos = pos;
                break;
            }
            pos++;
        }
        if (element_pos < 0) {
            return false;
        }
        if (element_pos <= 0) {
            return false;
        }
        FlowElementCollection moving_element = items.get(element_pos);
        FlowElementCollection previous_element = items.get(element_pos-1);
        int saved_order = moving_element.chain_order;
        moving_element.chain_order = previous_element.chain_order;
        previous_element.chain_order = saved_order;
        moving_element.update();
        previous_element.update();
        return true;
    }

    @Transactional
    public static boolean moveDown(long flow_id, long flow_element_id) {
        List<FlowElementCollection> items = findByFlowId(flow_id);
        int element_pos = -1;
        int pos = 0;
        for (FlowElementCollection element : items) {
            if (element.id == flow_element_id) {
                element_pos = pos;
                break;
            }
            pos++;
        }
        if (element_pos < 0) {
            return false;
        }
        if (element_pos >= items.size()-1) {
            return false;
        }
        FlowElementCollection moving_element = items.get(element_pos);
        FlowElementCollection next_element = items.get(element_pos+1);
        int saved_order = moving_element.chain_order;
        moving_element.chain_order = next_element.chain_order;
        next_element.chain_order = saved_order;
        moving_element.update();
        next_element.update();
        return true;
    }

    @Override
    public int compareTo(FlowElementCollection item) {
        return chain_order - item.chain_order;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof FlowElementCollection) {
            return equals((FlowElementCollection) other);
        }
        return super.equals(other);
    }

    public boolean equals(FlowElementCollection other) {
        return chain_order == other.chain_order;
    }

}

