package views.formdata;

import java.util.List;

import models.Entry;
import models.Project;
import models.Company;
/**
 * Backing class for the entry form.
 */
public class EntryFormData {

    public String name = "";
    public String date = "";
    public String rootProject = "";
    public String subProject = "";
    public String companyName = "";
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
        companyName = entry.getCompany();
        address = entry.getStreetAddress();
        truck = entry.getTruckLine();
        status = entry.getStatus();
    }

    /**
     * Required for form instantiation.
     */
    public EntryFormData() {
    }

    public List<String> optionsSubProject() {
        return Project.listSubProjectNames(rootProject);
    }
    public List<String> optionsStreetAddresses() {
        return Company.listStreetAddresses(companyName);
    }

}
