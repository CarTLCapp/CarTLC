package com.cartlc.tracker.data;

import android.support.annotation.NonNull;

import timber.log.Timber;

/**
 * Created by dug on 5/10/17.
 */

public class DataProjectGroup implements Comparable<DataProjectGroup> {
    public long id;
    public final long projectNameId;
    public final long addressId;
    String mProjectName;
    DataAddress mAddress;

    public DataProjectGroup(long projectId, long addressId) {
        this.projectNameId = projectId;
        this.addressId = addressId;
    }

    public DataProjectGroup(long rowId, long projectId, long addressId) {
        this.id = rowId;
        this.projectNameId = projectId;
        this.addressId = addressId;
    }

    public String getProjectName() {
        if (mProjectName == null) {
            mProjectName = TableProjects.getInstance().query(projectNameId);
            if (mProjectName == null) {
                Timber.e("Could not find project ID=" + projectNameId);
            }
        }
        return mProjectName;
    }

    public DataAddress getAddress() {
        if (mAddress == null) {
            mAddress = TableAddress.getInstance().query(addressId);
            if (mAddress == null) {
                Timber.e("Could not find address ID=" + addressId);
            }
        }
        return mAddress;
    }

    @Override
    public int compareTo(@NonNull DataProjectGroup o) {
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

    @Override
    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("ID=");
        sbuf.append(projectNameId);
        sbuf.append(" [" );
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
