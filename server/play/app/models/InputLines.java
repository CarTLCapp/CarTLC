package models;

import java.util.*;
import javax.persistence.*;

import com.avaje.ebean.Model;
import play.data.format.*;
import play.data.validation.*;

import com.avaje.ebean.*;

/**
 * Input many lines at once
 */
public class InputLines extends Model {

    private static final long serialVersionUID = 1L;

    private String mLines = "";

    public InputLines() {
    }

    public InputLines(String lines) {
        if (lines == null) {
            mLines = "";
        }
        mLines = lines;
    }

    public String [] getLines() {
        return mLines.split("\\n");
    }

    public void setLines(String lines) {
        mLines = lines;
    }
}

