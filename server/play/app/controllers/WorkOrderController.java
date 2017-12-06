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

import views.formdata.WorkOrderFormData;

public class WorkOrderController extends Controller {

    private FormFactory formFactory;
    private AmazonHelper amazonHelper;
    private WorkOrderList workList = new WorkOrderList();
    private ProgressGrid progressGrid = new ProgressGrid();

    private static final String EXPORT_FILENAME = "export.csv";

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
        workList.computeFilters();
        workList.compute();
        return ok(views.html.workOrder_list.render(workList, sortBy, order, Secured.getClient(ctx()), message));
    }


    @Security.Authenticated(Secured.class)
    public Result viewProgressGrid() {
        progressGrid.setClient(Secured.getClient(ctx()));
        progressGrid.computeFilters();
        progressGrid.compute();
        return ok(views.html.progress_grid.render(progressGrid, Secured.getClient(ctx())));
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
    public Result deleteLastUploaded() {
        int count = WorkOrder.deleteLastUploaded(Secured.getClient(ctx()));
        return INDEX(count + " work orders deleted");
    }

    /**
     * Handle work order line deletion
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
            return ok(views.html.workOrder_editForm.render(id, workOrderForm, client));
        } else {
            workOrderForm.reject("adminstrator", "Non administrators cannot change work order entries.");
            return badRequest(views.html.home.render(client));
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
            if (workOrderFormData.truck_number.trim().length() > 0) {
                truck.truck_number = Integer.parseInt(workOrderFormData.truck_number);
            } else {
                truck.truck_number = 0;
            }
            truck.license_plate = workOrderFormData.license_plate;
            if (truck.id == 0) {
                truck.project_id = workOrderItem.project_id;
                truck.company_name_id = CompanyName.save(workOrderItem.getCompanyName());
                truck.created_by = (int) workOrderItem.client_id;
                truck.created_by_client = true;
                truck.upload_id = workOrderItem.upload_id;
                truck.save();
            } else {
                truck.update();
            }
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
