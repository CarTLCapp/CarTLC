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
        return ok(
                views.html.project_list.render(Project.list())
        );
    }

    /**
     * Display the 'edit form' of an existing project name.
     *
     * @param id Id of the project to edit
     */
    public Result edit(Long id) {
        Form<Project> projectForm = formFactory.form(Project.class).fill(
                Project.find.byId(id)
        );
        return ok(
            views.html.project_editForm.render(id, projectForm)
        );
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

                Version.inc(Version.PROJECT);
            }
        } finally {
            txn.end();
        }
        return list();
    }

    /**
     * Display the 'new project form'.
     */
    public Result create() {
        Form<Project> projectForm = formFactory.form(Project.class);
        return ok(
                views.html.project_createForm.render(projectForm)
        );
    }

    /**
     * Handle the 'new user form' submission
     */
    public Result save() {
        Form<Project> projectForm = formFactory.form(Project.class).bindFromRequest();
        if(projectForm.hasErrors()) {
            return badRequest(views.html.project_createForm.render(projectForm));
        }
        projectForm.get().save();
        flash("success", "Project " + projectForm.get().name + " has been created");
        return list();
    }

    /**
     * Display a form to enter in many projects at once.
     */
    public Result createMany() {
        Form<InputLines> linesForm = formFactory.form(InputLines.class);
        return ok(
                views.html.projects_createForm.render(linesForm)
        );
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
        Version.inc(Version.PROJECT);

        return list();
    }

    /**
     * Handle project deletion
     */
    public Result delete(Long id) {
        // TODO: If the client is in the database, mark it as disabled instead.
        Project.find.ref(id).delete();
        Version.inc(Version.PROJECT);
        flash("success", "Project has been deleted");
        return list();
    }


}

