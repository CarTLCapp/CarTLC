package models;

import java.util.*;

import javax.persistence.*;

import play.db.ebean.*;
import play.data.validation.*;
import play.db.ebean.Transactional;
import play.data.format.*;

import com.avaje.ebean.*;

/**
 * Message entity managed by Ebean
 */
@Entity
public class Message extends com.avaje.ebean.Model {

    private static final long serialVersionUID = 1L;

    public static Finder<Long, Message> find = new Finder<Long, Message>(Message.class);

    public static PagedList<Message> list(int page, int pageSize, String sortBy, String order) {
        return
                find.where()
                        .orderBy(sortBy + " " + order)
                        .findPagedList(page, pageSize);
    }

    @Id
    public Long id;

    @Constraints.Required
    public int tech_id;

    @Formats.DateTime(pattern = "yyyy-MM-dd kk:mm")
    public Date log_time;

    @Constraints.Required
    public int code;

    @Constraints.Required
    public String message;

    @Constraints.Required
    public String trace;

    public String getTechName() {
        Client client = Client.find.byId((long) tech_id);
        if (client == null) {
            return "NOT FOUND: " + tech_id;
        }
        return client.fullName();
    }

    public String getCodeName() {
        switch (code) {
            case 0:
                return "";
            case 2:
                return "Verbose";
            case 3:
                return "Debug";
            case 4:
                return "Info";
            case 5:
                return "Warn";
            case 6:
                return "Error";
            case 7:
                return "Assert";
            default:
                return Integer.toString(code);
        }
    }

    public String getTrace() {
        if (trace != null) {
            return trace.replaceAll("\\n", "<br/>");
        }
        return "";
    }
}

