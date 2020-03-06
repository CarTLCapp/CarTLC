/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package models;

import java.util.*;
import javax.persistence.*;

import play.data.validation.*;
import play.data.format.*;

/**
 * Entry entity managed by Ebean
 */
@Entity 
public class EntryV2 extends com.avaje.ebean.Model {

    private static final long serialVersionUID = 1L;

    @Id
    public Long id;

    @Constraints.Required
    public int tech_id;

    @Formats.DateTime(pattern="yyyy-MM-dd kk:mm")
    public Date entry_time;

    @Constraints.Required
    public long project_id;

    @Constraints.Required
    public long address_id;

    @Constraints.Required
    public long equipment_collection_id;

    @Constraints.Required
    public long picture_collection_id;

    @Constraints.Required
    public long note_collection_id;

    @Constraints.Required
    private int truck_number;

    @Constraints.Required
    private String license_plate;

    public static Finder<Long,EntryV2> find = new Finder<Long,EntryV2>(EntryV2.class);

    // TODO: Once data has been transfered, this code can be removed
    // and the database can be cleaned up by removing this table.
    public static void transfer() {
    }
}

