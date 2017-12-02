package com.cartlc.tracker.data;

import android.content.Context;
import android.text.TextUtils;

import com.cartlc.tracker.R;
import com.cartlc.tracker.etc.TruckStatus;
import com.cartlc.tracker.event.EventRefreshProjects;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by dug on 5/13/17.
 */

public class DataEntry {
    public long                         id;
    public long                         date;
    public DataProjectAddressCombo      projectAddressCombo;
    public DataCollectionEquipmentEntry equipmentCollection;
    public DataPictureCollection        pictureCollection;
    public long                         noteCollectionId;
    public long                         truckId;
    public TruckStatus                  status;
    public int                          serverId;
    public short                        serverErrorCount;
    public boolean                      uploadedMaster;
    public boolean                      uploadedAws;
    public boolean                      hasError;

    public DataEntry() {
    }

    public String getProjectName() {
        return projectAddressCombo.getProjectName();
    }

    public DataProject getProject() {
        return projectAddressCombo.getProject();
    }

    public DataAddress getAddress() {
        return projectAddressCombo.getAddress();
    }

    public String getAddressBlock() {
        DataAddress address = getAddress();
        if (address != null) {
            return address.getBlock();
        }
        return null;
    }

    public String getAddressLine() {
        DataAddress address = getAddress();
        if (address != null) {
            return address.getLine();
        }
        return "Invalid";
    }

    public String getStatus(Context ctx) {
        if (status != null) {
            return status.getString(ctx);
        }
        return TruckStatus.UNKNOWN.getString(ctx);
    }

    // Return all the notes, with values overlaid.
    // Place these values into TableNote as well so they are persisted forward.
    public List<DataNote> getNotesAllWithValuesOverlaid() {
        // These are all the notes.
        List<DataNote> allNotes = getNotesByProject();
        // Get value overrides
        List<DataNote> valueNotes = getNotesWithValuesOnly();
        List<DataNote> result = new ArrayList<>();
        // Add to result notes without values.
        for (DataNote note : allNotes) {
            DataNote valueNote = getNoteFrom(valueNotes, note);
            if (valueNote != null) {
                TableNote.getInstance().update(valueNote);
                result.add(valueNote);
            } else {
                result.add(note);
            }
        }
        return result;
    }

    DataNote getNoteFrom(List<DataNote> list, DataNote check) {
        for (DataNote note : list) {
            if (note.id == check.id) {
                return note;
            }
        }
        return null;
    }

    // Get all the notes as indicated by the project.
    // This will also include any current edits in place as well.
    public List<DataNote> getNotesByProject() {
        return TableCollectionNoteProject.getInstance().getNotes(projectAddressCombo.projectNameId);
    }

    // Return only the notes with values.
    public List<DataNote> getNotesWithValuesOnly() {
        return TableCollectionNoteEntry.getInstance().query(noteCollectionId);
    }

    public String getNotesLine() {
        StringBuilder sbuf = new StringBuilder();
        for (DataNote note : getNotesWithValuesOnly()) {
            if (!TextUtils.isEmpty(note.value)) {
                if (sbuf.length() > 0) {
                    sbuf.append(", ");
                }
                sbuf.append(note.value);
            }
        }
        return sbuf.toString();
    }

    public List<String> getEquipmentNames() {
        if (equipmentCollection != null) {
            return equipmentCollection.getEquipmentNames();
        }
        return null;
    }

    public List<DataEquipment> getEquipment() {
        if (equipmentCollection != null) {
            return equipmentCollection.getEquipment();
        }
        return null;
    }

    public List<DataPicture> getPictures() {
        return pictureCollection.pictures;
    }

    public void saveNotes(long collectionId) {
        noteCollectionId = collectionId;
        TableCollectionNoteEntry.getInstance().save(noteCollectionId, getNotesByProject());
    }

    public void saveNotes() {
        TableCollectionNoteEntry.getInstance().save(noteCollectionId, getNotesByProject());
    }

    public void checkPictureUploadComplete() {
        for (DataPicture item : pictureCollection.pictures) {
            if (!item.uploaded) {
                return;
            }
        }
        uploadedAws = true;
        TableEntry.getInstance().saveUploaded(this);
        EventBus.getDefault().post(new EventRefreshProjects());
    }

    public DataTruck getTruck() {
        return TableTruck.getInstance().query(truckId);
    }

    public String getTruckLine(Context ctx) {
        DataTruck truck = getTruck();
        if (truck != null) {
            return truck.toString();
        }
        return ctx.getString(R.string.error_missing_truck_short);
    }

    public String getEquipmentLine(Context ctx) {
        StringBuilder sbuf = new StringBuilder();
        for (String name : getEquipmentNames()) {
            if (sbuf.length() > 0) {
                sbuf.append(", ");
            }
            sbuf.append(name);
        }
        if (sbuf.length() == 0) {
            sbuf.append(ctx.getString(R.string.status_no_equipment));
        }
        return sbuf.toString();
    }

    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("ID=");
        sbuf.append(id);
        sbuf.append(", ADDRESS=");
        sbuf.append(projectAddressCombo == null ? "NULL" : projectAddressCombo.id);
        sbuf.append(", EQUIPID=");
        sbuf.append(equipmentCollection == null ? "NULL" : equipmentCollection.id);
        sbuf.append(", NOTEID=");
        sbuf.append(noteCollectionId);
        sbuf.append(", TRUCKID=");
        sbuf.append(truckId);
        sbuf.append(", SERVERID=");
        sbuf.append(serverId);
        sbuf.append(", STATUS=");
        sbuf.append(status == null ? "NULL" : status.toString());
        return sbuf.toString();
    }
}
