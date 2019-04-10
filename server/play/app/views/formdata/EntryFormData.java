package views.formdata;

import java.util.List;

import models.Entry;
import models.Project;
/**
 * Backing class for the entry form.
 */
public class EntryFormData {

    public String name = "";
    public String date = "";
    public String rootProject = "";
    public String subProject = "";
    public String address = "";
    public String truck = "";
    public String status = "";
    /**
     * Required for form instantiation.
     */
    public EntryFormData(Entry entry) {
        name = entry.getTechName();
        date = entry.getDateTime();
        rootProject = entry.getRootProjectName();
        subProject = entry.getSubProjectName();
        address = entry.getAddressLine();
        truck = entry.getTruckLine();
        status = entry.getStatus();
    }

    /**
     * Required for form instantiation.
     */
    public EntryFormData() {
    }

    public List<String> optionsSubProject() {
        return Project.listSubProjects(rootProject);
    }

}
