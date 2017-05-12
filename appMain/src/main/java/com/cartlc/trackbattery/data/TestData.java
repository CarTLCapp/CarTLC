package com.cartlc.trackbattery.data;

import java.util.Arrays;

/**
 * Created by dug on 5/9/17.
 */

public class TestData {

    public static void Init() {
        if (TableProjectGroups.getInstance().count() == 0) {
            TableProjects.getInstance().add(Arrays.asList(PROJECTS));
        }
        if (TableAddress.getInstance().count() == 0) {
            TableAddress.getInstance().add(Arrays.asList(ADDRESSES));
        }
        if (TableEquipment.getInstance().count() == 0) {
            TableEquipment.getInstance().add(Arrays.asList(EQUIPMENT));
        }
    }

    static final String[] PROJECTS = {
            "Five Cubits",
            "Digital Fleet",
            "Smart Witness",
            "Fed Ex",
            "Other",
            "Verifi"
    };

    static final DataAddress[] ADDRESSES = {
            new DataAddress("IMI", "1201 Mason St", "San Francisco", "California"),
            new DataAddress("Alamo RM", "1155 Battery St", "San Francisco", "California"),
            new DataAddress("Ozinga", "865 Market St", "San Francisco", "California"),
            new DataAddress("CW Roberts", "120 4th St", "San Francisco", "California"),
            new DataAddress("CW Roberts", "2222 Fillmore St", "San Francisco", "California"),
            new DataAddress("CW Roberts", "499 Bay St", "San Francisco", "California"),
            new DataAddress("Point RM", "1111 S Figueroa St", "Los Angeles", "California"),
            new DataAddress("Central Concrete", "111 S Grand Ave", "Los Angeles", "California"),
            new DataAddress("IMI", "1 S State St", "Chicago", "Illinois"),
            new DataAddress("Alamo RM", "111 N State St", "Chicago", "Illinois"),
            new DataAddress("Ozinga", "200 E Ohio St #1", "Chicago", "Illinois"),
            new DataAddress("Ozinga", "521 N State St", "Chicago", "Illinois"),
            new DataAddress("CW Roberts", "601 W Dayton St", "Madison", "Wisconsin"),
            new DataAddress("Point RM", "2424 University Ave", "Madison", "Wisconsin"),
            new DataAddress("Central Concrete", "2115 King St", "Madison", "Wisconsin"),
            new DataAddress("IMI", "1001 Howard St", "Detroit", "Michigan"),
            new DataAddress("Alamo RM", "1425 W Lafayette Blvd", "Detroit", "Michigan"),
            new DataAddress("Alamo RM", "100 Renaissance Center", "Detroit", "Michigan"),
            new DataAddress("Alamo RM", "2155 Gratiot Ave", "Detroit", "Michigan"),
    };

    static final DataEquipment[] EQUIPMENT = {
            new DataEquipment("Five Cubits", "OBC"),
            new DataEquipment("Five Cubits", "RDT"),
            new DataEquipment("Five Cubits", "VMX"),
            new DataEquipment("Five Cubits", "6 pin Canbus"),
            new DataEquipment("Five Cubits", "9 pin Canbus"),
            new DataEquipment("Five Cubits", "Green Canbus"),
            new DataEquipment("Five Cubits", "External Antenna"),
            new DataEquipment("Five Cubits", "Internal Antenna"),
            new DataEquipment("Five Cubits", "Other"),
            new DataEquipment("Five Cubits", "Uninstall"),
            new DataEquipment("Five Cubits", "Repair Work"),
            new DataEquipment("Digital Fleet", "Tablet"),
            new DataEquipment("Digital Fleet", "Canbus"),
            new DataEquipment("Digital Fleet", "Other"),
            new DataEquipment("Digital Fleet", "Uninstall"),
            new DataEquipment("Digital Fleet", "Repair Work"),
            new DataEquipment("Smart Witness", "KP1S"),
            new DataEquipment("Smart Witness", "CPI"),
            new DataEquipment("Smart Witness", "SVC 1080"),
            new DataEquipment("Smart Witness", "Modem"),
            new DataEquipment("Smart Witness", "Driver Facing Camera"),
            new DataEquipment("Smart Witness", "Back Up Camera"),
            new DataEquipment("Smart Witness", "Side Camera 1"),
            new DataEquipment("Smart Witness", "Side Camera 2"),
            new DataEquipment("Smart Witness", "DVR"),
            new DataEquipment("Smart Witness", "Uninstall"),
            new DataEquipment("Smart Witness", "Other"),
            new DataEquipment("Smart Witness", "Repair Work"),
            new DataEquipment("Fed Ex", "KP1S"),
            new DataEquipment("Fed Ex", "Driver Facing Camera"),
            new DataEquipment("Fed Ex", "Modem"),
            new DataEquipment("Fed Ex", "Mobileye"),
            new DataEquipment("Fed Ex", "Backup Sensors"),
            new DataEquipment("Fed Ex", "Re-Calibrate"),
            new DataEquipment("Fed Ex", "Other"),
            new DataEquipment("Fed Ex", "Uninstall"),
            new DataEquipment("Fed Ex", "Repair Work"),
            new DataEquipment("Verifi", "V3 Full Install"),
            new DataEquipment("Verifi", "V4 Full Install"),
            new DataEquipment("Verifi", "Admix Tank"),
            new DataEquipment("Verifi", "Nozzle"),
            new DataEquipment("Verifi", "FDM Upgrade"),
            new DataEquipment("Verifi", "Crossover Upgrade"),
            new DataEquipment("Verifi", "WTAA Install"),
            new DataEquipment("Verifi", "Commissioning"),
            new DataEquipment("Verifi", "Uninstall"),
            new DataEquipment("Verifi", "Repair Work"),
    };

}
