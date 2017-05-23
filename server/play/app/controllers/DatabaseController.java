package controllers;

import javax.inject.*;
import java.sql.*;
import java.lang.StringBuilder;
import play.*;
import play.mvc.*;
import play.db.*;

@Singleton
public class DatabaseController extends Controller {

    private Database db;

    @Inject
    public DatabaseController(Database db) {
        this.db = db;
    }

    public Result test() {
        StringBuilder sbuf = new StringBuilder();
        try {
            Connection conn = db.getConnection();
            Statement stmt = conn.createStatement();
            String sql = "SELECT _id, client_name, client_code FROM clients";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                long row_id = rs.getLong("_id");
                String client_name = rs.getString("client_name");
                String client_code = rs.getString("client_code");
                sbuf.append("ID=");
                sbuf.append(row_id);
                sbuf.append(", NAME=");
                sbuf.append(client_name);
                sbuf.append(", CODE=");
                sbuf.append(client_code);
                sbuf.append("\n");
            }
            conn.close();
        } catch (Exception ex) {
            sbuf.append(ex.getMessage());
        }
        return ok(sbuf.toString());
    }

}
