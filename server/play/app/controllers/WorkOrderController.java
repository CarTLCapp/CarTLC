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

    @Inject
    public WorkOrderController(FormFactory formFactory, AmazonHelper amazonHelper) {
        super();
        this.amazonHelper = amazonHelper;
        this.formFactory = formFactory;
    }

    public Result INDEX() {
        return list(0, "client_id", "desc");
    }

    public Result list(int page, String sortBy, String order) {
        workList.setPage(page);
        workList.setSortBy(sortBy);
        workList.setOrder(order);
        workList.clearCache();
        workList.setProjects(Secured.getClient(ctx()));
        workList.compute();
        return ok(views.html.work_order_list.render(workList, sortBy, order));
    }

    @Security.Authenticated(Secured.class)
    public Result uploadForm() {
        Form<WorkImport> importForm = formFactory.form(WorkImport.class);
        return ok(views.html.work_order_upload.render(importForm));
    }

    @Security.Authenticated(Secured.class)
    public Result upload() {
        Form<WorkImport> importForm = formFactory.form(WorkImport.class).bindFromRequest();
        Logger.info("FILENAME=" + importForm.get().filename);
        Logger.info("PROJECT=" + importForm.get().project);

        File file = new File(importForm.get().filename);
        if (file.exists()) {
            Logger.info("DOES EXIST");
        }

//        MultipartFormData<File> body = request().body().asMultipartFormData();
//        FilePart<File> importname = body.getFile("name");
//        if (importname != null) {
//            String fileName = importname.getFilename();
//            if (fileName.trim().length() > 0) {
//                File file = importname.getFile();
//                importOrders(file);
//            } else {
//                return badRequest("No filename entered");
//            }
//        } else {
//            flash("error", "Missing file");
//            return badRequest("No file name entered");
//        }
        return INDEX();
    }

    @Transactional
    void importOrders(File file) {
        Client client = Secured.getClient(ctx());
        WorkOrderReader reader = new WorkOrderReader(client, null);
        try {
            reader.load(file);
        } catch (Exception ex) {
            flash("error", ex.getMessage());
        }
    }

    public Result view(Long work_order_id) {
        WorkOrder order = WorkOrder.find.byId(work_order_id);
        if (order == null) {
            return badRequest("Could not find work order ID " + work_order_id);
        }
        List<Entry> list = Entry.getFulfilledBy(order);
        if (list == null || list.size() <= 0) {
            return badRequest("No fulfilled entry for this work order");
        }
        Entry entry = list.get(0);
        entry.loadPictures(request().host(), amazonHelper);
        return ok(views.html.entry_view.render(entry, Secured.getClient(ctx())));
    }

}
