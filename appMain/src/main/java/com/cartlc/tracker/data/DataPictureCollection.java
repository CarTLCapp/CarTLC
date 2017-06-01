package com.cartlc.tracker.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dug on 5/15/17.
 */

public class DataPictureCollection {
    public long id; // collection id shared by all pictures.
    public List<DataPicture> pictures = new ArrayList();

    public DataPictureCollection(long id) {
        this.id = id;
    }

    public void add(DataPicture picture) {
        pictures.add(picture);
    }
}
