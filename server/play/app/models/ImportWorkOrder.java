package models;

import java.util.*;
import javax.persistence.*;

import com.avaje.ebean.Model;
import play.data.format.*;
import play.data.validation.*;

import com.avaje.ebean.*;

public class ImportWorkOrder extends Model {
    private static final long serialVersionUID = 1L;
    public String project;
    public String company;
}

