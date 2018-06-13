/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package modules;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.lang.StringBuilder;
import java.io.IOException;

import models.*;
import play.db.ebean.Transactional;
import play.Logger;

public class EntryListWriter {

    static final String TECH_NAME = "Name";
    static final String DATE = "Date";
    static final String PROJECT = "Project";
    static final String COMPANY = "Company";
    static final String STREET = "Street";
    static final String CITY = "City";
    static final String STATE = "State";
    static final String ZIP = "Zip";
    static final String TRUCK = "Truck";
    static final String EQUIPMENT = "Equipment";
    static final String STATUS = "Status";

    List<Entry> mList;
    NoteColumns mNoteColumns;

    public EntryListWriter(List<Entry> list) {
        mList = list;
        mNoteColumns = new NoteColumns();
    }

    public void save(File file) throws IOException {
        BufferedWriter br = new BufferedWriter(new FileWriter(file));
        br.write(DATE);
        br.write(",");
        br.write(TECH_NAME);
        br.write(",");
        br.write(PROJECT);
        br.write(",");
        br.write(COMPANY);
        br.write(",");
        br.write(STREET);
        br.write(",");
        br.write(CITY);
        br.write(",");
        br.write(STATE);
        br.write(",");
        br.write(ZIP);
        br.write(",");
        br.write(TRUCK);
        br.write(",");
        br.write(EQUIPMENT);
        br.write(",");
        br.write(STATUS);
        br.write(mNoteColumns.getHeaders());
        br.write("\n");
        for (Entry entry : mList) {
            br.write(entry.getDate());
            br.write(",");
            br.write(entry.getTechName());
            br.write(",");
            br.write(entry.getProjectLine());
            br.write(",");
            br.write(entry.getCompany());
            br.write(",");
            br.write(entry.getStreet());
            br.write(",");
            br.write(entry.getCity());
            br.write(",");
            br.write(entry.getState());
            br.write(",");
            br.write(entry.getZipCode());
            br.write(",");
            br.write(entry.getTruckLine());
            br.write(",");
            br.write(chkComma(entry.getEquipmentLine()));
            br.write(",");
            br.write(entry.getStatus());
            br.write(mNoteColumns.getValues(entry));
            br.write("\n");
        }
        br.close();
    }

    private String chkComma(String line) {
        if (hasComma(line)) {
            StringBuilder sbuf = new StringBuilder();
            sbuf.append('"');
            sbuf.append(line);
            sbuf.append('"');
            return sbuf.toString();
        }
        return line;
    }

    private boolean hasComma(String line) {
        return line.indexOf(',') >= 0;
    }

    class NoteColumns {

        HashMap<String, Integer> mNoteColumns = new HashMap<String, Integer>();
        int mNextColumn;
        String [] mColumns;

        NoteColumns() {
            prepare();
        }

        void prepare() {
            mNextColumn = 0;
            mNoteColumns.clear();
            for (Entry entry : mList) {
                prepare(entry.getNotes());
            }
            mColumns = new String[mNextColumn];
        }

        void prepare(List<EntryNoteCollection> notes) {
            for (EntryNoteCollection note : notes) {
                if (!note.getValue().isEmpty()) {
                    if (!mNoteColumns.containsKey(note.getName())) {
                        mNoteColumns.put(note.getName(), mNextColumn++);
                    }
                }
            }
        }

        String getHeaders() {
            for (int i = 0; i < mColumns.length; i++) {
                mColumns[i] = null;
            }
            for (String key: mNoteColumns.keySet()) {
                mColumns[mNoteColumns.get(key)] = key;
            }
            StringBuilder sbuf = new StringBuilder();
            for (int i = 0; i < mColumns.length; i++) {
                sbuf.append(",");
                sbuf.append(mColumns[i]);
            }
            return sbuf.toString();
        }

        String getValues(Entry entry) {
            for (int i = 0; i < mColumns.length; i++) {
                mColumns[i] = null;
            }
            for (EntryNoteCollection note : entry.getNotes()) {
                mColumns[mNoteColumns.get(note.getName())] = note.getValue();
            }
            StringBuilder sbuf = new StringBuilder();
            for (int i = 0; i < mColumns.length; i++) {
                sbuf.append(",");
                if (mColumns[i] != null) {
                    sbuf.append(mColumns[i]);
                }
            }
            return sbuf.toString();
        }

    }
}