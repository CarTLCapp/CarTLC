/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package models;

import java.util.*;
import javax.persistence.*;

import com.avaje.ebean.Model;
import play.data.format.*;
import play.data.validation.*;
import play.Logger;

import com.avaje.ebean.*;

/**
 * Project entity managed by Ebean
 */
@Entity 
public class ProjectNoteCollection extends Model {

    private static final long serialVersionUID = 1L;

	@Id
    public Long id;
    
    @Constraints.Required
    public Long project_id;

    @Constraints.Required
    public Long note_id;

    public static Finder<Long,ProjectNoteCollection> find = new Finder<Long,ProjectNoteCollection>(ProjectNoteCollection.class);

    public static List<ProjectNoteCollection> list() { return find.all(); }

    public static List<Note> findNotes(long project_id) {
        List<ProjectNoteCollection> items = find.where()
                .eq("project_id", project_id)
                .findList();
        List<Note> list = new ArrayList<Note>();
        for (ProjectNoteCollection item : items) {
            Note note = Note.find.byId(item.note_id);
            if (note == null) {
                Logger.error("Could not locate note ID " + item.note_id);
            } else {
                list.add(note);
            }
        }
        return list;
    }

    public static List<Project> findProjects(long note_id) {
        List<ProjectNoteCollection> items = find.where()
                .eq("note_id", note_id)
                .findList();
        List<Project> list = new ArrayList<Project>();
        for (ProjectNoteCollection item : items) {
            Project project = Project.find.byId(item.project_id);
            if (project == null) {
                Logger.error("Could not locate project ID " + item.project_id);
            } else {
                list.add(project);
            }
        }
        return list;
    }

    public static boolean hasNote(long project_id, long note_id) {
        return find.where()
                .eq("note_id", note_id)
                .eq("project_id", project_id)
                .findRowCount() > 0;
    }

    public static boolean has(ProjectNoteCollection collection) {
        return find.where()
                        .eq("project_id", collection.project_id)
                        .eq("note_id", collection.note_id)
                        .findRowCount() > 0;
    }

    public static ProjectNoteCollection get(ProjectNoteCollection collection) {
        List<ProjectNoteCollection> items =
                find.where()
                        .eq("project_id", collection.project_id)
                        .eq("note_id", collection.note_id)
                        .findList();
        if  (items.size() > 0) {
            return items.get(0);
        }
        return null;
    }

    public static void deleteByProjectId(long project_id) {
        List<ProjectNoteCollection> items = find.where()
                .eq("project_id", project_id)
                .findList();
        for (ProjectNoteCollection item : items) {
            item.delete();
        }
    }

    public static void deleteByNoteId(long note_id) {
        List<ProjectNoteCollection> items = find.where()
                .eq("note_id", note_id)
                .findList();
        for (ProjectNoteCollection item : items) {
            item.delete();
        }
    }

    public static void replace(long project_id, List<Note> notes) {
        deleteByProjectId(project_id);
        for (Note note : notes) {
            ProjectNoteCollection entry = new ProjectNoteCollection();
            entry.project_id = project_id;
            entry.note_id = note.id;
            entry.save();
        }
    }


}

