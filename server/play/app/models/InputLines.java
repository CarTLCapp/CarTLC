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

    public InputLines(String lines) {
        if (lines == null) {
            this.lines = "";
        }
        this.lines = lines;
    }

    public String lines = "";

    public String [] getLines() {
        return lines.split("\\n");
    }

    public void setLines(String lines) {
        this.lines = lines;
    }
}

