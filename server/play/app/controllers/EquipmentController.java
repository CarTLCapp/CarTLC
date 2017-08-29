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
    @Security.Authenticated(Secured.class)
    public Result list() {
        return ok(views.html.equipment_list.render(Equipment.list(), Secured.getClient(ctx())));
    }

    /**
     * Display the 'edit form' of an existing equipment name.
     *
     * @param id Id of the equipment to edit
     */
    @Security.Authenticated(Secured.class)
    public Result edit(Long id) {
        Form<Equipment> equipmentForm = formFactory.form(Equipment.class).fill(Equipment.find.byId(id));
        return ok(views.html.equipment_editForm.render(id, equipmentForm));
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

                Version.inc(Version.VERSION_EQUIPMENT);
            }
        } finally {
            txn.end();
        }
        return list();
    }

    /**
     * Display the 'new equipment form'.
     */
    @Security.Authenticated(Secured.class)
    public Result create() {
        Form<Equipment> equipmentForm = formFactory.form(Equipment.class);
        return ok(views.html.equipment_createForm.render(equipmentForm));
    }

    /**
     * Handle the 'new user form' submission
     */
    @Security.Authenticated(Secured.class)
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
    @Security.Authenticated(Secured.class)
    public Result createMany() {
        Form<InputLines> linesForm = formFactory.form(InputLines.class);
        return ok(views.html.equipments_createForm.render(linesForm));
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
        final String PROJECT = "Project:";
        for (String name : lines) {
            name = name.trim();
            if (!name.isEmpty()) {
                if (name.startsWith(PROJECT)) {
                    Project project = Project.findByName(name.substring(PROJECT.length()).trim());
                    if (project != null) {
                        activeProject = project;
                        continue;
                    }
                }
                if (activeProject == null) {
                    return badRequest("First line must indicate valid project");
                }
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
        Version.inc(Version.VERSION_EQUIPMENT);

        return list();
    }

    /**
     * Handle equipment deletion
     */
    public Result delete(Long id) {
        // TODO: If the client is in the database, mark it as disabled instead.
        Equipment.find.ref(id).delete();
        Version.inc(Version.VERSION_EQUIPMENT);
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
            Version.inc(Version.VERSION_EQUIPMENT);
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
            Version.inc(Version.VERSION_EQUIPMENT);
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
        ArrayNode array = top.putArray("equipments");
        List<Equipment> equipments = Equipment.appList(tech_id);
        List<Long> equipmentIds = new ArrayList<Long>();
        for (Equipment item : equipments) {
            ObjectNode node = array.addObject();
            node.put("id", item.id);
            node.put("name", item.name);
            if (item.created_by != 0) {
                node.put("is_local", true);
            }
            equipmentIds.add(item.id);
        }
        array = top.putArray("project_equipment");
        for (ProjectEquipmentCollection item : ProjectEquipmentCollection.find.all()) {
            if (equipmentIds.contains(item.equipment_id)) {
                ObjectNode node = array.addObject();
                node.put("id", item.id);
                node.put("project_id", item.project_id);
                node.put("equipment_id", item.equipment_id);
            }
        }
        return ok(top);
    }
}

