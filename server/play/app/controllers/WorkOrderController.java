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

import play.Logger;

public class WorkOrderController extends Controller {

    private AmazonHelper amazonHelper;
    private WorkList workList = new WorkList();

    @Inject
    public WorkOrderController(AmazonHelper amazonHelper) {
        super();
        this.amazonHelper = amazonHelper;
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

    public Result upload() {
        Logger.debug("IMPORT");
        MultipartFormData<File> body = request().body().asMultipartFormData();
        FilePart<File> picture = body.getFile("name");
        if (picture != null) {
            String fileName = picture.getFilename();
            String contentType = picture.getContentType();
            Logger.debug("FILE=" + fileName);
            File file = picture.getFile();
            return ok("File uploaded");
        } else {
            flash("error", "Missing file");
            return badRequest();
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
