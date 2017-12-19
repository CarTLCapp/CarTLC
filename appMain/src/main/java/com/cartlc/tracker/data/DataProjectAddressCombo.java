package com.cartlc.tracker.data;

import android.support.annotation.NonNull;

import java.util.List;

import timber.log.Timber;

/**
 * Created by dug on 5/10/17.
 */

public class DataProjectAddressCombo implements Comparable<DataProjectAddressCombo> {
    public long id;
    public long projectNameId;
    public long addressId;
    String      mProjectName;
    DataAddress mAddress;

    public DataProjectAddressCombo(long projectNameId, long addressId) {
        this.projectNameId = projectNameId;
        this.addressId = addressId;
    }

    public DataProjectAddressCombo(long rowId, long projectNameId, long addressId) {
        this.id = rowId;
        this.projectNameId = projectNameId;
        this.addressId = addressId;
    }

    public void reset(long projectNameId, long addressId) {
        this.projectNameId = projectNameId;
        this.addressId = addressId;
        mProjectName = null;
        mAddress = null;
    }

    public String getProjectName() {
        if (mProjectName == null) {
            mProjectName = TableProjects.getInstance().queryProjectName(projectNameId);
            if (mProjectName == null) {
                Timber.e("Could not find project ID=" + projectNameId);
            }
        }
        return mProjectName;
    }

    public DataProject getProject() {
        return TableProjects.getInstance().queryById(projectNameId);
    }

    public DataAddress getAddress() {
        if (mAddress == null) {
            mAddress = TableAddress.getInstance().query(addressId);
            if (mAddress == null) {
                Timber.e("Could not find address ID=" + addressId);
                Timber.e("ADDRESSES=" + TableAddress.getInstance().toString());
            }
        }
        return mAddress;
    }

    public String getAddressLine() {
        DataAddress address = getAddress();
        if (address != null) {
            return address.getLine();
        }
        return "Invalid";
    }

    public String getCompanyName() {
        DataAddress address = getAddress();
        if (address != null) {
            return address.company;
        }
        return null;
    }

    public String getHintLine() {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append(getProjectName());
        sbuf.append("\n");
        sbuf.append(getCompanyName());
        return sbuf.toString();
    }

    public List<DataEntry> getEntries() {
        return TableEntry.getInstance().queryForProjectAddressCombo(id);
    }

    @Override
    public int compareTo(@NonNull DataProjectAddressCombo o) {
        String name = getProjectName();
        String otherName = o.getProjectName();
        if (name != null && otherName != null) {
            return name.compareTo(otherName);
        }
        if (name == null && otherName == null) {
            return 0;
        }
        return 1;
    }

    public boolean hasValidState() {
        return getAddress().hasValidState();
    }

    public void fix() {
        getAddress().fix();
    }

    @Override
    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("ID=");
        sbuf.append(id);
        sbuf.append(", PROJID=");
        sbuf.append(projectNameId);
        sbuf.append(" [");
        sbuf.append(getProjectName());
        sbuf.append("] ADDRESS=");
        sbuf.append(addressId);
        if (getAddress() != null) {
            sbuf.append(" [");
            sbuf.append(getAddress().getBlock());
            sbuf.append("]");
        }
        return sbuf.toString();
    }
}
