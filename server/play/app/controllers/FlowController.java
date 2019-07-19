/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package controllers;

import com.avaje.ebean.Ebean;

import play.db.ebean.Transactional;
import com.avaje.ebean.Transaction;

import play.mvc.*;
import play.data.*;

import static play.data.Form.*;

import models.*;
import models.flow.*;
import views.formdata.InputFlow;
import views.formdata.InputFlowElement;

import javax.inject.Inject;
import javax.persistence.PersistenceException;

import java.util.List;
import java.util.ArrayList;
import play.Logger;

import play.libs.Json;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * Manage a database of users
 */
public class FlowController extends Controller {

    private FormFactory formFactory;

    @Inject
    public FlowController(FormFactory formFactory) {
        this.formFactory = formFactory;
    }

    /**
     * Display the list of flows.
     */
    public Result list() {
        return ok(views.html.flow_list.render(Flow.list(), Secured.getClient(ctx())));
    }

    public Result LIST() {
        return Results.redirect(routes.FlowController.list());
    }

    public Result EDIT(Long id) {
        return Results.redirect(routes.FlowController.edit(id));
    }

    public Result EDIT(Long id, String message) {
        if (message != null && !message.isEmpty()) {
            return Results.redirect(routes.FlowController.editWithMessage(id, message));
        } else {
            return Results.redirect(routes.FlowController.edit(id));
        }
    }

    /**
     * Display the 'new flow form'.
     */
    public Result create() {
        return edit(0L);
    }

    public Result edit(Long flowId) {
        return editWithMessage(flowId, "");
    }

    /**
     * Display the 'edit form' of an existing flow.
     *
     * @param id Id of the user to edit
     */
    @Security.Authenticated(Secured.class)
    public Result editWithMessage(Long flowId, String message) {
        Client curClient = Secured.getClient(ctx());
        if (!curClient.is_admin) {
            return badRequest("Non administrators cannot create flows.");
        }
        Form<InputFlow> flowForm;
        if (flowId > 0) {
            flowForm = formFactory.form(InputFlow.class).fill(new InputFlow(Flow.get(flowId)));
        } else {
            flowForm = formFactory.form(InputFlow.class).fill(new InputFlow());
        }
        if (Secured.isAdmin(ctx())) {
            return ok(views.html.flow_editForm.render(flowId, flowForm, message, curClient));
        } else {
            return HomeController.PROBLEM("Non administrators cannot change flows.");
        }
    }

    /**
     * Handle the 'new flow form' submission
     */
    @Security.Authenticated(Secured.class)
    public Result save(Long flowId) {
        Client curClient = Secured.getClient(ctx());
        if (!curClient.is_admin) {
            return HomeController.PROBLEM("Non administrators cannot change flows.");
        }
        Form<InputFlow> flowForm = formFactory.form(InputFlow.class).bindFromRequest();
        InputFlow inputFlow = flowForm.get();
        String message = "";
        Ebean.beginTransaction();
        try {
            Flow editFlow;
            if (flowId > 0) {
                editFlow = Flow.get(flowId);
            } else {
                editFlow = new Flow();
            }
            Project project = Project.findByName(inputFlow.root_project_name, inputFlow.sub_project_name);
            if (project == null) {
                message = "Cannot find project named: " + inputFlow.root_project_name + " - " + inputFlow.sub_project_name;
            } else {
                editFlow.sub_project_id = project.id;
                if (flowId > 0) {
                    editFlow.update();
                } else {
                    editFlow.save();
                    flowId = editFlow.id;
                }
                Ebean.commitTransaction();
            }
        } finally {
            Ebean.endTransaction();
        }
        return EDIT(flowId, message);
    }

    @Security.Authenticated(Secured.class)
    @Transactional
    public Result delete(Long flowId) {
        Client curClient = Secured.getClient(ctx());
        if (!curClient.is_admin) {
            return HomeController.PROBLEM("Non administrators cannot change flows.");
        }
        Flow.delete(flowId);
        return LIST();
    }

    @Security.Authenticated(Secured.class)
    @Transactional
    public Result moveUp(Long flowId, Long elementId) {
        Form<InputFlow> flowForm = formFactory.form(InputFlow.class).bindFromRequest();
        Client curClient = Secured.getClient(ctx());
        if (!curClient.is_admin) {
            return HomeController.PROBLEM("Non administrators cannot change flows.");
        }
        FlowElementCollection.moveUp(flowId, elementId);
        return EDIT(flowId);
    }

