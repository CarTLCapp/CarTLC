/**
 * Copyright 2018, 2020, FleetTLC. All rights reserved
 */
package modules;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;
import java.lang.StringBuilder;
import java.io.IOException;
import java.io.File;

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

    private static final String SEPARATOR = ";";
    private static final String EXPORT_DATA = "/tmp/export_data";

    EntryPagedList mList;
    NoteColumns mNoteColumns;
    BufferedWriter mBW;
    File mFinalFile;
    int mPage;
    int mCount;
    boolean mCanViewTrucks;

    public EntryListWriter(EntryPagedList list) {
        mList = list;
        mNoteColumns = new NoteColumns();
        mCanViewTrucks = mList.canViewTrucks;
    }

    public void open() throws IOException {
        mBW = new BufferedWriter(new FileWriter(new File(EXPORT_DATA)));
        mPage = 0;
        mCount = 0;
        mNoteColumns.reset();
    }

    public boolean computeNext() throws IOException {
        if (mList.hasNext()) {
            mList.setPage(++mPage);
            mList.compute();
            return true;
        } else {
            mBW.close();
            return false;
        }
    }

    public void finish(String filename) throws IOException {
        mBW.close();

        BufferedReader br = new BufferedReader(new FileReader(EXPORT_DATA));
        mFinalFile = new File(filename);
        mBW = new BufferedWriter( new FileWriter(mFinalFile));

        writeHeader();

        int i;
        do {
            i = br.read();
            if (i != -1) {
                mBW.write((char) i);
            }
        } while (i != -1);
        br.close();
        mBW.close();
    }

    private void writeHeader() throws IOException {
        mBW.write(DATE);
        mBW.write(SEPARATOR);
        mBW.write(TECH_NAME);
        mBW.write(SEPARATOR);
        mBW.write(ROOT_PROJECT);
        mBW.write(SEPARATOR);
        mBW.write(SUB_PROJECT);
        mBW.write(SEPARATOR);
        mBW.write(COMPANY);
        mBW.write(SEPARATOR);
        mBW.write(STREET);
        mBW.write(SEPARATOR);
        mBW.write(CITY);
        mBW.write(SEPARATOR);
        mBW.write(STATE);
        mBW.write(SEPARATOR);
        mBW.write(ZIP);
        mBW.write(SEPARATOR);
        if (mCanViewTrucks) {
            mBW.write(TRUCK);
            mBW.write(SEPARATOR);
        }
        mBW.write(EQUIPMENT);
        mBW.write(SEPARATOR);
        mBW.write(STATUS);
        mBW.write(mNoteColumns.getHeaders(SEPARATOR));
        mBW.write("\n");
    }

    public int writeNext() throws IOException {
        for (Entry entry : mList.getList()) {
            mBW.write(chkNull(entry.getDate()));
            mBW.write(SEPARATOR);
            mBW.write(chkNull(entry.getTechName()));
            mBW.write(SEPARATOR);
            mBW.write(chkNull(entry.getRootProjectName()));
            mBW.write(SEPARATOR);
            mBW.write(chkNull(entry.getSubProjectName()));
            mBW.write(SEPARATOR);
            mBW.write(chkNull(entry.getCompany()));
            mBW.write(SEPARATOR);
            mBW.write(chkNull(entry.getStreet()));
            mBW.write(SEPARATOR);
            mBW.write(chkNull(entry.getCity()));
            mBW.write(SEPARATOR);
            mBW.write(chkNull(entry.getState()));
            mBW.write(SEPARATOR);
            mBW.write(chkNull(entry.getZipCode()));
            mBW.write(SEPARATOR);
            if (mCanViewTrucks) {
                mBW.write(chkNull(entry.getTruckLine()));
                mBW.write(SEPARATOR);
            }
            mBW.write(chkSeparator(entry.getEquipmentLine(mList.mForClientId)));
            mBW.write(SEPARATOR);
            mBW.write(chkNull(entry.getStatus()));
            mBW.write(chkNull(mNoteColumns.getValues(entry, SEPARATOR)));
            mBW.write("\n");
            mCount++;
        }
        return mCount;
    }

    public File getFile() {
        return mFinalFile;
    }

    public void abort() throws IOException {
        mBW.close();
    }

    private String chkNull(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }

    private String chkSeparator(String line) {
        if (line == null) {
            return "";
        }
        if (hasSeparator(line)) {
            StringBuilder sbuf = new StringBuilder();
            sbuf.append('"');
            sbuf.append(line);
            sbuf.append('"');
            return sbuf.toString();
        }
        return line;
    }

    private boolean hasSeparator(String line) {
        return line.indexOf(SEPARATOR) >= 0;
    }

    class NoteColumns {
        ArrayList<String> mColumnHeaders = new ArrayList<String>();
        HashMap<Integer, String> mColumnValues = new HashMap<Integer, String>();

        NoteColumns() {
        }

        void reset() {
            mColumnHeaders.clear();
            mColumnValues.clear();
        }

        String getHeaders(String separator) {
            StringBuilder sbuf = new StringBuilder();
            for (int index = 0; index < mColumnHeaders.size(); index++) {
                sbuf.append(separator);
                sbuf.append(mColumnHeaders.get(index));
            }
            return sbuf.toString();
        }

        String getValues(Entry entry, String separator) {
            mColumnValues.clear();
            for (EntryNoteCollection note : entry.getNotes(mList.mForClientId)) {
                String header = note.getName();
                if (!mColumnHeaders.contains(header)) {
                    mColumnHeaders.add(header);
                }
                int index = mColumnHeaders.indexOf(header);
                mColumnValues.put(index, note.getValue());
            }
            StringBuilder sbuf = new StringBuilder();
            for (int index = 0; index < mColumnHeaders.size(); index++) {
                sbuf.append(separator);
                if (mColumnValues.containsKey(index)) {
                    sbuf.append(mColumnValues.get(index));
                }
            }
            return sbuf.toString();
        }

    }
}
