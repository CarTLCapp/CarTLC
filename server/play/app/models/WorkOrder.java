package models;

import java.util.*;

import javax.persistence.*;

import play.db.ebean.*;
import play.data.validation.*;
import play.db.ebean.Transactional;
import play.data.format.*;

import play.Logger;

/**
 * User entity managed by Ebean
 */
@Entity
public class WorkOrder extends com.avaje.ebean.Model {

    private static final long serialVersionUID = 1L;

    @Id
    public Long id;

    @Constraints.Required
    public long client_id;

    @Constraints.Required
    public long project_id;

    @Constraints.Required
    public long company_id;

    @Constraints.Required
    public long truck_id;

    public static Finder<Long, WorkOrder> find = new Finder<Long, WorkOrder>(WorkOrder.class);

    public static List<WorkOrder> list() {
        return find.all();
    }
}

