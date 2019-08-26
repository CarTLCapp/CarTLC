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
public class FlowElement extends Model implements Comparable<FlowElement> {

    private static final long serialVersionUID = 1L;
    public static final int MAX_LEN = 60;

    @Id
    public Long id;

    @Constraints.Required
    public int line_num;

    @Constraints.Required
    public String prompt;

    @Constraints.Required
    public short prompt_type;

    @Constraints.Required
    public byte request_image;

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

    public boolean hasPrompt() { return prompt != null && prompt.length() > 0; }

    public boolean hasPromptPopup() { return prompt != null && prompt.length() > MAX_LEN; }

    public PromptType getPromptType() { return PromptType.from(prompt_type); }

    public String getPromptTypeName() { return getPromptType().getDesc(); }

    public boolean hasNotes() { return FlowNoteCollection.hasNotes(id); }

    public List<Note> getNotes() { return FlowNoteCollection.findNotesByFlowElementId(id); }

    public int getNumImages() { return (int) request_image; }

    private String getName() {
        StringBuilder sbuf = new StringBuilder();
        PromptType promptType = getPromptType();
        sbuf.append(promptType.getDesc());
        if (hasPrompt()) {
            sbuf.append(": ");
            if (prompt.length() > MAX_LEN) {
                sbuf.append(prompt.substring(0, MAX_LEN));
            } else {
                sbuf.append(prompt);
            }
        }
        return sbuf.toString();
    }

    public String getPromptId() {
        return String.format("prompt%d", id);
    }

    public String getPromptSummary() {
        return prompt.substring(0, MAX_LEN);
    }

    public String getFlags() {
        StringBuilder sbuf = new StringBuilder();
        if (request_image == 1) {
            sbuf.append("Image");
        } else if (request_image > 1) {
            sbuf.append(String.format("%d Images", (int) request_image));
        } else {
            sbuf.append("-");
        }
        return sbuf.toString();
    }

    public String getNotesId(){
        return String.format("notes%d", id);
    }

    public String getNotesSummary() {
        return String.format("%d Notes", FlowNoteCollection.countNotes(id));
    }

    public String getNotesLine() {
        StringBuilder sbuf = new StringBuilder();
        for (Note note : getNotes()) {
            if (sbuf.length() > 0) {
                sbuf.append(", ");
            }
            sbuf.append(note.name);
        }
        return sbuf.toString();
    }

    public static void delete(long elementId) {
        FlowElementCollection.deleteByFlowElementId(elementId);
        FlowNoteCollection.deleteByFlowElementId(elementId);
        FlowElement element = FlowElement.find.ref(elementId);
        element.delete();
    }

    @Override
    public int compareTo(FlowElement item) {
        return line_num - item.line_num;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof FlowElement) {
            return equals((FlowElement) other);
        }
        return super.equals(other);
    }

    public boolean equals(FlowElement other) {
        return line_num == other.line_num;
    }

}

