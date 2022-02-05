/**
 * Copyright 2021, FleetTLC. All rights reserved
 */
package controllers;

import modules.AmazonHelper;
import modules.WorkerExecutionContext;
import play.Logger;
import play.libs.concurrent.HttpExecution;
import play.mvc.*;
import play.data.*;

import com.typesafe.config.Config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.text.SimpleDateFormat;

import javax.persistence.PersistenceException;
import javax.inject.Inject;

import views.formdata.CleanupFormData;
import play.mvc.Security;

import models.*;
import modules.TimeHelper;

import play.db.ebean.Transactional;

public class CleanupController extends Controller {

    private static final int PAGE_SIZE = 100;

    private AmazonHelper mAmazonHelper;
    private FormFactory mFormFactory;
    private boolean mCleaningTrucks = false;
    private boolean mAborted = false;
    private CleanupTrucks mCleanupTrucks;
    private String lastResult = "";

    @Inject
    public CleanupController(
            AmazonHelper amazonHelper,
            FormFactory formFactory) {
        mAmazonHelper = amazonHelper;
        mFormFactory = formFactory;
    }

    @Security.Authenticated(Secured.class)
    public Result index() {
        Form<CleanupFormData> cleanupForm = mFormFactory.form(CleanupFormData.class).fill(new CleanupFormData());
        return ok(views.html.cleanup.render(cleanupForm, Secured.getClient(ctx()), Repaired.count(), lastResult));
    }

    // region DELETE OLD ENTRIES

    @Transactional
    @Security.Authenticated(Secured.class)
    public Result deleteDated() throws PersistenceException {
        Client client = Secured.getClient(ctx());
        Form<CleanupFormData> cleanupForm = mFormFactory.form(CleanupFormData.class).bindFromRequest();
        CleanupFormData data = cleanupForm.get();
        SimpleDateFormat format = new SimpleDateFormat(TimeHelper.DATE_TIME_FORMAT);
        return index();
    }

    // endregion DELETE OLD ENTRIES

    // region ENTRY FIXUP

    @Security.Authenticated(Secured.class)
    public CompletionStage<Result> entryFixup() {
        String host = request().host();
        CompletableFuture<Result> completableFuture = new CompletableFuture<>();
        Executors.newCachedThreadPool().submit(() -> {
            entryFixup1(host, completableFuture);
            return null;
        });
        return completableFuture;
    }

    private void entryFixup1(String host, CompletableFuture<Result> completableFuture) {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("DONE");
        completableFuture.complete(ok(sbuf.toString()));
    }

    // endregion ENTRY FIXUP

    // region PICTURE CLEANUP

    @Security.Authenticated(Secured.class)
    public CompletionStage<Result> pictureCleanup() {
        String host = request().host();
        CompletableFuture<Result> completableFuture = new CompletableFuture<>();
        Executors.newCachedThreadPool().submit(() -> {
            mAmazonHelper.list(host, (keys) -> pictureCleanup1(host, completableFuture, keys));
            return null;
        });
        return completableFuture;
    }

    private void pictureCleanup1(String host, CompletableFuture<Result> completableFuture, List<String> keys) {
        warn("Checking for orphaned pictures from " + keys.size() + " keys");
        ArrayList<String> missing = new ArrayList<>();
        for (String key : keys) {
            if (!Entry.hasEntryForPicture(key)) {
                missing.add(key);
            }
        }
        warn("ORPHANED REMOTE PICTURES=" + missing.size());
        mAmazonHelper.deleteAction().host(host).deleteLocalFile(true).listener((deleted, errors) -> {
            pictureCleanup2(host, completableFuture, deleted);
        }).delete(missing);
        warn("DONE");
    }

    private void pictureCleanup2(String host, CompletableFuture<Result> completableFuture, int deletedRemote) {
        List<String> orphanedDb = pictureCollectionIdCleanup(host);
        mAmazonHelper.deleteAction()
                .host(host)
                .deleteLocalFile(true)
                .listener((deleted, errors) -> {
                    pictureCleanup3(host, completableFuture, deletedRemote, deleted);
                })
                .delete(orphanedDb);
    }

    private void pictureCleanup3(String host, CompletableFuture<Result> completableFuture, int deletedRemote, int deletedDb) {
        int orphanedLocal = pictureLocalFileCleanup();
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("REMOTE FILES DELETED=");
        sbuf.append(deletedRemote);
        sbuf.append(", ORPHANED NODES=");
        sbuf.append(deletedDb);
        sbuf.append(", LOCAL DELETE=");
        sbuf.append(orphanedLocal);
        completableFuture.complete(ok(sbuf.toString()));
        warn(sbuf.toString());
    }

