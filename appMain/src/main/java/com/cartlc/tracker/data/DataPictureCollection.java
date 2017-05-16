package com.cartlc.tracker.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dug on 5/15/17.
 */

public class DataPictureCollection {
    public long id;
    public List<DataPicture> pictures = new ArrayList();

    public DataPictureCollection(long id) {
        this.id = id;
    }

    public void add(DataPicture picture) {
        pictures.add(picture);
    }

    public void add(String filename) {
        pictures.add(new DataPicture(filename));
    }
}
