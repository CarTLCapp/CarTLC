/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import play.mvc.*;
import play.data.*;
import static play.data.Form.*;
import akka.actor.*;
import scala.concurrent.duration.Duration;

import models.*;
import modules.WorkerExecutionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import javax.inject.Inject;
import javax.persistence.PersistenceException;
import java.util.concurrent.*;
import java.io.IOException;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import modules.AmazonHelper;
import modules.Globals;
import modules.EntryListWriter;
import java.io.File;
import play.db.ebean.Transactional;
import play.libs.Json;
import play.Logger;
import play.libs.concurrent.HttpExecution;

/**
 * Manage a database of equipment.
 */
public class EntryController extends Controller {

    private static final int PAGE_SIZE = 100;
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'zzz";
    private static final String EXPORT_FILENAME = "/tmp/export.csv";

    private AmazonHelper mAmazonHelper;
    private EntryPagedList mEntryList = new EntryPagedList();
    private FormFactory mFormFactory;
    private SimpleDateFormat mDateFormat;
    private Globals mGlobals;
    private WorkerExecutionContext mExecutionContext;
    private ActorSystem mActorSystem;
    private String mExportMsg;

    @Inject
    public EntryController(AmazonHelper amazonHelper,
                           FormFactory formFactory,
                           WorkerExecutionContext executionContext,
                           ActorSystem actorSystem,
                           Globals globals) {
        mAmazonHelper = amazonHelper;
        mFormFactory = formFactory;
        mDateFormat = new SimpleDateFormat(DATE_FORMAT);
        mGlobals = globals;
        mExecutionContext = executionContext;
        mActorSystem = actorSystem;
    }

    public Result list(int page, String sortBy, String order) {
        if (mGlobals.isClearSearch()) {
            mEntryList.clearSearch();
            mGlobals.setClearSearch(false);
            mEntryList.clearCache();
        } else if (page == 0) {
            mEntryList.clearCache();
        }
        mEntryList.setPage(page);
        mEntryList.setSortBy(sortBy);
        mEntryList.setOrder(order);
        mEntryList.computeFilters(Secured.getClient(ctx()));
        mEntryList.compute();
        Form<InputSearch> searchForm = mFormFactory.form(InputSearch.class).fill(mEntryList.getInputSearch());
        return ok(views.html.entry_list.render(mEntryList, sortBy, order, searchForm));
    }

    public Result list() {
        return list(0, null, null);
    }

    public Result search() {
        Form<InputSearch> searchForm = mFormFactory.form(InputSearch.class).bindFromRequest();
        InputSearch search = searchForm.get();
        mEntryList.setSearch(search.search);
        mEntryList.clearCache();
        mEntryList.compute();
        return ok(views.html.entry_list.render(mEntryList,
                mEntryList.getSortBy(), mEntryList.getOrder(), searchForm));
    }

    public Result searchClear() {
        mEntryList.clearSearch();
        mEntryList.clearCache();
        mEntryList.compute();
        Form<InputSearch> searchForm = mFormFactory.form(InputSearch.class);
        return ok(views.html.entry_list.render(mEntryList,
                mEntryList.getSortBy(), mEntryList.getOrder(), searchForm));
    }

    public Result showByTruck(long truck_id) {
        mGlobals.setClearSearch(false);
        mEntryList.setSearch(null);
        mEntryList.setByTruckId(truck_id);
        return list();
    }

    public CompletionStage<Result> computeTotalNumRows() {
        Executor myEc = HttpExecution.fromThread((Executor) mExecutionContext);
        return CompletableFuture.completedFuture(mEntryList.computeTotalNumRows()).thenApplyAsync(result -> {
            StringBuilder sbuf = new StringBuilder();
            if (mEntryList.hasPrev()) {
                sbuf.append("prev");
            } else {
                sbuf.append("prev disabled");
            }
            sbuf.append("|");
            sbuf.append(mEntryList.getDisplayingXtoYofZ());
            sbuf.append("|");
            if (mEntryList.hasNext()) {
                sbuf.append("next");
            } else {
                sbuf.append("next disabled");
            }
            sbuf.append("|");
            if (result == 0) {
                sbuf.append("No entries");
            } else if (result == 1) {
                sbuf.append("One entry");
            } else {
                sbuf.append(result);
                sbuf.append(" entries found");
            }
            return ok(sbuf.toString());
        }, myEc);
    }

    public Result export() {
        Logger.info("export() BEGIN");
        Client client = Secured.getClient(ctx());
        return export(client);
    }

