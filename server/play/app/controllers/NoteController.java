package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;

import play.mvc.*;
import play.data.*;

import static play.data.Form.*;

import play.Logger;

import models.*;

import java.util.List;
import java.util.ArrayList;

import javax.inject.Inject;
import javax.persistence.PersistenceException;

import play.db.ebean.Transactional;

import play.libs.Json;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.JsonNode;

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
    @Security.Authenticated(Secured.class)
    public Result list() {
        return list(false);
    }

    @Security.Authenticated(Secured.class)
    public Result list_disabled() {
        return list(true);
    }

    @Security.Authenticated(Secured.class)
    public Result list(boolean disabled) {
        return ok(views.html.note_list.render(Note.list(disabled), Secured.getClient(ctx()), disabled));
    }

    /**
     * Display the 'edit form' of an existing note name.
     *
     * @param id Id of the note to edit
     */
    @Security.Authenticated(Secured.class)
    public Result edit(Long id) {
        Form<Note> noteForm = formFactory.form(Note.class).fill(Note.find.byId(id));
        return ok(views.html.note_editForm.render(id, noteForm));
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
        Note newNoteData = noteForm.get();
        if (Note.hasNoteWithName(newNoteData.name, id)) {
            return badRequest("Already a note named: " + newNoteData.name);
        }
        newNoteData.id = id;
        newNoteData.update();
        Logger.info("Note " + newNoteData.name + " has been updated");
        Version.inc(Version.VERSION_NOTE);
        return list();
    }

    /**
     * Display the 'new note form'.
     */
    @Security.Authenticated(Secured.class)
    public Result create() {
        Form<Note> noteForm = formFactory.form(Note.class);
        return ok(views.html.note_createForm.render(noteForm));
    }

    /**
     * Handle the 'new note form' submission
     */
    public Result save() {
        Form<Note> noteForm = formFactory.form(Note.class).bindFromRequest();
        if (noteForm.hasErrors()) {
            return badRequest(views.html.note_createForm.render(noteForm));
        }
        Client client = Secured.getClient(ctx());
        Note note = noteForm.get();
        if (client != null && client.id > 0) {
            note.created_by = Long.valueOf(client.id).intValue();
            note.created_by_client = true;
        }
        List<Note> notes = Note.findByName(note.name);
        if (notes != null && notes.size() > 0) {
            return badRequest("There is already a note named: " + note.name);
        }
        note.save();
        flash("success", "Note " + note.name + " has been created");
        return list();
    }

    /**
     * Display a form to enter in many notes at once.
     */
    @Security.Authenticated(Secured.class)
    public Result createMany() {
        Form<InputLines> linesForm = formFactory.form(InputLines.class);
        return ok(views.html.notes_createForm.render(linesForm));
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
        final String PROJECT = "Project:";
        String[] lines = linesForm.get().getLines();
        List<Project> activeProjects = new ArrayList<Project>();
        for (String name : lines) {
            name = name.trim();
            if (!name.isEmpty()) {
                if (name.startsWith(PROJECT)) {
                    String line = name.substring(PROJECT.length()).trim();
                    String [] project_names = line.split(",");
                    for (String project_name : project_names) {
                        String use_name = project_name.trim();
                        if (use_name.length() > 0) {
                            Project project = Project.findByName(use_name);
                            if (project != null) {
                                activeProjects.add(project);
                            } else {
                                return badRequest("Could not locate any project with name: '" + use_name + "'");
                            }
                        }
                    }
                    continue;
                }
                if (activeProjects.size() == 0) {
                    return badRequest("First line must being with " + PROJECT + " and then be followed by a comma separated list of projects");
                }
                Note.Type type = null;
                int pos = name.indexOf(':');
                if (pos >= 0) {
                    String typeStr = name.substring(pos + 1).trim();
                    name = name.substring(0, pos).trim();
                    type = Note.Type.from(typeStr);
                    if (type == null) {
                        return badRequest("Invalid note type: '" + typeStr + "'");
                    }
                }
                List<Note> notes = Note.findByName(name);
                Note note;
                if (notes == null | notes.size() == 0) {
                    if (type == null) {
                        type = Note.Type.TEXT;
                    }
                    note = new Note();
                    note.name = name;
                    note.type = type;
                    note.save();
                } else {
                    if (notes.size() > 1) {
                        Logger.error("Found too many notes with name: " + name);
                    }
                    note = notes.get(0);
                    if (type != null) {
                        note.type = type;
                        note.update();
                    }
                }
                for (Project project : activeProjects) {
                    ProjectNoteCollection collection = new ProjectNoteCollection();
                    collection.project_id = project.id;
                    collection.note_id = note.id;
                    if (!ProjectNoteCollection.has(collection)) {
                        collection.save();
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
    @Security.Authenticated(Secured.class)
    public Result delete(Long id) {
        Note note = Note.find.byId(id);
        if (Entry.hasEntryForNote(id)) {
            note.disabled = true;
            note.update();
            Logger.info("Note has been disabled: it had entries: " + note.name);
        } else {
            Logger.info("Note has been deleted: " + note.name);
            note.delete();
        }
        Version.inc(Version.VERSION_NOTE);
        return list();
    }

    @Security.Authenticated(Secured.class)
    @Transactional
    public Result enable(Long id) {
        Note note = Note.find.byId(id);
        note.disabled = false;
        note.update();
        Version.inc(Version.VERSION_NOTE);
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
        JsonNode json = request().body().asJson();
        JsonNode value = json.findValue("tech_id");
        if (value == null) {
            return badRequest("missing field: tech_id");
        }
        int tech_id = value.intValue();
        ObjectNode top = Json.newObject();
        ArrayNode array = top.putArray("notes");
        ArrayList<Long> noteIds = new ArrayList<Long>();
        List<Note> notes = Note.appList();
        for (Note item : notes) {
            ObjectNode node = array.addObject();
            node.put("id", item.id);
            node.put("name", item.name);
            node.put("type", item.type.toString());
            node.put("num_digits", item.num_digits);
            noteIds.add(item.id);
        }
        array = top.putArray("project_note");
        for (ProjectNoteCollection item : ProjectNoteCollection.find.all()) {
            if (noteIds.contains(item.note_id)) {
                if (!Note.isDisabled(item.note_id)) {
                    ObjectNode node = array.addObject();
                    node.put("id", item.id);
                    node.put("project_id", item.project_id);
                    node.put("note_id", item.note_id);
                }
            }
        }
        return ok(top);
    }

}

