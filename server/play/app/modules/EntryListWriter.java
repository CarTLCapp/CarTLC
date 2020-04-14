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
    static final String ROOT_PROJECT = "Root";
    static final String SUB_PROJECT = "Sub";
    static final String COMPANY = "Company";
    static final String STREET = "Street";
    static final String CITY = "City";
    static final String STATE = "State";
    static final String ZIP = "Zip";
    static final String TRUCK = "Truck";
    static final String EQUIPMENT = "Equipment";
    static final String STATUS = "Status";

    EntryPagedList mList;
    NoteColumns mNoteColumns;
    BufferedWriter mBR;
    File mFile;
    int mPage;
    int mCount;

    public EntryListWriter(EntryPagedList list) {
        mList = list;
        mNoteColumns = new NoteColumns();
    }

    public void open(File file) throws IOException {
        mNoteColumns.prepare();
        mFile = file;
        mBR = new BufferedWriter(new FileWriter(file));
        mBR.write(DATE);
        mBR.write(",");
        mBR.write(TECH_NAME);
        mBR.write(",");
        mBR.write(ROOT_PROJECT);
        mBR.write(",");
        mBR.write(SUB_PROJECT);
        mBR.write(",");
        mBR.write(COMPANY);
        mBR.write(",");
        mBR.write(STREET);
        mBR.write(",");
        mBR.write(CITY);
        mBR.write(",");
        mBR.write(STATE);
        mBR.write(",");
        mBR.write(ZIP);
        mBR.write(",");
        mBR.write(TRUCK);
        mBR.write(",");
        mBR.write(EQUIPMENT);
        mBR.write(",");
        mBR.write(STATUS);
        mBR.write(mNoteColumns.getHeaders());
        mBR.write("\n");
        mPage = 0;
        mCount = 0;
    }

    public boolean computeNext() throws IOException {
        if (mList.hasNext()) {
            mList.setPage(++mPage);
            mList.compute();
            mNoteColumns.prepare();
            return true;
        } else {
            mBR.close();
            return false;
        }
    }

    public int writeNext() throws IOException {
        for (Entry entry : mList.getList()) {
            mBR.write(chkNull(entry.getDate()));
            mBR.write(",");
            mBR.write(chkNull(entry.getTechName()));
            mBR.write(",");
            mBR.write(chkNull(entry.getRootProjectName()));
            mBR.write(",");
            mBR.write(chkNull(entry.getSubProjectName()));
            mBR.write(",");
            mBR.write(chkNull(entry.getCompany()));
            mBR.write(",");
            mBR.write(chkNull(entry.getStreet()));
            mBR.write(",");
            mBR.write(chkNull(entry.getCity()));
            mBR.write(",");
            mBR.write(chkNull(entry.getState()));
            mBR.write(",");
            mBR.write(chkNull(entry.getZipCode()));
            mBR.write(",");
            if (mList.canViewTrucks) {
                mBR.write(chkNull(entry.getTruckLine()));
                mBR.write(",");
            }
            mBR.write(chkComma(entry.getEquipmentLine(mList.mForClientId)));
            mBR.write(",");
            mBR.write(chkNull(entry.getStatus()));
            mBR.write(chkNull(mNoteColumns.getValues(entry)));
            mBR.write("\n");
            mCount++;
        }
        return mCount;
    }

    public File getFile() {
        return mFile;
    }

    public void abort() throws IOException {
        mBR.close();
    }

    private String chkNull(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }

    private String chkComma(String line) {
        if (line == null) {
            return "";
        }
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

        NoteColumns() {}

        void prepare() {
            mNextColumn = 0;
            mNoteColumns.clear();
            for (Entry entry : mList.getList()) {
                prepare(entry.getNotes(mList.mForClientId));
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
            for (EntryNoteCollection note : entry.getNotes(mList.mForClientId)) {
		if (note.getName() != null && mNoteColumns.containsKey(note.getName())) {
                    mColumns[mNoteColumns.get(note.getName())] = note.getValue();
		}
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
