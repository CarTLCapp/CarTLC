package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;

import java.util.List;
import java.io.File;


import play.mvc.*;
import play.data.*;
import play.mvc.Http.*;
import play.mvc.Http.MultipartFormData.*;

import static play.data.Form.*;

import views.formdata.LoginFormData;
import play.mvc.Security;

import models.*;

import javax.inject.Inject;
import javax.persistence.PersistenceException;

import modules.AmazonHelper;
import play.db.ebean.Transactional;

import play.Logger;

public class WorkOrderController extends Controller {

    private FormFactory formFactory;
    private AmazonHelper amazonHelper;
    private WorkOrderList workList = new WorkOrderList();
    private ProgressGrid progressGrid = new ProgressGrid();

    @Inject
    public WorkOrderController(FormFactory formFactory, AmazonHelper amazonHelper) {
        super();
        this.amazonHelper = amazonHelper;
        this.formFactory = formFactory;
    }

    public Result INDEX() {
        return list(0, "client_id", "desc", "");
    }

    public Result INDEX(String msg) {
        return list(0, "client_id", "desc", msg);
    }

    @Security.Authenticated(Secured.class)
    public Result list(int page, String sortBy, String order, String message) {
        workList.setClient(Secured.getClient(ctx()));
        workList.setPage(page);
        workList.setSortBy(sortBy);
        workList.setOrder(order);
        workList.clearCache();
        workList.setProjects();
        workList.compute();
        return ok(views.html.work_order_list.render(workList, sortBy, order, Secured.getClient(ctx()), message));
    }


    @Security.Authenticated(Secured.class)
    public Result viewProgressGrid() {
        progressGrid.setClient(Secured.getClient(ctx()));
        progressGrid.setProjects();
        progressGrid.compute();
        return ok(views.html.progress_grid.render(progressGrid, Secured.getClient(ctx())));
    }

    public Result importWorkOrdersForm() {
        return importWorkOrdersForm("");
    }

    @Security.Authenticated(Secured.class)
    public Result importWorkOrdersForm(String errors) {
        Form<InputWord> importForm = formFactory.form(InputWord.class);
        return ok(views.html.work_order_import.render(importForm, Secured.getClient(ctx()), errors));
    }

    @Security.Authenticated(Secured.class)
    public Result importWorkOrders() {
        Form<InputWord> importForm = formFactory.form(InputWord.class).bindFromRequest();
        StringBuilder sbuf = new StringBuilder();
        String projectName = importForm.get().word;
        MultipartFormData<File> body = request().body().asMultipartFormData();
        if (body != null) {
            FilePart<File> importname = body.getFile("filename");
            if (importname != null) {
                String fileName = importname.getFilename();
                if (fileName.trim().length() > 0) {
                    File file = importname.getFile();
                    if (file.exists()) {
                        Project project = Project.findByName(projectName);
                        Client client = Secured.getClient(ctx());
                        WorkOrderReader reader = new WorkOrderReader(client, project);
                        if (!reader.load(file)) {
                            sbuf.append("Errors:\n");
                            sbuf.append(reader.getErrors());
                            String warnings = reader.getWarnings();
                            if (warnings.length() > 0) {
                                sbuf.append("\nWarnings:\n");
                                sbuf.append(warnings);
                            }
                        } else {
                            return INDEX(reader.getWarnings());
                        }
                    } else {
                        sbuf.append("File does not exist: " + fileName);
                    }
                } else {
                    sbuf.append("No filename entered");
                }
            } else {
                sbuf.append("No filename entered");
            }
        } else {
            sbuf.append("Invalid call");
        }
        if (sbuf.length() == 0) {
            return INDEX();
        } else {
            return importWorkOrdersForm(sbuf.toString());
        }
    }

    public Result exportWorkOrdersForm() {
        return exportWorkOrdersForm("");
    }

    @Security.Authenticated(Secured.class)
    public Result exportWorkOrdersForm(String errors) {
        Form<InputWord> exportForm = formFactory.form(InputWord.class);
        return ok(views.html.work_order_export.render(exportForm, Secured.getClient(ctx()), errors));
    }

    @Security.Authenticated(Secured.class)
    public Result exportWorkOrders() {
        Form<InputWord> exportForm = formFactory.form(InputWord.class).bindFromRequest();
        String fileName = exportForm.get().word;
        StringBuilder sbuf = new StringBuilder();
        if (fileName.trim().length() > 0) {
            File file = exportname.getFile();
            Client client = Secured.getClient(ctx());
            WorkOrderWriter writer = new WorkOrderWriter(client);
            if (!writer.save(file)) {
                sbuf.append("Errors:\n");
                sbuf.append(writer.getError());
            } else {
                return INDEX();
            }
        } else {
            sbuf.append("No filename entered");
        }
        if (sbuf.length() == 0) {
            return INDEX();
        } else {
            return exportWorkOrdersForm(sbuf.toString());
        }
    }

    public Result view(Long work_order_id) {
        WorkOrder order = WorkOrder.find.byId(work_order_id);
        if (order == null) {
            return badRequest("Could not find work order ID " + work_order_id);
        }
        Entry entry = Entry.getFulfilledBy(order);
        if (entry == null) {
            return badRequest("No fulfilled entry for this work order");
        }
        entry.loadPictures(request().host(), amazonHelper);
        return ok(views.html.entry_view.render(entry, Secured.getClient(ctx())));
    }

    @Security.Authenticated(Secured.class)
    public Result deleteLastUploaded() {
        int count = WorkOrder.deleteLastUploaded(Secured.getClient(ctx()));
        return INDEX(count + " work orders deleted");
    }

}
