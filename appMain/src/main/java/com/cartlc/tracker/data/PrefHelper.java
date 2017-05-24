package com.cartlc.tracker.data;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by dug on 4/17/17.
 */

public class PrefHelper extends PrefHelperBase {

    static PrefHelper sInstance;

    public static PrefHelper getInstance() {
        return sInstance;
    }

    public static void Init(Context ctx) {
        new PrefHelper(ctx);
    }

    static public final String KEY_PROJECT                      = "project";
    static public final String KEY_COMPANY                      = "company";
    static public final String KEY_STREET                       = "street";
    static public final String KEY_STATE                        = "state";
    static public final String KEY_CITY                         = "city";
    static final        String KEY_CURRENT_PROJECT_GROUP_ID     = "current_project_group_id";
    static final        String KEY_SAVED_PROJECT_GROUP_ID       = "saved_project_group_id";
    static final        String KEY_FIRST_NAME                   = "first_name";
    static final        String KEY_LAST_NAME                    = "last_name";
    static final        String KEY_TRUCK_NUMBER                 = "truck_number";
    static final        String KEY_NEXT_EQUIPMENT_COLLECTION_ID = "next_equipment_collection_id";
    static final        String KEY_NEXT_PICTURE_COLLECTION_ID   = "next_picture_collection_id";
    static final        String KEY_TECH_ID                      = "tech_id";

    static final String PICTURE_DATE_FORMAT = "yy-MM-dd_HH:mm:ss";

    PrefHelper(Context ctx) {
        super(ctx);
        sInstance = this;
    }

    public long getTechID() {
        return getLong(KEY_TECH_ID, 0);
    }

    public void setTechID(long id) {
        setLong(KEY_TECH_ID, id);
    }

    public String getStreet() {
        return getString(KEY_STREET, null);
    }

    public void setStreet(String value) {
        setString(KEY_STREET, value);
    }

    public String getState() {
        return getString(KEY_STATE, null);
    }

    public void setState(String value) {
        setString(KEY_STATE, value);
    }

    public String getCompany() {
        return getString(KEY_COMPANY, null);
    }

    public void setCompany(String value) {
        setString(KEY_COMPANY, value);
    }

    public String getCity() {
        return getString(KEY_CITY, null);
    }

    public void setCity(String value) {
        setString(KEY_CITY, value);
    }

    public String getProjectName() {
        return getString(KEY_PROJECT, null);
    }

    public Long getProjectId() {
        long projectNameId = TableProjects.getInstance().query(getProjectName());
        if (projectNameId >= 0) {
            return projectNameId;
        }
        return null;
    }

    public void setProject(String value) {
        setString(KEY_PROJECT, value);
    }

    public long getCurrentProjectGroupId() {
        return getLong(KEY_CURRENT_PROJECT_GROUP_ID, -1L);
    }

    public void setCurrentProjectGroupId(long id) {
        setLong(KEY_CURRENT_PROJECT_GROUP_ID, id);
    }

    public long getSavedProjectGroupId() {
        return getLong(KEY_SAVED_PROJECT_GROUP_ID, -1L);
    }

    public void setSavedProjectGroupId(long id) {
        setLong(KEY_SAVED_PROJECT_GROUP_ID, id);
    }

    public void setFirstName(String name) {
        setString(KEY_FIRST_NAME, name);
    }

    public String getFirstName() {
        return getString(KEY_FIRST_NAME, null);
    }

    public void setLastName(String name) {
        setString(KEY_LAST_NAME, name);
    }

    public String getLastName() {
        return getString(KEY_LAST_NAME, null);
    }

    public boolean hasName() {
        return !TextUtils.isEmpty(getFirstName()) && !TextUtils.isEmpty(getLastName());
    }

    public long getTruckNumber() {
        return getLong(KEY_TRUCK_NUMBER, 0);
    }

    public void setTruckNumber(long id) {
        setLong(KEY_TRUCK_NUMBER, id);
    }

    public List<String> addState(List<String> list) {
        return addIfNotFound(list, getState());
    }

    public List<String> addCity(List<String> list) {
        return addIfNotFound(list, getCity());
    }

    public List<String> addCompany(List<String> list) {
        return addIfNotFound(list, getCompany());
    }

    public List<String> addIfNotFound(List<String> list, String element) {
        if (element != null && !list.contains(element)) {
            list.add(element);
            Collections.sort(list);
        }
        return list;
    }

    public DataProjectAddressCombo getCurrentProjectGroup() {
        long projectGroupId = getCurrentProjectGroupId();
        return TableProjectAddressCombo.getInstance().query(projectGroupId);
    }

    public void setupFromCurrentProjectId() {
        DataProjectAddressCombo projectGroup = getCurrentProjectGroup();
        if (projectGroup != null) {
            setProject(projectGroup.getProjectName());
            DataAddress address = projectGroup.getAddress();
            if (address != null) {
                setState(address.state);
                setCity(address.city);
                setCompany(address.company);
                setStreet(address.street);
            }
        }
    }

