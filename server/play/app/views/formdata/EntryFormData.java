package views.formdata;

import models.Entry;

/**
 * Backing class for the login form.
 */
public class EntryFormData {

    public String name = "";
    public String date = "";
    public String project = "";
    public String address = "";
    public String truck = "";
    public String status = "";
    /**
     * Required for form instantiation.
     */
    public EntryFormData(Entry entry) {
        name = entry.getTechName();
        date = entry.getDate();
        project = entry.getProjectLine();
        address = entry.getAddressLine();
        truck = entry.getTruckLine();
        status = entry.getStatus();
    }

    /**
     * Required for form instantiation.
     */
    public EntryFormData() {
    }

}
