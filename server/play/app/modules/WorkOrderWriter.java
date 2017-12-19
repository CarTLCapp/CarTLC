package models;

import java.util.List;
import java.util.ArrayList;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.lang.StringBuilder;

import models.WorkOrder;
import play.db.ebean.Transactional;
import play.Logger;
import modules.WorkOrderField;

public class WorkOrderWriter {

    String error;
    long client_id;

    public WorkOrderWriter(Client client) {
        if (client == null || client.is_admin) {
            client_id = 0;
        } else {
            client_id = client.id;
        }
    }

    public boolean save(File file) {
        try {
            List<WorkOrder> list;
            if (client_id == 0) {
                list = WorkOrder.list();
            } else {
                list = WorkOrder.findByClientId(client_id);
            }
            BufferedWriter br = new BufferedWriter(new FileWriter(file));
            br.write(WorkOrderField.TRUCK_NUMBER.getName());
            br.write(",");
            br.write(WorkOrderField.LICENSE.getName());
            br.write(",");
            br.write(WorkOrderField.COMPANY.getName());
            br.write(",");
            br.write(WorkOrderField.STREET.getName());
            br.write(",");
            br.write(WorkOrderField.CITY.getName());
            br.write(",");
            br.write(WorkOrderField.STATE.getName());
            br.write(",");
            br.write(WorkOrderField.ZIP.getName());
            br.write("\n");
            Truck truck;
            Company company;
            for (WorkOrder order : list) {
                truck = order.getTruck();
                br.write(truck.truck_number);
                br.write(",");
                br.write(truck.license_plate);
                br.write(",");
                company = order.getCompany();
                br.write(company.getName());
                br.write(",");
                br.write(company.street);
                br.write(",");
                br.write(company.city);
                br.write(",");
                br.write(company.state);
                br.write(",");
                br.write(company.zipcode);
                br.write("\n");
            }
            br.close();
        } catch (Exception ex) {
            error = ex.getMessage();
        }
        return error == null;
    }

    public String getError() {
        return error;
    }

}