    public void recoverProject() {
        setCurrentProjectGroupId(getSavedProjectGroupId());
        setupFromCurrentProjectId();
    }

    public void clearCurProject() {
        clearLastEntry();
        setState(null);
        setCity(null);
        setCompany(null);
        setStreet(null);
        setProject(null);
        setSavedProjectGroupId(getCurrentProjectGroupId());
        setCurrentProjectGroupId(-1L);
    }

    public void clearLastEntry() {
        setTruckNumber(0);
        TableEquipment.getInstance().clearChecked();
        TablePendingPictures.getInstance().clear();
        TableNote.getInstance().clearValues();
    }

    public boolean hasCurProject() {
        long projectGroupId = getCurrentProjectGroupId();
        if (projectGroupId < 0) {
            return false;
        }
        DataProjectAddressCombo projectGroup = TableProjectAddressCombo.getInstance().query(projectGroupId);
        if (projectGroup == null) {
            return false;
        }
        return true;
    }

    public boolean saveNewProjectIfNeeded() {
        String state   = getState();
        String street  = getStreet();
        String city    = getCity();
        String company = getCompany();
        String project = getProjectName();
        if (TextUtils.isEmpty(project) || TextUtils.isEmpty(state) || TextUtils.isDigitsOnly(street)
                || TextUtils.isEmpty(city) || TextUtils.isEmpty(company)) {
            return false;
        }
        long addressId = TableAddress.getInstance().queryAddressId(company, street, city, state);
        if (addressId < 0) {
            DataAddress address = new DataAddress(company, street, city, state);
            addressId = TableAddress.getInstance().add(address);
        }
        long projectNameId = TableProjects.getInstance().query(project);
        if (addressId >= 0 && projectNameId >= 0) {
            long projectGroupId = TableProjectAddressCombo.getInstance().queryProjectGroupId(projectNameId, addressId);
            if (projectGroupId < 0) {
                projectGroupId = TableProjectAddressCombo.getInstance().add(new DataProjectAddressCombo(projectNameId, addressId));
            }
            setCurrentProjectGroupId(projectGroupId);
            return true;
        }
        return false;
    }

    public void setCurrentProjectGroup(DataProjectAddressCombo group) {
        setCurrentProjectGroupId(group.id);
        setProject(group.getProjectName());
        setAddress(group.getAddress());
    }

    void setAddress(DataAddress address) {
        setCompany(address.company);
        setStreet(address.street);
        setCity(address.city);
        setState(address.state);
    }

    public long getNextEquipmentCollectionID() {
        return getLong(KEY_NEXT_EQUIPMENT_COLLECTION_ID, 0L);
    }

    public void incNextEquipmentCollectionID() {
        setLong(KEY_NEXT_EQUIPMENT_COLLECTION_ID, getNextEquipmentCollectionID() + 1);
    }

    public long getNextPictureCollectionID() {
        return getLong(KEY_NEXT_PICTURE_COLLECTION_ID, 0L);
    }

    public void incNextPictureCollectionID() {
        setLong(KEY_NEXT_PICTURE_COLLECTION_ID, getNextPictureCollectionID() + 1);
    }

    public DataEntry createEntry() {
        long projectGroupId = getCurrentProjectGroupId();
        if (projectGroupId < 0) {
            return null;
        }
        DataProjectAddressCombo projectGroup = TableProjectAddressCombo.getInstance().query(projectGroupId);
        if (projectGroup == null) {
            return null;
        }
        DataEntry entry = new DataEntry();
        entry.projectNameId = projectGroup.projectNameId;
        entry.equipmentCollection = new DataEquipmentEntryCollection(getNextEquipmentCollectionID());
        entry.equipmentCollection.addChecked();
        entry.pictureCollection = TablePendingPictures.getInstance().createCollection();
        entry.addressId = projectGroup.addressId;
        entry.truckNumber = getTruckNumber();
        entry.saveNotes();
        entry.date = System.currentTimeMillis();
        return entry;
    }

    public String genPictureFilename() {
        long          tech_id    = getTechID();
        long          project_id = getProjectId();
        StringBuilder sbuf       = new StringBuilder();
        sbuf.append("picture_");
        sbuf.append(tech_id);
        sbuf.append("_");
        sbuf.append(project_id);
        sbuf.append("_");
        SimpleDateFormat fmt = new SimpleDateFormat(PICTURE_DATE_FORMAT);
        sbuf.append(fmt.format(new Date(System.currentTimeMillis())));
        sbuf.append(".jpg");
        return sbuf.toString();
    }

    public File genFullPictureFile() {
        return new File(mCtx.getExternalFilesDir(Environment.DIRECTORY_PICTURES), genPictureFilename());
    }
}