    private Result export(Client client) {
        Logger.info("export() START");
        EntryPagedList entryList = new EntryPagedList(mEntryList);
        entryList.computeFilters(client);
        entryList.compute();
        EntryListWriter writer = new EntryListWriter(entryList.getList());
        File file = new File(EXPORT_FILENAME);
        try {
            writer.save(file);
        } catch (IOException ex) {
            Logger.error("export() ERROR: " + ex.getMessage());
            return badRequest2(ex.getMessage());
        }
        Logger.info("export() END");
        return ok(file);
    }

    @Security.Authenticated(Secured.class)
    public CompletionStage<Result> exportBackground() {
        Logger.info("export() BACKGROUND BEGIN");
        Client client = Secured.getClient(ctx());
        Executor myEc = HttpExecution.fromThread((Executor) mExecutionContext);
        return CompletableFuture.supplyAsync(() -> export(client), myEc);
//        return CompletionStage<Result>.completedFuture(export(client)).thenApplyAsync(result -> {
//            return result;
//        }, myEc);
    }

    /**
     * Display the picture for an entry.
     */
    public Result pictures(Long entry_id) {
        Entry entry = Entry.find.byId(entry_id);
        if (entry == null) {
            return badRequest2("Could not find entry ID " + entry_id);
        }
        loadPictures(entry);
        List<PictureCollection> pictures = entry.getPictures();
        return ok(views.html.entry_list_picture.render(pictures));
    }

    void loadPictures(Entry entry) {
        entry.loadPictures(request().host(), mAmazonHelper);
    }

    public Result getImage(String picture) {
        File localFile = mAmazonHelper.getLocalFile(picture);
        if (localFile.exists()) {
            return ok(localFile);
        } else {
            return ok(picture);
        }
    }

    /**
     * Display the notes for an entry.
     */
    public Result notes(Long entry_id) {
        Entry entry = Entry.find.byId(entry_id);
        if (entry == null) {
            return badRequest2("Could not find entry ID " + entry_id);
        }
        return ok(views.html.entry_list_note.render(entry.getNotes()));
    }

    /**
     * Display details for the entry including delete button.
     */
    public Result view(Long entry_id) {
        Entry entry = Entry.find.byId(entry_id);
        if (entry == null) {
            return badRequest2("Could not find entry ID " + entry_id);
        }
        loadPictures(entry);
        return ok(views.html.entry_view.render(entry, Secured.getClient(ctx())));
    }

