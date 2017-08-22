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

    public String projects;

    public String[] getProjectLines() {
        return projects.split("\\n");
    }

    public static InputClient find(long client_id) {
        Client client = Client.find.byId(client_id);
        if (client == null) {
            return null;
        }
        InputClient input = new InputClient();
        input.name = client.name;
        input.password = client.password;
        input.projects = client.getProjectsBlock();
        return input;
    }

    public List<Project> getProjectsFromLine() throws DataErrorException {
        List<Project> projects = new ArrayList<Project>();
        String[] lines = this.projects.split("\\n");
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty()) {
                Project project = Project.findByName(line);
                if (project == null) {
                    throw new DataErrorException("No such project named: " + line);
                }
                projects.add(project);
            }
        }
        return projects;
    }
}