    private List<String> pictureCollectionIdCleanup(String host) {
        warn("pictureCollectionIdCleanup()");
        ArrayList<String> orphaned = new ArrayList<>();
        List<PictureCollection> list = PictureCollection.findNoEntries();
        for (PictureCollection collection : list) {
            warn("Orphaned collection: " + collection.toString());
            orphaned.add(collection.picture);
            collection.delete();
        }
        return orphaned;
    }

    private int pictureLocalFileCleanup() {
        File dir = mAmazonHelper.getLocalDirectory();
        ArrayList<File> orphaned = new ArrayList<>();
        for (File file : dir.listFiles()) {
            if (!Entry.hasEntryForPicture(file.getName())) {
                orphaned.add(file);
            }
        }
        for (File file : orphaned) {
            warn("ORPHANED LOCAL FILE DELETED: " + file.getName());
            file.delete();
        }
        return orphaned.size();
    }

    // endregion PICTURE CLEANUP

    // region TRUCK CLEANUP

    /**
     * Clean up the list of entries with the same truck id. This is an error.
     * Note: in the new flow style this problem will no longer be an issue as this field is obsolete.
     *
     * @return
     */
    @Security.Authenticated(Secured.class)
    public Result truckCleanup() {
        Client client = Secured.getClient(ctx());
        if (mCleaningTrucks) {
            info("truckCleanup(): ABORTED");
            mCleaningTrucks = false;
            mAborted = true;
            return ok("R");
        }
        info("truckCleanup()");
        mCleaningTrucks = true;
        mAborted = false;
        mCleanupTrucks = new CleanupTrucks();
        return ok("#" + mCleanupTrucks.getReport() + "...");
    }

    public Result truckCleanupNext() {
        if (mAborted) {
            mAborted = false;
            mCleaningTrucks = false;
            info("truckCleanupNext(): ABORT");
            return ok("R");
        }
        if (mCleanupTrucks.processNextPage()) {
            debug("truckCleanupNext(): PAGE " + mCleanupTrucks.mPage + " OF " + mCleanupTrucks.mNumPages);
            return ok("#" + mCleanupTrucks.getReport() + "...");
        }
        if (mCleanupTrucks.reassignNextTruck()) {
            return ok("#" + mCleanupTrucks.getReport() + "...");
        }
        if (mCleanupTrucks.reassignNextCompanyName()) {
            return ok("#" + mCleanupTrucks.getReport() + "...");
        }
        if (mCleanupTrucks.deleteNextTruck()) {
            return ok("#" + mCleanupTrucks.getReport() + "...");
        }
        debug("truckCleanupNext(): DONE");
        mCleaningTrucks = false;
        lastResult = mCleanupTrucks.getReport();
        return ok("D" + lastResult);
    }

    private class CleanupTrucks {

        final int mNumTrucks;
        final int mPageSize = PAGE_SIZE;
        final int mNumPages;
        ArrayList<Truck> mReassignTrucks = new ArrayList<Truck>();
        ArrayList<Truck> mDeleteTrucks = new ArrayList<Truck>();
        ArrayList<Entry> mFixTruckCompanyName = new ArrayList<Entry>();
        int mPage = -1;
        int mProcessed = 0;
        int mFixedCompanyNames = 0;
        int mReassignedCount = 0;
        int mDeletedCount = 0;

        CleanupTrucks() {
            mNumTrucks = Truck.countTrucks();
            mNumPages = mNumTrucks / mPageSize + (mNumTrucks % mPageSize > 0 ? 1 : 0);
        }

        boolean processNextPage() {
            if (++mPage >= mNumPages) {
                return false;
            }
            List<Truck> trucks = Truck.getTrucks(mPage, mPageSize);
            for (Truck truck : trucks) {
                mProcessed++;
                int num_entries = truck.countEntries();
                if (num_entries > 0) {
                    if (num_entries > 1) {
                        mReassignTrucks.add(truck);
                        info("Found truck with more than one entry: " + truck.toString());
                    } else if (truck.company_name_id == 0) {
                        info("Found truck with no company name: " + truck.toString());
                        mFixTruckCompanyName.addAll(Entry.findByTruckId(truck.id));
                    }
                    continue;
                }
                if (WorkOrder.countWorkOrdersForTruck(truck.id) > 0) {
                    continue;
                }
                info("Found orphaned truck: " + truck.toString());
                mDeleteTrucks.add(truck);
            }
            return true;
        }

