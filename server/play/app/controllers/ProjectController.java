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
import play.db.ebean.Transactional;

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
     * Display the list of projects.
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
        Form<Project> projectForm = formFactory.form(Project.class).fill(
                Project.find.byId(id)
        );
        return ok(views.html.project_editForm.render(id, projectForm));
    }

    /**
     * Handle the 'edit form' submission
     *
     * @param id Id of the project to edit
     */
    public Result update(Long id) throws PersistenceException {
        Form<Project> projectForm = formFactory.form(Project.class).bindFromRequest();
        if (projectForm.hasErrors()) {
            return badRequest(views.html.project_editForm.render(id, projectForm));
        }
        Transaction txn = Ebean.beginTransaction();
        try {
            Project savedProject = Project.find.byId(id);
            if (savedProject != null) {
                Project newProjectData = projectForm.get();
                savedProject.name = newProjectData.name;
                savedProject.update();
                flash("success", "Project " + projectForm.get().name + " has been updated");
                txn.commit();

                Version.inc(Version.VERSION_PROJECT);
            }
        } finally {
            txn.end();
        }
        return list();
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
    public Result save() {
        Form<Project> projectForm = formFactory.form(Project.class).bindFromRequest();
        if (projectForm.hasErrors()) {
            return badRequest(views.html.project_createForm.render(projectForm));
        }
        projectForm.get().save();
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
    public Result saveMany() {
        Form<InputLines> linesForm = formFactory.form(InputLines.class).bindFromRequest();
        if (linesForm.hasErrors()) {
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
    public Result delete(Long id) {
        if (Entry.hasEntryForProject(id)) {
            Project project = Project.find.ref(id);
            project.disabled = true;
            project.save();
            flash("success", "Project has been disabled: it had entries");
        } else {
            Project.find.ref(id).delete();
            flash("success", "Project has been deleted");
        }
        Version.inc(Version.VERSION_PROJECT);
        return list();
    }

    public Result query() {
        ObjectNode top = Json.newObject();
        ArrayNode array = top.putArray("projects");
        for (Project project : Project.find.all()) {
            if (!project.disabled) {
                ObjectNode node = array.addObject();
                node.put("id", project.id);
                node.put("name", project.name);
            }
        }
        return ok(top);
    }

}

