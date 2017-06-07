package com.cartlc.tracker.data;

import com.cartlc.tracker.app.TBApplication;

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

//    static final DataAddress[] ADDRESSES = {
//            new DataAddress("IMI", "1201 Mason St", "San Francisco", "California"),
//            new DataAddress("Alamo RM", "1155 Battery St", "San Francisco", "California"),
//            new DataAddress("Ozinga", "865 Market St", "San Francisco", "California"),
//            new DataAddress("CW Roberts", "120 4th St", "San Francisco", "California"),
//            new DataAddress("CW Roberts", "2222 Fillmore St", "San Francisco", "California"),
//            new DataAddress("CW Roberts", "499 Bay St", "San Francisco", "California"),
//            new DataAddress("Point RM", "1111 S Figueroa St", "Los Angeles", "California"),
//            new DataAddress("Central Concrete", "111 S Grand Ave", "Los Angeles", "California"),
//            new DataAddress("IMI", "1 S State St", "Chicago", "Illinois"),
//            new DataAddress("Alamo RM", "111 N State St", "Chicago", "Illinois"),
//            new DataAddress("Ozinga", "200 E Ohio St #1", "Chicago", "Illinois"),
//            new DataAddress("Ozinga", "521 N State St", "Chicago", "Illinois"),
//            new DataAddress("CW Roberts", "601 W Dayton St", "Madison", "Wisconsin"),
//            new DataAddress("Point RM", "2424 University Ave", "Madison", "Wisconsin"),
//            new DataAddress("Central Concrete", "2115 King St", "Madison", "Wisconsin"),
//            new DataAddress("IMI", "1001 Howard St", "Detroit", "Michigan"),
//            new DataAddress("Alamo RM", "1425 W Lafayette Blvd", "Detroit", "Michigan"),
//            new DataAddress("Alamo RM", "100 Renaissance Center", "Detroit", "Michigan"),
//            new DataAddress("Alamo RM", "2155 Gratiot Ave", "Detroit", "Michigan")
//    };

    static void AddCollections() {
        TableCollectionEquipmentProject.getInstance().addByName("Five Cubits", Arrays.asList(new String[]{
                "OBC",
                "RDT",
                "Tablet",
                "6 pin Canbus",
                "9 pin Canbus",
                "Green Canbus",
                "External Antenna",
                "Internal Antenna",
                "Speaker Box",
                "Other",
        }));
        TableCollectionEquipmentProject.getInstance().addByName("Digital Fleet", Arrays.asList(new String[]{
                "Tablet",
                "6 pin Canbus",
                "9 pin Canbus",
                "Green Canbus",
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
