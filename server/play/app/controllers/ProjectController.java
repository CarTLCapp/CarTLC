/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package controllers;

import play.mvc.*;
import play.data.*;

import static play.data.Form.*;

import models.*;
import views.formdata.InputLines;
import views.formdata.ProjectData;
import views.formdata.InputProject;

import javax.inject.Inject;
import javax.persistence.PersistenceException;

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

    public Result LIST() {
        return Results.redirect(routes.ProjectController.list());
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

                RootProject rootProject = RootProject.findByName(newProjectData.rootProject);
                if (rootProject == null) {
                    return badRequest("could not find root project: " + newProjectData.rootProject);
                }
                savedProject.root_project_id = rootProject.id;
                savedProject.name = newProjectData.name;
                savedProject.update();

                ProjectEquipmentCollection.replace(id, Equipment.getChecked(projectForm));
                ProjectNoteCollection.replace(id, Note.getChecked(projectForm));

                Logger.info("Project " + savedProject.name + " has been updated");

                Version.inc(Version.VERSION_PROJECT);
            }
        } catch (Exception ex) {
            Logger.error(ex.getMessage());
        }
        return LIST();
    }

    /**
     * Display the 'new project form'.
     */
    @Security.Authenticated(Secured.class)
    public Result create() {
        if (Secured.isAdmin(ctx())) {
            Form<ProjectData> projectForm = formFactory.form(ProjectData.class);
            return ok(views.html.project_createForm.render(projectForm));
        } else {
            return HomeController.PROBLEM("Only administators can create projects");
        }
    }

    /**
     * Handle the 'new user form' submission
     */
    @Security.Authenticated(Secured.class)
    @Transactional
    public Result save() {
        Form<ProjectData> projectForm = formFactory.form(ProjectData.class).bindFromRequest();
        if (projectForm.hasErrors() || !Secured.isAdmin(ctx())) {
            return badRequest(views.html.project_createForm.render(projectForm));
        }
        ProjectData projectData = projectForm.get();
        if (Project.findByName(projectData.rootProject, projectData.name) != null) {
            return badRequest("Already a project named: " + projectData.rootProject + " - " + projectData.name);
        }
        RootProject rootProject = RootProject.findByName(projectData.rootProject);
        if (rootProject == null) {
            return badRequest("Could not find root project named: " + projectData.rootProject);
        }
        Project project = new Project();
        project.name = projectData.name;
        project.root_project_id = rootProject.id;
        project.save();
        flash("success", "Project " + rootProject.name + "-" + project.name + " has been created");
        Version.inc(Version.VERSION_PROJECT);
        return LIST();
    }

    /**
     * Display a form to enter in many projects at once.
     */
    @Security.Authenticated(Secured.class)
    public Result createMany() {
        if (Secured.isAdmin(ctx())) {
            Form<InputLines> linesForm = formFactory.form(InputLines.class);
            return ok(views.html.projects_createForm.render(linesForm, Secured.getClient(ctx())));
        } else {
            return HomeController.PROBLEM("Only administrators can create projects");
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
            return badRequest(views.html.projects_createForm.render(linesForm, Secured.getClient(ctx())));
        }
        String[] lines = linesForm.get().getLines();
        for (String name : lines) {
            name = name.trim();
            if (!name.isEmpty()) {
                if (Project.findByName(name) == null) {
                    String [] names = Project.split(name);
                    if (names.length != 2) {
                        return badRequest("Malformed project pair: " + name);
                    }
                    RootProject rootProject = RootProject.findByName(names[0]);
                    if (rootProject == null) {
                        return badRequest("Could not find root project named: " + names[0]);
                    }
                    Project newProject = new Project();
                    newProject.root_project_id = rootProject.id;
                    newProject.name = names[1];
                    newProject.save();
                } else {
                    Logger.info("Already created: " + name);
                }
            }
        }
        Version.inc(Version.VERSION_PROJECT);
        return LIST();
    }

    /**
     * Handle project deletion
     */
    @Transactional
    @Security.Authenticated(Secured.class)
    public Result delete(Long id) {
        if (!Secured.isAdmin(ctx())) {
            return HomeController.PROBLEM("Only administrators can delete projects");
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
        return LIST();
    }

    public Result query() {
        ObjectNode top = Json.newObject();
        ArrayNode array = top.putArray("projects");
        for (Project project : Project.find.all()) {
            String rootProject = project.getRootProjectName();
            if (rootProject == null) {
                String name = project.name;
                ObjectNode node = array.addObject();
                node.put("id", project.id);
                node.put("disabled", project.disabled);
                node.put("name", name);
            }
        }
        return ok(top);
    }

    @Security.Authenticated(Secured.class)
    @Transactional
    public Result enable(Long id) {
        if (!Secured.isAdmin(ctx())) {
            return HomeController.PROBLEM("Only administrators can enable projects");
        }
        Project project = Project.find.byId(id);
        project.disabled = false;
        project.update();
        Version.inc(Version.VERSION_PROJECT);
        return list();
    }

}

