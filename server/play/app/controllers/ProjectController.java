package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;

import play.mvc.*;
import play.data.*;

import static play.data.Form.*;

import models.*;

import javax.inject.Inject;
import javax.persistence.PersistenceException;

import play.db.ebean.Transactional;
import play.libs.Json;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import play.db.ebean.Transactional;

import java.util.List;
import java.util.ArrayList;
import play.Logger;

/**
 * Manage a database of projects.
 */
public class ProjectController extends Controller {

    private FormFactory formFactory;

    @Inject
    public ProjectController(FormFactory formFactory) {
        this.formFactory = formFactory;
    }

    /**
     * Display the list of projects.
     */
    public Result list() {
        return list(false);
    }

    /**
     * Display the list of disabled projects.
     */
    public Result list_disabled() {
        return list(true);
    }

    /**
     * Display the list of active or disabled projects.
     */
    public Result list(boolean disabled) {
        return ok(views.html.project_list.render(Project.list(disabled), Secured.getClient(ctx()), disabled));
    }

    /**
     * Display the 'edit form' of an existing project name.
     *
     * @param id Id of the project to edit
     */
    @Security.Authenticated(Secured.class)
    public Result edit(Long id) {
        Form<InputProject> projectForm = formFactory.form(InputProject.class).fill(new InputProject(Project.find.byId(id)));
        return ok(views.html.project_editForm.render(id, projectForm, Secured.getClient(ctx())));
    }

    /**
     * Handle the 'edit form' submission
     *
     * @param id Id of the project to edit
     */
    public Result update(Long id) throws PersistenceException {
        Form<InputProject> projectForm = formFactory.form(InputProject.class).bindFromRequest();
        if (projectForm.hasErrors()) {
            return badRequest(views.html.project_editForm.render(id, projectForm, Secured.getClient(ctx())));
        }
        try {
            Project savedProject = Project.find.byId(id);
            if (savedProject != null) {
                InputProject newProjectData = projectForm.get();
                savedProject.name = newProjectData.name;
                savedProject.update();

                ProjectEquipmentCollection.addNew(id, getCheckedEquipments(projectForm));
                ProjectNoteCollection.addNew(id, getCheckedNotes(projectForm));

                Logger.info("Project " + savedProject.name + " has been updated");

                Version.inc(Version.VERSION_PROJECT);
            }
        } catch (Exception ex) {
            Logger.error(ex.getMessage());
        }
        return list();
    }

    List<Equipment> getCheckedEquipments(Form<InputProject> projectForm) {
        List<Equipment> equipments = new ArrayList<Equipment>();
        for (Equipment equipment : Equipment.list()) {
            try {
                if (projectForm.field(equipment.name).getValue().get().equals("true")) {
                    equipments.add(equipment);
                }
            } catch (Exception ex) {
            }
        }
        return equipments;
    }

    List<Note> getCheckedNotes(Form<InputProject> projectForm) {
        List<Note> notes = new ArrayList<Note>();
        for (Note note : Note.list()) {
            try {
                if (projectForm.field(note.name).value().get().equals("true")) {
                    notes.add(note);
                }
            } catch (Exception ex) {
            }
        }
        return notes;
    }

    /**
     * Display the 'new project form'.
     */
    @Security.Authenticated(Secured.class)
    public Result create() {
        if (Secured.isAdmin(ctx())) {
            Form<Project> projectForm = formFactory.form(Project.class);
            return ok(views.html.project_createForm.render(projectForm));
        } else {
            return badRequest(views.html.home.render(Secured.getClient(ctx())));
        }
    }

    /**
     * Handle the 'new user form' submission
     */
    @Security.Authenticated(Secured.class)
    @Transactional
    public Result save() {
        Form<Project> projectForm = formFactory.form(Project.class).bindFromRequest();
        if (projectForm.hasErrors() || !Secured.isAdmin(ctx())) {
            return badRequest(views.html.project_createForm.render(projectForm));
        }
        Project project = projectForm.get();
        if (Project.findByName(project.name) != null) {
            return badRequest("Already a project named: " + project.name);
        }
        project.save();
        flash("success", "Project " + projectForm.get().name + " has been created");
        return list();
    }

    /**
     * Display a form to enter in many projects at once.
     */
    @Security.Authenticated(Secured.class)
    public Result createMany() {
        if (Secured.isAdmin(ctx())) {
            Form<InputLines> linesForm = formFactory.form(InputLines.class);
            return ok(views.html.projects_createForm.render(linesForm));
        } else {
            return badRequest(views.html.home.render(Secured.getClient(ctx())));
        }
    }

    /**
     * Create many projects at once.
     */
    @Transactional
    @Security.Authenticated(Secured.class)
    public Result saveMany() {
        Form<InputLines> linesForm = formFactory.form(InputLines.class).bindFromRequest();
        if (linesForm.hasErrors() || !Secured.isAdmin(ctx())) {
            return badRequest(views.html.projects_createForm.render(linesForm));
        }
        String[] lines = linesForm.get().getLines();
        for (String name : lines) {
            name = name.trim();
            if (!name.isEmpty()) {
                if (Project.findByName(name) == null) {
                    Project newProject = new Project();
                    newProject.name = name;
                    newProject.save();
                } else {
                    Logger.info("Already created: " + name);
                }
            }
        }
        Version.inc(Version.VERSION_PROJECT);
        return list();
    }

    /**
     * Handle project deletion
     */
    @Transactional
    @Security.Authenticated(Secured.class)
    public Result delete(Long id) {
        if (!Secured.isAdmin(ctx())) {
            return badRequest(views.html.home.render(Secured.getClient(ctx())));
        }
        if (Entry.hasEntryForProject(id)) {
            Project project = Project.find.byId(id);
            project.disabled = true;
            project.update();
            Logger.info("Project has been disabled: it had entries: " + project.name);
        } else {
            try {
                Project.find.ref(id).delete();
                Logger.info("Project has been deleted");
            } catch (Exception ex) {
                Logger.error(ex.getMessage());
                Project project = Project.find.byId(id);
                project.disabled = true;
                project.update();
                Logger.info("Project has been disabled: it could not be deleted: " + project.name);
            }
        }
        Version.inc(Version.VERSION_PROJECT);
        return list();
    }

    public Result query() {
        ObjectNode top = Json.newObject();
        ArrayNode array = top.putArray("projects");
        for (Project project : Project.find.all()) {
            ObjectNode node = array.addObject();
            node.put("id", project.id);
            node.put("name", project.name);
            node.put("disabled", project.disabled);
        }
        return ok(top);
    }

    @Security.Authenticated(Secured.class)
    @Transactional
    public Result enable(Long id) {
        if (!Secured.isAdmin(ctx())) {
            return badRequest(views.html.home.render(Secured.getClient(ctx())));
        }
        Project project = Project.find.byId(id);
        project.disabled = false;
        project.update();
        Version.inc(Version.VERSION_PROJECT);
        return list();
    }

}

