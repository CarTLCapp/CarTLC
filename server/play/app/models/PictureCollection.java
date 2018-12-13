/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package models;

import java.util.*;

import javax.persistence.*;

import com.avaje.ebean.Model;

import play.data.validation.*;
import modules.AmazonHelper;

import java.io.File;

/**
 * Groups of pictures entity managed by Ebean
 */
@Entity
public class PictureCollection extends Model {

    private static final long serialVersionUID = 1L;

    @Id
    public Long id;

    @Constraints.Required
    public Long collection_id;

    @Constraints.Required
    public String picture;

    @Constraints.Required
    public String note;

    public static Finder<Long, PictureCollection> find = new Finder<Long, PictureCollection>(PictureCollection.class);

    public static List<PictureCollection> list() {
        return find.all();
    }

    public static List<PictureCollection> findByCollectionId(long collection_id) {
        return find.where()
                .eq("collection_id", collection_id)
                .findList();
    }

    public static List<PictureCollection> findByPictureName(String picture) {
        return find.where()
                .like("picture", picture)
                .findList();
    }

    public static void deleteByCollectionId(long collection_id, AmazonHelper.DeleteAction amazonAction) {
        List<PictureCollection> items = find.where()
                .eq("collection_id", collection_id)
                .findList();
        ArrayList<String> files = new ArrayList<>();
        for (PictureCollection item : items) {
            files.add(item.picture);
            item.delete();
        }
        if (amazonAction != null) {
            amazonAction.deleteLocalFile(true).delete(files);
        }
    }

    public static List<PictureCollection> findNoEntries() {
        List<PictureCollection> missing = new ArrayList<>();
        for (PictureCollection collection : list()) {
            if (!Entry.hasEntryForPictureCollectionId(collection.collection_id)) {
                missing.add(collection);
            }
        }
        return missing;
    }

    public boolean hasNote() {
        return note != null && !note.isEmpty();
    }

    @Override
    public String toString() {
        return "PictureCollection{" +
                "id=" + id +
                ", collection_id=" + collection_id +
                ", picture='" + picture + '\'' +
                ", note='" + note + '\'' +
                '}';
    }
}

