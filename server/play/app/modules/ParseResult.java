/**
 * Copyright 2021, FleetTLC. All rights reserved
 */
package modules;

import play.Logger;
import java.util.ArrayList;
import models.Entry;
import org.apache.commons.text.StringEscapeUtils;

public class ParseResult {

    public Entry entry;
    public ArrayList<String> missing = new ArrayList<String>();
    public boolean retServerId = false;
    public boolean fatal = false;
    public String errorMsg = null;
    public int secondary_tech_id = 0;

    public ParseResult() {
    }

    public ParseResult(String err) {
        errorMsg = err;
    }
}
