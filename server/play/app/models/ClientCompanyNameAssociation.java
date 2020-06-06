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
import views.formdata.InputClient;

/**
 * Project entity managed by Ebean
 */
@Entity
public class ClientCompanyNameAssociation extends Model {

    private static final long serialVersionUID = 1L;

    @Id
    public Long id;

    @Constraints.Required
    public Long client_id;

    @Constraints.Required
    public Long company_name_id;

    public static Finder<Long, ClientCompanyNameAssociation> find = new Finder<Long, ClientCompanyNameAssociation>(ClientCompanyNameAssociation.class);

    public static List<ClientCompanyNameAssociation> list() {
        return find.all();
    }

    public static boolean hasCompanyName(long client_id, long company_name_id) {
        List<ClientCompanyNameAssociation> items = find.where()
                .eq("client_id", client_id)
                .eq("company_name_id", company_name_id)
                .findList();
        return items.size() > 0;
    }

    public static String findCompanyNameFor(long client_id) {
        List<String> result = findCompaniesFor(client_id);
        if (result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    public static List<String> findCompaniesFor(long client_id) {
        List<ClientCompanyNameAssociation> items = find.where()
                .eq("client_id", client_id)
                .findList();
        List<String> result = new ArrayList<String>();
        for (ClientCompanyNameAssociation item : items) {
            String name = CompanyName.get(item.company_name_id);
            if (name != null) {
                result.add(name);
            }
        }
        return result;
    }

    public static String getCompanyLine(long client_id) {
        List<ClientCompanyNameAssociation> items = find.where()
                .eq("client_id", client_id)
                .findList();
        StringBuilder sbuf = new StringBuilder();
        for (ClientCompanyNameAssociation item : items) {
            if (sbuf.length() > 0) {
                sbuf.append(", ");
            }
            sbuf.append(CompanyName.get(item.company_name_id));
        }
        return sbuf.toString();
    }

    public static void deleteEntries(long client_id) {
        List<ClientCompanyNameAssociation> items = find.where()
                .eq("client_id", client_id)
                .findList();
        for (ClientCompanyNameAssociation item : items) {
            item.delete();
        }
    }

    public static void process(long client_id, Form entryForm) {
        deleteEntries(client_id);
        for (CompanyName item : CompanyName.list()) {
            if (ClientAssociation.isTrue(entryForm, item.idString())) {
                ClientCompanyNameAssociation entry = new ClientCompanyNameAssociation();
                entry.client_id = client_id;
                entry.company_name_id = item.id;
                entry.save();
            }
        }
    }

}

