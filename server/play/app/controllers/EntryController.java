package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import play.mvc.*;
import play.data.*;
import static play.data.Form.*;

import models.*;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.persistence.PersistenceException;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import java.util.Date;
import java.util.Iterator;
import modules.AmazonHelper;
import java.io.File;
import play.db.ebean.Transactional;
import play.libs.Json;
import play.Logger;
/**
 * Manage a database of equipment.
 */
public class EntryController extends Controller {

    private static final int PAGE_SIZE = 100;

    private AmazonHelper amazonHelper;
    private EntryList entryList = new EntryList();

    @Inject
    public EntryController(AmazonHelper amazonHelper) {
        this.amazonHelper = amazonHelper;
    }

    public Result list(int page, String sortBy, String order) {
        entryList.setPage(page);
        entryList.setSortBy(sortBy);
        entryList.setOrder(order);
        entryList.clearCache();
        entryList.setProjects(Secured.getClient(ctx()));
        entryList.compute();
        return ok(views.html.entry_list.render(entryList, sortBy, order));
    }

    public Result list() {
        return list(0, "entry_time", "desc");
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
        entry.loadPictures(request().host(), amazonHelper);
    }

    public Result getImage(String picture) {
        File localFile = amazonHelper.getLocalFile(picture);
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
        Entry entry = Entry.find.ref(entry_id);
        if (entry != null) {
            entry.remove(amazonHelper);
            flash("success", "Entry has been deleted");
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
        JsonNode value = json.findValue("tech_id");
        if (value == null) {
            missing.add("tech_id");
        } else {
            entry.tech_id = value.intValue();
        }
        value = json.findValue("date");
        if (value == null) {
            missing.add("date");
        } else {
            entry.entry_time = new Date(value.longValue());
        }
        int truck_number;
        String license_plate;
        value = json.findValue("truck_number");
        if (value != null) {
            truck_number = value.intValue();
        } else {
            truck_number = 0;
        }
        value = json.findValue("license_plate");
        if (value != null) {
            license_plate = value.textValue();
        } else {
            license_plate = null;
        }
        if (truck_number == 0 && license_plate == null) {
            missing.add("truck_number");
            missing.add("license_plate");
        } else {
            Truck truck = Truck.add(truck_number, license_plate);
            entry.truck_id = truck.id;
        }
        value = json.findValue("project_id");
        if (value == null) {
            missing.add("project_id");
        } else {
            entry.project_id = value.longValue();
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
                    } catch (Exception ex) {
                        return badRequest2("address: " + ex.getMessage());
                    }
                } else {
                    return badRequest2("address");
                }
            }
        } else {
            entry.company_id = value.longValue();
        }
        value = json.findValue("equipment");
        if (value != null) {
            if (value.getNodeType() != JsonNodeType.ARRAY) {
                return badRequest2("Expected array for element 'equipment'");
            } else {
                int collection_id = Version.inc(Version.NEXT_EQUIPMENT_COLLECTION_ID);
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
                            Equipment equipment = Equipment.findByName(name);
                            if (equipment == null) {
                                equipment = new Equipment();
                                equipment.name = name;
                                equipment.created_by = entry.tech_id;
                                equipment.save();
                                Version.inc(Version.VERSION_EQUIPMENT);
                            }
                            collection.equipment_id = equipment.id;
                            newEquipmentCreated = true;
                        }
                    } else {
                        collection.equipment_id = subvalue.longValue();
                    }
                    collection.save();
                }
                entry.equipment_collection_id = collection_id;
            }
        }
        value = json.findValue("picture");
        if (value != null) {
            if (value.getNodeType() != JsonNodeType.ARRAY) {
                return badRequest2("Expected array for element 'picture'");
            } else {
                int collection_id = Version.inc(Version.NEXT_PICTURE_COLLECTION_ID);
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
                return badRequest2("Expected array for element 'notes'");
            } else {
                int collection_id = Version.inc(Version.NEXT_NOTE_COLLECTION_ID);
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
                            Note note = Note.findByName(name);
                            if (note == null) {
                                // No ability to create notes in version 1.
                                missing.add("note:" + name);
                            } else {
                                collection.note_id = note.id;
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
        entry.save();
        return ok(Integer.toString(0));
    }

    Result missingRequest(ArrayList<String> missing) {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("Missing fields:");
        for (String field : missing)
        {
            sbuf.append(" ");
            sbuf.append(field);
        }
        sbuf.append("\n");
        return badRequest2(sbuf.toString());
    }

    Result badRequest2(String field) {
        Logger.error("ERROR: " + field);
        return badRequest(field);
    }
}

