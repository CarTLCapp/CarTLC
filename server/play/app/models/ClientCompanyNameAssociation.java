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

    public static String findCompanyNameFor(long client_id) {
        List<ClientCompanyNameAssociation> items = find.where()
                .eq("client_id", client_id)
                .findList();
        String companyName = null;
        for (ClientCompanyNameAssociation item : items) {
            companyName = CompanyName.get(item.company_name_id);
            if (companyName != null) {
                break;
            }
        }
        return companyName;
    }

    public static void deleteEntries(long client_id) {
        List<ClientCompanyNameAssociation> items = find.where()
                .eq("client_id", client_id)
                .findList();
        for (ClientCompanyNameAssociation item : items) {
            item.delete();
        }
    }

    public static void save(long client_id, String companyName) {
        deleteEntries(client_id);
        ClientCompanyNameAssociation entry = new ClientCompanyNameAssociation();
        entry.client_id = client_id;
        entry.company_name_id = CompanyName.save(companyName);
        entry.save();
    }

}

