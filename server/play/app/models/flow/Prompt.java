/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package models.flow;

import java.util.*;

import javax.persistence.*;

import play.db.ebean.*;
import play.data.validation.*;
import play.db.ebean.Transactional;
import play.data.format.*;

import play.Logger;

/**
 * User entity managed by Ebean
 */
@Entity
public class Prompt extends com.avaje.ebean.Model {

    private static final long serialVersionUID = 1L;

    @Id
    public Long id;

    @Constraints.Required
    public String line;

    public static Finder<Long, Prompt> find = new Finder<Long, Prompt>(Prompt.class);

    public static Prompt get(long id) {
        if (id > 0) {
            return find.byId(id);
        }
        return null;
    }

    public static List<Prompt> list() {
        return find.all();
    }

    public static ArrayList<String> listPrompts() {
        ArrayList<String> prompts = new ArrayList<String>();
        for (Prompt prompt : list()) {
            prompts.add(prompt.line);
        }
        return prompts;
    }

    public static ArrayList<String> listPromptsWithBlank() {
        ArrayList<String> prompts = listPrompts();
        prompts.add(0, "");
        return prompts;
    }

    public static long requestPrompt(String line) {
        if (line == null) {
            return 0L;
        }
        Prompt prompt = getPrompt(line);
        if (prompt == null) {
            prompt = addPrompt(line);
        }
        return prompt.id;
    }

    @Transactional
    public static Prompt getPrompt(String line) {
        if (line == null) {
            return null;
        }
        List<Prompt> items = find.where()
                .eq("line", line)
                .findList();
        if (items.size() == 0) {
            return null;
        }
        if (items.size() > 1) {
            Logger.error("Found more than one line: " + line);
        }
        return items.get(0);
    }

    /**
     * Adds the specified prompt to the DB.
     */
    @Transactional
    private static Prompt addPrompt(String line) {
        Prompt prompt = new Prompt();
        prompt.line = line;
        prompt.save();
        return prompt;
    }

}

