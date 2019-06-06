/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package models;

import java.util.*;

import javax.persistence.*;

import com.avaje.ebean.Model;

import play.data.format.*;
import play.data.validation.*;
import play.Logger;

import com.avaje.ebean.*;
import play.db.ebean.Transactional;

/**
 * Project entity managed by Ebean
 */
@Entity
public class ClientEquipmentAssociation extends Model {

    private static final long serialVersionUID = 1L;

    @Id
    public Long id;

    @Constraints.Required
    public Long client_id;

    @Constraints.Required
    public Long equipment_id;

    public static Finder<Long, ClientEquipmentAssociation> find = new Finder<Long, ClientEquipmentAssociation>(ClientEquipmentAssociation.class);

    public static List<ClientEquipmentAssociation> list() {
        return find.all();
    }

}

