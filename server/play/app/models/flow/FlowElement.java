/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package models.flow;

import java.util.*;

import javax.persistence.*;

import com.avaje.ebean.Model;
import javax.persistence.Transient;

import play.data.validation.*;
import modules.AmazonHelper;

import java.io.File;
import models.Note;

/**
 * A single element describing a flow managed by Ebean
 */
@Entity
public class FlowElement extends Model {

    private static final long serialVersionUID = 1L;

    @Id
    public Long id;

    @Constraints.Required
    public Long toast_id;

    @Constraints.Required
    public Long dialog_id;

    @Constraints.Required
    public Long confirmation_id;

    @Constraints.Required
    public boolean request_image;

    @Constraints.Required
    public boolean generic_note;

    @Transient
    public boolean isFirst = false;

    @Transient
    public boolean isLast = false;

    public static Finder<Long, FlowElement> find = new Finder<Long, FlowElement>(FlowElement.class);

    public static List<FlowElement> list() {
        return find.all();
    }

    public static FlowElement get(long id) {
        return find.byId(id);
    }

    public boolean hasToast() { return toast_id != null && toast_id > 0; }

    public boolean hasDialog() { return dialog_id != null && dialog_id > 0; }

    public boolean hasConfirmation() { return confirmation_id != null && confirmation_id > 0; }

    public boolean hasNotes() { return FlowNoteCollection.hasNotes(id); }

    public Prompt getToast() {
        if (toast_id != null) return Prompt.get(toast_id); else return null;
    }

    public String getToastValue() { return prompt(getToast()); }

    public Prompt getDialog() {
        if (dialog_id != null) return Prompt.get(dialog_id); else return null;
    }

    public String getDialogValue() { return prompt(getDialog()); }

    public Prompt getConfirmation() {
        if (confirmation_id != null) return Prompt.get(confirmation_id); else return null;
    }

    public String getConfirmationValue() { return prompt(getConfirmation()); }

    public List<Note> getNotes() { return FlowNoteCollection.findNotesByFlowElementId(id); }

    public List<Bit> getBits() {
        List<Bit> list = new ArrayList<Bit>();
        if (toast_id != null && toast_id > 0) {
            list.add(new Bit(getId("toast"), "Toast", getToastValue()));
        }
        if (dialog_id != null && dialog_id > 0) {
            list.add(new Bit(getId("dialog"), "Dialog", getDialogValue()));
        }
        if (request_image) {
            list.add(new Bit(null, "Image", null));
        }
        int count = FlowNoteCollection.countNotes(id);
        if (count > 0) {
            if (count == 1) {
                list.add(new Bit(getId("note"), "Note", getNoteLine()));
            } else {
                list.add(new Bit(getId("note"), String.format("%d Notes", count), getNoteLine()));
            }
        }
        if (confirmation_id != null && confirmation_id > 0) {
            list.add(new Bit(getId("confirm"), "Confirm", getConfirmationValue()));
        }
        return list;
    }

    private String getId(String base) {
        return String.format("%s%d", base, id);
    }

    private String getNoteLine() {
        StringBuilder sbuf = new StringBuilder();
        for (Note note : getNotes()) {
            if (sbuf.length() > 0) {
                sbuf.append(" ");
            }
            sbuf.append(note.name);
        }
        return sbuf.toString();
    }

    private String prompt(Prompt prompt) {
        if (prompt == null) {
            return "";
        }
        return prompt.line;
    }

    public static void delete(long elementId) {
        FlowElementCollection.deleteByFlowElementId(elementId);
        FlowNoteCollection.deleteByFlowElementId(elementId);
        FlowElement element = FlowElement.find.ref(elementId);
        element.deleteMe();
    }

    public void deleteMe() {
        Long toast_id = this.toast_id;
        Long dialog_id = this.dialog_id;
        Long confirmation_id = this.confirmation_id;
        
        delete();

        if (toast_id != null && toast_id != 0 && !usingPrompt(toast_id)) {
            Prompt.find.byId(toast_id).delete();
        }
        if (dialog_id != null && dialog_id != 0 && !usingPrompt(dialog_id)) {
            Prompt.find.byId(dialog_id).delete();
        }
        if (confirmation_id != null && confirmation_id != 0 && !usingPrompt(confirmation_id)) {
            Prompt.find.byId(confirmation_id).delete();
        }
    }

    public static boolean usingPrompt(long promptId) {
        return find.where()
                .disjunction()
                .eq("toast_id", promptId)
                .eq("dialog_id", promptId)
                .eq("confirmation_id", promptId)
                .endJunction()
                .findRowCount() > 0;
    }

}

