/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;

import java.util.List;
import java.io.File;

import play.mvc.*;
import play.data.*;
import play.mvc.Http.*;
import play.mvc.Http.MultipartFormData.*;

import play.mvc.Security;

import models.*;

import javax.inject.Inject;
import javax.persistence.PersistenceException;

import modules.AmazonHelper;
import modules.WorkOrderReader;
import modules.WorkOrderWriter;

import play.Logger;

import views.formdata.WorkOrderFormData;

public class WorkOrderController extends Controller {

    private FormFactory formFactory;
    private AmazonHelper amazonHelper;
    private WorkOrderList workList = new WorkOrderList();
    private WorkOrderSummaryList summaryList = new WorkOrderSummaryList();
    private ProgressGrid progressGrid = new ProgressGrid();
    private int editingUploadId;

    private static final String EXPORT_FILENAME = "export.csv";

    @Inject
    public WorkOrderController(FormFactory formFactory, AmazonHelper amazonHelper) {
        super();
        this.amazonHelper = amazonHelper;
        this.formFactory = formFactory;
    }

    public Result INDEX() {
        return list(0, "last_modified", "desc", "");
    }

    public Result INDEX(String msg) {
        return list(0, "last_modified", "desc", msg);
    }

    @Security.Authenticated(Secured.class)
    public Result list(int page, String sortBy, String order, String message) {
        summaryList.setClient(Secured.getClient(ctx()));
        summaryList.setPage(page);
        summaryList.setSortBy(sortBy);
        summaryList.setOrder(order);
        summaryList.clearCache();
        summaryList.computeFilters();
        summaryList.compute();
        return ok(views.html.workOrder_summary_list.render(summaryList, sortBy, order, Secured.getClient(ctx()), message));
    }

    @Security.Authenticated(Secured.class)
    public Result listOrders(Integer upload_id, int page, String sortBy, String order, String message) {
        if (upload_id == 0) {
            upload_id = editingUploadId;
            editingUploadId = 0;
        }
        if (upload_id > 0) {
            workList.setUploadId(upload_id);
        }
        workList.setClient(Secured.getClient(ctx()));
        workList.setPage(page);
        workList.setSortBy(sortBy);
        workList.setOrder(order);
        workList.clearCache();
        workList.computeFilters();
        workList.compute();
        return ok(views.html.workOrder_list.render(workList, sortBy, order, Secured.getClient(ctx()), message));
    }

    @Security.Authenticated(Secured.class)
    public Result viewProgressGrid(Integer upload_id) {
        progressGrid.setClient(Secured.getClient(ctx()));
        progressGrid.setUploadId(upload_id);
        progressGrid.clearCache();
        progressGrid.computeFilters();
        progressGrid.compute();
        String home;
        if (upload_id == null || upload_id == 0) {
            home = "/work/summary/list";
        } else {
            home = "/work/list?u=" + upload_id;
        }
        return ok(views.html.progress_grid.render(progressGrid, Secured.getClient(ctx()), home));
    }

    public Result importWorkOrdersForm() {
        return importWorkOrdersForm("");
    }

    @Security.Authenticated(Secured.class)
    public Result importWorkOrdersForm(String errors) {
        Form<ImportWorkOrder> importForm = formFactory.form(ImportWorkOrder.class);
        return ok(views.html.workOrder_import.render(importForm, Secured.getClient(ctx()), errors));
    }

    @Security.Authenticated(Secured.class)
    public Result importWorkOrders() {
        Form<ImportWorkOrder> importForm = formFactory.form(ImportWorkOrder.class).bindFromRequest();
        StringBuilder sbuf = new StringBuilder();
        String projectName = importForm.get().project;
        String companyName = importForm.get().company;
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
                        WorkOrderReader reader = new WorkOrderReader(client, project, companyName);
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

    @Security.Authenticated(Secured.class)
    public Result exportWorkOrders() {
        Client client = Secured.getClient(ctx());
        File file = new File(EXPORT_FILENAME);
        WorkOrderWriter writer = new WorkOrderWriter(client);
        if (!writer.save(file)) {
            INDEX("Errors: " + writer.getError());
        }
        return ok(file);
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
    public Result deleteSummary(Integer upload_id) {
        int count = WorkOrder.deleteByUploadId(upload_id, Secured.getClient(ctx()));
        return INDEX(count + " work orders deleted");
    }

    /**
     * Handle single work order line deletion
     */
    @Security.Authenticated(Secured.class)
    public Result delete(Long id) {
        WorkOrder workOrderItem = WorkOrder.get(id);
        if (workOrderItem == null) {
            String message = "Invalid work order ID: " + id;
            flash(message);
            return ok(message);
        }
        WorkOrder.find.byId(id).delete();
        return INDEX();
    }

    /**
     * Display the 'edit form' of an existing Technician.
     *
     * @param id Id of the user to edit
     */
    @Security.Authenticated(Secured.class)
    public Result edit(Long id) {
        WorkOrder workOrderItem = WorkOrder.get(id);
        if (workOrderItem == null) {
            return badRequest("Could not find work order with ID " + id);
        }
        Form<WorkOrderFormData> workOrderForm = formFactory.form(WorkOrderFormData.class).fill(new WorkOrderFormData(workOrderItem));
        Client client = Secured.getClient(ctx());
        if (Secured.isAdmin(ctx())) {
            editingUploadId = workOrderItem.upload_id;
            return ok(views.html.workOrder_editForm.render(id, workOrderForm, client));
        } else {
            return HomeController.PROBLEM("Non administrators cannot change work order entries.");
        }
    }

    /**
     * Handle the 'edit form' submission
     *
     * @param id Id of the work order to edit
     */
    public Result update(Long id) throws PersistenceException {
        Client client = Secured.getClient(ctx());
        Form<WorkOrderFormData> workOrderForm = formFactory.form(WorkOrderFormData.class).bindFromRequest();
        if (workOrderForm.hasErrors()) {
            return badRequest(views.html.workOrder_editForm.render(id, workOrderForm, client));
        }
        WorkOrder workOrderItem = WorkOrder.get(id);
        if (workOrderItem == null) {
            return badRequest("Could not find work order with ID " + id);
        }
        Truck truck = workOrderItem.getTruck();
        if (truck == null) {
            truck = new Truck();
        }
        try {
            WorkOrderFormData workOrderFormData = workOrderForm.get();
            truck.truck_number = workOrderFormData.truck_number;
            truck.license_plate = workOrderFormData.license_plate;
            if (truck.id == 0) {
                truck.project_id = workOrderItem.project_id;
                truck.company_name_id = CompanyName.save(workOrderItem.getCompanyName());
                truck.created_by = (int) workOrderItem.client_id;
                truck.created_by_client = true;
                truck.upload_id = workOrderItem.upload_id;
                truck.save();
                Logger.info("New truck line: " + truck.toString());
                workOrderItem.truck_id = truck.id;
                workOrderItem.update();
            } else {
                truck.update();
                Logger.info("Truck updated: " + truck.toString());
            }
            Version.inc(Version.VERSION_TRUCK);
        } catch (Exception ex) {
            Logger.error(ex.getMessage());
        }
        return INDEX();
    }

    public static boolean isValidTruckNumber(String line) {
        if (line == null || line.isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(line);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

}
