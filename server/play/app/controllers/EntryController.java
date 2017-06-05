package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import play.mvc.*;
import play.data.*;
import static play.data.Form.*;
import play.Logger;

import models.*;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.persistence.PersistenceException;
import play.db.ebean.Transactional;
import play.libs.Json;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import java.util.Date;
import java.util.Iterator;
import modules.AmazonHelper;
import modules.AmazonHelper.OnDownloadComplete;
import java.io.File;

/**
 * Manage a database of equipment.
 */
public class EntryController extends Controller {

    private static final int PAGE_SIZE = 100;

    private FormFactory formFactory;
    private AmazonHelper amazonHelper;

    @Inject
    public EntryController(FormFactory formFactory, AmazonHelper amazonHelper) {
        this.formFactory = formFactory;
        this.amazonHelper = amazonHelper;
    }

    public Result list(int page, String sortBy, String order) {
        return ok(views.html.entry_list.render(Entry.list(page, PAGE_SIZE, sortBy, order), sortBy, order));
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
        List<PictureCollection> pictures = entry.getPictures();
        for (PictureCollection picture : pictures) {
            File localFile = amazonHelper.getLocalFile(picture.picture);
            if (!localFile.exists()) {
                try {
                    amazonHelper.download(picture.picture, new OnDownloadComplete() {
                        public void onDownloadComplete(File file) {
                            Logger.info("COMPLETED: " + file.getAbsolutePath());
                        }
                    });
                } catch (Exception ex) {
                    Logger.error(ex.getMessage());
                }
            }
        }
        return ok(views.html.entry_list_picture.render(pictures));
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
        value = json.findValue("truck_number");
        if (value == null) {
            missing.add("truck_number");
        } else {
            entry.truck_number = value.intValue();
        }
        value = json.findValue("project_id");
        if (value == null) {
            value = json.findValue("project_name");
            if (value == null) {
                missing.add("project_id");
            } else {
                String projectName = value.textValue().trim();
                if (projectName.length() > 0) {
                    Project project = new Project();
                    project.name = projectName;
                    project.save();
                    Version.inc(Version.VERSION_PROJECT);
                } else {
                    return badRequest2("project_name");
                }
            }
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
                        company.is_local = true;
                        company.save();
                    } catch (Exception ex) {
                        return badRequest2("address: " + ex.getMessage());
                    }
                } else {
                    return badRequest2("address");
                }
            }
        } else {
            entry.address_id = value.longValue();
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
                            Equipment equipment = new Equipment();
                            equipment.name = subvalue.textValue();
                            equipment.save();
                            collection.equipment_id = equipment.id;
                            newEquipmentCreated = true;
                        }
                    } else {
                        collection.equipment_id = subvalue.longValue();
                    }
                    collection.save();
                }
                if (newEquipmentCreated) {
                    Version.inc(Version.VERSION_EQUIPMENT);
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
                    JsonNode subvalue = ele.findValue("filename");
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

