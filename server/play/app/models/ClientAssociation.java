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
public class ClientAssociation extends Model {

    private static final long serialVersionUID = 1L;

    @Id
    public Long id;

    @Constraints.Required
    public Long client_id;

    @Constraints.Required
    public Long company_name_id;

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

    // ---------
    // COMPANIES
    // ---------

    public static String findCompanyNameFor(long client_id) {
        List<String> result = findCompaniesFor(client_id);
        if (result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    public static List<String> findCompaniesFor(long client_id) {
        List<ClientAssociation> items = find.where()
                .eq("client_id", client_id)
                .findList();
        List<String> result = new ArrayList<String>();
        for (ClientAssociation item : items) {
            String name = CompanyName.get(item.company_name_id);
            if (name != null) {
                result.add(name);
            }
        }
        return result;
    }

    public static String getCompanyLine(long client_id) {
        List<ClientAssociation> items = find.where()
                .eq("client_id", client_id)
                .findList();
        StringBuilder sbuf = new StringBuilder();
        for (ClientAssociation item : items) {
            if (sbuf.length() > 0) {
                sbuf.append(", ");
            }
            sbuf.append(CompanyName.get(item.company_name_id));
        }
        return sbuf.toString();
    }

    public static void deleteEntries(long client_id) {
        List<ClientAssociation> items = find.where()
                .eq("client_id", client_id)
                .findList();
        for (ClientAssociation item : items) {
            item.delete();
        }
    }

    public static void save(long client_id, String companyName) {
        List<ClientAssociation> items = find.where()
                .eq("client_id", client_id)
                .findList();
        long company_name_id = CompanyName.save(companyName);
        if (items.size() > 0) {
            for (ClientAssociation item : items) {
                item.company_name_id = company_name_id;
                item.update();
            }
        } else {
            ClientAssociation entry = new ClientAssociation();
            entry.client_id = client_id;
            entry.company_name_id = company_name_id;
            entry.save();
        }
    }

}

