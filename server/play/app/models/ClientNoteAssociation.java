/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package models;

import java.util.*;

import javax.persistence.*;

import com.avaje.ebean.Model;

import play.data.format.*;
import play.data.validation.*;
import play.data.Form;

import play.Logger;

import com.avaje.ebean.*;

import play.db.ebean.Transactional;

/**
 * Project entity managed by Ebean
 */
@Entity
public class ClientNoteAssociation extends Model {

    private static final long serialVersionUID = 1L;

    @Id
    public Long id;

    @Constraints.Required
    public Long client_id;

    @Constraints.Required
    public Long note_id;

    public static Finder<Long, ClientNoteAssociation> find = new Finder<Long, ClientNoteAssociation>(ClientNoteAssociation.class);

    public static List<ClientNoteAssociation> list() {
        return find.all();
    }

    public static boolean hasNote(long client_id, long note_id) {
        List<ClientNoteAssociation> items = find.where()
                .eq("client_id", client_id)
                .eq("note_id", note_id)
                .findList();
        return items.size() > 0;
    }

    public static void deleteEntries(long client_id) {
        List<ClientNoteAssociation> items = find.where()
                .eq("client_id", client_id)
                .findList();
        for (ClientNoteAssociation item : items) {
            item.delete();
        }
    }

    public static List<Note> getNotes(long client_id) {
        List<ClientNoteAssociation> items = find.where()
                .eq("client_id", client_id)
                .findList();
        ArrayList<Note> result = new ArrayList<Note>();
        for (ClientNoteAssociation item : items) {
            Note note = Note.find.byId(item.note_id);
            result.add(note);
        }
        return result;
    }

    public static void process(long client_id, Form entryForm) {
        deleteEntries(client_id);
        for (Note note : Note.list()) {
            if (ClientAssociation.isTrue(entryForm, note.idString())) {
                ClientNoteAssociation item = new ClientNoteAssociation();
                item.client_id = client_id;
                item.note_id = note.id;
                item.save();
            }
        }
    }

    public static boolean allTrue(Form entryForm) {
        for (Note note : Note.list()) {
            if (!ClientAssociation.isTrue(entryForm, note.idString())) {
                return false;
            }
        }
        return true;
    }

}

