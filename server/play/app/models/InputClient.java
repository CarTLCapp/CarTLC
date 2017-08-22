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

    public List<Long> projects = new ArrayList<Long>();
//
//    public static InputClient get(long client_id) {
//        return new InputClient(Client.find.byId(client_id));
//    }

    public void addProject(long project_id) {
        if (!projects.contains(project_id)) {
            projects.add(project_id);
        }
    }

    public void removeProject(long project_id) {
        if (projects.contains(project_id)) {
            projects.remove(project_id);
        }
    }

    public boolean hasProject(long project_id) {
        return projects.contains(project_id);
    }

    public List<Project> getProjects() {
        List<Project> projects = new ArrayList<Project>();
        for (Long id : this.projects) {
            Project project = Project.find.byId(id);
            if (project != null) {
                projects.add(project);
            }
        }
        return projects;
    }

}

