package models;

import java.util.*;
import javax.persistence.*;

import com.avaje.ebean.Model;
import play.data.format.*;
import play.data.validation.*;

import com.avaje.ebean.*;

public class InputClient extends Model {
    private static final long serialVersionUID = 1L;
    public String name;
    public String password;
    public String company;

    public InputClient(Client client) {
        name = client.name;
        password = client.password;
        company = ClientCompanyNameAssociation.findCompanyNameFor(client.id);
    }

    public InputClient() {

    }
}

