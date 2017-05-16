package com.cartlc.tracker.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dug on 5/15/17.
 */

public class DataPictureCollection {
    public long id;
    public List<String> pictures = new ArrayList();

    public DataPictureCollection(long id) {
        this.id = id;
    }

    public void add(String pictureFilename) {
        pictures.add(pictureFilename);
    }

    public void add(List<String> pictures) {
        pictures.addAll(pictures);
    }
}