        boolean reassignNextTruck() {
            if (mReassignTrucks.size() == 0) {
                return false;
            }
            ArrayList<Truck> reassign = new ArrayList<Truck>();
            if (mReassignTrucks.size() > PAGE_SIZE) {
                reassign.addAll(mReassignTrucks.subList(0, PAGE_SIZE));
                mReassignTrucks.removeAll(reassign);
            } else {
                reassign.addAll(mReassignTrucks);
                mReassignTrucks.clear();
            }
            for (Truck truck : reassign) {
                List<Entry> instances = reassignTruck(truck);
                if (truck.company_name_id == 0) {
                    mFixTruckCompanyName.addAll(instances);
                }
            }
            return true;
        }

        List<Entry> reassignTruck(Truck truck) {
            List<Entry> instances = Entry.findByTruckId(truck.id);
            boolean isReassigned = false;
            int instance_id = Version.inc(Version.NEXT_REPAIRED_INSTANCE_ID);
            for (Entry entry : instances) {
                info("ENTRY HAD DUP TRUCK : " + entry.toString() + (isReassigned ? " [REASSIGNED]" : ""));
                isReassigned = true;
                Repaired.addDupTruckId(entry.id, instance_id);
            }
            for (int i = 1; i < instances.size(); i++) {
                Entry entry = instances.get(i);
                reassignTruckValue(entry);
                mReassignedCount++;
            }
            return instances;
        }

        boolean reassignNextCompanyName() {
            if (mFixTruckCompanyName.size() == 0) {
                return false;
            }
            ArrayList<Entry> fix = new ArrayList<Entry>();
            if (mFixTruckCompanyName.size() > PAGE_SIZE) {
                fix.addAll(mFixTruckCompanyName.subList(0, PAGE_SIZE));
                mFixTruckCompanyName.removeAll(fix);
            } else {
                fix.addAll(mFixTruckCompanyName);
                mFixTruckCompanyName.clear();
            }
            for (Entry entry : fix) {
                if (entry.fixTruckCompanyName()) {
                    info("ENTRY FIXED TRUCK COMPANY NAME : " + entry.toString());
                    mFixedCompanyNames++;
                }
            }
            return true;
        }

        boolean deleteNextTruck() {
            if (mDeleteTrucks.size() == 0) {
                return false;
            }
            ArrayList<Truck> deleteList = new ArrayList<Truck>();
            if (mDeleteTrucks.size() > PAGE_SIZE) {
                deleteList.addAll(mDeleteTrucks.subList(0, PAGE_SIZE));
                mDeleteTrucks.removeAll(deleteList);
            } else {
                deleteList.addAll(mDeleteTrucks);
                mDeleteTrucks.clear();
            }
            for (Truck truck : deleteList) {
                truck.delete();
                mDeletedCount++;
                info("TRUCK DELETED: " + truck.toString());
            }
            return true;
        }

        String getReport() {
            StringBuilder sbuf = new StringBuilder();
            sbuf.append("PROCESSED ");
            sbuf.append(mProcessed);
            sbuf.append(" OF ");
            sbuf.append(mNumTrucks);
            sbuf.append(" REASSIGNED=");
            sbuf.append(mReassignedCount);
            if (mReassignTrucks.size() > 0) {
                sbuf.append("/");
                sbuf.append(mReassignTrucks.size());
            }
            sbuf.append(" COMPANY NAME FIXED=");
            sbuf.append(mFixedCompanyNames);
            if (mFixTruckCompanyName.size() > 0) {
                sbuf.append("/");
                sbuf.append(mFixTruckCompanyName.size());
            }
            sbuf.append(" DELETED=");
            sbuf.append(mDeletedCount);
            if (mDeleteTrucks.size() > 0) {
                sbuf.append("/");
                sbuf.append(mDeleteTrucks.size());
            }
            return sbuf.toString();
        }
    }

    @Transactional
    private Truck reassignTruckValue(Entry entry) {
        Truck existing = entry.getTruck();
        Truck truck = new Truck();
        truck.truck_number = existing.truck_number;
        truck.project_id = existing.project_id;
        truck.company_name_id = existing.company_name_id;
        truck.license_plate = existing.license_plate;
        truck.created_by = existing.created_by;
        truck.created_by_client = existing.created_by_client;
        truck.upload_id = existing.upload_id;
        truck.save();

        entry.truck_id = truck.id;
        entry.update();

        return truck;
    }

    // endregion TRUCK CLEANUP

    // region Logger

    private void warn(String msg) {
        Logger.warn(msg);
    }

    private void info(String msg) {
        Logger.info(msg);
    }

    private void debug(String msg) {
        Logger.debug(msg);
    }

    // endregion Logger

}
