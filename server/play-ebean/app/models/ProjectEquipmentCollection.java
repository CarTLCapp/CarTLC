package models;

import java.util.*;
import javax.persistence.*;

import com.avaje.ebean.Model;
import play.data.format.*;
import play.data.validation.*;
import play.Logger;

import com.avaje.ebean.*;

/**
 * Project entity managed by Ebean
 */
@Entity 
public class ProjectEquipmentCollection extends Model {

    private static final long serialVersionUID = 1L;

	@Id
    public Long id;
    
    @Constraints.Required
    public Long project_id;

    @Constraints.Required
    public Long equipment_id;

    public static Finder<Long,ProjectEquipmentCollection> find = new Finder<Long,ProjectEquipmentCollection>(ProjectEquipmentCollection.class);

    public static List<ProjectEquipmentCollection> list() { return find.all(); }

    public static List<Equipment> findEquipments(long project_id) {
        List<ProjectEquipmentCollection> items = find.where()
                .eq("project_id", project_id)
                .findList();
        List<Equipment> list = new ArrayList<Equipment>();
        for (ProjectEquipmentCollection item : items) {
            Equipment equipment = Equipment.find.byId(item.equipment_id);
            if (equipment == null) {
                Logger.error("Could not locate equipment ID " + item.equipment_id);
            } else {
                list.add(equipment);
            }
        }
        return list;
    }

    public static List<Project> findProjects(long equipment_id) {
        List<ProjectEquipmentCollection> items = find.where()
                .eq("equipment_id", equipment_id)
                .findList();
        List<Project> list = new ArrayList<Project>();
        for (ProjectEquipmentCollection item : items) {
            Project project = Project.find.byId(item.project_id);
            if (project == null) {
                Logger.error("Could not locate project ID " + item.project_id);
            } else {
                list.add(project);
            }
        }
        return list;
    }

    public static boolean has(ProjectEquipmentCollection collection) {
        List<ProjectEquipmentCollection> items =
                find.where()
                        .eq("project_id", collection.project_id)
                        .eq("equipment_id", collection.equipment_id)
                        .findList();
        return items.size() > 0;
    }

}

