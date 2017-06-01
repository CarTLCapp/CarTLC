package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import play.mvc.*;
import play.data.*;
import static play.data.Form.*;
import play.Logger;

import models.*;

import javax.inject.Inject;
import javax.persistence.PersistenceException;
import play.db.ebean.Transactional;

import play.libs.Json;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * Manage a database of note.
 */
public class NoteController extends Controller {

    private FormFactory formFactory;

    @Inject
    public NoteController(FormFactory formFactory) {
        this.formFactory = formFactory;
    }

    /**
     * Display the list of notes.
     */
    public Result list() {
        return ok(views.html.note_list.render(Note.list()));
    }

    /**
     * Display the 'edit form' of an existing note name.
     *
     * @param id Id of the note to edit
     */
    public Result edit(Long id) {
        Form<Note> noteForm = formFactory.form(Note.class).fill(Note.find.byId(id));
        return ok(
            views.html.note_editForm.render(id, noteForm)
        );
    }

    /**
     * Handle the 'edit form' submission
     *
     * @param id Id of the note to edit
     */
    public Result update(Long id) throws PersistenceException {
        Form<Note> noteForm = formFactory.form(Note.class).bindFromRequest();
        if (noteForm.hasErrors()) {
            return badRequest(views.html.note_editForm.render(id, noteForm));
        }
        Transaction txn = Ebean.beginTransaction();
        try {
            Note savedNote = Note.find.byId(id);
            if (savedNote != null) {
                Note newNoteData = noteForm.get();
                savedNote.name = newNoteData.name;
                savedNote.type = newNoteData.type;
                savedNote.update();
                flash("success", "Note " + noteForm.get().name + " has been updated");
                txn.commit();

                Version.inc(Version.VERSION_NOTE);
            }
        } finally {
            txn.end();
        }
        return list();
    }

    /**
     * Display the 'new note form'.
     */
    public Result create() {
        Form<Note> noteForm = formFactory.form(Note.class);
        return ok(
                views.html.note_createForm.render(noteForm)
        );
    }

    /**
     * Handle the 'new user form' submission
     */
    public Result save() {
        Form<Note> noteForm = formFactory.form(Note.class).bindFromRequest();
        if (noteForm.hasErrors()) {
            return badRequest(views.html.note_createForm.render(noteForm));
        }
        noteForm.get().save();
        flash("success", "Note " + noteForm.get().name + " has been created");
        return list();
    }

    /**
     * Display a form to enter in many notes at once.
     */
    public Result createMany() {
        Form<InputLines> linesForm = formFactory.form(InputLines.class);
        return ok(
                views.html.notes_createForm.render(linesForm)
        );
    }

    /**
     * Create many notes at once.
     */
    @Transactional
    public Result saveMany() {
        Form<InputLines> linesForm = formFactory.form(InputLines.class).bindFromRequest();
        if (linesForm.hasErrors()) {
            return badRequest(views.html.notes_createForm.render(linesForm));
        }
        String[] lines = linesForm.get().getLines();
        Project activeProject = null;
        for (String name : lines) {
            name = name.trim();
            if (!name.isEmpty()) {
                Project project = Project.findByName(name);
                if (project != null) {
                    activeProject = project;
                } else {
                    Note.Type type = null;
                    int pos = name.indexOf(':');
                    if (pos >= 0) {
                        String typeStr = name.substring(pos+1).trim();
                        name = name.substring(0, pos).trim();
                        type = Note.Type.from(typeStr);
                    }
                    Note note = Note.findByName(name);
                    if (note == null) {
                        if (type == null) {
                            type = Note.Type.TEXT;
                        }
                        note = new Note();
                        note.name = name;
                        note.type = type;
                        note.save();
                    } else if (type != null) {
                        note.type = type;
                        note.save();
                    }
                    if (activeProject != null) {
                        ProjectNoteCollection collection = new ProjectNoteCollection();
                        collection.project_id = activeProject.id;
                        collection.note_id = note.id;

                        if (!ProjectNoteCollection.has(collection)) {
                            collection.save();
                        }
                    }
                }
            }
        }
        Version.inc(Version.VERSION_NOTE);

        return list();
    }

    /**
     * Handle note deletion
     */
    public Result delete(Long id) {
        // TODO: If the client is in the database, mark it as disabled instead.
        Note.find.ref(id).delete();
        Version.inc(Version.VERSION_NOTE);
        flash("success", "Note has been deleted");
        return list();
    }

    @Transactional
    public Result addProject(Long id, Long project_id) {
        ProjectNoteCollection collection = new ProjectNoteCollection();
        collection.project_id = project_id;
        collection.note_id = id;
        if (!ProjectNoteCollection.has(collection)) {
            Version.inc(Version.VERSION_NOTE);
            collection.save();
        }
        return edit(id);
    }

    @Transactional
    public Result removeProject(Long id, Long project_id) {
        ProjectNoteCollection collection = new ProjectNoteCollection();
        collection.project_id = project_id;
        collection.note_id = id;
        collection = ProjectNoteCollection.get(collection);
        if (collection != null) {
            ProjectNoteCollection.find.ref(collection.id).delete();
            Version.inc(Version.VERSION_NOTE);
        }
        return edit(id);
    }

    public Result query() {
        ObjectNode top = Json.newObject();
        ArrayNode array = top.putArray("notes");
        for (Note item : Note.find.all()) {
            if (!item.disabled) {
                ObjectNode node = array.addObject();
                node.put("id", item.id);
                node.put("name", item.name);
                node.put("type", item.type.toString());
            }
        }
        array = top.putArray("project_note");
        for (ProjectNoteCollection item : ProjectNoteCollection.find.all()) {
            ObjectNode node = array.addObject();
            node.put("id", item.id);
            node.put("project_id", item.project_id);
            node.put("note_id", item.note_id);
        }
        return ok(top);
    }


}

