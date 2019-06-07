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

    public static boolean hasEquipment(long client_id, long equipment_id) {
        List<ClientEquipmentAssociation> items = find.where()
                .eq("client_id", client_id)
                .eq("equipment_id", equipment_id)
                .findList();
        return items.size() > 0;
    }

    public static void deleteEntries(long client_id) {
        List<ClientEquipmentAssociation> items = find.where()
                .eq("client_id", client_id)
                .findList();
        for (ClientEquipmentAssociation item : items) {
            item.delete();
        }
    }

    public static List<Equipment> getEquipments(long client_id) {
        List<ClientEquipmentAssociation> items = find.where()
                .eq("client_id", client_id)
                .findList();
        ArrayList<Equipment> result = new ArrayList<Equipment>();
        for (ClientEquipmentAssociation item : items) {
            Equipment equipment = Equipment.find.byId(item.equipment_id);
            result.add(equipment);
        }
        return result;
    }

    public static void process(long client_id, Form entryForm) {
        deleteEntries(client_id);
        for (Equipment equipment : Equipment.list()) {
            if (ClientAssociation.isTrue(entryForm, equipment.idString())) {
                ClientEquipmentAssociation item = new ClientEquipmentAssociation();
                item.client_id = client_id;
                item.equipment_id = equipment.id;
                item.save();
            }
        }
    }

    public static boolean allTrue(Form entryForm) {
        for (Equipment equipment : Equipment.list()) {
            if (!ClientAssociation.isTrue(entryForm, equipment.idString())) {
                return false;
            }
        }
        return true;
    }

}