    @Security.Authenticated(Secured.class)
    public Result delete(Long entry_id) {
        Entry entry = Entry.find.byId(entry_id);
        if (entry != null) {
            entry.remove(mAmazonHelper);
            Logger.info("Entry has been deleted: " + entry_id);
        }
        return list();
    }

    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public Result enter() {
        Entry entry = new Entry();
        ArrayList<String> missing = new ArrayList<String>();
        JsonNode json = request().body().asJson();
        Logger.debug("GOT: " + json.toString());
        boolean retServerId = false;
        JsonNode value;
        value = json.findValue("tech_id");
        if (value == null) {
            missing.add("tech_id");
        } else {
            entry.tech_id = value.intValue();
        }
        int secondary_tech_id;
        value = json.findValue("secondary_tech_id");
        if (value != null) {
            secondary_tech_id = value.intValue();
        } else {
            secondary_tech_id = 0;
        }
        value = json.findValue("date_string");
        if (value != null) {
            String date_value = value.textValue();
            try {
                entry.entry_time = mDateFormat.parse(date_value);
                entry.time_zone = pickOutTimeZone(date_value, 'Z');
            } catch (Exception ex) {
                Logger.error("While parsing " + date_value + ":" + ex.getMessage());
            }
        } else {
            value = json.findValue("date");
            if (value == null) {
                missing.add("date");
            } else {
                entry.entry_time = new Date(value.longValue());
            }
        }
        value = json.findValue("server_id");
        if (value != null) {
            entry.id = value.longValue();
            retServerId = true;
            Entry existing;
            if (entry.id > 0) {
                existing = Entry.find.byId(entry.id);
                if (existing == null) {
                    existing = Entry.findByDate(entry.tech_id, entry.entry_time);
                    if (existing != null) {
                        Logger.info("Could not find entry with ID " + entry.id + ", so located based on time=" + entry.entry_time);
                    }
                } else {
                    existing.entry_time = entry.entry_time;
                    existing.tech_id = entry.tech_id;
                }
            } else {
                existing = Entry.findByDate(entry.tech_id, entry.entry_time);
            }
            if (existing == null) {
                entry.id = 0L;
            } else {
                entry = existing;
            }
        }
        int truck_id;
        String truck_number;
        String license_plate;
        value = json.findValue("truck_id");
        if (value == null) {
            truck_id = 0;
        } else {
            truck_id = value.intValue();
        }
        value = json.findValue("truck_number_string");
        if (value == null) {
            value = json.findValue("truck_number");
            if (value != null) {
                truck_number = Integer.toString(value.intValue());
            } else {
                truck_number = null;
            }
        } else {
            truck_number = value.textValue();
        }
        value = json.findValue("license_plate");
        if (value != null) {
            license_plate = value.textValue();
        } else {
            license_plate = null;
        }
        value = json.findValue("project_id");
        if (value == null) {
            missing.add("project_id");
        } else {
            entry.project_id = value.longValue();
        }
        value = json.findValue("status");
        if (value != null) {
            entry.status = Entry.Status.from(value.textValue());
        }
        value = json.findValue("address_id");
        if (value == null) {
            value = json.findValue("address");
            if (value == null) {
                missing.add("address_id");
            } else {
                String address = value.textValue().trim();
                if (address.length() > 0) {
                    try {
                        Company company = Company.parse(address);
                        Company existing = Company.has(company);
                        if (existing != null) {
                            company = existing;
                        } else {
                            company.created_by = entry.tech_id;
                            company.save();
                            Version.inc(Version.VERSION_COMPANY);
                        }
                        entry.company_id = company.id;
                        CompanyName.save(company.name);
                    } catch (Exception ex) {
                        return badRequest2("address: " + ex.getMessage());
                    }
                } else {
                    return badRequest2("address");
                }
            }
        } else {
            entry.company_id = value.longValue();
            Company company = Company.get(entry.company_id);
            if (company == null) {
                Technician.AddReloadCode(entry.tech_id, 'c');
                return badRequest2("address: no such company with ID " + entry.company_id);
            }
        }
        if (truck_number == null && license_plate == null && truck_id == 0) {
            missing.add("truck_id");
            missing.add("truck_number");
            missing.add("license_plate");
        } else {
            // Note: I don't call Version.inc(Version.VERSION_TRUCK) intentionally.
            // The reason is that other techs don't need to know about a local techs truck updates.
            Truck truck = Truck.add(entry.project_id, entry.company_id, truck_id, truck_number, license_plate, entry.tech_id);
            entry.truck_id = truck.id;
        }
        value = json.findValue("equipment");
        if (value != null) {
            if (value.getNodeType() != JsonNodeType.ARRAY) {
                Logger.error("Expected array for element 'equipment'");
            } else {
                int collection_id;
                if (entry.equipment_collection_id > 0) {
                    collection_id = (int) entry.equipment_collection_id;
                    EntryEquipmentCollection.deleteByCollectionId(entry.equipment_collection_id);
                } else {
                    collection_id = Version.inc(Version.NEXT_EQUIPMENT_COLLECTION_ID);
                }
                boolean newEquipmentCreated = false;
                Iterator<JsonNode> iterator = value.elements();
                while (iterator.hasNext()) {
                    JsonNode ele = iterator.next();
                    EntryEquipmentCollection collection = new EntryEquipmentCollection();
                    collection.collection_id = (long) collection_id;
                    JsonNode subvalue = ele.findValue("equipment_id");
                    if (subvalue == null) {
                        subvalue = ele.findValue("equipment_name");
                        if (subvalue == null) {
                            missing.add("equipment_id");
                            missing.add("equipment_name");
                        } else {
                            String name = subvalue.textValue();
                            List<Equipment> equipments = Equipment.findByName(name);
                            Equipment equipment;
                            if (equipments.size() == 0) {
                                equipment = new Equipment();
                                equipment.name = name;
                                equipment.created_by = entry.tech_id;
                                equipment.created_by_client = false;
                                equipment.save();
                                Logger.info("Created new equipment: " + equipment.toString());
                                newEquipmentCreated = true;

                                ProjectEquipmentCollection.addNew(entry.project_id, equipment);
                                Logger.info("Registered for project:" + entry.project_id);
                            } else {
                                if (equipments.size() > 1) {
                                    Logger.error("Too many equipments found with name: " + name);
                                }
                                equipment = equipments.get(0);
                            }
                            collection.equipment_id = equipment.id;
                        }
                    } else {
                        collection.equipment_id = subvalue.longValue();
                    }
                    collection.save();
                }
                entry.equipment_collection_id = collection_id;
                if (newEquipmentCreated) {
                    Version.inc(Version.VERSION_EQUIPMENT);
                }
            }
        }
        value = json.findValue("picture");
        if (value != null) {
            if (value.getNodeType() != JsonNodeType.ARRAY) {
                Logger.error("Expected array for element 'picture'");
            } else {
                int collection_id;
                if (entry.picture_collection_id > 0) {
                    collection_id = (int) entry.picture_collection_id;
                    PictureCollection.deleteByCollectionId(entry.picture_collection_id, null);
                } else {
                    collection_id = Version.inc(Version.NEXT_PICTURE_COLLECTION_ID);
                }
                Iterator<JsonNode> iterator = value.elements();
                while (iterator.hasNext()) {
                    JsonNode ele = iterator.next();
                    PictureCollection collection = new PictureCollection();
                    collection.collection_id = (long) collection_id;
                    JsonNode subvalue = ele.findValue("note");
                    if (subvalue != null) {
                        collection.note = subvalue.textValue();
                    }
                    subvalue = ele.findValue("filename");
                    if (subvalue == null) {
                        missing.add("filename");
                    } else {
                        collection.picture = subvalue.textValue();
                        collection.save();
                    }
                }
                entry.picture_collection_id = collection_id;
            }
        }
        value = json.findValue("notes");
        if (value != null) {
            if (value.getNodeType() != JsonNodeType.ARRAY) {
                Logger.error("Expected array for element 'notes'");
            } else {
                int collection_id;
                if (entry.note_collection_id > 0) {
                    collection_id = (int) entry.note_collection_id;
                    EntryNoteCollection.deleteByCollectionId(entry.note_collection_id);
                } else {
                    collection_id = Version.inc(Version.NEXT_NOTE_COLLECTION_ID);
                }
                Iterator<JsonNode> iterator = value.elements();
                while (iterator.hasNext()) {
                    JsonNode ele = iterator.next();
                    EntryNoteCollection collection = new EntryNoteCollection();
                    collection.collection_id = (long) collection_id;
                    JsonNode subvalue = ele.findValue("id");
                    if (subvalue == null) {
                        subvalue = ele.findValue("name");
                        if (subvalue == null) {
                            missing.add("note:id, note:name");
                        } else {
                            String name = subvalue.textValue();
                            List<Note> notes = Note.findByName(name);
                            if (notes == null || notes.size() == 0) {
                                // A tech can get into a situation where they are effectively
                                // creating notes, even though the APP doesn't explicitly allow it,
                                // if the server created a note, they got it, and then the server
                                // later deletes it before the tech's app can update.
                                Note note = new Note();
                                note.name = name;
                                note.type = Note.Type.TEXT;
                                note.created_by = entry.tech_id;
                                note.created_by_client = false;
                                note.save();
                                collection.note_id = note.id;
                                // Note: don't inc version number because other techs don't really need to know.
                                Logger.info("Created new note: " + note.toString());
                            } else if (notes.size() > 1) {
                                Logger.error("Too many notes with name: " + name);
                                missing.add("note: '" + name + "'");
                            } else {
                                collection.note_id = notes.get(0).id;
                            }
                        }
                    } else {
                        collection.note_id = subvalue.longValue();
                    }
                    subvalue = ele.findValue("value");
                    if (value == null) {
                        missing.add("note:value");
                    } else {
                        collection.note_value = subvalue.textValue();
                    }
                    collection.save();
                }
                entry.note_collection_id = collection_id;
            }
        }
        if (missing.size() > 0) {
            return missingRequest(missing);
        }
        if (entry.id != null && entry.id > 0) {
            entry.update();
            Logger.debug("Updated entry " + entry.id);
        } else {
            entry.save();
            Logger.debug("Created new entry " + entry.id);
        }
        if (secondary_tech_id > 0) {
            SecondaryTechnician.save(entry.id, secondary_tech_id);
            Logger.debug("Assigned secondary technician " + secondary_tech_id + " to " + entry.id);
        }
        long ret_id;
        if (retServerId) {
            ret_id = entry.id;
        } else {
            ret_id = 0;
        }
        return ok(Long.toString(ret_id));
    }

    String pickOutTimeZone(String value, char sp) {
        int pos = value.indexOf(sp);
        if (pos >= 0) {
            return value.substring(pos+1);
        }
        return null;
    }

    Result missingRequest(ArrayList<String> missing) {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("Missing fields:");
        boolean comma = false;
        for (String field : missing)
        {
            if (comma) {
                sbuf.append(", ");
            }
            sbuf.append(field);
            comma = true;
        }
        sbuf.append("\n");
        return badRequest2(sbuf.toString());
    }

    Result badRequest2(String field) {
        Logger.error("ERROR: " + field);
        return badRequest(field);
    }

}

