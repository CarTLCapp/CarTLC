/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package controllers;

import play.mvc.*;
import play.data.*;

import static play.data.Form.*;

import models.*;
import views.formdata.RootProjectData;
import views.formdata.InputRootProject;

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
public class RootProjectController extends Controller {

    private FormFactory formFactory;

    @Inject
    public RootProjectController(FormFactory formFactory) {
        this.formFactory = formFactory;
    }

    /**
     * Display the list of root projects.
     */
    public Result list() {
        return list(false);
    }

    /**
     * Display the list of disabled root projects.
     */
    public Result list_disabled() {
        return list(true);
    }

    /**
     * Display the list of active or disabled root projects.
     */
    @Security.Authenticated(Secured.class)
    public Result list(boolean disabled) {
        return ok(views.html.root_project_list.render(RootProject.list(disabled), Secured.getClient(ctx()), disabled));
    }

    public Result LIST() {
        return Results.redirect(routes.RootProjectController.list());
    }

    /**
     * Display the 'edit form' of an existing root project name.
     *
     * @param id Id of the project to edit
     */
    @Security.Authenticated(Secured.class)
    public Result edit(Long id) {
        Form<InputRootProject> projectForm = formFactory.form(InputRootProject.class).fill(new InputRootProject(RootProject.find.byId(id)));
        return ok(views.html.root_project_editForm.render(id, projectForm, Secured.getClient(ctx())));
    }

    /**
     * Handle the 'edit form' submission
     *
     * @param id Id of the project to edit
     */
    public Result update(Long id) throws PersistenceException {
        Form<InputRootProject> projectForm = formFactory.form(InputRootProject.class).bindFromRequest();
        if (projectForm.hasErrors()) {
            return badRequest(views.html.root_project_editForm.render(id, projectForm, Secured.getClient(ctx())));
        }
        try {
            RootProject savedProject = RootProject.find.byId(id);
            if (savedProject != null) {
                InputRootProject newProjectData = projectForm.get();
                savedProject.name = newProjectData.name;
                savedProject.update();

                Logger.info("Root Project " + savedProject.name + " has been updated");

                Version.inc(Version.VERSION_PROJECT);
            }
        } catch (Exception ex) {
            Logger.error(ex.getMessage());
        }
        return LIST();
    }

    /**
     * Display the 'new root project form'.
     */
    @Security.Authenticated(Secured.class)
    public Result create() {
        if (Secured.isAdmin(ctx())) {
            Form<RootProjectData> projectForm = formFactory.form(RootProjectData.class);
            return ok(views.html.root_project_createForm.render(projectForm));
        } else {
            return HomeController.PROBLEM("Only administators can create root projects");
        }
    }

    /**
     * Handle save root project submission
     */
    @Security.Authenticated(Secured.class)
    @Transactional
    public Result save() {
        Form<RootProjectData> projectForm = formFactory.form(RootProjectData.class).bindFromRequest();
        if (projectForm.hasErrors() || !Secured.isAdmin(ctx())) {
            return badRequest(views.html.root_project_createForm.render(projectForm));
        }
        RootProjectData projectData = projectForm.get();
        if (RootProject.findByName(projectData.name) != null) {
            return badRequest("Already a root project named: " + projectData.name);
        }
        RootProject project = new RootProject();
        project.name = projectData.name;
        project.save();
        flash("success", "Root Project " + project.name + " has been created");
        return LIST();
    }

    /**
     * Handle project deletion
     */
    @Transactional
    @Security.Authenticated(Secured.class)
    public Result delete(Long id) {
        if (!Secured.isAdmin(ctx())) {
            return HomeController.PROBLEM("Only administrators can delete root projects");
        }
        if (Entry.hasEntryForRootProject(id)) {
            RootProject project = RootProject.find.byId(id);
            project.disabled = true;
            project.update();
            Logger.info("Root Project has been disabled: it had entries: " + project.name);
        } else {
            try {
                RootProject.find.ref(id).delete();
                Logger.info("Project has been deleted");
            } catch (Exception ex) {
                Logger.error(ex.getMessage());
                RootProject project = RootProject.find.byId(id);
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
            if (rootProject != null) {
                String name = project.name;
                ObjectNode node = array.addObject();
                node.put("id", project.id);
                node.put("disabled", project.disabled);
                node.put("root_project", rootProject);
                node.put("sub_project", name);
            }
        }
        return ok(top);
    }

    @Security.Authenticated(Secured.class)
    @Transactional
    public Result enable(Long id) {
        if (!Secured.isAdmin(ctx())) {
            return HomeController.PROBLEM("Only administrators can enable root projects");
        }
        RootProject project = RootProject.find.byId(id);
        project.disabled = false;
        project.update();
        Version.inc(Version.VERSION_PROJECT);
        return list();
    }

    @Security.Authenticated(Secured.class)
    public Result listSubProjects(int index) {
        List<String> rootProjectsWithBlank = RootProject.listNamesWithBlank();
        String selected = rootProjectsWithBlank.get(index);
        List<String> names = Project.listSubProjectNames(selected);
        StringBuilder sbuf = new StringBuilder();
        for (String name : names) {
            if (sbuf.length() > 0) {
                sbuf.append("\n");
            }
            sbuf.append(name);
        }
        return ok(sbuf.toString());
    }

}

