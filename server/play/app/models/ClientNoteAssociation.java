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
public class ClientNoteAssociation extends Model {

    private static final long serialVersionUID = 1L;

    @Id
    public Long id;

    @Constraints.Required
    public Long client_id;

    @Constraints.Required
    public Long note_id;

    public static Finder<Long, ClientNoteAssociation> find = new Finder<Long, ClientNoteAssociation>(ClientNoteAssociation.class);

    public static List<ClientNoteAssociation> list() {
        return find.all();
    }

}

