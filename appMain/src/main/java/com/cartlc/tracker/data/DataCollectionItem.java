package com.cartlc.tracker.data;

/**
 * Created by dug on 5/30/17.
 */

public class DataCollectionItem {
    public long id; // row_id
    public long collection_id; // project_id or collection_id
    public long value_id;
    public int  server_id;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DataCollectionItem) {
            return equals((DataCollectionItem) obj);
        }
        return super.equals(obj);
    }

    public boolean equals(DataCollectionItem other) {
        return collection_id == other.collection_id &&
                value_id == other.value_id;
    }
}