    @Security.Authenticated(Secured.class)
    @Transactional
    public Result moveDown(Long flowId, Long elementId) {
        Form<InputFlow> flowForm = formFactory.form(InputFlow.class).bindFromRequest();
        Client curClient = Secured.getClient(ctx());
        if (!curClient.is_admin) {
            return HomeController.PROBLEM("Non administrators cannot change flows.");
        }
        FlowElementCollection.moveDown(flowId, elementId);
        return EDIT(flowId);
    }

    @Security.Authenticated(Secured.class)
    public Result editElement(Long flowId, Long elementId) {
        Client curClient = Secured.getClient(ctx());
        if (!curClient.is_admin) {
            return HomeController.PROBLEM("Non administrators cannot change flows.");
        }
        Form<InputFlowElement> flowElementForm = formFactory.form(InputFlowElement.class).fill(new InputFlowElement(FlowElement.get(elementId)));
        return ok(views.html.flow_editElementForm.render(elementId, flowId, flowElementForm, curClient));
    }

    @Security.Authenticated(Secured.class)
    public Result addElement(Long flowId) {
        Client curClient = Secured.getClient(ctx());
        if (!curClient.is_admin) {
            return HomeController.PROBLEM("Non administrators cannot change flows.");
        }
        Form<InputFlowElement> flowElementForm = formFactory.form(InputFlowElement.class).fill(new InputFlowElement());
        return ok(views.html.flow_editElementForm.render(0L, flowId, flowElementForm, curClient));
    }

    @Security.Authenticated(Secured.class)
    @Transactional
    public Result deleteElement(Long flowId, Long elementId) {
        Client curClient = Secured.getClient(ctx());
        if (!curClient.is_admin) {
            return HomeController.PROBLEM("Non administrators cannot change flows.");
        }
        FlowElement.delete(elementId);
        return EDIT(flowId);
    }

    @Transactional
    @Security.Authenticated(Secured.class)
    public Result saveElement(Long flowId, Long elementId) {
        Client curClient = Secured.getClient(ctx());
        if (!curClient.is_admin) {
            return HomeController.PROBLEM("Non administrators cannot change flows.");
        }
        Form<InputFlowElement> flowElementForm = formFactory.form(InputFlowElement.class).bindFromRequest();
        if (flowElementForm.hasErrors()) {
            return badRequest(views.html.flow_editElementForm.render(flowId, elementId, flowElementForm, curClient));
        }
        InputFlowElement inputFlowElement = flowElementForm.get();
        FlowElement flowElement;
        if (elementId > 0) {
            flowElement = FlowElement.get(elementId);
        } else {
            flowElement = new FlowElement();
        }
        flowElement.prompt = inputFlowElement.prompt;
        flowElement.prompt_type = inputFlowElement.getPromptType().getCode();
        flowElement.request_image = inputFlowElement.hasImage;
        flowElement.generic_note = inputFlowElement.hasGenericNote;

        if (elementId > 0) {
            flowElement.update();
        } else {
            flowElement.save();
            elementId = flowElement.id;
        }
        if (flowId == 0) {
            Flow flow = new Flow();
            flow.save();
            flowId = flow.id;
        }
        if (!FlowElementCollection.hasFlowElement(flowId, elementId)) {
            FlowElementCollection.create(flowId, elementId);
        }
        FlowNoteCollection.process(elementId, flowElementForm);

        return EDIT(flowId);
    }

    public Result query() {
        ObjectNode top = Json.newObject();
        ArrayNode array = top.putArray("flows");
        for (Flow flow : Flow.list()) {
            ObjectNode node = array.addObject();
            node.put("flow_id", flow.id);
            node.put("sub_project_id", flow.sub_project_id);
            ArrayNode elementsNode = node.putArray("elements");
            for (FlowElement element : flow.getFlowElements()) {
                ObjectNode elementNode = elementsNode.addObject();
                if (element.hasPrompt()) {
                    elementNode.put("prompt", element.prompt);
                }
                elementNode.put("type", element.getPromptType().getCodeString());
                elementNode.put("requestImage", element.request_image);
                elementNode.put("genericNote", element.generic_note);
                if (element.hasNotes()) {
                    ArrayNode notesNote = elementNode.putArray("notes");
                    for (Note note : element.getNotes()) {
                        ObjectNode noteNode = notesNote.addObject();
                        noteNode.put("node_id", note.id);
                    }
                }
            }
        }
        return ok(top);
    }

}
            
