/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package controllers;

import java.util.List;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;

import play.mvc.*;
import play.data.*;
import play.db.ebean.Transactional;

import static play.data.Form.*;

import views.formdata.InputTechnician;

import models.*;

import javax.inject.Inject;
import javax.persistence.PersistenceException;

import play.libs.Json;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Manage a database of users
 */
public class TechnicianController extends Controller {

    private FormFactory formFactory;

    @Inject
    public TechnicianController(FormFactory formFactory) {
        this.formFactory = formFactory;
    }

    /**
     * Display the list of users.
     */
    public Result list() {
        return ok(views.html.technician_list.render(Technician.listEnabled(), Secured.getClient(ctx())));
    }

    public static Result LIST() {
        return Results.redirect(routes.TechnicianController.list());
    }

    /**
     * Display the 'edit form' of an existing Technician.
     *
     * @param id Id of the user to edit
     */
    @Security.Authenticated(Secured.class)
    public Result edit(Long id) {
        Client client = Secured.getClient(ctx());
        if (Secured.isAdmin(ctx())) {
            Form<InputTechnician> techForm = formFactory.form(InputTechnician.class).fill(new InputTechnician(Technician.find.byId(id)));
            return ok(views.html.technician_editForm.render(id, techForm, client));
        } else {
            return HomeController.PROBLEM("Only adminstrators can change technician details");
        }
    }

    @Security.Authenticated(Secured.class)
    public Result resetUpload(Long id) {
        Technician tech = Technician.find.byId(id);
        if (tech != null) {
            tech.reset_upload = true;
            tech.update();
        }
        return list();
    }

    /**
     * Handle the 'edit form' submission
     *
     * @param id Id of the user to edit
     */
    public Result update(Long id) throws PersistenceException {
        Form<InputTechnician> techForm = formFactory.form(InputTechnician.class).bindFromRequest();
        Transaction txn = Ebean.beginTransaction();
        try {
            Technician savedTechnician = Technician.find.byId(id);
            if (savedTechnician == null) {
                return badRequest("Could not locate technician with ID " + id);
            }
            InputTechnician newTechData = techForm.get();
            int codeValue;
            try {
                codeValue = Integer.valueOf(newTechData.code);
            } catch (NumberFormatException ex) {
                return badRequest(ex.getMessage());
            }
            String errorMessage;
            if (codeValue == 0) {
                codeValue = Technician.findLastCode() + 1;
            } else if (codeValue <= Technician.BASE_CODE) {
                return badRequest("Code value must be greater than " + Technician.BASE_CODE);
            } else if ((errorMessage = isCodeUsed(savedTechnician, codeValue)) != null) {
                return badRequest(errorMessage);
            }
            if (newTechData.first_name == null || newTechData.first_name.isEmpty()) {
                return badRequest("No first name entered.");
            }
            if (newTechData.last_name == null || newTechData.last_name.isEmpty()) {
                return badRequest("No last name entered.");
            }
            savedTechnician.first_name = newTechData.first_name;
            savedTechnician.last_name = newTechData.last_name;
            savedTechnician.code = codeValue;
            savedTechnician.update();
            txn.commit();
        } finally {
            txn.end();
        }
        return LIST();
    }

    private String isCodeUsed(Technician forTech, int checkCode) {
        List<Technician> alreadyHasCode = Technician.listWithCode(checkCode);
        for (Technician tech : alreadyHasCode) {
            if (tech.id != forTech.id && tech.code == checkCode) {
                StringBuffer sbuf = new StringBuffer();
                sbuf.append("This code is already being used by another technician: ");
                sbuf.append(tech.first_name);
                sbuf.append(" ");
                sbuf.append(tech.last_name);
                return sbuf.toString();
            }
        }
        return null;
    }

    private String isCodeUsed(int checkCode) {
        List<Technician> alreadyHasCode = Technician.listWithCode(checkCode);
        for (Technician tech : alreadyHasCode) {
            if (tech.code == checkCode) {
                StringBuffer sbuf = new StringBuffer();
                sbuf.append("This code is already being used by another technician: ");
                sbuf.append(tech.first_name);
                sbuf.append(" ");
                sbuf.append(tech.last_name);
                return sbuf.toString();
            }
        }
        return null;
    }

    /**
     * Handle user deletion
     */
    @Security.Authenticated(Secured.class)
    public Result delete(Long id) {
        try {
            Technician.find.byId(id).delete();
        } catch (Exception ex) {
            Transaction txn = Ebean.beginTransaction();
            try {
                Technician tech = Technician.find.byId(id);
                tech.disabled = true;
                tech.update();
                txn.commit();
            } finally {
                txn.end();
            }
        }
        return LIST();
    }

    /**
     * Display the 'new technician form'.
     */
    @Security.Authenticated(Secured.class)
    public Result create() {
        Form<InputTechnician> techForm = formFactory.form(InputTechnician.class).fill(new InputTechnician());
        return ok(views.html.technician_createForm.render(techForm));
    }

    /**
     * Handle the 'new technician form' submission
     */
    @Security.Authenticated(Secured.class)
    @Transactional
    public Result save() {
        Form<InputTechnician> techForm = formFactory.form(InputTechnician.class).bindFromRequest();
        if (techForm.hasErrors()) {
            return badRequest(views.html.technician_createForm.render(techForm));
        }
        Client curClient = Secured.getClient(ctx());
        if (!curClient.is_admin) {
            techForm.withError("adminstrator", "Non administrators cannot create technicians.");
            return badRequest(views.html.technician_createForm.render(techForm));
        }
        InputTechnician inputTech = techForm.get();
        int codeValue;
        try {
            codeValue = Integer.valueOf(inputTech.code);
        } catch (NumberFormatException ex) {
            return badRequest(ex.getMessage());
        }
        if (codeValue <= Technician.BASE_CODE) {
            return badRequest("Code value must be greater than " + Technician.BASE_CODE);
        }
        String errorMessage;
        if ((errorMessage = isCodeUsed(codeValue)) != null) {
            return badRequest(errorMessage);
        }
        if (inputTech.first_name == null || inputTech.first_name.isEmpty()) {
            return badRequest("No first name entered.");
        }
        if (inputTech.last_name == null || inputTech.last_name.isEmpty()) {
            return badRequest("No last name entered.");
        }
        Technician newTech = new Technician();
        newTech.first_name = inputTech.first_name;
        newTech.last_name = inputTech.last_name;
        newTech.code = codeValue;
        newTech.save();
        return LIST();
    }

    public Result query() {
        ObjectNode top = Json.newObject();
        ArrayNode array = top.putArray("technicians");
        for (Technician tech : Technician.listEnabled()) {
            ObjectNode node = array.addObject();
            node.put("first_name", tech.first_name);
            node.put("last_name", tech.last_name);
            node.put("code", tech.code);
        }
        return ok(top);
    }

}
            
