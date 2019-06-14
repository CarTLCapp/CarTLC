/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package models;

import java.util.*;

import javax.persistence.*;

import com.avaje.ebean.Model;

import play.data.format.*;
import play.data.validation.*;
import play.data.Form;

import play.Logger;

import com.avaje.ebean.*;
import play.db.ebean.Transactional;

/**
 * Project entity managed by Ebean
 */
@Entity
public class ClientAssociation extends Model {

    private static final long serialVersionUID = 1L;

    @Id
    public Long id;

    @Constraints.Required
    public Long client_id;

    @Constraints.Required
    public boolean show_pictures;

    @Constraints.Required
    public boolean show_trucks;

    @Constraints.Required
    public boolean show_all_notes;

    @Constraints.Required
    public boolean show_all_equipments;

    public static Finder<Long, ClientAssociation> find = new Finder<Long, ClientAssociation>(ClientAssociation.class);

    public static List<ClientAssociation> list() {
        return find.all();
    }

    public static void deleteEntries(long client_id) {
        List<ClientAssociation> items = find.where()
                .eq("client_id", client_id)
                .findList();
        for (ClientAssociation item : items) {
            item.delete();
        }
    }

    public static boolean hasShowPictures(long client_id) {
        List<ClientAssociation> items = find.where()
                .eq("client_id", client_id)
                .findList();
        if (items.size() > 0) {
            for (ClientAssociation item : items) {
                if (item.show_pictures) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasShowTrucks(long client_id) {
        List<ClientAssociation> items = find.where()
                .eq("client_id", client_id)
                .findList();
        if (items.size() > 0) {
            for (ClientAssociation item : items) {
                if (item.show_trucks) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasShowAllNotes(long client_id) {
        List<ClientAssociation> items = find.where()
                .eq("client_id", client_id)
                .findList();
        if (items.size() > 0) {
            for (ClientAssociation item : items) {
                if (item.show_all_notes) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasShowAllEquipments(long client_id) {
        List<ClientAssociation> items = find.where()
                .eq("client_id", client_id)
                .findList();
        if (items.size() > 0) {
            for (ClientAssociation item : items) {
                if (item.show_all_equipments) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void process(Client client, Form entryForm) {
        List<ClientAssociation> items = find.where()
                .eq("client_id", client.id)
                .findList();
        ClientAssociation item;
        if (items.size() > 0) {
            item = items.get(0);
            for (int i = 1; i < items.size(); i++) {
                items.get(i).delete();
            }
        } else {
            item = new ClientAssociation();
            item.client_id = client.id;
        }
        item.show_pictures = isTrue(entryForm, "hasShowPictures"); // Note: must match client_editForm.scala.html
        item.show_trucks = isTrue(entryForm, "hasShowTrucks"); // Note: must match client_editForm.scala.html

        if (ClientNoteAssociation.allTrue(entryForm)) {
            item.show_all_notes = true;
            ClientNoteAssociation.deleteEntries(client.id);
        } else {
            item.show_all_notes = false;
            ClientNoteAssociation.process(client.id, entryForm);
        }
        if (ClientEquipmentAssociation.allTrue(entryForm)) {
            item.show_all_equipments = true;
            ClientEquipmentAssociation.deleteEntries(client.id);
        } else {
            item.show_all_equipments = false;
            ClientEquipmentAssociation.process(client.id, entryForm);
        }
        if (item.id == null || item.id == 0) {
            item.save();
        } else {
            item.update();
        }
    }

    public static boolean isTrue(Form entryForm, String field) {
        try {
            Optional<String> value = entryForm.field(field).getValue();
            if (value.isPresent()) {
                if (value.get().equals("true")) {
                    return true;
                }
            }
        } catch (Exception ex) {
        }
        return false;
    }

}

