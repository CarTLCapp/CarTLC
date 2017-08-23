package models;

import java.util.*;
import javax.persistence.*;

import com.avaje.ebean.Model;
import play.data.format.*;
import play.data.validation.*;
import play.Logger;
import modules.AmazonHelper;
import java.io.File;

import com.avaje.ebean.*;

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

    public static Finder<Long,PictureCollection> find = new Finder<Long,PictureCollection>(PictureCollection.class);

    public static List<PictureCollection> list() { return find.all(); }

    public static List<PictureCollection> findByCollectionId(long collection_id) {
        return find.where()
                .eq("collection_id", collection_id)
                .findList();
    }

    public static void deleteByCollectionId(long collection_id, AmazonHelper amazonHelper) {
        List<PictureCollection> items = find.where()
                .eq("collection_id", collection_id)
                .findList();
        for (PictureCollection item : items) {
            File file = amazonHelper.getLocalFile(item.picture);
            file.delete();
            item.delete();
        }
    }

}

