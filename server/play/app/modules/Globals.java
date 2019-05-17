/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package modules;

import javax.inject.*;
import java.util.Calendar;

import models.*;
import play.*;

@Singleton
public class Globals {

    boolean mClearSearch;
    boolean mDidInit = false;

    public Globals() {
    }

    public void checkInit() {
        if (mDidInit) {
            return;
        }
        mDidInit = true;
        Client.initClient();
        Company.saveNames();
        Technician.fixCodeZeros();
        Calendar c1 = Calendar.getInstance();
        if (c1.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
            superInit();
        }
    }

    private void superInit() {
        EntryEquipmentCollection.removeUnused();
        EntryNoteCollection.removeUnused();
        WorkOrder.fixTrucks();
        Logger.info("SUPER INIT DONE");
    }

    public boolean isClearSearch() {
        return mClearSearch;
    }

    public void setClearSearch(boolean flag) {
        mClearSearch = flag;
    }

}