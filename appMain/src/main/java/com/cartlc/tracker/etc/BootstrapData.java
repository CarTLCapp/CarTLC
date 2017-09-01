package com.cartlc.tracker.etc;

import com.cartlc.tracker.app.TBApplication;
import com.cartlc.tracker.data.DataAddress;
import com.cartlc.tracker.data.DataNote;
import com.cartlc.tracker.data.TableAddress;
import com.cartlc.tracker.data.TableCollectionEquipmentProject;
import com.cartlc.tracker.data.TableCollectionNoteProject;
import com.cartlc.tracker.data.TableProjects;

import java.util.Arrays;

/**
 * Created by dug on 5/9/17.
 */

public class BootstrapData {

    public static void Init() {
        if (TableAddress.getInstance().count() == 0) {
            TableAddress.getInstance().add(Arrays.asList(ADDRESSES));
        }
        if (TableProjects.getInstance().count() == 0) {
            AddCollections();
            AddNotes();
        }
    }

    static final DataAddress[] ADDRESSES = {
            new DataAddress("Alamo Concrete"),
            new DataAddress("CW Roberts"),
            new DataAddress("Central Concrete"),
            new DataAddress("IMI"),
            new DataAddress("Ozinga"),
            new DataAddress("Wingra Ready Mix"),
            new DataAddress("Point Ready Mix"),
    };

    static void AddCollections() {
        TableCollectionEquipmentProject.getInstance().addByName("Five Cubits", Arrays.asList(new String[]{
                "OBC",
                "OBC Bracket",
                "RDT",
                "VMX",
                "Tablet",
                "Charging Converter",
                "6 pin Canbus Cable",
                "9 pin Canbus Cable",
                "Green Canbus Cable",
                "ODB II Canbus Cable",
                "GDS Cup",
                "External Antenna",
                "Internal Antenna",
                "External Speaker",
                "Microphone",
                "Tablet",
                "Repair Work",
                "Speaker Box",
                "Other",
        }));
        TableCollectionEquipmentProject.getInstance().addByName("Digital Fleet", Arrays.asList(new String[]{
                "Antenna",
                "Tablet",
                "Modem",
                "JBox",
                "Canbus",
                "Ram Mount/Cradle",
                "Charging Converter",
                "Repair Work",
                "Uninstall",
                "Other",
        }));
        TableCollectionEquipmentProject.getInstance().addByName("Smart Witness", Arrays.asList(new String[]{
                "KP1S",
                "CPI",
                "SVC 1080",
                "Modem",
                "Driver Facing Camera",
                "Back Up Camera",
                "Side Camera 1",
                "Side Camera 2",
                "Other",
        }));
        TableCollectionEquipmentProject.getInstance().addByName("Fed Ex", Arrays.asList(new String[]{
                "KP1S",
                "SVA30",
                "Modem",
                "Mobileye",
                "Backup Sensors",
                "Other",
        }));
        TableCollectionEquipmentProject.getInstance().addByName("Verifi", Arrays.asList(new String[]{
                "Other",
        }));
        TableProjects.getInstance().addTest(TBApplication.OTHER);
    }

    static void AddNotes() {
        TableCollectionNoteProject.getInstance().addByName("Five Cubits", Arrays.asList(new DataNote[]{
                new DataNote("Serial #", DataNote.Type.ALPHANUMERIC),
                new DataNote("IMEI #", DataNote.Type.NUMERIC),
                new DataNote("Other", DataNote.Type.MULTILINE),
        }));
        TableCollectionNoteProject.getInstance().addByName("Digital Fleet", Arrays.asList(new DataNote[]{
                new DataNote("Serial #"),
                new DataNote("IMEI #"),
                new DataNote("Other"),
        }));
        TableCollectionNoteProject.getInstance().addByName("Smart Witness", Arrays.asList(new DataNote[]{
                new DataNote("Serial #"),
                new DataNote("IMEI #"),
                new DataNote("Sim #", DataNote.Type.NUMERIC_WITH_SPACES),
                new DataNote("DRID #", DataNote.Type.TEXT),
                new DataNote("Other"),
        }));
        TableCollectionNoteProject.getInstance().addByName("Fed Ex", Arrays.asList(new DataNote[]{
                new DataNote("Serial #"),
                new DataNote("IMEI #"),
                new DataNote("Sim #"),
                new DataNote("DRID #"),
                new DataNote("Mobileye", DataNote.Type.TEXT),
                new DataNote("Other"),
        }));
        TableCollectionNoteProject.getInstance().addByName("Verifi", Arrays.asList(new DataNote[]{
                new DataNote("Serial #"),
                new DataNote("IMEI #"),
                new DataNote("Other")
        }));
        TableCollectionNoteProject.getInstance().addByName("Other", Arrays.asList(new DataNote[]{
                new DataNote("Serial #"),
                new DataNote("IMEI #"),
                new DataNote("Other")
        }));
    }

}
