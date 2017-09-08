package models;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.lang.StringBuilder;
import java.lang.Integer;

import models.WorkOrder;
import models.Version;
import play.db.ebean.Transactional;
import play.Logger;
import modules.WorkOrderField;

public class WorkOrderReader {

    HashMap<Integer,Integer> fieldPos = new HashMap<Integer,Integer>();
    ArrayList<String> errors = new ArrayList<String>();
    ArrayList<String> warnings = new ArrayList<String>();
    long client_id;
    long project_id;
    int upload_id;
    int lineCount;

    public WorkOrderReader(Client client, Project project) {
        if (client == null || client.is_admin) {
            client_id = 0;
        } else {
            client_id = client.id;
        }
        if (project != null) {
            project_id = project.id;
        }
        upload_id = Version.inc(Version.NEXT_UPLOAD_ID);
    }

    public boolean load(File file) {
        int companyNewCount = 0;
        int truckNewCount = 0;
        int orderCount = 0;
        fieldPos.clear();
        errors.clear();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            int fieldCount = 0;
            lineCount = 0;
            while ((line = br.readLine()) != null) {
                lineCount++;
                if (line.trim().length() == 0) {
                    continue;
                }
                if (fieldPos.size() == 0) {
                    List<String> names = Arrays.asList(line.split(","));
                    for (int pos = 0; pos < names.size(); pos++) {
                        String name = names.get(pos).trim();
                        WorkOrderField field = WorkOrderField.find(name);
                        if (field != null) {
                            if (fieldPos.containsKey(field.ordinal())) {
                                error("This field exists more than once: " + field.toString());
                            } else {
                                fieldPos.put(field.ordinal(), pos);
                            }
                        }
                    }
                    fieldCount = names.size();
                } else {
                    if (fieldPos.size() < 5) {
                        warning("Not enough valid columns detected: " + fieldPos.size(), line);
                        continue;
                    }
                    List<String> values = Arrays.asList(line.split(","));
                    if (values.size() != fieldCount) {
                        warning("Incorrect number of fields on line", line);
                        continue;
                    }
                    Truck truck = new Truck();
                    truck.truck_number = getFieldValueInt(values, WorkOrderField.TRUCK_NUMBER);
                    truck.license_plate = getFieldValue(values, WorkOrderField.LICENSE);
                    Truck etruck = Truck.findFirst(truck.truck_number, truck.license_plate);
                    long truck_id;
                    if (etruck == null) {
                        truck.upload_id = upload_id;
                        truck.created_by = (int) client_id;
                        truck.created_by_client = true;
                        truck.save();
                        truck_id = truck.id;
                        truckNewCount++;
                    } else {
                        truck_id = etruck.id;
                    }
                    WorkOrder order;
                    order = WorkOrder.findFirstByTruckId(truck_id);
                    if (order == null) {
                        order = new WorkOrder();
                    }
                    order.client_id = client_id;
                    order.project_id = project_id;
                    order.upload_id = upload_id;
                    order.truck_id = truck_id;
                    Company company = new Company();
                    company.street = getFieldValue(values, WorkOrderField.STREET);
                    company.city = getFieldValue(values, WorkOrderField.CITY);
                    company.state = getFieldValue(values, WorkOrderField.STATE);
                    company.zipcode = getFieldValue(values, WorkOrderField.ZIP);
                    company.name = getFieldValue(values, WorkOrderField.COMPANY);
                    Company existing = Company.has(company);
                    if (existing == null) {
                        company.created_by = (int) client_id;
                        company.created_by_client = true;
                        company.upload_id = upload_id;
                        company.save();
                        order.company_id = company.id;
                        companyNewCount++;
                    } else {
                        order.company_id = existing.id;
                    }
                    if (WorkOrder.has(order) == null) {
                        order.save();
                        orderCount++;
                    }
                }
            }
            br.close();
        } catch (Exception ex) {
            error(ex.getMessage());
        }
        if (companyNewCount > 0) {
            warnings.add("Added " + companyNewCount + " new companies");
        }
        if (truckNewCount > 0) {
            warnings.add("Added " + truckNewCount + " new trucks");
            Version.inc(Version.VERSION_TRUCK);
        }
        warnings.add("Added " + orderCount + " new orders");
        return errors.size() == 0;
    }

    void error(String msg) {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("Line ");
        sbuf.append(lineCount);
        sbuf.append(": ");
        sbuf.append(msg);
        warnings.add(sbuf.toString());
    }

    void warning(String msg, String line) {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("Line ");
        sbuf.append(lineCount);
        sbuf.append(": ");
        sbuf.append(msg);
        if (line != null) {
            sbuf.append(" {");
            sbuf.append(line);
            sbuf.append("}");
        }
        warnings.add(sbuf.toString());
    }

    public String getErrors() {
        return getMessages(errors);
    }

    public String getWarnings() {
        return getMessages(warnings);
    }

    String getMessages(List<String> msgs) {
        StringBuilder sbuf = new StringBuilder();
        for (String msg : msgs) {
            if (sbuf.length() > 0) {
                sbuf.append("\n");
            }
            sbuf.append(msg);
        }
        return sbuf.toString();
    }

    String getFieldValue(List<String> values, WorkOrderField field) {
        if (fieldPos.containsKey(field.ordinal())) {
            int pos = fieldPos.get(field.ordinal());
            if (pos < values.size()) {
                return values.get(pos);
            }
        }
        return null;
    }

    int getFieldValueInt(List<String> values, WorkOrderField field) {
        String value = getFieldValue(values, field);
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            Logger.error(ex.getMessage());
        }
        return 0;
    }
}