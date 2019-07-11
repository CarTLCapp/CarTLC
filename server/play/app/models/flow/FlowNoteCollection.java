/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package models.flow;

import java.util.*;

import javax.persistence.*;

import com.avaje.ebean.Model;

import play.data.validation.*;
import play.data.Form;

import modules.AmazonHelper;

import java.io.File;
import models.Note;

/**
 * Groups of notes for a particular flow element managed by Ebean
 */
@Entity
public class FlowNoteCollection extends Model {

    private static final long serialVersionUID = 1L;

    @Id
    public Long id;

    @Constraints.Required
    public Long flow_element_id;

    @Constraints.Required
    public Long note_id;

    public static Finder<Long, FlowNoteCollection> find = new Finder<Long, FlowNoteCollection>(FlowNoteCollection.class);

    public static List<FlowNoteCollection> list() {
        return find.all();
    }

    public static boolean hasNotes(long flow_element_id) {
        return countNotes(flow_element_id) > 0;
    }

    public static int countNotes(long flow_element_id) {
        return find.where()
                .eq("flow_element_id", flow_element_id)
                .findRowCount();
    }

    public static boolean hasNote(long flow_element_id, long note_id) {
        return find.where()
                .eq("flow_element_id", flow_element_id)
                .eq("note_id", note_id)
                .findRowCount() > 0;
    }

    public static List<FlowNoteCollection> findByFlowElementId(long flow_element_id) {
        return find.where()
                .eq("flow_element_id", flow_element_id)
                .findList();
    }

    public static List<Note> findNotesByFlowElementId(long flow_element_id) {
        List<FlowNoteCollection> items = findByFlowElementId(flow_element_id);
        List<Note> list = new ArrayList<Note>();
        for (FlowNoteCollection item : items) {
            list.add(item.getNote());
        }
        return list;
    }

    public static void deleteByFlowElementId(long flow_element_id) {
        List<FlowNoteCollection> items = find.where()
                .eq("flow_element_id", flow_element_id)
                .findList();
        for (FlowNoteCollection item : items) {
            item.delete();
        }
    }

    public Note getNote() {
        return Note.get(note_id);
    }

    public static void process(long flow_element_id, Form flowElementForm) {
        deleteByFlowElementId(flow_element_id);
        for (Note note : Note.list()) {
            if (isTrue(flowElementForm, note.idString())) {
                FlowNoteCollection item = new FlowNoteCollection();
                item.flow_element_id = flow_element_id;
                item.note_id = note.id;
                item.save();
            }
        }
    }

    private static boolean isTrue(Form flowElementForm, String field) {
        try {
            Optional<String> value = flowElementForm.field(field).getValue();
            if (value.isPresent()) {
                if (value.get().equals("true")) {
                    return true;
                }
            }
        } catch (Exception ex) {
        }
        return false;
    }

}

