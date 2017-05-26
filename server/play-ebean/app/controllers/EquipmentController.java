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
 * Manage a database of equipment.
 */
public class EquipmentController extends Controller {

    private FormFactory formFactory;

    @Inject
    public EquipmentController(FormFactory formFactory) {
        this.formFactory = formFactory;
    }

    /**
     * Display the list of equipments.
     */
    public Result list() {
        return ok(views.html.equipment_list.render(Equipment.list()));
    }

    /**
     * Display the 'edit form' of an existing equipment name.
     *
     * @param id Id of the equipment to edit
     */
    public Result edit(Long id) {
        Form<Equipment> equipmentForm = formFactory.form(Equipment.class).fill(Equipment.find.byId(id));
        return ok(
            views.html.equipment_editForm.render(id, equipmentForm)
        );
    }

    /**
     * Handle the 'edit form' submission
     *
     * @param id Id of the equipment to edit
     */
    public Result update(Long id) throws PersistenceException {
        Form<Equipment> equipmentForm = formFactory.form(Equipment.class).bindFromRequest();
        if (equipmentForm.hasErrors()) {
            return badRequest(views.html.equipment_editForm.render(id, equipmentForm));
        }
        Transaction txn = Ebean.beginTransaction();
        try {
            Equipment savedEquipment = Equipment.find.byId(id);
            if (savedEquipment != null) {
                Equipment newEquipmentData = equipmentForm.get();
                savedEquipment.name = newEquipmentData.name;
                savedEquipment.update();
                flash("success", "Equipment " + equipmentForm.get().name + " has been updated");
                txn.commit();

                Version.inc(Version.EQUIPMENT);
            }
        } finally {
            txn.end();
        }
        return list();
    }

    /**
     * Display the 'new equipment form'.
     */
    public Result create() {
        Form<Equipment> equipmentForm = formFactory.form(Equipment.class);
        return ok(
                views.html.equipment_createForm.render(equipmentForm)
        );
    }

    /**
     * Handle the 'new user form' submission
     */
    public Result save() {
        Form<Equipment> equipmentForm = formFactory.form(Equipment.class).bindFromRequest();
        if (equipmentForm.hasErrors()) {
            return badRequest(views.html.equipment_createForm.render(equipmentForm));
        }
        equipmentForm.get().save();
        flash("success", "Equipment " + equipmentForm.get().name + " has been created");
        return list();
    }

    /**
     * Display a form to enter in many equipments at once.
     */
    public Result createMany() {
        Form<InputLines> linesForm = formFactory.form(InputLines.class);
        return ok(
                views.html.equipments_createForm.render(linesForm)
        );
    }

    /**
     * Create many equipments at once.
     */
    @Transactional
    public Result saveMany() {
        Form<InputLines> linesForm = formFactory.form(InputLines.class).bindFromRequest();
        if (linesForm.hasErrors()) {
            return badRequest(views.html.equipments_createForm.render(linesForm));
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
                    Equipment equipment = Equipment.findByName(name);
                    if (equipment == null) {
                        equipment = new Equipment();
                        equipment.name = name;
                        equipment.save();
                    }
                    if (activeProject != null) {
                        ProjectEquipmentCollection collection = new ProjectEquipmentCollection();
                        collection.project_id = activeProject.id;
                        collection.equipment_id = equipment.id;

                        if (!ProjectEquipmentCollection.has(collection)) {
                            collection.save();
                        }
                    }
                }
            }
        }
        Version.inc(Version.EQUIPMENT);

        return list();
    }

    /**
     * Handle equipment deletion
     */
    public Result delete(Long id) {
        // TODO: If the client is in the database, mark it as disabled instead.
        Equipment.find.ref(id).delete();
        Version.inc(Version.EQUIPMENT);
        flash("success", "Equipment has been deleted");
        return list();
    }

    @Transactional
    public Result addProject(Long id, Long project_id) {
        ProjectEquipmentCollection collection = new ProjectEquipmentCollection();
        collection.project_id = project_id;
        collection.equipment_id = id;
        if (!ProjectEquipmentCollection.has(collection)) {
            collection.save();
            Version.inc(Version.EQUIPMENT);
        }
        return edit(id);
    }

    @Transactional
    public Result removeProject(Long id, Long project_id) {
        ProjectEquipmentCollection collection = new ProjectEquipmentCollection();
        collection.project_id = project_id;
        collection.equipment_id = id;
        collection = ProjectEquipmentCollection.get(collection);
        if (collection != null) {
            ProjectEquipmentCollection.find.ref(collection.id).delete();
            Version.inc(Version.EQUIPMENT);
        }
        return edit(id);
    }

}

