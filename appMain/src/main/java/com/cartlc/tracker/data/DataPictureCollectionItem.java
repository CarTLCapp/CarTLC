package com.cartlc.tracker.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dug on 5/15/17.
 */

public class DataPictureCollectionItem {
    public long id;
    public List<DataPicture> pictures = new ArrayList();

    public DataPictureCollectionItem(long id) {
        this.id = id;
    }

    public void add(DataPicture picture) {
        pictures.add(picture);
    }
}
