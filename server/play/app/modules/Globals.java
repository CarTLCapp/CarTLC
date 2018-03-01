package modules;

import javax.inject.*;
import models.*;
import play.*;

@Singleton
public class Globals {

    boolean mClearSearch;

    public Globals() {
        Client.initClient();
        EntryV2.transfer();
        Company.saveNames();
        EntryEquipmentCollection.removeUnused();
        EntryNoteCollection.removeUnused();
        TruckV6.transfer();
        WorkOrder.fixTrucks();
    }

    public boolean isClearSearch() {
        return mClearSearch;
    }

    public void setClearSearch(boolean flag) {
        mClearSearch = flag;
    }

}